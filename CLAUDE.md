# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
# Run desktop app
./gradlew :composeApp:run

# Build desktop distribution (DMG)
./gradlew :composeApp:packageDmg

# Run Android (via IDE or CLI)
./gradlew :composeApp:installDebug

# iOS — open iosApp in Xcode and run

# Run all checks (no test task configured by default; tests run per-platform)
./gradlew :composeApp:check

# Clean
./gradlew clean
```

- **Kotlin** 2.3.0, **Compose Multiplatform** 1.10.0, **AGP** 8.12.0, **Room** 3.0.0-alpha04
- All dependency versions in `gradle/libs.versions.toml`
- Desktop entry point: `composeApp/src/desktopMain/kotlin/com/darcy/kmpdemo/main.kt`

## Architecture

### Project Structure

This is a **secure instant messaging app** using Kotlin Multiplatform + Compose Multiplatform, targeting Android, iOS, and Desktop.

The single shared module is `composeApp/`. Each platform's main entry point calls the shared `App()` composable:

| Platform | Entry file | 
|----------|-----------|
| Android  | `src/androidMain/.../MainActivity.kt` |
| Desktop  | `src/desktopMain/.../main.kt` |
| iOS      | `src/iosMain/.../MainViewController.kt` |

### Package Layout (`com.darcy.kmpdemo`)

| Package | Purpose |
|---------|---------|
| `App.kt` | Root composable, sets up NavHost + theme |
| `ui/screen/` | Compose screens per platform (`phone/`, `desktop/`) |
| `ui/base/` | MVVM foundation: `BaseViewModel`, `IReducer`, `IState`, `IIntent`, `IEvent` |
| `ui/screen/phone/` | Login, Register, Chat, Friends, Dynamic, Mine screens |
| `ui/screen/desktop/` | Desktop-only layout with NavigationRail sidebar |
| `network/http/` | Ktor HTTP client with custom encrypt/decrypt body plugins |
| `network/websocket/` | Krossbow STOMP + raw Ktor WebSocket; heartbeat, frame parsing, encrypted session wrapping |
| `crypto/` | Message cipher, transport cipher, file cipher, HMAC, JSON crypto helper |
| `storage/database/` | Room database with 9 tables (users, conversations, messages, keys, sessions, read status) |
| `x3dh/` | X3DH key agreement + Double Ratchet algorithm (Alice/Bob models, ECDH, EdDSA, HKDF, chain keys) |
| `bean/` | DTOs for HTTP, WebSocket, UI models |
| `platform/` | expect/actual declarations (SSL, files, key-value storage, crypto providers, etc.) |
| `repository/` | Data repository markers |

### UI Architecture (MVVM + Reducer)

Each screen follows: **State** → **Intent** → **Reducer** → **ViewModel** (+ optional **Event**, **Repository**)

- `BaseViewModel` manages `StateFlow<State>` for persistent state and `SharedFlow<Event>` for one-shot events
- Reducers are pure functions processed as a chain on each dispatched `Intent`
- Use `io {}` / `main {}` extension functions on ViewModel for coroutine dispatch

### Crypto Architecture

Full end-to-end encryption using:
1. **X3DH** — shared secret establishment via identity keys, signed pre-keys, one-time pre-keys
2. **Double Ratchet** — forward secrecy, message key derivation via HKDF
3. **TransportCipher** — on-wire encryption for HTTP payloads
4. **MessageCipher** — per-message encryption
5. **HMAC** — message authentication codes

### Networking

- **HTTP**: Ktor client with custom `EncryptRequestBodyPlugin` / `DecryptResponseBodyPlugin` for automatic body encryption
- **WebSocket**: Krossbow STOMP over Ktor WebSocket; `CryptoSessionWrapImpl` encrypts WebSocket frames
- Server: `10.0.0.241:7443` (local network)

### Database

Room database with 9 entities in `DarcyIMDatabase`: User, Conversation, Friendship, PrivateMessage, IdentityKey, SignedPreKey, OneTimePreKey, SessionRecord, MessageReadStatus, SkippedMessageKey.
