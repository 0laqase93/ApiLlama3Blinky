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

    private static String filterSpecialCharacters(String input) {
        if (input == null) {
            return "";
        }
        String withoutNewlines = input.replace("\n", "");
        return withoutNewlines.replaceAll("[^a-zA-ZáéíóúÁÉÍÓÚüÜñÑ0-9 .,¿¡\\[\\]!?]", "");
    }
}
