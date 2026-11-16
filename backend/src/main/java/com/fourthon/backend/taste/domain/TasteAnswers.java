package com.fourthon.backend.taste.domain;

import java.util.List;
import java.util.Set;

public record TasteAnswers(
        Set<TasteTag> selectedTags,
        List<LifeWork> lifeWorks,
        List<VsChoice> vsChoices,
        String freeText
) {
}
