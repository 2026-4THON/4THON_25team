package com.fourthon.backend.show.client;

public class YoutubeQuotaExceededException extends YoutubeException {

    public YoutubeQuotaExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}
