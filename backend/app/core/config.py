from functools import lru_cache
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    app_name: str = "MovDash API"
    app_env: str = "development"
    api_v1_prefix: str = "/api/v1"
    tmdb_api_key: str = ""
    tmdb_base_url: str = "https://api.themoviedb.org/3"
    jellyfin_base_url: str = ""
    jellyfin_api_key: str = ""
    request_timeout_seconds: float = 12.0
    cors_origins: str = "http://localhost:3000,http://10.0.2.2:8000"

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        extra="ignore",
    )

    @property
    def cors_origin_list(self) -> list[str]:
        return [origin.strip() for origin in self.cors_origins.split(",") if origin.strip()]


@lru_cache
def get_settings() -> Settings:
    return Settings()
