"""Armazenamento simples dos encaminhamentos em um arquivo JSON Lines."""

import json
import os
import threading
from datetime import datetime, timezone

from .config import settings

_lock = threading.Lock()


def _caminho() -> str:
    os.makedirs(settings.data_dir, exist_ok=True)
    return os.path.join(settings.data_dir, "encaminhamentos.jsonl")


def listar() -> list[dict]:
    caminho = _caminho()
    if not os.path.exists(caminho):
        return []
    with _lock, open(caminho, encoding="utf-8") as f:
        return [json.loads(linha) for linha in f if linha.strip()]


def salvar(registro: dict) -> dict:
    with _lock:
        caminho = _caminho()
        proximo_id = 1
        if os.path.exists(caminho):
            with open(caminho, encoding="utf-8") as f:
                proximo_id = sum(1 for linha in f if linha.strip()) + 1
        completo = {
            "id": proximo_id,
            "registrado_em": datetime.now(timezone.utc).isoformat(timespec="seconds"),
            **registro,
        }
        with open(caminho, "a", encoding="utf-8") as f:
            f.write(json.dumps(completo, ensure_ascii=False) + "\n")
        return completo
