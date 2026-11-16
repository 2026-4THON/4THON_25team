package com.fourthon.backend.taste.dto;

import com.fourthon.backend.taste.domain.TagType;
import com.fourthon.backend.taste.domain.TasteReport;
import com.fourthon.backend.taste.domain.TasteTag;
import com.fourthon.backend.taste.domain.VsChoice;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public final class TasteResponse {

    private TasteResponse() {
    }

    @Schema(description = "BE-ANL-03 취향 리포트")
    public record Report(
            @Schema(description = "AI 한 줄 브리핑") String briefing,
            List<String> genres,
            List<String> moods,
            List<LifeWorkItem> lifeWorks,
            List<String> vsChoices,
            String freeText,
            LocalDateTime updatedAt
    ) {

        public static Report from(TasteReport report) {
            return new Report(
                    report.getBriefing(),
                    labels(report, TagType.GENRE),
                    labels(report, TagType.MOOD),
                    report.getLifeWorks().stream()
                            .map(lifeWork -> new LifeWorkItem(lifeWork.getCategory().getKey(), lifeWork.getTitle()))
                            .toList(),
                    report.getVsChoices().stream()
                            .map(VsChoice::getChoice)
                            .toList(),
                    report.getFreeText(),
                    report.getUpdatedAt()
            );
        }

        private static List<String> labels(TasteReport report, TagType type) {
            return report.getSelectedTags().stream()
                    .filter(tag -> tag.getType() == type)
                    .sorted(Comparator.comparing(Enum::ordinal))
                    .map(TasteTag::getLabel)
                    .toList();
        }
    }

    public record LifeWorkItem(
            String category,
            String title
    ) {
    }
}
