# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Spring Boot 3.2.4 weather dashboard app. Fetches live weather from the Open Meteo API (free, no key), authenticates users via GitHub OAuth2 (OIDC), and lets logged-in users save/delete favourite cities stored in PostgreSQL. Uses H2 locally and PostgreSQL in Docker/Railway.

## Build & Run Commands

```bash
# Build
mvn clean package -DskipTests

# Run locally (H2, no Docker — OAuth won't work without GitHub credentials)
mvn spring-boot:run

# Run with Docker (PostgreSQL + pgAdmin)
docker compose up --build
```

**Environment variables required for GitHub SSO:**
```
GITHUB_CLIENT_ID=<from GitHub OAuth App>
GITHUB_CLIENT_SECRET=<from GitHub OAuth App>
```

**Access points:**
- App: `http://localhost:8080`
- H2 console (local): `http://localhost:8080/h2-console` (JDBC: `jdbc:h2:mem:weatherdb`)
- pgAdmin (Docker): `http://localhost:5050` (admin@portfolio.com / admin)

## Architecture

```
WeatherController  ──▶  WeatherService  ──▶  Open Meteo API (geocoding + forecast)
       │
       ├──▶  FavoriteCityRepository  ──▶  PostgreSQL / H2
       └──▶  OAuth2User (GitHub principal)
```

- **`WeatherController`** — single controller; `GET /?city=X` loads weather + user data. `POST /cities/save` and `POST /cities/delete/{id}` require authentication.
- **`WeatherService`** — calls Open Meteo geocoding API first (city → lat/lon), then forecast API. Maps WMO weather codes to conditions, Bootstrap icons, and CSS background classes.
- **`FavoriteCity`** entity — `cityName`, `country`, `latitude`, `longitude`, `username` (GitHub login), `savedAt`. Unique constraint on `(username, city_name)`.
- **`SecurityConfig`** — only `/cities/**` requires auth; everything else is public. GitHub OAuth2 via Spring Security.
- **`templates/index.html`** — Thymeleaf + Bootstrap 5. Background gradient changes dynamically with weather condition. `sec:authorize` tags show/hide save and favourite sections based on login state.

## Key Conventions

- Weather search uses `GET /?city=X` (not POST) so URLs are bookmarkable and redirect-safe
- After save/delete, redirects back to `/?city=X` to preserve the current city context
- `WeatherData.backgroundClass` and `WeatherData.textClass` drive the full-page visual theme
- GitHub OAuth principal attribute `"login"` is the username; `"avatar_url"` is the profile image
- `GITHUB_CLIENT_ID` / `GITHUB_CLIENT_SECRET` are injected via env vars — never hardcoded

## GitHub OAuth App Setup

1. Go to https://github.com/settings/developers → OAuth Apps → New OAuth App
2. Homepage URL: `http://localhost:8080` (or Railway domain for production)
3. Callback URL: `http://localhost:8080/login/oauth2/code/github`
4. Copy Client ID and Secret → set as env vars locally or in Railway settings

## Docker Details

Multi-stage Dockerfile: Maven build → Eclipse Temurin JRE 21, non-root user `spring`. PostgreSQL persists via `postgres_data` named volume. App waits for DB health check before starting.
