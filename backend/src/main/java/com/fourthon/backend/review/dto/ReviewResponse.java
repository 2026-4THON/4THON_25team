
package com.fourthon.backend.review.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ReviewResponse {

    public record Actor(
            Long id,
            String name
    ) {}

    public record Item(
            Long reviewId,
            Long ticketId,
            String type,
            Long authorId,
            String authorName,
            String content,
            LocalDate viewedAt,
            LocalDateTime createdAt,
            List<Actor> actors,
            long likeCount,
            boolean likedByMe
    ) {}

    public record PageResult(
            List<Item> content,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean first,
            boolean last
    ) {}

    public record LikeResult(
            Long reviewId,
            long likeCount,
            boolean likedByMe
    ) {}
}
