package com.blinky.apillama3blinky.controller.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class OllamaDTO {
    @NotBlank
    private String model;

    @NotBlank
    private String prompt;

    private boolean stream;

    @Min(value = 0)
    @Max(value = 1)
    private double temperature;

    @Min(value = 0)
    @Max(value = 1)
    private double topP;

    @Positive
    private int numPredict;

    public OllamaDTO(String model, String prompt, boolean stream) {
        this.model = model;
        this.prompt = prompt;
        this.stream = stream;
        this.temperature = 0.4;  // Valor por defecto recomendado
        this.topP = 0.5;         // Valor por defecto recomendado
        this.numPredict = 128;   // Más tokens para respuestas más largas
    }

    // Sobrecarga para control total
    public OllamaDTO(String model, String prompt, boolean stream, double temperature, double topP, int numPredict) {
        this.model = model;
        this.prompt = prompt;
        this.stream = stream;
        this.temperature = temperature;
        this.topP = topP;
        this.numPredict = numPredict;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public boolean isStream() {
        return stream;
    }

    public void setStream(boolean stream) {
        this.stream = stream;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getTopP() {
        return topP;
    }

    public void setTopP(double topP) {
        this.topP = topP;
    }

    public int getNumPredict() {
        return numPredict;
    }

    public void setNumPredict(int numPredict) {
        this.numPredict = numPredict;
    }
}
