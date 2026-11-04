from fastapi import APIRouter

from app.core.config import get_settings
from app.schemas import HealthResponse

router = APIRouter(tags=["system"])


@router.get("/health", response_model=HealthResponse)
async def health():
    settings = get_settings()
    return HealthResponse(
        status="ok",
        app=settings.app_name,
        environment=settings.app_env,
    )


@router.get("/api/v1/config/status")
async def config_status():
    settings = get_settings()
    return {
        "tmdb_configured": bool(settings.tmdb_api_key),
        "jellyfin_configured": bool(
            settings.jellyfin_base_url and settings.jellyfin_api_key
        ),
        "environment": settings.app_env,
        "secrets_exposed": False,
    }
