# MovDash

An Android-first movie discovery and authorized multi-source playback platform.

## Repository layout

- `app/` — native Android application (Kotlin + Jetpack Compose + Media3 player foundation)
- `backend/` — FastAPI API, TMDB catalogue and Jellyfin integration
- `docs/` — architecture and implementation notes

## Android app

Application ID: `com.movdash.app`  
Minimum Android version: API 26  
Debug build: `gradle :app:assembleDebug`

The app includes Home, Discover/Search, Library and Profile screens. Configure the backend URL from Settings. Android emulator default: `http://10.0.2.2:8000/`. For a physical phone, use the computer's LAN address or a deployed HTTPS backend.

## Backend

See the [backend README](backend/README.md) and [architecture notes](docs/ARCHITECTURE.md).

The backend provides:
- `GET /health`
- `GET /api/v1/config/status`
- `GET /api/v1/catalog/trending`
- `GET /api/v1/catalog/search?q=Inception`
- `GET /api/v1/sources`
- `GET /api/v1/sources/jellyfin/health`
- `GET /api/v1/sources/jellyfin/movies`

TMDB provides metadata, not video streams. Jellyfin integration is for a server the operator/user owns or is authorized to access. The Media3 player module is included; secure playback sessions are the next integration milestone.

## Android debug APK

GitHub Actions builds a debug APK on relevant pushes and manual workflow dispatch. Download the `movdash-debug-apk` artifact from the [Actions page](https://github.com/anshdeepofficial1/MovDash/actions).

## Security

Never commit API keys or Jellyfin tokens. Before public deployment, add user authentication, per-user encrypted provider credentials, HTTPS, rate limiting and source-rights checks.
