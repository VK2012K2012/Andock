# Andock

Andock is a local-first Android command deck, written in Kotlin and Jetpack Compose. It gives you an editable expressive deck, a saved desktop pairing profile, touchpad gesture controls, an activity timeline, and an emoji/snippet board. Everything in the Android application persists locally on the device.

The Android app does not claim to control a computer until a separately installed, user-approved Windows companion exists. Until then, commands and gestures are recorded locally in the activity timeline, pairing is a saved profile, and clipboard actions operate on Android itself. This makes the app usable today without faking a connected PC.

## Privacy and scope

Andock has no account system, analytics SDK, advertising, database server, cloud backend, payments, or subscription code. The future desktop connection will use user-approved local pairing and allow-listed command identifiers rather than arbitrary shell paths.

## Build

Install Android SDK Platform 36 and Java 21, then run:

```bash
./gradlew testDebugUnitTest assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## License

[MIT](LICENSE)

