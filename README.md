# MovDash

Android-first movie and TV discovery with authorized multi-source playback.

## Project

- **Android:** Kotlin, Jetpack Compose, Retrofit, Coil and AndroidX Media3.
- **Backend:** FastAPI with TMDB catalogue integration and optional Jellyfin personal-library playback.
- **Deployment:** Docker; Render Blueprint included in `render.yaml`.

## Android features

- AniDash-inspired dark UI, spotlight carousel and horizontal catalogue sections.
- Trending and popular movies and TV series.
- Search all titles, movies only or TV series only.
- Detail pages with synopsis, ratings, genres, seasons and episodes.
- Optional Jellyfin matching and playback through Media3.
- Persistent backend URL setting.

## Backend catalogue

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

## Authorized playback

Jellyfin routes support health checks, library search, movies, series, seasons, episodes and a byte-range stream proxy for Media3. Set `JELLYFIN_BASE_URL` and `JELLYFIN_API_KEY` on the backend only. The server-side proxy does not return the Jellyfin API key to the Android app.

TMDB is metadata/discovery only and does not provide playable movie streams. Jellyfin playback requires a media server and content you own or are authorized to access. Other playback providers must be added through documented APIs or explicit rights/authorization; this app does not scrape private stream endpoints.

## Run backend locally

See [backend setup](backend/README.md). Set `TMDB_API_KEY` in `backend/.env`, then run:

```bash
cd backend
pip install -r requirements-dev.txt
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

API docs: `http://localhost:8000/docs`.

## Deploy backend

Use Render → New → Blueprint and choose this repository. Render reads `render.yaml`. Add `TMDB_API_KEY` in the service environment variables, deploy, and copy the HTTPS service URL into Android Profile → Connection settings.

The Render free instance may sleep when idle. Jellyfin is optional; if enabled on a public deployment, put it behind proper user authentication and access controls before sharing the service.

## Build Android

GitHub Actions builds the debug APK on relevant pushes. Download the `movdash-debug-apk` artifact from [Actions](https://github.com/anshdeepofficial1/MovDash/actions).

## Security

Never commit API keys or Jellyfin tokens. TMDB/Jellyfin credentials stay on the backend. Before public release, add user authentication, per-user provider credentials, rate limiting, HTTPS-only settings, audit logging and provider rights checks.
