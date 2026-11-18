package com.fourthon.backend.internal.dto;

public final class InternalResponse {

    private InternalResponse() {
    }

    public record Started(
            String message
    ) {
    }
}
