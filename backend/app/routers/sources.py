import httpx
from fastapi import APIRouter, HTTPException, Query, Request
from fastapi.responses import StreamingResponse

from app.core.config import get_settings
from app.schemas import JellyfinItem, JellyfinMovie, PlaybackSession

router = APIRouter(prefix="/sources", tags=["sources"])


def jellyfin_headers(api_key: str) -> dict[str, str]:
    return {"X-Emby-Token": api_key, "Accept": "application/json"}


def _jellyfin_config():
    settings = get_settings()
    if not settings.jellyfin_base_url or not settings.jellyfin_api_key:
        raise HTTPException(
            status_code=503,
            detail="Jellyfin is not configured. Set JELLYFIN_BASE_URL and JELLYFIN_API_KEY on the backend.",
        )
    return settings


def _map_item(item: dict) -> JellyfinItem:
    return JellyfinItem(
        id=item["Id"],
        title=item.get("Name") or "Untitled",
        year=item.get("ProductionYear"),
        overview=item.get("Overview") or "",
        media_type=item.get("Type") or "Movie",
        image_tag=(item.get("ImageTags") or {}).get("Primary"),
        series_id=item.get("SeriesId"),
    )


@router.get("")
async def list_sources():
    settings = get_settings()
    return [
        {
            "id": "jellyfin",
            "name": "Jellyfin",
            "configured": bool(settings.jellyfin_base_url and settings.jellyfin_api_key),
            "type": "personal_media_server",
            "authorization": "operator_or_library_owner_required",
        },
        {
            "id": "tmdb",
            "name": "TMDB",
            "configured": bool(settings.tmdb_api_key),
            "type": "metadata_catalogue_only",
            "note": "Provides metadata and provider availability, not playable movie files.",
        },
    ]


@router.get("/jellyfin/health")
async def jellyfin_health():
    settings = _jellyfin_config()
    url = f"{settings.jellyfin_base_url.rstrip('/')}/System/Info"
    try:
        async with httpx.AsyncClient(timeout=settings.request_timeout_seconds) as client:
            response = await client.get(url, headers=jellyfin_headers(settings.jellyfin_api_key))
            response.raise_for_status()
            data = response.json()
    except httpx.HTTPStatusError as exc:
        raise HTTPException(status_code=502, detail=f"Jellyfin returned HTTP {exc.response.status_code}.") from exc
    except httpx.HTTPError as exc:
        raise HTTPException(status_code=502, detail="Could not reach the configured Jellyfin server.") from exc
    return {
        "status": "connected",
        "server_name": data.get("ServerName", "Jellyfin"),
        "version": data.get("Version"),
        "server_id": data.get("Id"),
    }


@router.get("/jellyfin/items", response_model=list[JellyfinItem])
async def jellyfin_items(
    q: str | None = Query(default=None, max_length=120),
    media_type: str = Query(default="all", pattern="^(all|movie|tv)$"),
    limit: int = Query(default=100, ge=1, le=200),
):
    settings = _jellyfin_config()
    include_types = "Movie,Series" if media_type == "all" else ("Movie" if media_type == "movie" else "Series")
    params = {
        "IncludeItemTypes": include_types,
        "Recursive": "true",
        "Fields": "Overview,ProductionYear,ImageTags",
        "Limit": limit,
        "SortBy": "SortName",
    }
    if q and q.strip():
        params["SearchTerm"] = q.strip()
    try:
        async with httpx.AsyncClient(timeout=settings.request_timeout_seconds) as client:
            response = await client.get(
                f"{settings.jellyfin_base_url.rstrip('/')}/Items",
                params=params,
                headers=jellyfin_headers(settings.jellyfin_api_key),
            )
            response.raise_for_status()
            payload = response.json()
    except httpx.HTTPStatusError as exc:
        raise HTTPException(status_code=502, detail=f"Jellyfin returned HTTP {exc.response.status_code}.") from exc
    except httpx.HTTPError as exc:
        raise HTTPException(status_code=502, detail="Could not retrieve the Jellyfin library.") from exc
    return [_map_item(item) for item in payload.get("Items", [])]


@router.get("/jellyfin/movies", response_model=list[JellyfinMovie])
async def jellyfin_movies():
    items = await jellyfin_items(media_type="movie", limit=200)
    return [
        JellyfinMovie(
            id=item.id, title=item.title, year=item.year, overview=item.overview,
            image_tag=item.image_tag, media_type="Movie"
        )
        for item in items
    ]


@router.get("/jellyfin/series", response_model=list[JellyfinItem])
async def jellyfin_series():
    return await jellyfin_items(media_type="tv", limit=200)


@router.get("/jellyfin/series/{series_id}/seasons")
async def jellyfin_seasons(series_id: str):
    settings = _jellyfin_config()
    try:
        async with httpx.AsyncClient(timeout=settings.request_timeout_seconds) as client:
            response = await client.get(
                f"{settings.jellyfin_base_url.rstrip('/')}/Shows/{series_id}/Seasons",
                headers=jellyfin_headers(settings.jellyfin_api_key),
            )
            response.raise_for_status()
            payload = response.json()
    except httpx.HTTPStatusError as exc:
        raise HTTPException(status_code=502, detail=f"Jellyfin returned HTTP {exc.response.status_code}.") from exc
    except httpx.HTTPError as exc:
        raise HTTPException(status_code=502, detail="Could not retrieve series seasons.") from exc
    return [_map_item(item) for item in payload.get("Items", [])]


@router.get("/jellyfin/series/{series_id}/episodes")
async def jellyfin_episodes(series_id: str, season_id: str | None = None):
    settings = _jellyfin_config()
    params = {"Fields": "Overview,ProductionYear,ImageTags", "Limit": 200}
    if season_id:
        params["SeasonId"] = season_id
    try:
        async with httpx.AsyncClient(timeout=settings.request_timeout_seconds) as client:
            response = await client.get(
                f"{settings.jellyfin_base_url.rstrip('/')}/Shows/{series_id}/Episodes",
                params=params,
                headers=jellyfin_headers(settings.jellyfin_api_key),
            )
            response.raise_for_status()
            payload = response.json()
    except httpx.HTTPStatusError as exc:
        raise HTTPException(status_code=502, detail=f"Jellyfin returned HTTP {exc.response.status_code}.") from exc
    except httpx.HTTPError as exc:
        raise HTTPException(status_code=502, detail="Could not retrieve series episodes.") from exc
    return [_map_item(item) for item in payload.get("Items", [])]


@router.get("/jellyfin/playback/{item_id}", response_model=PlaybackSession)
async def jellyfin_playback_session(item_id: str, request: Request):
    settings = _jellyfin_config()
    try:
        async with httpx.AsyncClient(timeout=settings.request_timeout_seconds) as client:
            response = await client.get(
                f"{settings.jellyfin_base_url.rstrip('/')}/Items/{item_id}",
                params={"Fields": "MediaSources,MediaStreams"},
                headers=jellyfin_headers(settings.jellyfin_api_key),
            )
            response.raise_for_status()
            item = response.json()
    except httpx.HTTPStatusError as exc:
        raise HTTPException(status_code=502, detail=f"Jellyfin returned HTTP {exc.response.status_code}.") from exc
    except httpx.HTTPError as exc:
        raise HTTPException(status_code=502, detail="Could not prepare the Jellyfin playback session.") from exc

    sources = item.get("MediaSources") or []
    if not sources:
        raise HTTPException(status_code=404, detail="Jellyfin did not return a playable media source for this item.")
    source = sources[0]
    streams = source.get("MediaStreams") or item.get("MediaStreams") or []
    audio_tracks = [
        {"index": stream.get("Index"), "language": stream.get("Language"), "codec": stream.get("Codec"), "channels": stream.get("Channels")}
        for stream in streams if stream.get("Type") == "Audio"
    ]
    subtitle_tracks = [
        {"index": stream.get("Index"), "language": stream.get("Language"), "codec": stream.get("Codec"), "is_external": stream.get("IsExternal", False)}
        for stream in streams if stream.get("Type") == "Subtitle"
    ]
    media_url = str(request.base_url).rstrip("/") + f"/api/v1/sources/jellyfin/stream/{item_id}"
    return PlaybackSession(
        provider="jellyfin",
        item_id=item_id,
        media_url=media_url,
        media_type=item.get("Type") or "Video",
        audio_tracks=audio_tracks,
        subtitle_tracks=subtitle_tracks,
    )


@router.get("/jellyfin/stream/{item_id}")
async def jellyfin_stream(item_id: str, request: Request):
    settings = _jellyfin_config()
    headers = jellyfin_headers(settings.jellyfin_api_key)
    range_header = request.headers.get("range")
    if range_header:
        headers["Range"] = range_header
    url = f"{settings.jellyfin_base_url.rstrip('/')}/Videos/{item_id}/stream"
    params = {"static": "true"}
    client = httpx.AsyncClient(timeout=None)
    try:
        upstream_request = client.build_request("GET", url, params=params, headers=headers)
        upstream = await client.send(upstream_request, stream=True)
    except httpx.HTTPError as exc:
        await client.aclose()
        raise HTTPException(status_code=502, detail="Could not open the Jellyfin media stream.") from exc

    if upstream.status_code >= 400:
        status = upstream.status_code
        await upstream.aclose()
        await client.aclose()
        raise HTTPException(status_code=status, detail="Jellyfin could not stream this item.")

    response_headers = {}
    for header in ("content-type", "content-length", "content-range", "accept-ranges", "last-modified"):
        if header in upstream.headers:
            response_headers[header] = upstream.headers[header]

    async def stream_chunks():
        try:
            async for chunk in upstream.aiter_bytes():
                yield chunk
        finally:
            await upstream.aclose()
            await client.aclose()

    return StreamingResponse(
        stream_chunks(),
        status_code=upstream.status_code,
        headers=response_headers,
        media_type=upstream.headers.get("content-type"),
    )
