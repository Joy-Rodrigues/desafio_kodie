#!/usr/bin/env bash
# Envia as mensagens de exemplo para o webhook do n8n (fluxo ATIVO).
# Uso: ./scripts/enviar_exemplos.sh [URL_DO_WEBHOOK]
# Para testar com o botão "Execute workflow" do n8n, use a URL de teste:
#   ./scripts/enviar_exemplos.sh http://localhost:5678/webhook-test/mensagens
set -euo pipefail

URL="${1:-http://localhost:5678/webhook/mensagens}"
ARQUIVO="$(dirname "$0")/../exemplos/mensagens.json"

python3 - "$URL" "$ARQUIVO" <<'EOF'
import json, sys, urllib.request, urllib.error

url, arquivo = sys.argv[1], sys.argv[2]
for msg in json.load(open(arquivo, encoding="utf-8")):
    req = urllib.request.Request(
        url,
        data=json.dumps(msg).encode("utf-8"),
        headers={"Content-Type": "application/json"},
        method="POST",
    )
    try:
        with urllib.request.urlopen(req, timeout=90) as r:
            print(f"[{r.status}] {msg['cliente']}: {r.read().decode('utf-8')}\n")
    except urllib.error.HTTPError as e:
        print(f"[{e.code}] {msg['cliente']}: {e.read().decode('utf-8')}\n")
EOF
