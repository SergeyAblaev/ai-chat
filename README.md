# AI-chat Project

## Multi-LLM AI Chat Backend with Spring Boot and Conversation Memory

### Project description
Secure REST API for an AI chatbot using Java, Spring Boot, and Spring AI. The application integrates OpenAI, Anthropic Claude, and Google Gemini through a centralized provider-routing mechanism.
The system automatically retries failed requests, switches to an available AI provider, and remembers the last successful provider to optimize subsequent requests. It supports both stateless chatbot requests and session-based conversations with persistent chat history.
Chat memory is stored through a JDBC repository, with the database schema managed by Liquibase. The application also includes HTTP Basic authentication, role-based access control, request validation, Swagger/OpenAPI documentation, health monitoring, logging, and automated tests.

### Test user

- username: user
- password: user123

### Endpoints

Both endpoints accept `POST` with the same input JSON:

```json
{
"prompt": "Question text"
}
```
But they are intended for different scenarios.

| Feature | `/ai-chat` | `/api/chatbot/chat` |
|---|---|---|
| Purpose | Long-running dialog with context | Fault-tolerant single request |
| Memory of previous messages | Yes, within an HTTP session | No |
| LLM selection | One embedded `ChatModel` | OpenAI → Anthropic → Gemini |
| Retry | No additional retry in the service | Up to 3 OpenAI retries |
| Fallback | No | Anthropic, then Gemini |
| `prompt` validation | `@Valid`, `prompt != null` | No explicit validation |
| Response format | Just a string | JSON `{"response":"..."}` |

### `/ai-chat`

`ChatController.java; ChatService.java`

Logic:

1. Spring validates the ChatRequest: the prompt field is marked with @NotNull.
2. The controller passes the prompt to the ChatService.
3. The ChatService has @SessionScope, so a separate service instance is created for each HTTP session.
4. When the service is created, a unique conversationId is generated.
5. The MessageChatMemoryAdvisor adds the conversation history to the request.
6. The message is sent to the default ChatModel.
7. The client receives the response text without the JSON wrapper.

The history is stored via JDBC in an in-memory HSQLDB: [ChatConfig.java](/Users/sergeyablaev/Projects/my/LLM/ai-chat/src/main/java/com/example/springai/memory/ChatConfig.java:13).
It will disappear after restarting the application.

Sample answer:

```text
The previous question was about Spring AI.
```

For memory to work between HTTP requests, the client must store and pass a session cookie (`JSESSIONID`). However, Spring Security is configured as stateless, so Basic Auth still needs to be sent with every request.
### `/api/chatbot/chat`

`ChatbotController.java; ChatbotService.java`
Logic:

1. The prompt is first sent to the primary LLM, OpenAI.
2. If any error occurs, the request to OpenAI is retried, with a maximum of three attempts.
3. If all three attempts fail, @Recover is called.
4. @Recover makes a single call to the secondary LLM, Anthropic.
5. If Anthropic is unavailable, the request is sent to Gemini.
6. If all three providers fail to respond, a RuntimeException is thrown, which typically translates to an HTTP 500.

The order of the models is specified in `ChatbotConfiguration.java`

```text
OpenAI — 3 attempts
↓ error
Anthropic — 1 attempt
↓ error
Gemini — 1 attempt
↓ error
HTTP 500
```

The endpoint has no memory: each request is independent of previous ones.

The response is returned as JSON:

```json
{
"response": "Model Response"
}
```

Summary: `/ai-chat` should be used for conversations where the model needs to remember previous conversations; `/api/chatbot/chat` is used when service availability and automatic switching between LLM providers are more important. Both endpoints require Basic Authentication according to `SecurityConfig.java`

### Swagger
- [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

### Notes

Before the application start, you need to set the environment variables (ANTHROPIC_API_KEY, OPENAI_API_KEY, GEMINI_API_KEY):
Example:
export GEMINI_API_KEY="your_secret_key"