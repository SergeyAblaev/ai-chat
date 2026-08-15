# AI Chat Console frontend

React + TypeScript + Vite frontend for the AI Chat Console.

Use Node.js `20.19+`, `22.13+`, or `24+`. Odd-numbered non-LTS releases such
as Node.js 23 are not supported by the current Vite/Vitest toolchain.

## Local development

```shell
cp .env.example .env.local
npm install
npm run dev
```

Browser requests use `VITE_API_BASE_URL` (`/api` by default). During local
development Vite proxies `/api` to `VITE_BACKEND_PROXY_TARGET`, which defaults
to `http://localhost:8080`.

The backend requires HTTP Basic authentication. Credentials are intentionally
not stored in environment examples or committed source files. Enter them in
the connection screen at runtime; the frontend keeps them only in React memory
for the current tab and clears them on disconnect or reload.

For production, serve the API through the same HTTPS origin (for example,
reverse-proxy `/api` to Spring Boot). The backend does not currently publish a
cross-origin CORS policy, so a separate browser origin requires an explicit
backend CORS configuration.

## Verification

```shell
npm run lint
npm run test
npm run typecheck
npm run build
```

The root `ai-chat-console.design-contract.v1.json` file is a design reference.
The application must not traverse that JSON to construct the runtime UI.

The exported Light-theme variables and styles are represented as static CSS
custom properties in `src/styles/tokens.css`. Semantic Figma names are preserved
in kebab-case (for example, `bg/canvas` becomes `--bg-canvas`). The generic
Figma service variable `Colors/Color` is intentionally excluded because it is
not a semantic product token.
