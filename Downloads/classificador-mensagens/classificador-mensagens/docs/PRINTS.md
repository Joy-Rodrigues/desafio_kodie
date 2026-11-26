# Documentação Visual do Fluxo de Automação

Esta página reúne os prints exigidos na seção 7 do desafio. Cada item descreve o que o print deve mostrar. As imagens ficam na pasta [`docs/img/`](img/).

> **Importante:** antes de tirar os prints, confira se nenhuma chave de API aparece na tela. No n8n deste projeto não há credenciais, mas a chave fica no `.env`; não o mostre.

## 1. Fluxo completo

Canvas do n8n com todos os nodes conectados (use *Fit view*, atalho `1`).

![Fluxo completo](img/01-fluxo-completo.png)

**Descrição:** o Webhook recebe a mensagem, o Edit Fields normaliza os dados, o HTTP Request chama a API de classificação (IA), o Switch separa por setor, cada Edit Fields define o destino e o último HTTP Request registra o encaminhamento.

## 2. Execução bem-sucedida

Aba **Executions** com uma execução com status *Success* (todos os nodes verdes).

![Execução bem-sucedida](img/02-execucao-sucesso.png)

**Descrição:** comprova que o fluxo completo funcionou do início ao fim para uma mensagem de teste.

## 3. Configuração dos nodes

Abra cada node (duplo clique) e tire um print com a configuração visível.

| # | Node | O que mostrar | Print |
|---|---|---|---|
| 3.1 | Webhook | Método `POST`, path `mensagens`, *Respond: When Last Node Finishes* | `img/03-1-webhook.png` |
| 3.2 | Edit Fields (Normalizar) | Os 3 campos: `mensagem`, `cliente`, `canal` | `img/03-2-normalizar.png` |
| 3.3 | HTTP Request (Classificar com IA) | `POST`, URL `http://api:8000/classificar`, body JSON | `img/03-3-classificar.png` |
| 3.4 | Switch | As 4 regras por `setor` + saída *Atendimento* | `img/03-4-switch.png` |
| 3.5 | Edit Fields (Encaminhar ...) | Pelo menos um por setor, mostrando o campo `destino` | `img/03-5-encaminhar-*.png` |
| 3.6 | HTTP Request (Registrar encaminhamento) | `POST`, URL `http://api:8000/encaminhamentos`, body JSON | `img/03-6-registrar.png` |

![Webhook](img/03-1-webhook.png)
![Normalizar](img/03-2-normalizar.png)
![Classificar com IA](img/03-3-classificar.png)
![Switch](img/03-4-switch.png)
![Encaminhar (exemplo)](img/03-5-encaminhar-financeiro.png)
![Registrar encaminhamento](img/03-6-registrar.png)

## 4. Resultados em cada etapa

Dentro de uma execução, clique nos nodes e tire print da aba **OUTPUT** (ou use *Executions → abrir execução*).

| # | Etapa | O que o print deve mostrar |
|---|---|---|
| 4.1 | Saída do Webhook | O `body` recebido com `mensagem`, `cliente`, `canal` |
| 4.2 | Saída do HTTP Request (IA) | O JSON classificado: `tipo`, `setor`, `prioridade`, `sentimento`, `resumo`, `confianca` |
| 4.3 | Saída do Switch | Qual caminho foi seguido (setor) |
| 4.4 | Saída do Edit Fields (Encaminhar) | O campo `destino` preenchido |
| 4.5 | Saída do último HTTP Request | O registro criado, com `id` e `registrado_em` |
| 4.6 | `GET /encaminhamentos` no navegador | A lista de encaminhamentos gravados |

![Saída do Webhook](img/04-1-saida-webhook.png)
![Saída da IA](img/04-2-saida-ia.png)
![Saída do Switch](img/04-3-saida-switch.png)
![Saída do Encaminhar](img/04-4-saida-encaminhar.png)
![Saída do Registrar](img/04-5-saida-registrar.png)
![Lista de encaminhamentos](img/04-6-lista-encaminhamentos.png)

## Dica para gerar boas execuções

Rode `./scripts/enviar_exemplos.sh` (fluxo ativo): as 5 mensagens de exemplo passam por setores diferentes (financeiro, logística, suporte, comercial e atendimento), o que mostra o Switch funcionando com caminhos diferentes.
