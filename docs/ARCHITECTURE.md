# MovDash architecture

## Current backend milestone

```text
Android client (next milestone)
        |
        v
FastAPI backend
   |           |
   v           v
TMDB API    Authorized Jellyfin server
(metadata)  (operator/user-controlled media)
```

## Integration boundaries

- TMDB supplies catalogue metadata. It does not provide playable movie streams.
- Jellyfin connects to a media server the operator or user owns or is authorized to access.
- Future providers must have a documented API or explicit authorization and item-level distribution rights.
- Provider credentials must remain server-side; never package API keys into the Android APK.

## Planned playback flow

1. Android requests movie details.
2. Backend lists providers configured for the signed-in user.
3. User selects a provider and title.
4. Backend creates a short-lived playback session after checking access rights.
5. Android Media3 plays the authorized provider's supported HLS, DASH, or direct media URL.
6. App stores playback progress, not the movie file.

## Security before public deployment

- Add user authentication and per-user provider credentials.
- Encrypt provider credentials at rest.
- Restrict CORS to production origins.
- Add rate limiting and structured logs with credentials redacted.
- Use HTTPS.
- Avoid arbitrary URL proxying and open redirects.
- Validate provider identifiers and content rights before creating playback sessions.
