package com.fourthon.backend.ai.event;

import java.util.Set;

public record ReviewTagScoresChangedEvent(
        Set<Long> performanceIds
) {
}
