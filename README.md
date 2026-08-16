# AI-chat Project

**Multi-LLM AI Workspace** - A responsive full-stack AI workspace built with React, TypeScript, Spring Boot and Spring AI, featuring conversational memory,
multi-provider failover and execution observability.

![AI Chat Console screen.png](docs/AI%20Chat%20Console%20screen.png)

##  Backend

Multi-LLM AI Chat Backend with Spring Boot and Conversation Memory

Secure REST API for an AI chatbot using Java, Spring Boot, and Spring AI. The
application integrates OpenAI, Anthropic, and Google Gemini through a centralized
provider router with retries, fallback, execution tracing, and remembered provider
preference.

The API supports two modes:

- `RESILIENT`: one stateless request with retries and provider fallback.
- `CONTEXT`: an explicit conversation resource whose ID is used as the Spring AI
  chat-memory key.

All endpoints require HTTP Basic authentication. The default development user is
`user` / `user123`.


### Running

```bash
cd $PROJECT_ROOT/backend 

export OPENAI_API_KEY="your_secret_key"
export ANTHROPIC_API_KEY="your_secret_key"
export GEMINI_API_KEY="your_secret_key"
export OPENAI_MODEL="gpt-5-mini"
export ANTHROPIC_MODEL="claude-sonnet"
export GEMINI_MODEL="gemini-3.5-flash-lite"
export APP_USER_PASSWORD="user123"
export APP_ADMIN_PASSWORD="admin123"

mvn spring-boot:run
```

## Frontend

### Running

```bash
cd $PROJECT_ROOT/frontend

cp .env.example .env.local
npm install
npm run dev
```

or

```shell
cd frontend
npm run dev
```

### Build

```shell
cd frontend
npm run lint
```

```shell
cd frontend
npm run test
```

```shell
cd frontend
npm run typecheck
```

```shell
cd frontend
npm run build
```

```shell
cd frontend
npm audit
```

```shell
cd frontend
npm run start
```

### Stack
- React 19.2.8
- Vite 8.2.1
- TypeScript

## Dev Links

- [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- [http://localhost:5173/](http://localhost:5173/)

## Provider routing

For the first request, the default order is:

```text
OpenAI — up to 3 attempts
↓ failure
Anthropic — 1 attempt
↓ failure
Gemini — 1 attempt
↓ failure
HTTP 500
```

After a successful request, that provider is tried first on the next request.
Database failures from chat memory are not treated as provider failures and do
not trigger fallback.

### Modules documentation:

- [Backend README.md](backend/README.md)
- [Frontend README.md](frontend/README.md)