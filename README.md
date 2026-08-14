# Andock

**Andock** is an open-source Android command dock for a future local Windows companion. It is written in Kotlin with Jetpack Compose and Material 3 Expressive.

The current Android build is a polished local prototype: it provides a configurable command deck, pairing-code validation, command history, command creation, privacy-aware settings, and Material You color support. It intentionally has **no account, analytics SDK, database, cloud backend, subscription, or payment code**.

## Build

Open the project in a current Android Studio release, install the Android 36 SDK platform, then run the `app` configuration. A local JVM test suite covers pairing-code and command-state behavior.

## Roadmap

The first real companion will be a separate Windows desktop process that exposes an authenticated local connection after explicit approval. Andock will only send structured, allow-listed command identifiers; it will never accept or transmit arbitrary shell commands.

## License

[MIT](LICENSE)
