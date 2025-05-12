package com.blinky.apillama3blinky.controller.response;

public class PromptResponse {
    private String response;

    public PromptResponse(String response) {
        this.response = response;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
