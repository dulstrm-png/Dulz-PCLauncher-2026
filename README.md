# Dulz PC Launcher 2026

Native Android Kotlin launcher — **Dulz | STRM**, version **2026.1**.

## Requirements
- Android Studio
- JDK 17
- Android SDK Platform 35
- Android 8.0+ (API 26)

## Build
```bash
./gradlew assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`

## Features
- Native Kotlin UI; no WebView.
- HOME launcher intent with Android user consent.
- Responsive portrait/landscape desktop.
- Start menu and internal app search.
- BluStrak download manager using Android `DownloadManager`.
- User-supplied HTTP/HTTPS download URL only.
- Chrome handoff using package lookup and Intent.
- File Explorer using Storage Access Framework.
- Settings persisted with SharedPreferences.
- Realtime taskbar clock/date.
- Back handling for overlays and internal pages.
