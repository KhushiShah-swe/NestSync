# Contributing to NestSync

Start with the root README, the API guide, and an existing issue. Keep changes small enough to review and explain the user problem they solve.

## Development workflow

1. Create a branch from `main`, such as `feature/percentage-splits` or `fix/chore-validation`.
2. Implement the smallest complete increment with meaningful behavior tests.
3. Run the backend and frontend verification commands below.
4. Update the API guide and feature-status table when behavior changes.
5. Open a pull request using the included template and link its issue.

```bash
cd nestsync-backend
./mvnw -B -ntp verify
cd ../nestsync-frontend
npm ci
npm run lint
npm run format:check
npm run test:coverage
npm run build
```

Use `npm run format` to format frontend files. Java uses Google Java Format conventions. Money uses `BigDecimal` and exact cents; never introduce floating-point arithmetic into persisted financial calculations. Writable API fields belong in validated request DTOs, and every household resource lookup must retain its household boundary.

## Definition of done

- Acceptance criteria are demonstrably met.
- Relevant success, validation, and access-control cases are tested.
- CI passes, including MySQL and browser verification where affected.
- User-facing changes include loading/error/empty states and keyboard-accessible controls.
- Documentation matches delivered functionality and states remaining limitations.
- No private data, tokens, database files, dependency directories, or compiled artifacts are committed.

CI is configured; branch protection/rulesets are repository settings and are not implied by the workflow files. Once configured, require the verification jobs and review before merging.

## Issues and sprint planning

Use the User story form for features and Bug report form for defects. Estimate with 1, 2, 3, 5, 8, or 13 points. Points describe relative uncertainty/effort, not hours. Split a story if its acceptance criteria cannot be demonstrated within one sprint.

Suggested board columns: Backlog → Ready → In progress → In review → Done. The roadmap describes proposed two-week sprint goals. Dates and ownership should be assigned during actual planning.

## Commit messages

Use concise intent-focused messages such as `feat: support custom expense allocations`, `fix: reject foreign-household assignees`, or `docs: clarify local MySQL setup`. Avoid claiming tests passed unless they were run.
