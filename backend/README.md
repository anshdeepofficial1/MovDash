# MovDash API

FastAPI backend for the Android app.

## Features

- TMDB trending, popular, top-rated, upcoming/airing and genre discovery for movies and TV.
- Multi-search and type-specific search.
- Movie and series details, season/episode metadata and provider availability links.
- Optional Jellyfin integration for an authorized personal library.
- Jellyfin library search, series seasons/episodes and byte-range streaming proxy.
- Health and configuration endpoints.

TMDB is a metadata and discovery service; it does not supply playable video streams. Jellyfin playback works only for a server and media library you own or are authorized to access.

## Local setup

```bash
cd backend
python -m venv .venv
# Activate the environment, then:
pip install -r requirements-dev.txt
cp .env.example .env
```

Set `TMDB_API_KEY` in `.env`. To enable personal-library playback, also set `JELLYFIN_BASE_URL` and `JELLYFIN_API_KEY`. Never commit the real `.env` file or put provider credentials in the Android app.

Run:

```bash
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

API docs: `http://localhost:8000/docs`.

## Deploy to Render

1. Push this repository to GitHub.
2. In Render, choose **New → Blueprint** and select this repository.
3. Render reads the root `render.yaml` and creates the `movdash-api` web service.
4. Add your TMDB API key in the service environment variables. Add Jellyfin URL and API key only if you operate an authorized Jellyfin server.
5. Wait for `/health` to report healthy, then copy the service's HTTPS URL.
6. In MovDash Android, open Profile → Connection settings and paste that URL.

The free Render instance may sleep when idle, so the first request after inactivity can take time. Do not use wildcard CORS for a production web client; Android native requests do not require browser CORS.

## API routes

- `GET /health`
- `GET /api/v1/config/status`
- `GET /api/v1/catalog/home`
- `GET /api/v1/catalog/trending?media_type=movie|tv|all`
- `GET /api/v1/catalog/popular/{movie|tv}`
- `GET /api/v1/catalog/top-rated/{movie|tv}`
- `GET /api/v1/catalog/upcoming/{movie|tv}`
- `GET /api/v1/catalog/genres/{movie|tv}`
- `GET /api/v1/catalog/discover/{movie|tv}`
- `GET /api/v1/catalog/search?q=...&media_type=multi|movie|tv`
- `GET /api/v1/catalog/{movie|tv}/{id}`
- `GET /api/v1/catalog/tv/{id}/season/{season_number}`
- `GET /api/v1/sources`
- `GET /api/v1/sources/jellyfin/health`
- `GET /api/v1/sources/jellyfin/items?q=...&media_type=all|movie|tv`
- `GET /api/v1/sources/jellyfin/series/{id}/seasons`
- `GET /api/v1/sources/jellyfin/series/{id}/episodes?season_id=...`
- `GET /api/v1/sources/jellyfin/playback/{item_id}`
- `GET /api/v1/sources/jellyfin/stream/{item_id}`

The stream endpoint forwards HTTP Range requests to Jellyfin so Media3 can seek and resume without exposing the Jellyfin API key to the app.
