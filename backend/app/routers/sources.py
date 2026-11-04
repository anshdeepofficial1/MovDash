import httpx
from fastapi import APIRouter, HTTPException

from app.core.config import get_settings
from app.schemas import JellyfinMovie

router = APIRouter(prefix="/sources", tags=["sources"])


def jellyfin_headers(api_key: str) -> dict[str, str]:
    return {"X-Emby-Token": api_key, "Accept": "application/json"}


@router.get("")
async def list_sources():
    settings = get_settings()
    return [
        {
            "id": "jellyfin",
            "name": "Jellyfin",
            "configured": bool(
                settings.jellyfin_base_url and settings.jellyfin_api_key
            ),
            "type": "personal_media_server",
            "authorization": "operator_or_library_owner_required",
        },
        {
            "id": "internet_archive",
            "name": "Internet Archive",
            "configured": False,
            "type": "licensed_or_public_domain_catalog",
            "note": "Planned integration; verify item-level rights before playback.",
        },
    ]


@router.get("/jellyfin/health")
async def jellyfin_health():
    settings = get_settings()
    if not settings.jellyfin_base_url or not settings.jellyfin_api_key:
        raise HTTPException(
            status_code=503,
            detail="Configure JELLYFIN_BASE_URL and JELLYFIN_API_KEY.",
        )

    url = f"{settings.jellyfin_base_url.rstrip('/')}/System/Info"
    try:
        async with httpx.AsyncClient(
            timeout=settings.request_timeout_seconds
        ) as client:
            response = await client.get(
                url,
                headers=jellyfin_headers(settings.jellyfin_api_key),
            )
            response.raise_for_status()
            data = response.json()
    except httpx.HTTPStatusError as exc:
        raise HTTPException(
            status_code=502,
            detail=f"Jellyfin returned HTTP {exc.response.status_code}.",
        ) from exc
    except httpx.HTTPError as exc:
        raise HTTPException(
            status_code=502,
            detail="Could not reach the configured Jellyfin server.",
        ) from exc

    return {
        "status": "connected",
        "server_name": data.get("ServerName", "Jellyfin"),
        "version": data.get("Version"),
        "server_id": data.get("Id"),
    }


@router.get("/jellyfin/movies", response_model=list[JellyfinMovie])
async def jellyfin_movies():
    settings = get_settings()
    if not settings.jellyfin_base_url or not settings.jellyfin_api_key:
        raise HTTPException(
            status_code=503,
            detail="Configure JELLYFIN_BASE_URL and JELLYFIN_API_KEY.",
        )

    url = f"{settings.jellyfin_base_url.rstrip('/')}/Items"
    params = {
        "IncludeItemTypes": "Movie",
        "Recursive": "true",
        "Fields": "Overview,ProductionYear,ImageTags",
        "Limit": 100,
    }

    try:
        async with httpx.AsyncClient(
            timeout=settings.request_timeout_seconds
        ) as client:
            response = await client.get(
                url,
                params=params,
                headers=jellyfin_headers(settings.jellyfin_api_key),
            )
            response.raise_for_status()
            payload = response.json()
    except httpx.HTTPStatusError as exc:
        raise HTTPException(
            status_code=502,
            detail=f"Jellyfin returned HTTP {exc.response.status_code}.",
        ) from exc
    except httpx.HTTPError as exc:
        raise HTTPException(
            status_code=502,
            detail="Could not retrieve the Jellyfin library.",
        ) from exc

    return [
        JellyfinMovie(
            id=item["Id"],
            title=item.get("Name") or "Untitled",
            year=item.get("ProductionYear"),
            overview=item.get("Overview") or "",
            image_tag=(item.get("ImageTags") or {}).get("Primary"),
        )
        for item in payload.get("Items", [])
    ]
