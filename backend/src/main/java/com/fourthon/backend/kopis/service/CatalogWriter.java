package com.fourthon.backend.kopis.service;

import com.fourthon.backend.ticket.domain.CatalogPerformance;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class CatalogWriter {

    private static final int BATCH_SIZE = 500;

    private final JdbcTemplate jdbcTemplate;

    public Long insertPerformance(PerformanceRow row) {
        Long id = nextPerformanceId();
        jdbcTemplate.update(
                "INSERT INTO catalog_performances (id, title, genre, venue, running_minutes, poster_url) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                id,
                row.title(),
                row.genre().name(),
                row.venue(),
                row.runningMinutes(),
                row.posterUrl()
        );
        return id;
    }

    public void updatePerformance(Long id, PerformanceRow row) {
        jdbcTemplate.update(
                "UPDATE catalog_performances SET title = ?, genre = ?, venue = ?, running_minutes = ?, poster_url = ? "
                        + "WHERE id = ?",
                row.title(),
                row.genre().name(),
                row.venue(),
                row.runningMinutes(),
                row.posterUrl(),
                id
        );
    }

    public void syncCast(Long performanceId, List<String> names) {
        Map<String, Long> castIdsByName = findCastIdsByName(performanceId);
        for (int order = 0; order < names.size(); order++) {
            String name = names.get(order);
            Long castId = castIdsByName.remove(name);
            if (castId != null) {
                jdbcTemplate.update(
                        "UPDATE catalog_performance_casts SET cast_order = ? WHERE id = ?",
                        order,
                        castId
                );
                continue;
            }
            Long actorId = insertActor(name);
            jdbcTemplate.update(
                    "INSERT INTO catalog_performance_casts (performance_id, actor_id, cast_order) VALUES (?, ?, ?)",
                    performanceId,
                    actorId,
                    order
            );
        }
        castIdsByName.values().forEach(castId -> jdbcTemplate.update(
                "DELETE FROM catalog_performance_casts WHERE id = ?",
                castId
        ));
    }

    public void syncSessions(
            Long performanceId,
            Collection<LocalDateTime> wantedStartTimes,
            LocalDateTime removableFrom) {

        List<LocalDateTime> wanted = new ArrayList<>(wantedStartTimes);
        jdbcTemplate.batchUpdate(
                "INSERT IGNORE INTO catalog_sessions (performance_id, starts_at) VALUES (?, ?)",
                wanted,
                BATCH_SIZE,
                (statement, startsAt) -> {
                    statement.setLong(1, performanceId);
                    statement.setObject(2, startsAt);
                }
        );

        Set<LocalDateTime> wantedSet = new HashSet<>(wanted);
        List<Long> removableIds = jdbcTemplate.query(
                        "SELECT s.id, s.starts_at FROM catalog_sessions s "
                                + "WHERE s.performance_id = ? AND s.starts_at >= ? "
                                + "AND NOT EXISTS (SELECT 1 FROM tickets t WHERE t.session_id = s.id)",
                        (resultSet, rowNum) -> new SessionRow(
                                resultSet.getLong("id"),
                                resultSet.getObject("starts_at", LocalDateTime.class)
                        ),
                        performanceId,
                        removableFrom
                ).stream()
                .filter(session -> !wantedSet.contains(session.startsAt()))
                .map(SessionRow::id)
                .toList();

        jdbcTemplate.batchUpdate(
                "DELETE FROM catalog_sessions WHERE id = ?",
                removableIds,
                BATCH_SIZE,
                (statement, id) -> statement.setLong(1, id)
        );
    }

    private Map<String, Long> findCastIdsByName(Long performanceId) {
        Map<String, Long> castIdsByName = new LinkedHashMap<>();
        jdbcTemplate.query(
                "SELECT c.id, a.name FROM catalog_performance_casts c "
                        + "JOIN catalog_actors a ON a.id = c.actor_id "
                        + "WHERE c.performance_id = ?",
                resultSet -> {
                    castIdsByName.putIfAbsent(resultSet.getString("name"), resultSet.getLong("id"));
                },
                performanceId
        );
        return castIdsByName;
    }

    private Long insertActor(String name) {
        Long id = nextActorId();
        jdbcTemplate.update("INSERT INTO catalog_actors (id, name) VALUES (?, ?)", id, name);
        return id;
    }

    private Long nextPerformanceId() {
        return jdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(id), 0) + 1 FROM catalog_performances",
                Long.class
        );
    }

    private Long nextActorId() {
        return jdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(id), 0) + 1 FROM catalog_actors",
                Long.class
        );
    }

    public record PerformanceRow(
            String title,
            CatalogPerformance.Genre genre,
            String venue,
            int runningMinutes,
            String posterUrl
    ) {
    }

    private record SessionRow(
            long id,
            LocalDateTime startsAt
    ) {
    }
}
