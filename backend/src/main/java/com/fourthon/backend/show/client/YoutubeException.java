package com.fourthon.backend.show.client;

public class YoutubeException extends RuntimeException {

    public YoutubeException(String message) {
        super(message);
    }

    public YoutubeException(String message, Throwable cause) {
        super(message, cause);
    }
}
