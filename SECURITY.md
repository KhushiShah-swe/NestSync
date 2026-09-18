# Security

NestSync is a portfolio MVP under active development. It includes BCrypt passwords, expiring signed JWTs, validation, household-scoped access, configured CORS, and secret-free example configuration.

## Reporting

Do not post credentials, tokens, private household records, or exploit details in a public issue. Use GitHub private vulnerability reporting if the repository owner has enabled it; otherwise request a private reporting channel without disclosing sensitive details. No external email address is supplied by this project.

## Current limitations

JWT logout is client-side and existing tokens are valid until expiration or signing-key rotation. Invite codes do not yet expire or rotate. All household members can modify shared records. Rate limiting, account recovery, concurrency controls, audit history, and production migrations remain roadmap items. A public rollout depends on completing the documented production-readiness work.

## Secret handling

The development profile generates a random signing key when none is supplied. The MySQL profile fails to start without a signing secret of at least 32 bytes. Real credentials belong in local environment files or a deployment secret store; `.env.example` and CI disposable credentials must never be reused in production.
