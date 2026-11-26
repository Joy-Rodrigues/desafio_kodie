import pytest

pytest.importorskip("fastapi")
pytest.importorskip("httpx")

from fastapi.testclient import TestClient  # noqa: E402

from app.main import app  # noqa: E402


@pytest.fixture
def client(monkeypatch, tmp_path):
    monkeypatch.setenv("LLM_PROVIDER", "mock")
    monkeypatch.setenv("DATA_DIR", str(tmp_path))
    return TestClient(app)


def test_health(client):
    r = client.get("/health")
    assert r.status_code == 200
    assert r.json()["status"] == "ok"


def test_classificar_retorna_json_organizado(client):
    r = client.post("/classificar", json={"mensagem": "Meu boleto veio com valor errado, cobrança indevida!", "cliente": "Ana"})
    assert r.status_code == 200
    corpo = r.json()
    assert corpo["setor"] == "financeiro"
    assert corpo["prioridade"] == "alta"
    assert corpo["cliente"] == "Ana"
    for campo in ("tipo", "setor", "prioridade", "sentimento", "resumo", "confianca", "modelo"):
        assert campo in corpo


def test_mensagem_vazia_e_rejeitada(client):
    assert client.post("/classificar", json={"mensagem": ""}).status_code == 422
    assert client.post("/classificar", json={}).status_code == 422


def test_registrar_e_listar_encaminhamentos(client):
    payload = {
        "mensagem": "Meu pedido não chegou",
        "cliente": "Bia",
        "canal": "email",
        "tipo": "reclamacao",
        "setor": "logistica",
        "prioridade": "media",
        "resumo": "Pedido não chegou.",
        "destino": "logistica@empresa.com",
    }
    r = client.post("/encaminhamentos", json=payload)
    assert r.status_code == 201
    assert r.json()["id"] == 1
    lista = client.get("/encaminhamentos").json()
    assert len(lista) == 1
    assert lista[0]["destino"] == "logistica@empresa.com"
