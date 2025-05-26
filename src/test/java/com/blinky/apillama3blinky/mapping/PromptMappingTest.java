package com.blinky.apillama3blinky.mapping;

import com.blinky.apillama3blinky.controller.response.OllamaResponse;
import com.blinky.apillama3blinky.controller.response.PromptResponse;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class PromptMappingTest {

    @Test
    public void testMapToResponseWithReaction() {
        // Create a test OllamaResponse with a reaction at the end
        OllamaResponse ollamaResponse = new OllamaResponse(
                "test-model",
                "2023-01-01",
                "This is a test response with special characters @#$%^&* [HAPPY]",
                true,
                "stop",
                Collections.emptyList()
        );

        // Map to PromptResponse
        PromptResponse result = PromptMapping.mapToResponse(ollamaResponse);

        // Verify the reaction was correctly extracted with square brackets
        assertEquals("[HAPPY]", result.getReaction());
        // Verify the main response is preserved exactly as received
        assertEquals("This is a test response with special characters @#$%^&*", result.getResponse());
    }

    @Test
    public void testMapToResponseWithoutReaction() {
        // Create a test OllamaResponse without a reaction
        OllamaResponse ollamaResponse = new OllamaResponse(
                "test-model",
                "2023-01-01",
                "This is a test response with special characters @#$%^&* but no reaction",
                true,
                "stop",
                Collections.emptyList()
        );

        // Map to PromptResponse
        PromptResponse result = PromptMapping.mapToResponse(ollamaResponse);

        // Verify no reaction was extracted
        assertNull(result.getReaction());
        // Verify the main response is preserved exactly as received
        assertEquals("This is a test response with special characters @#$%^&* but no reaction", result.getResponse());
    }

    @Test
    public void testMapToResponseWithSpecialCharsInReaction() {
        // Create a test OllamaResponse with special characters in the reaction
        OllamaResponse ollamaResponse = new OllamaResponse(
                "test-model",
                "2023-01-01",
                "This is a test response [HAPPY@#$%^&*]",
                true,
                "stop",
                Collections.emptyList()
        );

        // Map to PromptResponse
        PromptResponse result = PromptMapping.mapToResponse(ollamaResponse);

        // Verify the reaction was correctly extracted with special characters and square brackets
        assertEquals("[HAPPY@#$%^&*]", result.getReaction());
        // Verify the main response is preserved exactly as received
        assertEquals("This is a test response", result.getResponse());
    }

    @Test
    public void testMapToResponseWithSpacesBeforeReaction() {
        // Create a test OllamaResponse with spaces before the reaction brackets
        OllamaResponse ollamaResponse = new OllamaResponse(
                "test-model",
                "2023-01-01",
                "This is a test response with spaces before reaction   [HAPPY]",
                true,
                "stop",
                Collections.emptyList()
        );

        // Map to PromptResponse
        PromptResponse result = PromptMapping.mapToResponse(ollamaResponse);

        // Verify the reaction was correctly extracted with square brackets
        assertEquals("[HAPPY]", result.getReaction());
        // Verify the main response is preserved exactly as received
        assertEquals("This is a test response with spaces before reaction", result.getResponse());
    }
}
