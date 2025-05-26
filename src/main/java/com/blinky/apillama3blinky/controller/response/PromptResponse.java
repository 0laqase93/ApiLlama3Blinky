package com.blinky.apillama3blinky.controller.response;

/**
 * Response object for AI prompts, containing both the main response text
 * and a separate reaction field.
 */
public class PromptResponse {
    private String response;
    private String reaction;

    public PromptResponse(String response) {
        this.response = response;
    }

    public PromptResponse(String response, String reaction) {
        this.response = response;
        this.reaction = reaction;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getReaction() {
        return reaction;
    }

    public void setReaction(String reaction) {
        this.reaction = reaction;
    }
}
