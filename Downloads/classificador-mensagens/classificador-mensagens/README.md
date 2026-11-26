# Classificador de Mensagens com IA + Automação (n8n)

Projeto do **Desafio Técnico KODIE — IA e Automação** (caso 4.1, *Classificação de Mensagens*).
Uma API em **FastAPI** usa um modelo de **IA** para classificar mensagens de clientes, e um fluxo no **n8n** recebe cada mensagem, chama a API e encaminha o resultado para o setor responsável.

**Autora:** Joyce Rodrigues Teotonio

---

## 1. Apresentação da Ideia

A ideia nasceu do cenário proposto no desafio: uma pequena empresa recebe todos os dias dúvidas, pedidos e reclamações de clientes, e alguém precisa ler cada mensagem, descobrir o assunto e encaminhar para a pessoa certa. É um trabalho repetitivo, lento e fácil de errar quando o volume cresce.

Pensei em separar as responsabilidades: a **IA interpreta** o texto (algo que regras fixas fazem mal, porque o cliente escreve de mil jeitos), a **API garante um formato confiável** e a **automação executa** o encaminhamento.

## 2. Problema Escolhido

**Caso 4.1: Classificação de Mensagens.**
Uma empresa recebe mensagens de clientes sem um processo organizado para identificar o assunto. A solução precisa:

- identificar **o que o cliente quer** (tipo da mensagem);
- descobrir **quem deve atender** (setor);
- indicar **o quão urgente é** (prioridade);
- devolver tudo em um **formato estruturado** que uma automação consiga usar.

## 3. Solução Proposta

```
Cliente / sistema
      │  POST /webhook/mensagens  {mensagem, cliente, canal}
      ▼
┌───────────────────────────── n8n ─────────────────────────────┐
│ Webhook → Edit Fields → HTTP Request ──────────┐              │
│                                                │ POST /classificar
│                                                ▼              │
│                                   ┌──────── API FastAPI ───────┐
│                                   │ prompt estruturado → IA    │
│                                   │ extrai JSON → valida       │
│                                   └────────────┬───────────────┘
│                          JSON classificado ◄───┘               │
│ Switch (por setor) → Edit Fields (destino) → HTTP Request      │
│                                   │ POST /encaminhamentos      │
└───────────────────────────────────┼────────────────────────────┘
                                    ▼
                        API registra o encaminhamento
                        (GET /encaminhamentos para consultar)
```

**Como a IA participa:** ela recebe a mensagem do cliente e um prompt com as categorias permitidas e as regras de classificação, e responde com um JSON contendo:

| Campo | Valores |
|---|---|
| `tipo` | `duvida`, `reclamacao`, `solicitacao`, `elogio`, `outro` |
| `setor` | `suporte`, `financeiro`, `comercial`, `logistica`, `atendimento` |
| `prioridade` | `alta`, `media`, `baixa` |
| `sentimento` | `positivo`, `neutro`, `negativo` |
| `resumo` | resumo da mensagem em até 140 caracteres |
| `confianca` | número de 0 a 1 |

**Como garantimos o formato adequado da resposta** (uma das perguntas do desafio):

1. o prompt exige JSON e lista os valores permitidos, com um exemplo;
2. a chamada usa temperatura 0 e o modo de resposta JSON do provedor;
3. a API extrai o JSON (mesmo se vier dentro de ` ```json `) e **valida com Pydantic**; valores fora da lista são rejeitados;
4. se a resposta vier inválida, a API **tenta de novo** uma vez; se falhar de novo, devolve erro `502` claro, em vez de passar lixo adiante;
5. o texto do cliente é delimitado no prompt e tratado como *dado*, o que reduz o risco de *prompt injection*.

## 4. Tecnologias Utilizadas

- **Python 3.12** + **FastAPI** (API) + **Pydantic** (validação) + **httpx** (chamadas HTTP)
- **IA:** Google Gemini (`gemini-2.5-flash`, padrão) ou qualquer API compatível com OpenAI (OpenAI, Groq, OpenRouter...), escolhida pela variável `LLM_PROVIDER`
- **n8n** (automação)
- **Docker** e **Docker Compose** (entrega opcional)
- **pytest** (testes)

## 5. Funcionamento do Sistema

### API (`app/`)

| Arquivo | Função |
|---|---|
| `main.py` | Rotas da API |
| `prompts.py` | Prompt do sistema e montagem da mensagem do usuário |
| `classifier.py` | Chamada à IA, extração do JSON, validação e nova tentativa |
| `schemas.py` | Modelos Pydantic de entrada e saída |
| `storage.py` | Grava os encaminhamentos em `data/encaminhamentos.jsonl` |
| `config.py` | Lê as variáveis de ambiente |

| Rota | O que faz |
|---|---|
| `GET /health` | Verifica se a API está no ar e mostra o provedor configurado |
| `POST /classificar` | Recebe a mensagem e devolve a classificação da IA |
| `POST /encaminhamentos` | Registra o encaminhamento decidido pelo n8n |
| `GET /encaminhamentos` | Lista os encaminhamentos registrados |

Documentação interativa (Swagger): `http://localhost:8000/docs`.

**Exemplo de requisição:**

```bash
curl -X POST http://localhost:8000/classificar \
  -H "Content-Type: application/json" \
  -d '{"mensagem": "Fui cobrada duas vezes na fatura deste mês, preciso do estorno urgente!", "cliente": "Ana Souza", "canal": "email"}'
```

**Exemplo de resposta (o texto exato varia conforme o modelo):**

```json
{
  "tipo": "reclamacao",
  "setor": "financeiro",
  "prioridade": "alta",
  "sentimento": "negativo",
  "resumo": "Cliente foi cobrada duas vezes pela fatura do mês e pede estorno urgente.",
  "confianca": 0.95,
  "mensagem": "Fui cobrada duas vezes na fatura deste mês, preciso do estorno urgente!",
  "cliente": "Ana Souza",
  "canal": "email",
  "modelo": "gemini:gemini-2.5-flash"
}
```

### Fluxo n8n (`n8n/workflow.json`)

| # | Node | Função |
|---|---|---|
| 1 | **Webhook** | Recebe `POST /webhook/mensagens` com `mensagem`, `cliente` e `canal` |
| 2 | **Edit Fields** (Normalizar) | Tira os campos de dentro do `body` e define valores padrão |
| 3 | **HTTP Request** (Classificar com IA) | Chama `POST /classificar` na API |
| 4 | **Switch** (Roteador por setor) | Separa o fluxo por `setor`; o que não for conhecido cai em *Atendimento* |
| 5 | **Edit Fields** (5 nodes, um por setor) | Define o `destino` (e-mail do setor responsável) |
| 6 | **HTTP Request** (Registrar encaminhamento) | Chama `POST /encaminhamentos`; a resposta volta ao solicitante do webhook |

Os e-mails de destino (`suporte@empresa-exemplo.com` etc.) são fictícios, e o encaminhamento fica registrado pela API. Para enviar de verdade, basta trocar o último node por Gmail, Slack, Telegram ou similar.

### Documentação visual

Os prints do fluxo (fluxo completo, execução com sucesso, configuração de cada node e dados de cada etapa) ficam em [`docs/PRINTS.md`](docs/PRINTS.md), com a descrição de cada um.

## 6. Como Executar

### Pré-requisitos

- Docker e Docker Compose (caminho recomendado), **ou** Python 3.12+ e uma instância do n8n
- Uma chave de API de IA. A do Gemini é gratuita em <https://aistudio.google.com/apikey>. Sem chave, dá para testar o fluxo com `LLM_PROVIDER=mock`.

### Opção A: Docker Compose (API + n8n)

```bash
git clone <URL_DO_REPOSITORIO>
cd classificador-mensagens

cp .env.example .env
# edite o .env e preencha LLM_API_KEY (ou use LLM_PROVIDER=mock para testar sem chave)

docker compose up --build -d
```

1. API: <http://localhost:8000/docs>
2. n8n: <http://localhost:5678> (na primeira vez ele pede para criar uma conta local)
3. Importe o fluxo, no terminal:

   ```bash
   docker compose exec n8n n8n import:workflow --input=/workflows/workflow.json
   ```

   ou, pela interface: **Workflows → ⋯ → Import from file** → `n8n/workflow.json`.
4. Abra o workflow e **ative** (chave *Active* no canto superior direito).
5. Envie as mensagens de exemplo:

   ```bash
   ./scripts/enviar_exemplos.sh
   ```

6. Veja os encaminhamentos registrados: <http://localhost:8000/encaminhamentos> e a aba **Executions** do n8n.

> Para testar sem ativar o fluxo: clique em **Execute workflow** no n8n e envie para a URL de teste:
> `./scripts/enviar_exemplos.sh http://localhost:5678/webhook-test/mensagens`
> (o n8n aceita uma chamada por clique no botão).

### Opção B: Rodando a API sem Docker

```bash
python -m venv .venv
source .venv/bin/activate        # Windows: .venv\Scripts\activate
pip install -r requirements.txt
cp .env.example .env             # preencha LLM_API_KEY
uvicorn app.main:app --reload
```

Neste modo o n8n roda fora do Docker. No fluxo importado, troque nos dois nodes HTTP Request a URL `http://api:8000` por `http://localhost:8000` (ou `http://host.docker.internal:8000`, se o n8n estiver em Docker).

### Testes

```bash
pytest -q
```

Os testes usam o provedor `mock`, então não gastam chamadas de IA.

### Variáveis de ambiente

| Variável | Descrição |
|---|---|
| `LLM_PROVIDER` | `gemini` (padrão), `openai` ou `mock` |
| `LLM_API_KEY` | Chave da API de IA |
| `LLM_MODEL` | Modelo (opcional) |
| `LLM_BASE_URL` | URL base, só para `openai`-compatível |
| `LLM_TIMEOUT` | Timeout da chamada à IA, em segundos |
| `DATA_DIR` | Pasta dos encaminhamentos |

> **Segurança:** o `.env` está no `.gitignore`. Só o `.env.example`, sem chaves, vai para o repositório.

## Estrutura do repositório

```
.
├── app/                    # API FastAPI
├── tests/                  # testes automatizados
├── n8n/workflow.json       # fluxo exportado do n8n
├── exemplos/mensagens.json # mensagens de teste
├── scripts/enviar_exemplos.sh
├── docs/                   # prints e descrição do fluxo
├── Dockerfile
├── docker-compose.yml
├── requirements.txt
└── .env.example
```

## Limitações e próximos passos

- O armazenamento é um arquivo de texto (suficiente para o desafio); em produção, usar um banco de dados.
- A API não tem autenticação; em produção, proteger com chave de API.
- Possíveis evoluções: enviar o encaminhamento por e-mail/Slack de verdade, responder automaticamente ao cliente e criar um painel com o volume por setor.
