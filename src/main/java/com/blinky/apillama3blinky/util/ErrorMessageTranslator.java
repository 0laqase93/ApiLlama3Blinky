package com.blinky.apillama3blinky.util;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for translating error messages to Spanish.
 */
@Component
public class ErrorMessageTranslator {

    private final Map<String, String> translationMap;

    public ErrorMessageTranslator() {
        translationMap = new HashMap<>();
        initializeTranslations();
    }

    private void initializeTranslations() {
        // User not found messages
        translationMap.put("User not found with id:", "Usuario no encontrado con id:");
        translationMap.put("User not found with email:", "Usuario no encontrado con email:");
        
        // Email already in use
        translationMap.put("Email already in use", "El correo electrónico ya está en uso");
        
        // Invalid password
        translationMap.put("Invalid password", "Contraseña inválida");
        
        // Bad credentials
        translationMap.put("Invalid credentials: User does not exist or password is incorrect", 
                          "Credenciales inválidas: El usuario no existe o la contraseña es incorrecta");
        
        // Validation failed
        translationMap.put("Validation failed", "La validación ha fallado");
        
        // Generic error message
        translationMap.put("An unexpected error occurred:", "Ha ocurrido un error inesperado:");
    }

    /**
     * Translates an error message from English to Spanish.
     * If the exact message is not found in the translation map, it tries to find a partial match.
     * If no match is found, returns the original message.
     *
     * @param message the error message in English
     * @return the translated message in Spanish, or the original message if no translation is found
     */
    public String translate(String message) {
        if (message == null) {
            return null;
        }

        // Check for exact match
        if (translationMap.containsKey(message)) {
            return translationMap.get(message);
        }

        // Check for partial match (for messages with dynamic content)
        for (Map.Entry<String, String> entry : translationMap.entrySet()) {
            if (message.startsWith(entry.getKey())) {
                String dynamicPart = message.substring(entry.getKey().length());
                return entry.getValue() + dynamicPart;
            }
        }

        // No translation found, return original message
        return message;
    }
}