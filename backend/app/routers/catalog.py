import httpx
from fastapi import APIRouter, HTTPException, Query

from app.core.config import get_settings
from app.schemas import MediaDetails, MediaSummary

router = APIRouter(prefix="/catalog", tags=["catalog"])
VALID_TYPES = {"movie", "tv"}
VALID_WINDOWS = {"day", "week"}


async def _tmdb_get(path: str, params: dict | None = None) -> dict:
    settings = get_settings()
    if not settings.tmdb_api_key:
        raise HTTPException(
            status_code=503,
            detail="TMDB is not configured. Add TMDB_API_KEY to the backend environment.",
        )
    request_params = {"api_key": settings.tmdb_api_key, "language": "en-US"}
    request_params.update(params or {})
    url = f"{settings.tmdb_base_url.rstrip('/')}/{path.lstrip('/')}"
    try:
        async with httpx.AsyncClient(timeout=settings.request_timeout_seconds) as client:
            response = await client.get(url, params=request_params)
            response.raise_for_status()
            return response.json()
    except httpx.HTTPStatusError as exc:
        raise HTTPException(status_code=502, detail=f"TMDB returned HTTP {exc.response.status_code}.") from exc
    except httpx.HTTPError as exc:
        raise HTTPException(status_code=502, detail="Could not reach TMDB. Please retry.") from exc


def _media_type(item: dict, requested: str | None = None) -> str:
    return item.get("media_type") or requested or ("tv" if "name" in item else "movie")


def _map_item(item: dict, requested_type: str | None = None) -> MediaSummary:
    media_type = _media_type(item, requested_type)
    return MediaSummary(
        id=item["id"],
        title=item.get("title") or item.get("name") or item.get("original_title") or item.get("original_name") or "Untitled",
        overview=item.get("overview") or "",
        poster_path=item.get("poster_path"),
        backdrop_path=item.get("backdrop_path"),
        release_date=item.get("release_date") or item.get("first_air_date"),
        vote_average=item.get("vote_average"),
        media_type=media_type,
        source="tmdb",
    )


def _map_results(payload: dict, requested_type: str | None = None) -> list[MediaSummary]:
    return [_map_item(item, requested_type) for item in payload.get("results", []) if item.get("id")]


def _validate_type(media_type: str) -> str:
    if media_type not in VALID_TYPES:
        raise HTTPException(status_code=400, detail="media_type must be 'movie' or 'tv'.")
    return media_type


@router.get("/home")
async def home_catalog():
    trending_movies, trending_tv, popular_movies, popular_tv = await __import__("asyncio").gather(
        _tmdb_get("trending/movie/week"),
        _tmdb_get("trending/tv/week"),
        _tmdb_get("movie/popular"),
        _tmdb_get("tv/popular"),
    )
    return {
        "trending_movies": _map_results(trending_movies, "movie"),
        "trending_series": _map_results(trending_tv, "tv"),
        "popular_movies": _map_results(popular_movies, "movie"),
        "popular_series": _map_results(popular_tv, "tv"),
    }


@router.get("/trending", response_model=list[MediaSummary])
async def trending(
    media_type: str = Query(default="movie", pattern="^(movie|tv|all)$"),
    time_window: str = Query(default="week", pattern="^(day|week)$"),
):
    payload = await _tmdb_get(f"trending/{media_type}/{time_window}")
    return _map_results(payload, media_type if media_type != "all" else None)


@router.get("/popular/{media_type}", response_model=list[MediaSummary])
async def popular(media_type: str):
    media_type = _validate_type(media_type)
    return _map_results(await _tmdb_get(f"{media_type}/popular"), media_type)


@router.get("/top-rated/{media_type}", response_model=list[MediaSummary])
async def top_rated(media_type: str):
    media_type = _validate_type(media_type)
    return _map_results(await _tmdb_get(f"{media_type}/top_rated"), media_type)


@router.get("/upcoming/{media_type}", response_model=list[MediaSummary])
async def upcoming(media_type: str):
    media_type = _validate_type(media_type)
    endpoint = "movie/upcoming" if media_type == "movie" else "tv/on_the_air"
    return _map_results(await _tmdb_get(endpoint), media_type)


@router.get("/genres/{media_type}")
async def genres(media_type: str):
    media_type = _validate_type(media_type)
    payload = await _tmdb_get(f"genre/{media_type}/list")
    return payload.get("genres", [])


@router.get("/discover/{media_type}", response_model=list[MediaSummary])
async def discover(
    media_type: str,
    page: int = Query(default=1, ge=1, le=500),
    sort_by: str = Query(default="popularity.desc", max_length=40),
    with_genres: str | None = Query(default=None, max_length=100),
    year: int | None = Query(default=None, ge=1888, le=2100),
):
    media_type = _validate_type(media_type)
    params = {"page": page, "sort_by": sort_by, "include_adult": "false"}
    if with_genres:
        params["with_genres"] = with_genres
    if year:
        params["primary_release_year" if media_type == "movie" else "first_air_date_year"] = year
    return _map_results(await _tmdb_get(f"discover/{media_type}", params), media_type)


@router.get("/search", response_model=list[MediaSummary])
async def search(
    q: str = Query(min_length=1, max_length=120),
    media_type: str = Query(default="multi", pattern="^(movie|tv|multi)$"),
    page: int = Query(default=1, ge=1, le=500),
):
    endpoint = "search/multi" if media_type == "multi" else f"search/{media_type}"
    payload = await _tmdb_get(endpoint, {"query": q.strip(), "include_adult": "false", "page": page})
    results = _map_results(payload, media_type if media_type != "multi" else None)
    return [item for item in results if item.media_type in VALID_TYPES]


@router.get("/{media_type}/{item_id}", response_model=MediaDetails)
async def details(media_type: str, item_id: int):
    media_type = _validate_type(media_type)
    payload = await _tmdb_get(f"{media_type}/{item_id}", {"append_to_response": "recommendations"})
    recommendations = payload.get("recommendations", {}).get("results", [])
    try:
        watch_payload = await _tmdb_get(f"{media_type}/{item_id}/watch/providers")
        watch_results = watch_payload.get("results", {})
    except HTTPException:
        watch_results = {}
    return MediaDetails(
        **_map_item(payload, media_type).model_dump(),
        tagline=payload.get("tagline") or "",
        runtime=payload.get("runtime") or (payload.get("episode_run_time") or [None])[0],
        genres=payload.get("genres", []),
        status=payload.get("status"),
        number_of_seasons=payload.get("number_of_seasons"),
        number_of_episodes=payload.get("number_of_episodes"),
        seasons=payload.get("seasons", []),
        recommendations=[_map_item(item, media_type) for item in recommendations if item.get("id")],
        watch_providers=watch_results,
    )


@router.get("/tv/{series_id}/season/{season_number}")
async def season_details(series_id: int, season_number: int):
    payload = await _tmdb_get(f"tv/{series_id}/season/{season_number}")
    return {
        "id": payload.get("id"),
        "name": payload.get("name"),
        "season_number": payload.get("season_number"),
        "episodes": [
            {
                "id": item.get("id"),
                "episode_number": item.get("episode_number"),
                "name": item.get("name"),
                "overview": item.get("overview") or "",
                "still_path": item.get("still_path"),
                "runtime": item.get("runtime"),
                "air_date": item.get("air_date"),
            }
            for item in payload.get("episodes", [])
        ],
    }
