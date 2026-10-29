# mesh-app

Spring Boot REST API (Java 21, Gradle) with Postgres, Redis, and JWT auth.

## Run

```bash
docker compose up -d
./gradlew bootRun
```

App listens on port **8088**.

Swagger UI: http://localhost:8088/swagger-ui.html

## Seed login

All seeded users use password `password`.

Examples:

- email: `ivan@meshgroup.ru`
- phone: `79001112233`

## Main API

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/auth/login` | Login (email or phone + password) → JWT |
| `GET` | `/api/users` | Search users (filters + paging) |
| `GET` | `/api/users/{id}` | Get user by id |
| `POST`/`PUT`/`DELETE` | `/api/users/me/emails` | Manage own emails |
| `POST`/`PUT`/`DELETE` | `/api/users/me/phones` | Manage own phones |
| `POST` | `/api/transfers` | Transfer funds (sender from JWT) |

## Layers

`controller` → `service` → `repository`
