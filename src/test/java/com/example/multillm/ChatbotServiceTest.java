package com.example.multillm;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariables;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@EnabledIfEnvironmentVariables({
    @EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".*"),
    @EnabledIfEnvironmentVariable(named = "ANTHROPIC_API_KEY", matches = ".*")
})
@TestPropertySource(properties = {
    "logging.level.com.example.multillm=DEBUG"
})
@ExtendWith(OutputCaptureExtension.class)
class ChatbotServiceTest {

    private static final String LLM_PROMPT = "What is the capital of Britan?";
    private static final String EXPECTED_LLM_RESPONSE = "London";

    private static final String PRIMARY_LLM = "gpt-5";
    private static final String SECONDARY_LLM = "gpt-3.5-turbo";


    @Autowired
    private ChatbotService chatbotService;


    @Nested
    @DirtiesContext
    @SetEnvironmentVariable.SetEnvironmentVariables({
            @SetEnvironmentVariable(key = "PRIMARY_LLM", value = PRIMARY_LLM),
            @SetEnvironmentVariable(key = "SECONDARY_LLM", value = SECONDARY_LLM)
    })
    class PrimaryLLMFailsLiveTest {

        @Test
        void whenLLMFails_thenChatbotFallbacksToSecondaryLLM(CapturedOutput capturedOutput) {
            String response = chatbotService.chat(LLM_PROMPT);

            assertThat(response)
                    .isNotEmpty()
                    .containsIgnoringCase(EXPECTED_LLM_RESPONSE);
            assertThat(capturedOutput.getOut())
                    .contains("Attempting to process prompt '" + LLM_PROMPT + "' with primary LLM. Attempt #1")
                    .contains("Attempting to process prompt '" + LLM_PROMPT + "' with primary LLM. Attempt #2")
                    .contains("Attempting to process prompt '" + LLM_PROMPT + "' with primary LLM. Attempt #3")
                    .contains("Primary LLM failure")
                    .contains("Attempting to process prompt '" + LLM_PROMPT + "' with secondary LLM")
                    .doesNotContain("Secondary LLM failure");
        }
    }

    @Nested
    @DirtiesContext
    @SetEnvironmentVariable.SetEnvironmentVariables({
        @SetEnvironmentVariable(key = "PRIMARY_LLM", value = PRIMARY_LLM),
        @SetEnvironmentVariable(key = "SECONDARY_LLM", value = SECONDARY_LLM)
    })
    class PrimaryLLMSucceedsLiveTest {

        @Test
        void whenPrimaryLLMAvailable_thenFallbackNotInitiated(CapturedOutput capturedOutput) {
            String response = chatbotService.chat(LLM_PROMPT);

            assertThat(response)
                .isNotEmpty()
                .containsIgnoringCase(EXPECTED_LLM_RESPONSE);
            assertThat(capturedOutput.getOut())
                .contains("Attempting to process prompt '" + LLM_PROMPT + "' with primary LLM. Attempt #1")
                .doesNotContain("Attempting to process prompt '" + LLM_PROMPT + "' with primary LLM. Attempt #2")
                .doesNotContain("Primary LLM failure");
        }
    }
}