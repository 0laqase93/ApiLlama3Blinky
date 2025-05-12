package com.blinky.apillama3blinky.service;

import com.blinky.apillama3blinky.config.PromptConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class PromptService {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PromptConfig getPromptFor(String name) {
        try {
            ClassPathResource resource = new ClassPathResource("prompts/" + name.toLowerCase() + ".json");
            return objectMapper.readValue(resource.getInputStream(), PromptConfig.class);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar el prompt para: " + name, e);
        }
    }
}
