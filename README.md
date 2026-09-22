# Logistics & Shipment Scheduling Platform

A microservices-based logistics and shipment scheduling system, built from scratch as a learning project to explore real-world distributed systems concerns — service-to-service communication, per-service data ownership, and graceful degradation under partial system failure.

## Overview

This project is intentionally built incrementally, in phases, to deeply understand microservices concepts rather than just assembling a tutorial. **Phase 1** (current) implements two independently deployable services that communicate synchronously over REST.

```
┌─────────────────┐         HTTP (REST)         ┌──────────────────┐
│                  │ ─────────────────────────> │                  │
│  order-service   │   GET /api/v1/users/{id}    │   user-service   │
│   (port 8082)    │ <───────────────────────── │   (port 8081)    │
│                  │                              │                  │
└────────┬─────────┘                              └────────┬─────────┘
         │                                                  │
         ▼                                                  ▼
   ┌───────────┐                                      ┌───────────┐
   │ order_db  │                                      │  user_db  │
   │ (Postgres)│                                      │ (Postgres)│
   └───────────┘                                      └───────────┘
```

Each service owns its own database exclusively — there are no shared tables, no cross-service joins, and no shared Java classes between services. When `order-service` needs to know about a user, it asks `user-service` directly over HTTP, live, rather than duplicating or joining data.

## Tech Stack

- **Java 21**, **Spring Boot 4.1**
- **Spring Data JPA** + **Hibernate** (PostgreSQL)
- **Spring Web** (REST APIs)
- **Spring Boot Validation** (Bean Validation / Jakarta Validation)
- **RestTemplate** (`spring-boot-starter-restclient`) for inter-service HTTP calls
- **Lombok**
- **Docker** (PostgreSQL containers per service)
- **Maven**

## Services

### `user-service` (port 8081)
Manages users (customers, drivers, admins). Full CRUD REST API backed by its own `user_db` PostgreSQL database.

### `order-service` (port 8082)
Manages shipment orders. Backed by its own `order_db` PostgreSQL database. When creating an order, it validates the referenced `userId` by calling `user-service` over HTTP — this is the core inter-service interaction in this project.

## Architecture & Design Decisions

**No cross-service data joins.** `Order` stores only a plain `userId` (a `Long`), never a full `User` object or JPA relationship — since `order_db` and `user_db` are physically separate databases, a real foreign-key relationship isn't possible, and duplicating user data into `order-service` would risk going stale. Fresh user data is fetched over HTTP only when actually needed.

**Layered architecture per service:** `Controller → Service (interface + impl) → Mapper → Repository → Entity`, with DTOs (`*Request` / `*Response`) fully separating the public API contract from internal persistence models.

**Resilience on inter-service calls.** `order-service`'s `UserClient` uses configured connection/read timeouts (via `RestTemplateBuilder`) and translates low-level HTTP/network exceptions into domain-specific ones:
- `HttpClientErrorException.NotFound` → `UserNotFoundException` → HTTP `400`
- `ResourceAccessException` (timeout / connection refused) → `ServiceUnavailableException` → HTTP `503`

This means if `user-service` goes down, `order-service` degrades gracefully with a clean `503` response instead of hanging indefinitely or throwing a raw `500`.

**Centralized exception handling.** Each service has a `@RestControllerAdvice` `GlobalExceptionHandler` that converts exceptions into consistent, structured JSON error responses (`timestamp`, `status`, `message`/`errors`).

## Prerequisites

- JDK 17+ (developed with JDK 21/22)
- Maven
- Docker Desktop
- An IDE (IntelliJ IDEA recommended)

## Running Locally

**1. Start the databases (Docker):**
```bash
docker run -d --name user-db -e POSTGRES_DB=user_db -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 postgres:16-alpine

docker run -d --name order-db -e POSTGRES_DB=order_db -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -p 5433:5432 postgres:16-alpine
```

**2. Run each service** (from IntelliJ, or via Maven):
```bash
cd user-service
./mvnw spring-boot:run
```
```bash
cd order-service
./mvnw spring-boot:run
```

`user-service` starts on `:8081`, `order-service` on `:8082`. Hibernate auto-creates the schema on first run (`ddl-auto: update`).

## API Reference

### `user-service` — `/api/v1/users`
| Method | Path | Description |
|---|---|---|
| POST | `/` | Create a user |
| GET | `/{id}` | Get a user by id |
| GET | `/` | List all users |
| PUT | `/{id}` | Update a user |
| DELETE | `/{id}` | Delete a user |

### `order-service` — `/api/v1/orders`
| Method | Path | Description |
|---|---|---|
| POST | `/` | Create an order (validates `userId` against `user-service`) |
| GET | `/{id}` | Get an order by id |
| GET | `/` | List all orders |
| GET | `/user/{userId}` | List orders for a given user |
| GET | `/status/{status}` | List orders by status |
| PATCH | `/{id}/status?status=X` | Update an order's status |
| DELETE | `/{id}` | Cancel an order (soft — sets status to `CANCELLED`) |

**Example — create an order:**
```bash
curl -X POST http://localhost:8082/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"pickupAddress":"Koramangala","deliveryAddress":"Whitefield","weightKg":5.5}'
```

## What's Not Here Yet (Roadmap)
This is a phased learning project. Deliberately not yet implemented:
- **Service discovery** (Eureka) — `order-service` currently reaches `user-service` via a configured base URL, not by service name
- **API Gateway** (Spring Cloud Gateway) — clients call each service's port directly
- **Circuit breakers / retries** — currently only basic timeouts
- **Async messaging** (Kafka) — order creation is a synchronous, blocking call
- **Docker Compose** — services and databases are currently started individually
- **Auth / JWT**
- **CI/CD** (GitHub Actions)

## Project Structure

```
logistics-platform/
├── user-service/
│   └── src/main/java/com/logistics/user/
│       ├── controller/   service/   repository/
│       ├── model/        dto/       mapper/
│       └── exception/
└── order-service/
    └── src/main/java/com/logistics/order/
        ├── controller/   service/   repository/
        ├── model/        dto/       mapper/
        ├── client/       config/    exception/
Let's do both — update the README first, then commit everything together.

Step 1: Update the README

Open your README.md and make two changes:

A. Remove this line from the "What's Not Here Yet" list:

- **Docker Compose** — services and databases are currently started individually

B. Add a new section — I'd put this right after "Running Locally", as an alternative/preferred way to run everything:

markdown
## Running with Docker Compose (recommended)

Instead of starting each database and service individually, the entire platform can be started with one command:

```bash
docker-compose up --build
```

This builds and starts all 4 containers — `user-db`, `order-db`, `user-service`, `order-service` — networked together. Inside this network, services reach each other by container name rather than `localhost` (e.g. `order-service` calls `user-service` at `http://user-service:8081`, configured via the `USER_SERVICE_URL` environment variable in `docker-compose.yml`).

To stop everything:
```bash
docker-compose down
```

To also wipe the database volumes (fresh start):
```bash
docker-compose down -v
```

Save the file.

Step 2: Check what's new/changed for Git
powershell
cd D:\workplace\logistics-platform
git status

You should see something like:

modified:   README.md
modified:   order-service/src/main/resources/application.yml
modified:   user-service/src/main/resources/application.yml
untracked:  docker-compose.yml
untracked:  order-service/Dockerfile
untracked:  user-service/Dockerfile

Let's update the README to reflect the new architecture, then push.

Step 1: Update the architecture diagram

Find the ASCII diagram near the top and replace it with this updated version showing all three services:

┌──────────────┐        registers         ┌──────────────────┐
│ user-service │ ────────────────────────▶│                   │
│  (port 8081) │                           │  eureka-server    │
└──────┬───────┘        registers         │   (port 8761)     │
│           ┌──────────────────────│                   │
│           │                      └───────────────────┘
│           ▼
┌──────┴───────┐  discovers user-service    
│ order-service│  by name via Eureka +
│  (port 8082) │  @LoadBalanced RestTemplate
└──────┬───────┘
│  HTTP call to whatever address
▼  Eureka resolves right now
┌──────────────┐
│ user-service │
└──────────────┘

        ▼                    ▼
┌───────────┐       ┌───────────┐
│ order_db  │       │  user_db  │
│ (Postgres)│       │ (Postgres)│
└───────────┘       └───────────┘
Step 2: Add a new section explaining service discovery

Insert this right after the "Architecture & Design Decisions" section:

markdown
## Service Discovery (Eureka)

Rather than `order-service` hardcoding `user-service`'s address, both services register themselves with a Eureka Server (`eureka-server`, port 8761) on startup. `order-service`'s `RestTemplate` is annotated `@LoadBalanced`, which lets it call `http://USER-SERVICE/...` — a **logical service name**, not a real host — and have the actual current address resolved dynamically through Eureka at request time.

This means `order-service` never needs to know or configure `user-service`'s real address, port, or host — even if it moves, restarts on a different port, or runs as multiple scaled instances.

Eureka dashboard: `http://localhost:8761`
Step 3: Update the Tech Stack list

Add this line:

markdown
- **Spring Cloud Netflix Eureka** (service registry + client-side discovery)
  Step 4: Update "What's Not Here Yet"

Remove:

- **Service discovery** (Eureka) — `order-service` currently reaches `user-service` via a configured base URL, not by service name
  Step 5: Update the "Running Locally" / Docker Compose sections

Mention the new port:

markdown
Eureka Server starts on `:8761` — dashboard viewable in a browser once running.

And update the Docker Compose command note to mention it now starts 5 containers instead of 4.

Step 6: Update Project Structure

Add eureka-server/ as a third top-level folder in that tree, noting it has no controller/service/repository layers — just the main application class and application.yml.
Client → API Gateway (8080) → routes via Eureka → user-service (8081) / order-service (8082)
↑ ↑
└──────────── registers/discovers ───────────┴──── Eureka Server (8761)


- **api-gateway** — single entry point for all clients. Routes requests based on path prefix (`/user-service/**`, `/order-service/**`), resolving the real address of each service dynamically through Eureka.
- **eureka-server** — service registry. Every service checks in on startup; the gateway and `order-service` both query it to find other services.
- **user-service / order-service** — as before, each owns its own database, communicate over REST.

## 2. Add a new "API Gateway" section

Insert this after the "Service Discovery (Eureka)" section:

````markdown
## API Gateway

`api-gateway` (port 8080) is the single entry point for all client traffic — clients never call `user-service` or `order-service` directly. Routes are generated **dynamically**: at startup, the gateway asks Eureka for every currently registered service and builds a route for each one automatically (`GatewayConfig.java`), rather than hardcoding a route per service.

Built on **Spring Cloud Gateway Server MVC** — the newer, non-reactive variant that matches the rest of this project's Spring MVC style, rather than the classic reactive (WebFlux-based) Gateway.

**Example usage:**
```bash
# Instead of calling user-service directly on :8081
curl http://localhost:8080/user-service/api/v1/users

# Instead of calling order-service directly on :8082
curl http://localhost:8080/order-service/api/v1/orders
```

**Known limitation:** routes are built once at gateway startup. A service that registers with Eureka *after* the gateway has already started won't get a route until the gateway restarts. Acceptable for this project's scale given the startup order enforced in `docker-compose.yml`; a production system would add periodic or event-driven route refresh.
````

## 3. Update the Tech Stack list

Add:
````markdown
- **Spring Cloud Gateway** (Server MVC variant) — single entry point, dynamic routing via Eureka
````

## 4. Update "What's Not Here Yet"

Remove:
API Gateway (Spring Cloud Gateway) — clients call each service's port directly

## 5. Update "Running with Docker Compose"

Update the container count mention:
````markdown
This builds and starts all 6 containers — `user-db`, `order-db`, `eureka-server`, `user-service`, `order-service`, `api-gateway` — networked together.
````

## 6. Update Project Structure

Add `api-gateway/` as a fourth top-level module in the tree, noting it has a `config/` package (for `GatewayConfig.java`) instead of the usual controller/service/repository layers.

---

Make these six edits, then:
````powershell
cd D:\workplace\logistics-platform
git add README.md
git commit -m "Update README for Phase 3: API Gateway and dynamic routing"
git push
````

Paste the `git status` or push output once done.