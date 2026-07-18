# cart-decision-tree

Spring Boot 4.1 / Java 25 implementation of a CART fruit-classification workflow with a server-rendered human interface and an MCP interface for AI agents.

## Architecture

The application uses ports and adapters:

- `domain/model` and `domain/service` contain the framework-free tree model and CART algorithm.
- `application/port/in` defines dataset, training, prediction, rendering, and evaluation use cases.
- `application/port/out` defines dataset and active-model storage contracts.
- `adapter/in/web` exposes Spring MVC + Thymeleaf pages enhanced by HTMX.
- `adapter/in/mcp` exposes the same use cases as Spring AI MCP tools.
- `adapter/out/jdbc` reads the Flyway-managed PostgreSQL datasets.
- `adapter/out/memory` atomically publishes an immutable active tree.

Both inbound adapters delegate to `CartApplicationService`; protocol adapters contain no CART business rules.

Runtime boundaries cap each loaded dataset at 512 examples, each generated tree at 1,023 nodes and 512 levels, and rendered tree text at 128,000 characters. Oversized inputs fail with sanitized application errors before recursive rendering or MCP serialization.

## Run locally

Start PostgreSQL and the application with collision-safe development ports:

```sh
./scripts/dev-up.sh
```

The defaults are PostgreSQL `5433` and HTTP `8081`, avoiding the standard `5432` and `8080` ports. The launcher checks each starting port and increments it until an available port is found. Override the starting points when needed:

```sh
POSTGRES_PORT=5440 SERVER_PORT=8090 ./scripts/dev-up.sh
```

The launcher prints the selected dashboard URL. The complete train/predict/inspect/evaluate workflow uses ordinary server-rendered forms without JavaScript; locally vendored HTMX 2.0.9 progressively enhances partial updates.

## Human routes

- `GET /` — dashboard and dataset summary.
- `POST /ui/tree/train` — train and activate a tree.
- `POST /ui/predictions` — classify one fruit.
- `GET /ui/tree` — inspect the active tree.
- `GET /ui/evaluation` — evaluate against holdout data.

Requests with `HX-Request: true` receive fragments; ordinary requests receive full HTML pages.

### Browser baseline

The UI targets current stable Safari, Chrome, Firefox, and Edge releases. Core train and prediction forms remain functional when JavaScript is disabled; HTMX and the small local script provide progressive enhancement only.

## MCP interface

The default streamable HTTP endpoint is `POST /mcp`. The server exposes:

- `cart_dataset_summary`
- `cart_train_tree`
- `cart_predict_fruit`
- `cart_render_tree`
- `cart_evaluate_tree`

Tool results are structured, inputs are bounded, and adapter failures are sanitized before MCP serialization.

## Verification

```sh
./gradlew clean test
./gradlew build
```

PostgreSQL adapter and Flyway tests use Testcontainers. Docker must be available for the complete suite.

## Frontend asset policy

HTMX is served locally from `src/main/resources/static/vendor/`; no runtime CDN is required. Version, upstream source, license, checksums, and the update procedure are recorded in `HTMX-ASSET.md` beside the asset.
