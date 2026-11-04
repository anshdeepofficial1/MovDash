import httpx
from fastapi import APIRouter, HTTPException, Query

from app.core.config import get_settings
from app.schemas import MovieSummary

router = APIRouter(prefix="/catalog", tags=["catalog"])


@router.get("/search", response_model=list[MovieSummary])
async def search_movies(q: str = Query(min_length=1, max_length=120)):
    settings = get_settings()
    if not settings.tmdb_api_key:
        raise HTTPException(
            status_code=503,
            detail="TMDB is not configured. Add TMDB_API_KEY to backend/.env.",
        )

    url = f"{settings.tmdb_base_url.rstrip('/')}/search/movie"
    params = {
        "api_key": settings.tmdb_api_key,
        "query": q,
        "include_adult": "false",
    }

    try:
        async with httpx.AsyncClient(
            timeout=settings.request_timeout_seconds
        ) as client:
            response = await client.get(url, params=params)
            response.raise_for_status()
            payload = response.json()
    except httpx.HTTPStatusError as exc:
        raise HTTPException(
            status_code=502,
            detail="TMDB rejected the request.",
        ) from exc
    except httpx.HTTPError as exc:
        raise HTTPException(
            status_code=502,
            detail="Could not reach TMDB.",
        ) from exc

    return [
        MovieSummary(
            id=item["id"],
            title=item.get("title") or item.get("original_title") or "Untitled",
            overview=item.get("overview") or "",
            poster_path=item.get("poster_path"),
            backdrop_path=item.get("backdrop_path"),
            release_date=item.get("release_date"),
            vote_average=item.get("vote_average"),
            source="tmdb",
        )
        for item in payload.get("results", [])
    ]
