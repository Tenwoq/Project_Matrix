# MIN Android Client

Android client for the MIN coursework messenger.

Stack:

- Kotlin
- Jetpack Compose
- Hilt
- Firebase Authentication
- Ktor Client
- Trixnity Matrix model dependency
- Clean Architecture package split: `data`, `domain`, `presentation`

For Android emulator, the client expects the backend at `http://10.0.2.2:8080`.

Firebase client configuration is stored in `app/google-services.json`.
