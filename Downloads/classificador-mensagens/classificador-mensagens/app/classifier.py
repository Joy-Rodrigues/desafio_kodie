"""Integração com o modelo de IA: chama o provedor, extrai o JSON e valida o resultado."""

import json
import re
import unicodedata

from pydantic import ValidationError

from .config import settings
from .prompts import SYSTEM_PROMPT, montar_mensagem_usuario
from .schemas import Classificacao

MAX_TENTATIVAS = 2


class ClassificationError(Exception):
    """A IA não respondeu ou respondeu fora do formato esperado."""


# ---------------------------------------------------------------- provedores


def _chamar_gemini(system: str, user: str) -> str:
    import httpx

    url = f"https://generativelanguage.googleapis.com/v1beta/models/{settings.llm_model}:generateContent"
    corpo = {
        "systemInstruction": {"parts": [{"text": system}]},
        "contents": [{"role": "user", "parts": [{"text": user}]}],
        "generationConfig": {"temperature": 0, "responseMimeType": "application/json"},
    }
    resp = httpx.post(
        url,
        json=corpo,
        headers={"x-goog-api-key": settings.llm_api_key},
        timeout=settings.llm_timeout,
    )
    resp.raise_for_status()
    dados = resp.json()
    try:
        return dados["candidates"][0]["content"]["parts"][0]["text"]
    except (KeyError, IndexError, TypeError) as exc:
        raise ClassificationError("Resposta inesperada do Gemini (sem texto).") from exc


def _chamar_openai(system: str, user: str) -> str:
    """Qualquer API compatível com /chat/completions (OpenAI, Groq, OpenRouter...)."""
    import httpx

    corpo = {
        "model": settings.llm_model,
        "temperature": 0,
        "response_format": {"type": "json_object"},
        "messages": [
            {"role": "system", "content": system},
            {"role": "user", "content": user},
        ],
    }
    resp = httpx.post(
        f"{settings.llm_base_url}/chat/completions",
        json=corpo,
        headers={"Authorization": f"Bearer {settings.llm_api_key}"},
        timeout=settings.llm_timeout,
    )
    resp.raise_for_status()
    try:
        return resp.json()["choices"][0]["message"]["content"]
    except (KeyError, IndexError, TypeError) as exc:
        raise ClassificationError("Resposta inesperada do provedor (sem texto).") from exc


def _normalizar(texto: str) -> str:
    sem_acento = unicodedata.normalize("NFKD", texto).encode("ascii", "ignore").decode()
    return sem_acento.lower()


def _chamar_mock(_system: str, user: str) -> str:
    """Provedor falso por palavras-chave. Serve para testes e demonstração sem chave de API."""
    texto = _normalizar(user.split("<<<INICIO>>>")[-1].split("<<<FIM>>>")[0])

    def tem(*palavras: str) -> bool:
        return any(p in texto for p in palavras)

    if tem("cobranca", "cobrado", "boleto", "fatura", "reembolso", "estorno", "nota fiscal", "pagamento"):
        setor = "financeiro"
    elif tem("entrega", "pedido", "rastreio", "encomenda", "atrasou", "devolucao", "troca"):
        setor = "logistica"
    elif tem("preco", "plano", "orcamento", "contratar", "desconto", "cancelar"):
        setor = "comercial"
    elif tem("erro", "senha", "acesso", "login", "nao funciona", "travando", "fora do ar", "bug"):
        setor = "suporte"
    else:
        setor = "atendimento"

    if tem("obrigad", "parabens", "excelente", "adorei", "otimo atendimento"):
        tipo, sentimento = "elogio", "positivo"
    elif tem("absurdo", "pessimo", "horrivel", "reclam", "indevid", "inaceitavel", "nunca chegou", "atrasou"):
        tipo, sentimento = "reclamacao", "negativo"
    elif "?" in texto or tem("como ", "qual ", "quando ", "duvida", "gostaria de saber"):
        tipo, sentimento = "duvida", "neutro"
    elif tem("quero", "preciso", "solicito", "gostaria", "pode enviar", "segunda via", "cancelar"):
        tipo, sentimento = "solicitacao", "neutro"
    else:
        tipo, sentimento = "outro", "neutro"

    if tem("urgente", "indevid", "fora do ar", "processo", "procon", "cobrado duas", "cobrado em duplicidade"):
        prioridade = "alta"
    elif tipo == "reclamacao" or tipo == "solicitacao":
        prioridade = "media"
    else:
        prioridade = "baixa"

    trecho = " ".join(texto.split())[:100]
    return json.dumps(
        {
            "tipo": tipo,
            "setor": setor,
            "prioridade": prioridade,
            "sentimento": sentimento,
            "resumo": f"[mock] {trecho}",
            "confianca": 0.5,
        }
    )


_PROVEDORES = {
    "gemini": _chamar_gemini,
    "openai": _chamar_openai,
    "mock": _chamar_mock,
}

# ------------------------------------------------------------ extração / validação


def extrair_json(texto: str) -> dict:
    """Aceita JSON puro ou envolto em ```json ... ``` e devolve um dict."""
    limpo = texto.strip()
    limpo = re.sub(r"^```(?:json)?\s*|\s*```$", "", limpo, flags=re.IGNORECASE)
    try:
        dados = json.loads(limpo)
    except json.JSONDecodeError:
        # último recurso: pega do primeiro "{" ao último "}"
        inicio, fim = limpo.find("{"), limpo.rfind("}")
        if inicio == -1 or fim <= inicio:
            raise ClassificationError("A IA não devolveu um JSON.") from None
        try:
            dados = json.loads(limpo[inicio : fim + 1])
        except json.JSONDecodeError as exc:
            raise ClassificationError("A IA devolveu um JSON inválido.") from exc
    if not isinstance(dados, dict):
        raise ClassificationError("A IA devolveu um JSON que não é um objeto.")
    return dados


def validar(dados: dict) -> Classificacao:
    # modelos às vezes devolvem valores com acento/maiúscula ("Dúvida", "Média"): normalizamos
    for campo in ("tipo", "setor", "prioridade", "sentimento"):
        if isinstance(dados.get(campo), str):
            dados[campo] = _normalizar(dados[campo]).strip()
    try:
        return Classificacao(**dados)
    except (ValidationError, TypeError) as exc:
        raise ClassificationError(f"JSON fora do formato esperado: {exc}") from exc


# ------------------------------------------------------------------ função principal


def classificar(mensagem: str, cliente: str | None = None, canal: str | None = None) -> Classificacao:
    provedor = settings.llm_provider
    chamar = _PROVEDORES.get(provedor)
    if chamar is None:
        raise ClassificationError(f"LLM_PROVIDER inválido: '{provedor}'. Use gemini, openai ou mock.")
    if provedor != "mock" and not settings.llm_api_key:
        raise ClassificationError("LLM_API_KEY não configurada. Veja o arquivo .env.example.")

    usuario = montar_mensagem_usuario(mensagem, cliente, canal)
    ultimo_erro: Exception | None = None
    for _ in range(MAX_TENTATIVAS):
        bruto = chamar(SYSTEM_PROMPT, usuario)
        try:
            return validar(extrair_json(bruto))
        except ClassificationError as exc:  # resposta fora do formato: tenta de novo
            ultimo_erro = exc
    raise ClassificationError(f"Falha após {MAX_TENTATIVAS} tentativas. Último erro: {ultimo_erro}")
