# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Spring Boot 3.2.4 full-stack product manager app. Serves a Thymeleaf HTML UI for CRUD operations on a `products` table. Uses H2 in-memory DB locally and PostgreSQL when running in Docker. Designed as a learning project for containerized Java app development and cloud deployment.

## Build & Run Commands

```bash
# Build JAR
mvn clean package -DskipTests

# Run locally (H2 in-memory, no Docker needed)
mvn spring-boot:run

# Run with Docker (PostgreSQL + pgAdmin)
docker compose up --build
```

**Access points:**
- App UI: `http://localhost:8080`
- H2 console (local only): `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:productdb`)
- pgAdmin (Docker): `http://localhost:5050` (admin@portfolio.com / admin)
- PostgreSQL (Docker): port 5432

## Architecture

Single-layer Thymeleaf MVC app — no separate REST API, the controller returns view names.

- **`ProductController`** — three routes: `GET /` (list), `POST /products/add` (insert), `POST /products/delete/{id}` (delete). Uses `BindingResult` for validation errors and `RedirectAttributes` for flash messages after redirect.
- **`Product`** entity — `id`, `productName`, `productNumber` (unique), `createdAt` (auto-set via `@PrePersist`)
- **`ProductRepository`** — plain `JpaRepository<Product, Long>`, no custom queries
- **`templates/index.html`** — Thymeleaf template with Bootstrap 5; includes a collapsible add-product form, product table with per-row delete, and flash message display

## Key Conventions

- Validation via Jakarta annotations on the entity + `BindingResult` in the controller (not `@RestControllerAdvice`)
- All form submissions use `POST` (HTML forms don't support `DELETE`); the delete route is `POST /products/delete/{id}`
- DB profile switching is automatic: `application.properties` sets H2 defaults; `docker-compose.yml` injects `SPRING_DATASOURCE_*` env vars that override them for PostgreSQL
- Lombok: `@Data`, `@Builder`, `@RequiredArgsConstructor`, `@Slf4j` throughout

## Docker Details

Multi-stage Dockerfile: Maven build → Eclipse Temurin JRE 21, runs as non-root user `spring`. PostgreSQL data persists via the `postgres_data` named volume. App waits for DB health check before starting (`depends_on: condition: service_healthy`).
