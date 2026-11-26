"""Configuração da aplicação, lida de variáveis de ambiente (.env)."""

import os

try:  # python-dotenv é opcional: no Docker as variáveis já vêm do compose
    from dotenv import load_dotenv

    load_dotenv()
except ImportError:  # pragma: no cover
    pass


def _get(nome: str, padrao: str = "") -> str:
    return os.getenv(nome, padrao).strip()


class Settings:
    """Lê as variáveis a cada acesso, o que facilita os testes."""

    @property
    def llm_provider(self) -> str:
        # gemini | openai | mock
        return _get("LLM_PROVIDER", "gemini").lower()

    @property
    def llm_api_key(self) -> str:
        return _get("LLM_API_KEY")

    @property
    def llm_model(self) -> str:
        padrao = "gpt-4o-mini" if self.llm_provider == "openai" else "gemini-2.5-flash"
        return _get("LLM_MODEL", padrao)

    @property
    def llm_base_url(self) -> str:
        # Só usado com o provedor "openai" (serve para OpenAI, Groq, OpenRouter etc.)
        return _get("LLM_BASE_URL", "https://api.openai.com/v1").rstrip("/")

    @property
    def llm_timeout(self) -> float:
        return float(_get("LLM_TIMEOUT", "30"))

    @property
    def data_dir(self) -> str:
        return _get("DATA_DIR", "data")


settings = Settings()
