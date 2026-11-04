from pydantic import BaseModel


class HealthResponse(BaseModel):
    status: str
    app: str
    environment: str


class MovieSummary(BaseModel):
    id: int
    title: str
    overview: str = ""
    poster_path: str | None = None
    backdrop_path: str | None = None
    release_date: str | None = None
    vote_average: float | None = None
    source: str


class JellyfinMovie(BaseModel):
    id: str
    title: str
    year: int | None = None
    overview: str = ""
    image_tag: str | None = None
    source: str = "jellyfin"
