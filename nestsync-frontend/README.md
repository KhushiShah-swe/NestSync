# NestSync frontend

React 19 + Vite 8. For the complete project setup, API contract, architecture, feature status, and contribution workflow, start with the [root README](../README.md).

```bash
npm ci
npm run dev
npm run lint
npm run test:coverage
npm run build
```

The Vite development server proxies `/api` to `http://localhost:8080`. In the container setup, Nginx performs that proxy. `VITE_API_BASE_URL` optionally overrides the API origin at build time; never put secrets in `VITE_` variables.
