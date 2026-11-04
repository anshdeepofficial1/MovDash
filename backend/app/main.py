from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.core.config import get_settings
from app.routers import catalog, health, sources

settings = get_settings()
app = FastAPI(
    title=settings.app_name,
    version="0.1.0",
    description="MovDash movie catalogue and authorized media-provider API.",
    docs_url="/docs",
    redoc_url="/redoc",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.cors_origin_list,
    allow_credentials=False,
    allow_methods=["GET", "POST", "OPTIONS"],
    allow_headers=["*"],
)

app.include_router(health.router)
app.include_router(catalog.router, prefix=settings.api_v1_prefix)
app.include_router(sources.router, prefix=settings.api_v1_prefix)
