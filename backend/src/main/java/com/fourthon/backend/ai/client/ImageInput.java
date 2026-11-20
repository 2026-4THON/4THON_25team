package com.fourthon.backend.ai.client;

public record ImageInput(
        String mimeType,
        byte[] data
) {
}
