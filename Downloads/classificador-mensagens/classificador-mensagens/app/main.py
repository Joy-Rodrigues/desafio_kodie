"""API FastAPI: classifica mensagens de clientes com IA e registra os encaminhamentos."""

import logging

from fastapi import FastAPI, HTTPException

from . import storage
from .classifier import ClassificationError, classificar
from .config import settings
from .schemas import ClassificacaoOut, EncaminhamentoIn, EncaminhamentoOut, MensagemIn

logger = logging.getLogger("classificador")

app = FastAPI(
    title="Classificador de Mensagens com IA",
    description="Desafio Técnico KODIE: IA + API + automação (n8n).",
    version="1.0.0",
)


@app.get("/health")
def health():
    return {"status": "ok", "provedor": settings.llm_provider, "modelo": settings.llm_model}


@app.post("/classificar", response_model=ClassificacaoOut)
def classificar_mensagem(entrada: MensagemIn):
    """Recebe a mensagem do cliente e devolve tipo, setor, prioridade, sentimento e resumo."""
    try:
        resultado = classificar(entrada.mensagem, entrada.cliente, entrada.canal)
    except ClassificationError as exc:
        logger.warning("Falha na classificação: %s", exc)
        raise HTTPException(status_code=502, detail=str(exc)) from exc
    except Exception as exc:  # erro de rede, timeout, 401/429 do provedor etc.
        logger.exception("Erro ao chamar o provedor de IA")
        raise HTTPException(status_code=502, detail=f"Erro ao chamar o provedor de IA: {type(exc).__name__}") from exc

    return ClassificacaoOut(
        **resultado.model_dump(),
        mensagem=entrada.mensagem,
        cliente=entrada.cliente,
        canal=entrada.canal,
        modelo=f"{settings.llm_provider}:{settings.llm_model}",
    )


@app.post("/encaminhamentos", response_model=EncaminhamentoOut, status_code=201)
def registrar_encaminhamento(entrada: EncaminhamentoIn):
    """Chamado pelo n8n depois de decidir o destino. Guarda o registro para consulta."""
    return storage.salvar(entrada.model_dump())


@app.get("/encaminhamentos", response_model=list[EncaminhamentoOut])
def listar_encaminhamentos():
    return storage.listar()
