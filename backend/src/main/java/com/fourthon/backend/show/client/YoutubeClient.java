package com.fourthon.backend.show.client;

import java.util.List;

public interface YoutubeClient {

    boolean isConfigured();

    List<VideoCandidate> search(String query, int maxResults);
}
