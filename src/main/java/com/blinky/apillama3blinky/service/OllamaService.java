package com.blinky.apillama3blinky.service;

import com.blinky.apillama3blinky.controller.dto.OllamaDTO;
import com.blinky.apillama3blinky.controller.response.OllamaResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class OllamaService {

    private final WebClient iaWebClient;

    public OllamaService(WebClient iaWebClient) {
        this.iaWebClient = iaWebClient;
    }

    public OllamaResponse sendPrompt(OllamaDTO ollamaDTO) {
        String url = "/api/generate";
        return iaWebClient.post()
                .uri(url)
                .bodyValue(ollamaDTO)
                .retrieve()
                .bodyToMono(OllamaResponse.class)
                .block();
    }
}
