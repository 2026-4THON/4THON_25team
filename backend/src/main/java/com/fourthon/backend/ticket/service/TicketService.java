
package com.fourthon.backend.ticket.service;

import com.fourthon.backend.ticket.domain.*;
import com.fourthon.backend.ticket.dto.*;
import com.fourthon.backend.ticket.repository.*;
import com.fourthon.backend.user.entity.User;
import com.fourthon.backend.user.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TicketService {

    private final TicketRepository tickets;
    private final UserRepository users;
    private final CatalogService catalog;
    private final CatalogSessionCastRepository sessionCasts;

    public TicketService(
            TicketRepository tickets,
            UserRepository users,
            CatalogService catalog,
            CatalogSessionCastRepository sessionCasts
    ) {
        this.tickets = tickets;
        this.users = users;
        this.catalog = catalog;
        this.sessionCasts = sessionCasts;
    }

    private ResponseStatusException bad(String message) {
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message
        );
    }

    private User requireUser(Long userId) {
        return users.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "로그인 회원을 찾을 수 없습니다"
                        )
                );
    }

    // 본인 티켓만 조회, 수정, 삭제 가능
    private Ticket owned(Long userId, Long ticketId) {
        return tickets.findByIdAndUser_Id(ticketId, userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "티켓을 찾을 수 없습니다"
                        )
                );
    }

    // 해당 회차의 출연 배우인지 확인
    private Map<Long, CatalogActor> validateAndLoadActors(
            TicketRequest req,
            CatalogSession session
    ) {
        List<Long> selected = req.castingActorIds();

        if (selected.isEmpty()) {
            throw bad("캐스팅을 선택해 주세요");
        }

        if (new HashSet<>(selected).size() != selected.size()) {
            throw bad("캐스팅 배우를 중복 선택할 수 없습니다");
        }

        Map<Long, CatalogActor> eligible =
                sessionCasts
                        .findBySession_IdOrderByCastOrderAsc(
                                session.getId()
                        )
                        .stream()
                        .map(CatalogSessionCast::getActor)
                        .collect(Collectors.toMap(
                                CatalogActor::getId,
                                Function.identity(),
                                (a, b) -> a
                        ));

        for (Long actorId : selected) {
            if (!eligible.containsKey(actorId)) {
                throw bad(
                        "해당 회차의 캐스팅에 없는 배우입니다: "
                                + actorId
                );
            }
        }

        return eligible;
    }

    // 티켓 입력값 검증
    private void validate(TicketRequest req) {

        if (req.culturePassAmount() > req.ticketPrice()) {
            throw bad(
                    "문화패스 사용액은 티켓 가격을 초과할 수 없습니다"
            );
        }

        if (req.rating().compareTo(
                new BigDecimal("0.5")) < 0
                || req.rating().compareTo(
                new BigDecimal("5.0")) > 0
                || req.rating()
                .multiply(BigDecimal.valueOf(2))
                .stripTrailingZeros()
                .scale() > 0) {

            throw bad(
                    "별점은 0.5부터 5.0까지 "
                            + "0.5점 단위여야 합니다"
            );
        }
    }

    // 후기 종류 + 배우 조합으로 기존 후기를 식별
    private record ReviewKey(
            TicketReview.ReviewType type,
            List<Long> actorIds
    ) {}

    private ReviewKey reviewKey(
            TicketReview.ReviewType type,
            Collection<Long> actorIds
    ) {
        return new ReviewKey(
                type,
                actorIds.stream()
                        .sorted()
                        .toList()
        );
    }

    private ReviewKey reviewKey(TicketReview review) {
        return reviewKey(
                review.getType(),
                review.getActors()
                        .stream()
                        .map(actor ->
                                actor.getActor().getId()
                        )
                        .toList()
        );
    }

    // 동일한 후기는 기존 ID를 유지하면서 갱신
    private void putReview(
            Ticket ticket,
            Map<ReviewKey, TicketReview> existing,
            Set<ReviewKey> kept,
            TicketReview.ReviewType type,
            String content,
            List<Long> actorIds,
            Map<Long, CatalogActor> eligible
    ) {
        ReviewKey key = reviewKey(type, actorIds);

        if (!kept.add(key)) {
            throw bad("같은 후기를 중복 작성할 수 없습니다");
        }

        TicketReview review = existing.get(key);

        if (review == null) {
            // 새 후기일 때만 신규 생성
            review = new TicketReview(
                    type,
                    content.trim()
            );

            for (int i = 0; i < actorIds.size(); i++) {
                review.addActor(
                        eligible.get(actorIds.get(i)),
                        i
                );
            }

            ticket.addReview(review);

        } else {
            // 기존 후기를 재사용하여 ID와 좋아요 유지
            review.changeContent(content.trim());

            review.replaceActors(
                    actorIds.stream()
                            .map(eligible::get)
                            .toList()
            );
        }
    }

    // 등록 및 수정 시 티켓 데이터 반영
    private void fill(
            Ticket ticket,
            TicketRequest req
    ) {
        validate(req);

        CatalogPerformance performance =
                catalog.requirePerformance(
                        req.performanceId()
                );

        CatalogSession session =
                catalog.requireSession(
                        req.performanceId(),
                        req.sessionId()
                );

        Map<Long, CatalogActor> eligible =
                validateAndLoadActors(req, session);

        Set<Long> selected =
                new HashSet<>(req.castingActorIds());

        // 배우 후기 검증
        Set<Long> reviewedActors = new HashSet<>();

        if (req.actorReviews() != null) {
            for (TicketRequest.ActorReviewInput input
                    : req.actorReviews()) {

                if (!selected.contains(input.actorId())) {
                    throw bad(
                            "배우 후기는 선택한 캐스팅 배우만 "
                                    + "작성할 수 있습니다"
                    );
                }

                if (!reviewedActors.add(input.actorId())) {
                    throw bad(
                            "배우 후기는 배우별로 "
                                    + "한 개만 작성할 수 있습니다"
                    );
                }
            }
        }

        // 페어 후기 검증
        Set<String> seenPairs = new HashSet<>();

        if (req.pairReviews() != null) {
            for (TicketRequest.PairReviewInput input
                    : req.pairReviews()) {

                List<Long> ids = input.actorIds();

                if (ids.size() < 2) {
                    throw bad(
                            "페어 후기는 배우를 2명 이상 "
                                    + "선택해야 합니다"
                    );
                }

                if (new HashSet<>(ids).size() != ids.size()) {
                    throw bad(
                            "같은 배우를 페어에 "
                                    + "중복 지정할 수 없습니다"
                    );
                }

                if (!selected.containsAll(ids)) {
                    throw bad(
                            "페어 후기는 선택한 캐스팅 배우만 "
                                    + "작성할 수 있습니다"
                    );
                }

                String pairKey = ids.stream()
                        .sorted()
                        .map(String::valueOf)
                        .collect(Collectors.joining("-"));

                if (!seenPairs.add(pairKey)) {
                    throw bad(
                            "동일한 배우 조합의 페어 후기는 "
                                    + "한 개만 작성할 수 있습니다"
                    );
                }
            }
        }

        // 기존 후기 확보
        Map<ReviewKey, TicketReview> existing =
                new HashMap<>();

        for (TicketReview review : ticket.getReviews()) {
            existing.put(
                    reviewKey(review),
                    review
            );
        }

        Set<ReviewKey> kept = new HashSet<>();

        // 티켓 기본 정보 갱신
        ticket.setIdentity(
                ticket.getUser(),
                performance,
                session
        );

        ticket.setDetails(
                req.seatFloor(),
                req.seatRow(),
                req.seatNumber(),
                req.ticketPrice(),
                req.culturePassAmount(),
                req.mdPrice(),
                req.companion(),
                req.companionRating(),
                req.rating()
                        .multiply(BigDecimal.valueOf(2))
                        .intValueExact()
        );

        // 캐스팅 정보 갱신
        ticket.replaceCastings();

        for (int i = 0;
             i < req.castingActorIds().size();
             i++) {

            ticket.addCasting(
                    new TicketCasting(
                            eligible.get(
                                    req.castingActorIds().get(i)
                            ),
                            i
                    )
            );
        }

        // 공연 후기 갱신
        if (req.performanceReview() != null
                && !req.performanceReview().isBlank()) {

            putReview(
                    ticket,
                    existing,
                    kept,
                    TicketReview.ReviewType.PERFORMANCE,
                    req.performanceReview(),
                    List.of(),
                    eligible
            );
        }

        // 배우별 후기 갱신
        if (req.actorReviews() != null) {
            for (TicketRequest.ActorReviewInput input
                    : req.actorReviews()) {

                putReview(
                        ticket,
                        existing,
                        kept,
                        TicketReview.ReviewType.ACTOR,
                        input.content(),
                        List.of(input.actorId()),
                        eligible
                );
            }
        }

        // 페어 후기 갱신
        if (req.pairReviews() != null) {
            for (TicketRequest.PairReviewInput input
                    : req.pairReviews()) {

                putReview(
                        ticket,
                        existing,
                        kept,
                        TicketReview.ReviewType.PAIR,
                        input.content(),
                        input.actorIds(),
                        eligible
                );
            }
        }

        // 수정 요청에서 제외된 후기만 삭제
        for (TicketReview review
                : new ArrayList<>(ticket.getReviews())) {

            if (!kept.contains(reviewKey(review))) {
                ticket.removeReview(review);
            }
        }
    }

    // 티켓 등록
    @Transactional
    public TicketResponse.Saved create(
            Long userId,
            TicketRequest request
    ) {
        Ticket ticket = new Ticket();

        ticket.setIdentity(
                requireUser(userId),
                catalog.requirePerformance(
                        request.performanceId()
                ),
                catalog.requireSession(
                        request.performanceId(),
                        request.sessionId()
                )
        );

        fill(ticket, request);

        tickets.saveAndFlush(ticket);

        return new TicketResponse.Saved(
                ticket.getId(),
                rank(userId, ticket.getId()),
                "티켓 등록이 완료되었습니다"
        );
    }

    // 티켓 수정
    @Transactional
    public TicketResponse.Saved update(
            Long userId,
            Long ticketId,
            TicketRequest request
    ) {
        Ticket ticket = owned(userId, ticketId);

        fill(ticket, request);

        tickets.flush();

        return new TicketResponse.Saved(
                ticket.getId(),
                rank(userId, ticket.getId()),
                "티켓 수정이 완료되었습니다"
        );
    }

    // 본인 티켓만 삭제
    @Transactional
    public void delete(
            Long userId,
            Long ticketId
    ) {
        tickets.delete(
                owned(userId, ticketId)
        );
    }

    // 관극 일시 기준 티켓 번호 계산
    private int rank(Long userId, Long ticketId) {

        List<Ticket> ordered =
                tickets.findByUser_IdOrderByViewedAtAscIdAsc(
                        userId
                );

        for (int i = 0; i < ordered.size(); i++) {
            if (ordered.get(i).getId().equals(ticketId)) {
                return i + 1;
            }
        }

        throw new IllegalStateException(
                "티켓 번호를 찾을 수 없습니다"
        );
    }

    // 나의 티켓 목록
    @Transactional(readOnly = true)
    public List<TicketResponse.Summary> list(
            Long userId,
            boolean newest
    ) {
        List<Ticket> ordered = new ArrayList<>(
                tickets.findByUser_IdOrderByViewedAtAscIdAsc(
                        userId
                )
        );

        Map<Long, Integer> ranks = new HashMap<>();

        for (int i = 0; i < ordered.size(); i++) {
            ranks.put(
                    ordered.get(i).getId(),
                    i + 1
            );
        }

        if (newest) {
            Collections.reverse(ordered);
        }

        return ordered.stream()
                .map(t -> new TicketResponse.Summary(
                        t.getId(),
                        ranks.get(t.getId()),
                        t.getPerformance().getId(),
                        t.getPerformance().getTitle(),
                        t.getPerformance().getGenre().name(),
                        t.getPerformance().getVenue(),
                        t.getPerformance().getPosterUrl(),
                        t.getViewedAt()
                ))
                .toList();
    }

    // 티켓 상세 조회
    @Transactional(readOnly = true)
    public TicketResponse.Detail detail(
            Long userId,
            Long ticketId
    ) {
        Ticket t = owned(userId, ticketId);

        List<TicketResponse.Actor> castings =
                t.getCastings()
                        .stream()
                        .map(c -> new TicketResponse.Actor(
                                c.getActor().getId(),
                                c.getActor().getName()
                        ))
                        .toList();

        List<TicketResponse.Review> reviews =
                t.getReviews()
                        .stream()
                        .sorted(Comparator.comparing(
                                r -> r.getType().ordinal()
                        ))
                        .map(r -> new TicketResponse.Review(
                                r.getId(),
                                r.getType().name(),
                                r.getContent(),
                                r.getActors()
                                        .stream()
                                        .map(ra ->
                                                new TicketResponse.Actor(
                                                        ra.getActor().getId(),
                                                        ra.getActor().getName()
                                                )
                                        )
                                        .toList()
                        ))
                        .toList();

        CatalogPerformance p = t.getPerformance();

        return new TicketResponse.Detail(
                t.getId(),
                rank(userId, ticketId),
                p.getId(),
                t.getSession().getId(),
                p.getTitle(),
                p.getGenre().name(),
                p.getVenue(),
                p.getPosterUrl(),
                p.getRunningMinutes(),
                t.getViewedAt(),
                t.getSeatFloor(),
                t.getSeatRow(),
                t.getSeatNumber(),
                t.getCompanion(),
                t.getCompanionRating(),
                castings,
                t.getTicketPrice(),
                t.getCulturePassAmount(),
                t.getMdPrice(),
                t.totalExpense(),
                BigDecimal.valueOf(t.getRatingHalfUnits())
                        .divide(BigDecimal.valueOf(2)),
                reviews
        );
    }
}
