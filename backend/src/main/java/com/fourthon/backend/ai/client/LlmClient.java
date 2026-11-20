package com.fourthon.backend.ai.client;

import java.util.List;

public interface LlmClient {

    boolean isConfigured();

    String generateJson(String prompt);

    String generateJson(String prompt, List<ImageInput> images);
}
