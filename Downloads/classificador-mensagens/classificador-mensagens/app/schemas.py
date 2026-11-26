"""Modelos de entrada e saída da API (validação com Pydantic)."""

from typing import Literal, Optional

from pydantic import BaseModel, Field

Tipo = Literal["duvida", "reclamacao", "solicitacao", "elogio", "outro"]
Setor = Literal["suporte", "financeiro", "comercial", "logistica", "atendimento"]
Prioridade = Literal["alta", "media", "baixa"]
Sentimento = Literal["positivo", "neutro", "negativo"]


class MensagemIn(BaseModel):
    mensagem: str = Field(..., min_length=3, max_length=2000, description="Texto enviado pelo cliente")
    cliente: Optional[str] = Field(None, max_length=120, description="Nome ou identificador do cliente")
    canal: Optional[str] = Field(None, max_length=50, description="Canal de origem (email, whatsapp, site...)")


class Classificacao(BaseModel):
    """Formato que a IA é obrigada a devolver. Qualquer coisa fora disso é rejeitada."""

    tipo: Tipo
    setor: Setor
    prioridade: Prioridade
    sentimento: Sentimento
    resumo: str = Field(..., min_length=1, max_length=300)
    confianca: float = Field(..., ge=0, le=1)


class ClassificacaoOut(Classificacao):
    mensagem: str
    cliente: Optional[str] = None
    canal: Optional[str] = None
    modelo: str


class EncaminhamentoIn(BaseModel):
    mensagem: str
    cliente: Optional[str] = None
    canal: Optional[str] = None
    tipo: Tipo
    setor: Setor
    prioridade: Prioridade
    resumo: str
    destino: str = Field(..., description="Destino do encaminhamento, ex.: suporte@empresa.com")


class EncaminhamentoOut(EncaminhamentoIn):
    id: int
    registrado_em: str
