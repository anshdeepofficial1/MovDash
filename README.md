<div align="center">

# ZYLO

**Your media. Your way.**

An open-source, customizable media player for discovering, organizing and watching movies, series and other media from your own authorized sources.

[Releases](https://github.com/anshdeepofficial1/ZYLO/releases) · [Issues](https://github.com/anshdeepofficial1/ZYLO/issues) · [Build from source](#build-from-source)

![License](https://img.shields.io/github/license/anshdeepofficial1/ZYLO?style=flat-square) ![Stars](https://img.shields.io/github/stars/anshdeepofficial1/ZYLO?style=flat-square) ![Issues](https://img.shields.io/github/issues/anshdeepofficial1/ZYLO?style=flat-square)

</div>

## About ZYLO

ZYLO is a Kotlin Multiplatform / Compose Multiplatform media client based on the open-source Nuvio Mobile project. It brings media discovery, library management and playback together in one interface.

- Browse movie and series catalogs with artwork, metadata and episode details.
- Connect compatible user-provided add-ons and media servers.
- Manage your library, watch history and playback progress.
- Customize audio, subtitles, playback and appearance.
- Connect supported tracking services where configured.

ZYLO does not provide or host a catalog of copyrighted streams. Use only sources you have permission to access.

## Availability

| Platform | Status |
| --- | --- |
| Android | Source build available; APK previews are development builds. |
| iOS | Source project included; a ZYLO-signed IPA is not yet published. |
| Desktop / TV | No ZYLO distribution announced. |

Check [GitHub Releases](https://github.com/anshdeepofficial1/ZYLO/releases) for available builds. The existing Orbit-labelled release is a legacy development build, not a verified ZYLO release. Do not install upstream Nuvio packages expecting ZYLO.

## Build from source

```bash
git clone --recurse-submodules https://github.com/anshdeepofficial1/ZYLO.git
cd ZYLO
```

### Android

Use Android Studio, JDK 21 and the Android SDK.

```bash
./gradlew :androidApp:assembleFullDebug -Pnuvio.android.distribution=full -Pnuvio.ios.distribution=appstore
```

On Windows, use `gradlew.bat` instead of `./gradlew`. The debug APK is generated under `androidApp/build/outputs/apk/full/debug/`.

The release application ID is `com.zylo.app`. Production signing requires your own private keystore; see [Security](./SECURITY.md).

### iOS

The iOS project requires macOS, Xcode and the applicable native dependencies. Full builds additionally require the upstream native engine framework. Signing and bundle identifiers must be configured for ZYLO before distributing an IPA.

```bash
env NUVIO_IOS_DISTRIBUTION=appstore xcodebuild \
  -project iosApp/iosApp.xcodeproj \
  -scheme iosApp \
  -configuration Debug \
  -sdk iphonesimulator \
  -derivedDataPath build/ios-derived-simulator \
  CODE_SIGNING_ALLOWED=NO build
```

## Configuration

The code retains upstream internal package names and configuration keys for compatibility. Integrations and hosted services require their own configuration; do not assume an independent ZYLO backend is available. Never commit private signing keys, API credentials or local configuration files.

## Privacy policy

Development builds may store preferences, library data and playback progress locally, and may contact configured metadata providers, add-ons, media servers and account/sync services. The inherited Nuvio service configuration is still present in the source. Those external services have their own privacy terms. Review the service settings before connecting an account or using a remote source. Do not enter sensitive credentials into an unverified development build.

This is a development-stage disclosure, not a claim that a standalone ZYLO backend or a production privacy program has been deployed. For privacy questions, contact the maintainers without posting private account information.

## Contributing

Read [CONTRIBUTING.md](./CONTRIBUTING.md) before submitting a change. Report reproducible bugs via [GitHub Issues](https://github.com/anshdeepofficial1/ZYLO/issues).

## Credits and license

ZYLO is a modified distribution of [Nuvio Mobile](https://github.com/NuvioMedia/NuvioMobile). Thanks to the original authors and contributors and the third-party libraries used by the project. Upstream copyright and license notices remain applicable.

Licensed under the [GNU General Public License v3.0](./LICENSE).
