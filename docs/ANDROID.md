# Android application

- Native app: Kotlin + Jetpack Compose
- Application ID: `com.movdash.app`
- Min SDK: 26
- Compile/target SDK: 37
- Home, discover/search, library and profile tabs
- Backend URL can be changed from Settings; the emulator default is `http://10.0.2.2:8000/`
- Movie cards load poster artwork from TMDB image URLs returned by the backend.
- ExoPlayer/Media3 HLS and DASH modules are included as the player foundation. A secure authorized playback-session endpoint is still required before the movie detail screen can start provider playback.

## Build

Open the repository root in Android Studio and run the `app` debug configuration, or use Gradle 9.5+ with JDK 17:

```bash
gradle :app:assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

GitHub Actions builds and uploads an artifact named `movdash-debug-apk` on pushes affecting Android/backend files and on manual dispatch.
