package com.fourthon.backend.ai.client;

public interface EmbeddingClient {

    boolean isConfigured();

    String model();

    float[] embed(String text);
}
