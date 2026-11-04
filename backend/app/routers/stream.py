from fastapi import APIRouter, Query
from app.schemas import StreamServer

router = APIRouter(prefix="/stream", tags=["stream"])


@router.get("/servers", response_model=list[StreamServer])
async def get_stream_servers(
    tmdb_id: int = Query(..., description="TMDB ID of movie or show"),
    media_type: str = Query("movie", pattern="^(movie|tv)$"),
    season: int | None = Query(None, description="Season number for TV shows"),
    episode: int | None = Query(None, description="Episode number for TV shows"),
):
    if media_type == "movie":
        return [
            StreamServer(
                id="river",
                name="Server 1 (River)",
                badge="Fast HLS",
                quality="1080p / 720p",
                language="Multi-Audio / Dual",
                stream_type="embed",
                stream_url=f"https://vidsrc.to/embed/movie/{tmdb_id}",
            ),
            StreamServer(
                id="vidlnx",
                name="Server 2 (VidLnx)",
                badge="HD 1080p",
                quality="1080p Ultra",
                language="Original / Subtitles",
                stream_type="embed",
                stream_url=f"https://vidsrc.me/embed/movie?tmdb={tmdb_id}",
            ),
            StreamServer(
                id="mbply",
                name="Server 3 (MbPly)",
                badge="Multi-Language",
                quality="Adaptive Auto",
                language="Hindi / Eng / Multi",
                stream_type="embed",
                stream_url=f"https://autoembed.cc/embed/player.php?id={tmdb_id}",
            ),
            StreamServer(
                id="catflix",
                name="Server 4 (Catflix)",
                badge="Auto-Adaptive",
                quality="HD / SD",
                language="Multi-Language",
                stream_type="embed",
                stream_url=f"https://multiembed.mov/?video_id={tmdb_id}&tmdb=1",
            ),
            StreamServer(
                id="neon",
                name="Server 5 (Neon)",
                badge="Direct Stream",
                quality="1080p High",
                language="Multi",
                stream_type="embed",
                stream_url=f"https://embed.su/embed/movie/{tmdb_id}",
            ),
        ]
    else:
        s = season if season and season > 0 else 1
        e = episode if episode and episode > 0 else 1
        return [
            StreamServer(
                id="river",
                name="Server 1 (River)",
                badge="Fast HLS",
                quality="1080p / 720p",
                language="Multi-Audio / Dual",
                stream_type="embed",
                stream_url=f"https://vidsrc.to/embed/tv/{tmdb_id}/{s}/{e}",
            ),
            StreamServer(
                id="vidlnx",
                name="Server 2 (VidLnx)",
                badge="HD 1080p",
                quality="1080p Ultra",
                language="Original / Subtitles",
                stream_type="embed",
                stream_url=f"https://vidsrc.me/embed/tv?tmdb={tmdb_id}&season={s}&episode={e}",
            ),
            StreamServer(
                id="mbply",
                name="Server 3 (MbPly)",
                badge="Multi-Language",
                quality="Adaptive Auto",
                language="Hindi / Eng / Multi",
                stream_type="embed",
                stream_url=f"https://autoembed.cc/embed/player.php?id={tmdb_id}&s={s}&e={e}",
            ),
            StreamServer(
                id="catflix",
                name="Server 4 (Catflix)",
                badge="Auto-Adaptive",
                quality="HD / SD",
                language="Multi-Language",
                stream_type="embed",
                stream_url=f"https://multiembed.mov/?video_id={tmdb_id}&tmdb=1&s={s}&e={e}",
            ),
            StreamServer(
                id="neon",
                name="Server 5 (Neon)",
                badge="Direct Stream",
                quality="1080p High",
                language="Multi",
                stream_type="embed",
                stream_url=f"https://embed.su/embed/tv/{tmdb_id}/{s}/{e}",
            ),
        ]
