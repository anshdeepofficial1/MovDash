# MovDash

An Android-first movie discovery and authorized multi-source playback platform.

## First milestone
This repository starts with a modular FastAPI backend:
- Health and configuration endpoints
- TMDB movie search for metadata (not video streams)
- Jellyfin connection health and library listing for servers you own or are authorized to access
- A provider adapter contract for future authorized integrations
- Docker setup and basic API tests

## Run locally
```bash
cd backend
python -m venv .venv
# Windows: .venv\\Scripts\\activate
# macOS/Linux: source .venv/bin/activate
pip install -r requirements-dev.txt
cp .env.example .env
uvicorn app.main:app --reload
```
Open http://127.0.0.1:8000/docs

Configure `TMDB_API_KEY`, `JELLYFIN_BASE_URL`, and `JELLYFIN_API_KEY` in `backend/.env`.
Never commit real credentials. TMDB provides metadata, not movie streams. Jellyfin is intended for a server you own or are authorized to access.

## API
- `GET /health`
- `GET /api/v1/config/status`
- `GET /api/v1/catalog/search?q=Inception`
- `GET /api/v1/sources`
- `GET /api/v1/sources/jellyfin/health`
- `GET /api/v1/sources/jellyfin/movies`

## Tests
```bash
cd backend
pip install -r requirements-dev.txt
pytest -q
```

See [architecture](docs/ARCHITECTURE.md) and [implementation plan](docs/IMPLEMENTATION_PLAN.md).
