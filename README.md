# AI-chat Project
Current logic:
First request, Fallback route:
`OpenAI -> Anthropic -> Gemini`

successfulProvider is saved

Next request:
directly to successfulProvider

### Swagger
http://localhost:8080/swagger-ui/index.html
http://localhost:8080/api-docs

### Notes

Before application startup, you need to set the environment variables (ANTHROPIC_API_KEY, OPENAI_API_KEY, GEMINI_API_KEY):
Example:
export GEMINI_API_KEY="your_secret_key"