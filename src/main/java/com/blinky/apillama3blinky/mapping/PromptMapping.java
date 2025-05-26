package com.blinky.apillama3blinky.mapping;

import com.blinky.apillama3blinky.controller.response.OllamaResponse;
import com.blinky.apillama3blinky.controller.response.PromptResponse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for mapping between different DTO and response objects.
 */
public class PromptMapping {

    // Pattern to match a reaction enclosed in square brackets at the end of the text
    // Allows for spaces before and after the brackets
    private static final Pattern REACTION_PATTERN = Pattern.compile("\\s*\\[(.*?)\\]\\s*$");

    /**
     * Maps an OllamaResponse to a PromptResponse, separating the reaction from the main response.
     *
     * @param ollamaResponse The Ollama API response to map
     * @return A new PromptResponse with separated response and reaction
     */
    public static PromptResponse mapToResponse(OllamaResponse ollamaResponse) {
        String rawResponse = ollamaResponse.getResponse();

        // 1. Extract reaction if it appears at the end as [TEXT]
        String reaction = null;
        String cleanedResponse = rawResponse;

        Matcher matcher = REACTION_PATTERN.matcher(rawResponse);
        if (matcher.find()) {
            reaction = "[" + matcher.group(1).trim() + "]";
            cleanedResponse = rawResponse.substring(0, matcher.start()).trim();
        } else {
            // 2. If not in brackets, look for hashtags like #HAPPY
            Pattern hashtagPattern = Pattern.compile("#(\\w+)");
            Matcher hashtagMatcher = hashtagPattern.matcher(rawResponse);
            if (hashtagMatcher.find()) {
                reaction = "[" + hashtagMatcher.group(1) + "]";
                cleanedResponse = rawResponse.replaceFirst("#" + hashtagMatcher.group(1), "").trim();
            }
        }

        // 3. Clean unnecessary special characters (quotes, line breaks, etc.)
        cleanedResponse = cleanedResponse
                .replaceAll("[\"”“]", "")  // Removes typographic and normal quotes
                .replaceAll("[\\n\\r]+", " ")  // Replaces line breaks with spaces
                .replaceAll("\\s{2,}", " ") // Removes multiple spaces
                .trim();

        return new PromptResponse(cleanedResponse, reaction);
    }
}
