package com.fourthon.backend.ai.prompt;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PromptLoader {

    private static final String PROMPT_DIRECTORY = "prompts/";
    private static final String PROMPT_EXTENSION = ".txt";

    private final Map<String, String> templates = new ConcurrentHashMap<>();

    public String render(String name, Map<String, String> variables) {
        String prompt = templates.computeIfAbsent(name, this::read);
        for (Map.Entry<String, String> variable : variables.entrySet()) {
            prompt = prompt.replace("{{" + variable.getKey() + "}}", variable.getValue());
        }
        return prompt;
    }

    private String read(String name) {
        ClassPathResource resource = new ClassPathResource(PROMPT_DIRECTORY + name + PROMPT_EXTENSION);
        try (InputStream inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("프롬프트를 읽을 수 없습니다: " + name, e);
        }
    }
}
