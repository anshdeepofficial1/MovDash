# ZYLO Web

This directory is a separate browser/PWA application for authorized media, alongside the existing Android app. The source import is pinned to [alphasquare404/NuvioWeb](https://github.com/alphasquare404/NuvioWeb/tree/8ab318ef050977515a69c7ee4944c9e6a90470ec), licensed GPL-3.0. Preserve upstream copyright, license, and credits.

## Vercel

Import the ZYLO repository; set **Root Directory** to `website`, Framework Preset to Other, install command to `npm ci`, build command to `node scripts/zylo-vercel-build.mjs`, and output directory to `dist`. Use Node.js 22. These settings are also defined in vercel.json.

Only browser-public configuration is accepted by the build wrapper. By default, the Vercel build uses the same publicly documented hosted account backend and browser-public anonymous client key as the default Android build. Signing in sends credentials to that hosted service, not to a separate ZYLO backend. If your phone uses a custom account server or build-time overrides, set both `NUVIO_SUPABASE_URL` and `NUVIO_SUPABASE_ANON_KEY` to that same server's HTTPS URL and public key in Vercel. Optional `NUVIO_SUPABASE_FALLBACK_URL` must be a trusted endpoint for the same account service. Set your own `TMDB_API_KEY` for metadata features that need it. Do not put service-role keys, server credentials, private media URLs, or OAuth secrets in client builds. The upstream Docker services are not included in a static Vercel deployment; server-dependent features require separate reviewed services. Native Android-only playback cannot be assumed to work on browsers.

Use only media sources you own or are licensed to access. This web app does not provide or host copyrighted media. No native ZYLO Direct provider endpoints are copied into the web app.

## Verification

The web client retains upstream profile, watch-progress, watched-items, and library synchronization. Exact resume uses the Android-compatible millisecond position/duration contract. Real account synchronization must be verified using the same account and profile on two devices; local-only Android progress will not appear until it has synced. A successful build is not a guarantee of browser playback. Test authorized HLS/MP4, subtitle support, library persistence, mobile/PWA layout, and any configured accounts separately.
