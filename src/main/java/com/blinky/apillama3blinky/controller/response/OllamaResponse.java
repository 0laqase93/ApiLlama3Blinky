package com.blinky.apillama3blinky.controller.response;

import java.util.List;

public class OllamaResponse {
    private String model;
    private String created_at;
    private String response;
    private boolean done;
    private String done_reason;
    List<Integer> context;

    public OllamaResponse(String model, String created_at, String response, boolean done, String done_reason, List<Integer> context) {
        this.model = model;
        this.created_at = created_at;
        this.response = response;
        this.done = done;
        this.done_reason = done_reason;
        this.context = context;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public String getDone_reason() {
        return done_reason;
    }

    public void setDone_reason(String done_reason) {
        this.done_reason = done_reason;
    }

    public List<Integer> getContext() {
        return context;
    }

    public void setContext(List<Integer> context) {
        this.context = context;
    }
}
