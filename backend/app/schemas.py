from pydantic import BaseModel


class HealthResponse(BaseModel):
    status: str
    app: str
    environment: str


class MediaSummary(BaseModel):
    id: int
    title: str
    overview: str = ""
    poster_path: str | None = None
    backdrop_path: str | None = None
    release_date: str | None = None
    vote_average: float | None = None
    media_type: str = "movie"
    source: str = "tmdb"


# Kept as an alias while the Android client migrates to MediaSummary.
class MovieSummary(MediaSummary):
    pass


class MediaDetails(MediaSummary):
    tagline: str = ""
    runtime: int | None = None
    genres: list[dict] = []
    status: str | None = None
    number_of_seasons: int | None = None
    number_of_episodes: int | None = None
    seasons: list[dict] = []
    recommendations: list[MediaSummary] = []
    watch_providers: dict = {}


class JellyfinMovie(BaseModel):
    id: str
    title: str
    year: int | None = None
    overview: str = ""
    image_tag: str | None = None
    media_type: str = "Movie"
    source: str = "jellyfin"


class JellyfinItem(BaseModel):
    id: str
    title: str
    year: int | None = None
    overview: str = ""
    media_type: str = "Movie"
    image_tag: str | None = None
    series_id: str | None = None


class PlaybackSession(BaseModel):
    provider: str
    item_id: str
    media_url: str
    media_type: str
    supports_direct_play: bool = True
    audio_tracks: list[dict] = []
    subtitle_tracks: list[dict] = []


class StreamServer(BaseModel):
    id: str
    name: str
    badge: str = "HD"
    quality: str = "1080p"
    language: str = "Multi-Audio"
    stream_type: str = "embed"
    stream_url: str
