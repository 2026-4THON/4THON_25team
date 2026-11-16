package com.fourthon.backend.ai.client;

public interface LlmClient {

    boolean isConfigured();

    String generateJson(String prompt);
}
