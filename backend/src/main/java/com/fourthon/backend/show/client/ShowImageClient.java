package com.fourthon.backend.show.client;

import com.fourthon.backend.ai.client.ImageInput;

import java.util.List;

public interface ShowImageClient {

    List<ImageInput> fetch(List<String> imageUrls);
}
