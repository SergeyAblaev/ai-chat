# AI-chat Project Backend API

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
