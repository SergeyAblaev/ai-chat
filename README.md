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
cd $PROJECT_ROOT

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



# NOTE --==move below to Backend ReadMe==

## API

### Resilient request

```http
POST /api/chatbot/chat
Content-Type: application/json

{
  "prompt": "Question text"
}
```

Successful response example:

```json
{
  "message": {
    "id": "msg_f2a5...",
    "role": "assistant",
    "content": "Model Response",
    "createdAt": "2026-08-07T16:02:31.425+07:00"
  },
  "execution": {
    "requestId": "req_39ab...",
    "mode": "RESILIENT",
    "provider": "ANTHROPIC",
    "model": "claude-sonnet",
    "status": "SUCCESS",
    "fallbackUsed": true,
    "attemptCount": 4,
    "durationMs": 4231,
    "attempts": [
      {
        "provider": "OPENAI",
        "attempt": 1,
        "status": "FAILED",
        "durationMs": 812
      },
      {
        "provider": "OPENAI",
        "attempt": 2,
        "status": "FAILED",
        "durationMs": 906
      },
      {
        "provider": "OPENAI",
        "attempt": 3,
        "status": "FAILED",
        "durationMs": 794
      },
      {
        "provider": "ANTHROPIC",
        "attempt": 1,
        "status": "SUCCESS",
        "durationMs": 1419
      },
      {
        "provider": "GEMINI",
        "attempt": null,
        "status": "SKIPPED",
        "durationMs": 0
      }
    ]
  }
}
```

`fallbackUsed` is true only when the current request moves to a different
provider. Retries of the same provider do not count as fallback. `attemptCount`
counts executed attempts and excludes `SKIPPED` provider entries. Attempt status
values are `PENDING`, `RUNNING`, `SUCCESS`, `FAILED`, and `SKIPPED`; synchronous
completed responses normally contain `SUCCESS`, `FAILED`, and `SKIPPED`. The
`model` field is read from the active Spring configuration for the successful
provider.

### Create a context conversation

```http
POST /api/conversations
```

Returns `201 Created`:

```json
{
  "id": "conv_7a91...",
  "createdAt": "2026-08-07T16:02:31.425+07:00",
  "title": null
}
```

List conversations, newest first:

```http
GET /api/conversations
```

The response is an array of conversation objects with the same `id`,
`createdAt`, and `title` fields as the create response.

### Send a context message

```http
POST /api/conversations/{conversationId}/messages
Content-Type: application/json

{
  "prompt": "Explain Spring AI"
}
```

Successful response:

```json
{
  "message": {
    "id": "msg_9c24...",
    "role": "assistant",
    "content": "Spring AI is...",
    "createdAt": "2026-08-07T16:03:12.150+07:00"
  },
  "execution": {
    "requestId": "req_81d0...",
    "mode": "CONTEXT",
    "provider": "OPENAI",
    "model": "gpt-5-mini",
    "status": "SUCCESS",
    "fallbackUsed": false,
    "attemptCount": 1,
    "durationMs": 735,
    "attempts": [
      {
        "provider": "OPENAI",
        "attempt": 1,
        "status": "SUCCESS",
        "durationMs": 735
      },
      {
        "provider": "ANTHROPIC",
        "attempt": null,
        "status": "SKIPPED",
        "durationMs": 0
      },
      {
        "provider": "GEMINI",
        "attempt": null,
        "status": "SKIPPED",
        "durationMs": 0
      }
    ]
  }
}
```

List the user and assistant messages recorded for a conversation:

```http
GET /api/conversations/{conversationId}/messages
```

Delete a conversation and clear its Spring AI chat memory:

```http
DELETE /api/conversations/{conversationId}
```

Successful deletion returns `204 No Content`.

An unknown conversation ID returns `404 Not Found`. A missing or blank `prompt`
returns `400 Bad Request`.

The previous `POST /ai-chat` session-scoped endpoint has been replaced by these
explicit conversation endpoints. Conversation metadata is currently kept in an
in-memory registry. Message history is stored by Spring AI through the JDBC chat
memory repository in the in-memory HSQLDB, so both are cleared on application
restart.

## Swagger

- [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

## Configuration

Set the provider keys and model names before starting the application:

```shell
export OPENAI_API_KEY="your_secret_key"
export ANTHROPIC_API_KEY="your_secret_key"
export GEMINI_API_KEY="your_secret_key"
export OPENAI_MODEL="gpt-5-mini"
export ANTHROPIC_MODEL="claude-sonnet"
```

`GEMINI_MODEL` is optional and defaults to the value configured in
`application.yaml`.
