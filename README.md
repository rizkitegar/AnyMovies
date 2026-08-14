# 🎬 AnyMovies

![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF.svg?style=flat&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-3DDC84.svg?style=flat&logo=jetpackcompose&logoColor=white)
![Android](https://img.shields.io/badge/Android-API%2024%2B-3DDC84.svg?style=flat&logo=android&logoColor=white)
![TMDB](https://img.shields.io/badge/API-TMDB%20v3-01B4E4.svg?style=flat&logo=themoviedatabase&logoColor=white)

A native Android movie catalog app that lets you explore genres, browse trending titles, and dive into details, trailers, and reviews — all powered by [The Movie Database (TMDB)](https://www.themoviedb.org/) API.

Built as a modular Clean Architecture playground: every feature is split into `domain` / `data` / `presentation` Gradle modules, with Compose driving the UI on top of a legacy View system kept around for interop.

## 📱 What You Can Do

- **Browse genres** — the app opens on a genre list pulled straight from TMDB.
- **Discover movies by genre** — tap a genre to page through movies sorted by popularity, loaded incrementally with Paging 3.
- **Inspect a movie** — open any title for its synopsis, trailer playback, and a paged list of user reviews.
- **Watch trailers in-app** — pick from available YouTube trailers via an embedded player.

## 🧱 Architecture

Each feature module is sliced into three layers, repeated for `genre`, `movies`, and `detail`:

```
feature/<name>/
├── domain/         # use cases, repository interfaces, entities
├── data/           # repository impls, remote/local data sources, mappers
└── presentation/   # Composables, ViewModels, UiState
```

Shared concerns live in `core/*` modules (networking, database, common utilities, UI state components — both Compose and legacy View variants), while `navigation/` holds the type-safe `Route` sealed hierarchy consumed by a single-Activity `NavHost`.

## 🛠️ Tech Stack

| Layer | Choice |
|---|---|
| Language | Kotlin 2.2.10 |
| UI | Jetpack Compose (Material 3) + legacy XML/View system in `core:ui-legacy` |
| Dependency Injection | Koin |
| Networking | Retrofit + OkHttp, kotlinx.serialization |
| Local storage | Room |
| Async | Kotlin Coroutines & Flow |
| Pagination | AndroidX Paging 3 |
| Image loading | Coil 3 |
| Navigation | Navigation Compose (type-safe routes) |
| Network debugging | Chucker (debug builds only) |
| Testing | JUnit 4/5, Robolectric, Turbine, MockWebServer, Espresso, Compose UI Test |

## 🌐 API Endpoints Used

All requests hit `https://api.themoviedb.org/3/` with a bearer token attached by `AuthInterceptor`. Endpoints in use:

| Endpoint | Used for | Module |
|---|---|---|
| `GET /genre/movie/list` | Genre list on the home screen | `feature:genre` |
| `GET /discover/movie` | Paged movies filtered by genre, sorted by popularity | `feature:movies` |
| `GET /movie/{movie_id}` | Movie detail (with `append_to_response=videos`) | `feature:detail` |
| `GET /movie/{movie_id}/videos` | Trailers / videos for a movie | `feature:detail` |
| `GET /movie/{movie_id}/reviews` | Paged user reviews for a movie | `feature:detail` |

Full reference: [TMDB API docs](https://developer.themoviedb.org/reference/intro/getting-started).

## 📂 Project Structure

```
AnyMovies/
├── app/                       # Application entry point, MainActivity, NavHost wiring
├── navigation/                # Shared Route definitions
├── core/
│   ├── common/                 # Dispatchers, Result/DomainError, shared DI
│   ├── network/                 # Retrofit/OkHttp setup, auth interceptor, TMDB constants
│   ├── database/                 # Room database, DAOs, entities
│   ├── ui/                        # Compose reusable state UI (loading, empty, error)
│   └── ui-legacy/                  # View-based equivalents (adapters, custom views)
├── feature/
│   ├── genre/{domain,data,presentation}/
│   ├── movies/{domain,data,presentation}/
│   └── detail/{domain,data,presentation}/
└── gradle/libs.versions.toml   # Centralized version catalog
```

## 🚀 Getting Started

### Requirements
- Android Studio (latest stable), JDK 17
- Android SDK 24+ installed
- A TMDB account for an API read-access token

### 1. Get a TMDB API key
Sign up at [themoviedb.org](https://www.themoviedb.org/), open account **Settings → API**, and copy your **API Read Access Token** (v4 auth, JWT-style).

### 2. Add it to `local.properties`
Create or open `local.properties` at the project root (already gitignored) and add:

```properties
TMDB_API_KEY=your_tmdb_read_access_token_here
```

This gets picked up by `core/network`'s build script and exposed as `BuildConfig.TMDB_API_KEY`, attached as a bearer token on every request.

### 3. Sync & run
Open the project in Android Studio and let Gradle sync, or from the command line:

```bash
./gradlew assembleDebug     # build a debug APK
./gradlew installDebug      # build and install on a connected device/emulator
```

## ✅ Running Tests

```bash
./gradlew test               # unit tests across all modules
```

---
