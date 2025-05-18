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

        // Validation messages
        translationMap.put("Email is required", "El correo electrónico es obligatorio");
        translationMap.put("Email should be valid", "El correo electrónico debe ser válido");
        translationMap.put("Password is required", "La contraseña es obligatoria");
        translationMap.put("Password must be at least 6 characters long", "La contraseña debe tener al menos 6 caracteres");
        translationMap.put("New password is required", "La nueva contraseña es obligatoria");

        // Event related messages
        translationMap.put("Event not found with id:", "Evento no encontrado con id:");
        translationMap.put("You don't have permission to access this event", "No tienes permiso para acceder a este evento");
        translationMap.put("Start time must be before end time", "La hora de inicio debe ser anterior a la hora de fin");
        translationMap.put("Start time is required", "La hora de inicio es obligatoria");
        translationMap.put("End time is required", "La hora de fin es obligatoria");
        translationMap.put("Authorization token is required", "Se requiere token de autorización");
        translationMap.put("Invalid token", "Token inválido");
        translationMap.put("Event ID in URL does not match ID in request body", "El ID del evento en la URL no coincide con el ID en el cuerpo de la solicitud");
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
