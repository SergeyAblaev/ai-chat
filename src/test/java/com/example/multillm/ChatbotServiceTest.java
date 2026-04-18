package com.example.multillm;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
    "logging.level.com.example.multillm=DEBUG"
})
@ExtendWith(OutputCaptureExtension.class)
class ChatbotServiceTest {

    private static final String LLM_PROMPT = "What is the capital of Britan?";
    private static final String EXPECTED_LLM_RESPONSE = "London";

    private static final String PRIMARY_LLM = "gpt-5";
    private static final String SECONDARY_LLM = "gpt-3.5-turbo";
    private static final String GEMINI_LLM = "gemini-3.5-flash-lite";

    private static final String INVALID_OPENAI_API_KEY = "sk-invalid-openai-8f3a1c7e";
    private static final String INVALID_ANTHROPIC_API_KEY = "sk-ant-invalid-6d9b2e4a";
    private static final String INVALID_GEMINI_API_KEY = "invalid-gemini-4c7f9a2d";


    @Autowired
    private ChatbotService chatbotService;


    @Nested
    @DirtiesContext
    @EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = "\\S+")
    @TestPropertySource(properties = {
        "spring.ai.openai.api-key=" + INVALID_OPENAI_API_KEY,
        "spring.ai.openai.chat.options.model=" + PRIMARY_LLM,
        "spring.ai.anthropic.api-key=" + INVALID_ANTHROPIC_API_KEY,
        "spring.ai.anthropic.chat.options.model=" + SECONDARY_LLM,
        "spring.ai.google.genai.chat.options.model=" + GEMINI_LLM
    })
    class PrimaryLLMFailsLiveTest {

        @Test
        void whenPrimaryAndSecondaryLLMsFail_thenChatbotFallbacksToGemini(CapturedOutput capturedOutput) {
            String response = chatbotService.chat(LLM_PROMPT);

            assertThat(response)
                    .isNotEmpty()
                    .containsIgnoringCase(EXPECTED_LLM_RESPONSE);
            assertThat(capturedOutput.getOut())
                    .contains("Attempting to process prompt with OPENAI. Attempt #1")
                    .contains("Attempting to process prompt with OPENAI. Attempt #2")
                    .contains("Attempting to process prompt with OPENAI. Attempt #3")
                    .contains("OPENAI failure")
                    .contains("Attempting to process prompt with ANTHROPIC. Attempt #1")
                    .contains("ANTHROPIC failure")
                    .contains("Attempting to process prompt with GEMINI. Attempt #1")
                    .doesNotContain("GEMINI failure");
        }
    }

    @Nested
    @DirtiesContext
    @EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = "\\S+")
    @TestPropertySource(properties = {
        "spring.ai.openai.chat.options.model=" + PRIMARY_LLM,
        "spring.ai.anthropic.api-key=" + INVALID_ANTHROPIC_API_KEY,
        "spring.ai.anthropic.chat.options.model=" + SECONDARY_LLM,
        "spring.ai.google.genai.api-key=" + INVALID_GEMINI_API_KEY
    })
    class PrimaryLLMSucceedsLiveTest {

        @Test
        void whenPrimaryLLMAvailable_thenFallbackNotInitiated(CapturedOutput capturedOutput) {
            String response = chatbotService.chat(LLM_PROMPT);

            assertThat(response)
                .isNotEmpty()
                .containsIgnoringCase(EXPECTED_LLM_RESPONSE);
            assertThat(capturedOutput.getOut())
                .contains("Attempting to process prompt with OPENAI. Attempt #1")
                .doesNotContain("Attempting to process prompt with OPENAI. Attempt #2")
                .doesNotContain("OPENAI failure");
        }
    }
}
