package com.blinky.apillama3blinky.mapping;

import com.blinky.apillama3blinky.controller.dto.OllamaDTO;
import com.blinky.apillama3blinky.controller.dto.PromptDTO;

public class OllamaMapping {
    public static OllamaDTO toOllamaDTO(PromptDTO promptDTO, String model) {
        return new OllamaDTO(
                model,
                promptDTO.getPrompt(),
                false
        );
    }
}
