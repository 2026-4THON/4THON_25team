package com.fourthon.backend.taste.domain;

import com.fourthon.backend.common.domain.TasteTag;

import java.util.List;
import java.util.Set;

public record TasteAnswers(
        Set<TasteTag> selectedTags,
        List<LifeWork> lifeWorks,
        List<VsChoice> vsChoices,
        String freeText
) {
}
