package com.blinky.apillama3blinky.mapping;

import com.blinky.apillama3blinky.controller.dto.PromptDTO;
import com.blinky.apillama3blinky.controller.response.OllamaResponse;
import com.blinky.apillama3blinky.controller.response.PromptResponse;

public class PromptMapping {

    public static PromptDTO mapToDTO(PromptResponse promptResponse) {
        return new PromptDTO(
                promptResponse.getResponse()
        );
    }

    public static PromptResponse mapToResponse(OllamaResponse ollamaResponse) {
        String filteredResponse = filterSpecialCharacters(ollamaResponse.getResponse());
        return new PromptResponse(filteredResponse);
    }

    /**
     * Filters out special characters from the input string, keeping only
     * alphanumeric characters, periods, commas, and square brackets.
     * 
     * @param input The string to filter
     * @return The filtered string
     */
    private static String filterSpecialCharacters(String input) {
        if (input == null) {
            return "";
        }
        // First remove newline characters
        String withoutNewlines = input.replace("\n", "");
        // Then keep only alphanumeric characters, spaces, periods, commas, and square brackets
        return withoutNewlines.replaceAll("[^a-zA-Z0-9 .,\\[\\]]", "");
    }
}
