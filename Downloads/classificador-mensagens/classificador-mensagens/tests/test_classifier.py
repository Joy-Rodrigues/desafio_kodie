import json

import pytest

from app import classifier
from app.classifier import ClassificationError, extrair_json, validar


@pytest.fixture(autouse=True)
def usar_mock(monkeypatch):
    monkeypatch.setenv("LLM_PROVIDER", "mock")


def test_extrai_json_puro():
    assert extrair_json('{"a": 1}') == {"a": 1}


def test_extrai_json_dentro_de_markdown():
    assert extrair_json('```json\n{"a": 1}\n```') == {"a": 1}


def test_extrai_json_com_texto_em_volta():
    assert extrair_json('Claro! Aqui está: {"a": 1} Espero ter ajudado.') == {"a": 1}


def test_rejeita_texto_sem_json():
    with pytest.raises(ClassificationError):
        extrair_json("não sei classificar")


def test_validar_normaliza_acento_e_maiuscula():
    dados = {
        "tipo": "Dúvida",
        "setor": "Logística",
        "prioridade": "Média",
        "sentimento": "Neutro",
        "resumo": "Cliente quer saber o prazo.",
        "confianca": 0.9,
    }
    c = validar(dados)
    assert (c.tipo, c.setor, c.prioridade) == ("duvida", "logistica", "media")


def test_validar_rejeita_valor_fora_da_lista():
    dados = {
        "tipo": "xingamento",
        "setor": "suporte",
        "prioridade": "alta",
        "sentimento": "negativo",
        "resumo": "x",
        "confianca": 0.9,
    }
    with pytest.raises(ClassificationError):
        validar(dados)


def test_validar_rejeita_confianca_invalida():
    dados = {
        "tipo": "duvida",
        "setor": "suporte",
        "prioridade": "baixa",
        "sentimento": "neutro",
        "resumo": "x",
        "confianca": 7,
    }
    with pytest.raises(ClassificationError):
        validar(dados)


def test_classificar_cobranca_duplicada_vai_para_financeiro_com_prioridade_alta():
    r = classifier.classificar("Fui cobrado duas vezes na fatura deste mês, isso é um absurdo!")
    assert r.setor == "financeiro"
    assert r.tipo == "reclamacao"
    assert r.prioridade == "alta"


def test_classificar_duvida_de_entrega():
    r = classifier.classificar("Qual é o prazo de entrega para o meu pedido?")
    assert r.setor == "logistica"
    assert r.tipo == "duvida"


def test_tenta_de_novo_quando_a_ia_responde_lixo(monkeypatch):
    respostas = iter(["isso não é json", json.dumps({
        "tipo": "elogio", "setor": "atendimento", "prioridade": "baixa",
        "sentimento": "positivo", "resumo": "Elogio ao atendimento.", "confianca": 0.9,
    })])
    monkeypatch.setitem(classifier._PROVEDORES, "mock", lambda s, u: next(respostas))
    assert classifier.classificar("Obrigado pelo atendimento!").tipo == "elogio"


def test_falha_depois_de_todas_as_tentativas(monkeypatch):
    monkeypatch.setitem(classifier._PROVEDORES, "mock", lambda s, u: "lixo")
    with pytest.raises(ClassificationError):
        classifier.classificar("qualquer coisa")


def test_provedor_invalido(monkeypatch):
    monkeypatch.setenv("LLM_PROVIDER", "inexistente")
    with pytest.raises(ClassificationError):
        classifier.classificar("oi, tudo bem?")


def test_sem_chave_de_api(monkeypatch):
    monkeypatch.setenv("LLM_PROVIDER", "gemini")
    monkeypatch.delenv("LLM_API_KEY", raising=False)
    with pytest.raises(ClassificationError):
        classifier.classificar("oi, tudo bem?")
