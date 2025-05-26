package com.blinky.apillama3blinky.service;

import com.blinky.apillama3blinky.controller.dto.OllamaDTO;
import com.blinky.apillama3blinky.controller.response.OllamaResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Service responsible for communicating with the Ollama API.
 * Handles sending prompts to the language model and receiving responses.
 */
@Service
public class OllamaService {

    private final WebClient iaWebClient;

    public OllamaService(WebClient iaWebClient) {
        this.iaWebClient = iaWebClient;
    }

    /**
     * Sends a prompt to the Ollama API and returns the generated response.
     * 
     * @param ollamaDTO Data transfer object containing the prompt and model configuration
     * @return The response from the Ollama API containing the generated text
     */
    public OllamaResponse sendPrompt(OllamaDTO ollamaDTO) {
        // Define the API endpoint for text generation
        String url = "/api/generate";

        // Make a POST request to the Ollama API
        return iaWebClient.post()
                .uri(url)
                .bodyValue(ollamaDTO)
                .retrieve()
                .bodyToMono(OllamaResponse.class)
                .block();
    }
}
