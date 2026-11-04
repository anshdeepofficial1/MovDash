# MovDash implementation plan

## Milestone 1 — Backend foundation (current)
- FastAPI application and environment-based settings
- Health and configuration status
- TMDB movie search
- Jellyfin connectivity and movie-library listing
- Provider adapter interface
- Docker setup and API smoke tests

## Milestone 2 — First real playback
- Connect an authorized Jellyfin server
- Resolve an item through the Jellyfin API
- Create a secure playback session
- Integrate AndroidX Media3
- Test audio tracks, subtitle tracks, seeking, and resume

## Milestone 3 — Android app
- Kotlin + Jetpack Compose project
- Home, search, details, watchlist, and settings
- Media3 playback screen
- Track selection and playback preferences

## Milestone 4 — Additional authorized sources
- Add integrations with documented APIs or written authorization
- Verify rights at item level
- Normalize provider quality, audio, subtitle, and protocol metadata
- Add source health monitoring

## Milestone 5 — Production hardening
- Authentication and encrypted provider credentials
- Database migrations and watch history
- Rate limits, observability, privacy documentation
- Device compatibility and performance testing
