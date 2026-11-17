package com.fourthon.backend.kopis.client;

public class KopisException extends RuntimeException {

    public KopisException(String message) {
        super(message);
    }

    public KopisException(String message, Throwable cause) {
        super(message, cause);
    }
}
