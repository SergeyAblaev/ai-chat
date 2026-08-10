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
not stored in environment examples or committed source files.

## Verification

```shell
npm run lint
npm run test
npm run typecheck
npm run build
```

The root `ai-chat-console.design-contract.v1.json` file is a design reference.
The application must not traverse that JSON to construct the runtime UI.
