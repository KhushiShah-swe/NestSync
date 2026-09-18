# Delivery and operations

## What is automated

The `CI` workflow verifies Java, React, MySQL integration, browser journeys, and the complete Compose stack. On a successful `main` push, the final jobs build and publish backend/frontend images to GHCR. Pull requests never publish images. Each image has `latest` and `sha-<full-commit-sha>` tags.

There is no public hosting environment, deployed URL, or automatic cloud rollout in this repository yet. The publishing jobs use the built-in `GITHUB_TOKEN`; they do not need a personal access token. If organization policy restricts package publishing, the repository owner must allow it. New GHCR packages can initially be private even when the source repository is public.

References: [GitHub container registry](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry), [Publishing Docker images](https://docs.github.com/en/actions/use-cases-and-examples/publishing-packages/publishing-docker-images).

## Run published images locally

After a successful delivery run and after package read access is available:

```bash
cp .env.example .env
# Set unique passwords and JWT_SECRET in .env first.
docker compose -f compose.yaml -f compose.images.yaml pull
docker compose -f compose.yaml -f compose.images.yaml up --detach --no-build --wait
```

`compose.images.yaml` selects the delivered images while retaining the base Compose networking, database volume, credentials, and health checks. If the package is private, sign in to GHCR using an appropriately scoped credential as described in GitHub’s documentation. Never store credentials in repository files.

For a reproducible rollback, set `NESTSYNC_IMAGE_TAG=sha-<known-good-commit>` in `.env`, pull, and rerun the same command. Keep the backend and frontend on the same commit tag. Image rollback does not undo database schema changes; take backups and introduce migrations before production use.

## MySQL native startup

Create a `nestsync` database and a non-root application account. Set `SPRING_PROFILES_ACTIVE=mysql`, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and `JWT_SECRET` in your shell or service configuration, then run the wrapper normally. Maven does not automatically read the root `.env` file.

Production should use a versioned migration tool and `ddl-auto=validate`. The current `update` setting is for the local/container MVP. Do not point the test suite at production: the test profile uses `create-drop`.

## Before making the application public

The deployment issue in the roadmap depends on these concrete changes:

- Versioned schema migrations and a verified backup/restore process.
- Defined membership roles, invite expiry/rotation, concurrency handling, and record-change auditing.
- Authentication rate limits, password reset policy, secret rotation, and token-revocation strategy.
- HTTPS, secure response headers/content security policy, exact CORS origins, and externalized secrets.
- Health monitoring, error logs without credentials, resource limits, and a documented rollback.
- Selection of a hosting account, database service, domain, and budget.

No cloud resources or paid services are provisioned by the current workflows.

## Local operating commands

```bash
docker compose ps
docker compose logs -f backend
docker compose restart backend
python3 scripts/smoke-test.py
```

The smoke script creates a disposable account/home, creates and removes one expense, and verifies balances through the frontend proxy. Use it against an isolated verification environment; the demo account remains because account deletion is not implemented.

Container ports bind to localhost by default. For production, put the web app behind a managed TLS ingress and keep MySQL private. Do not expose development services by merely changing these port bindings.
