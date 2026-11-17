
package com.fourthon.backend.my.service;

import com.fourthon.backend.my.dto.MySummaryResponse;
import com.fourthon.backend.my.repository.MyReviewSummaryRepository;
import com.fourthon.backend.my.repository.MyTicketSummaryRepository;
import com.fourthon.backend.taste.service.TasteService;
import com.fourthon.backend.user.entity.User;
import com.fourthon.backend.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class MySummaryService {

    private final UserRepository userRepository;
    private final MyTicketSummaryRepository ticketRepository;
    private final MyReviewSummaryRepository reviewRepository;
    private final TasteService tasteService;

    @Transactional(readOnly = true)
    public MySummaryResponse getSummary(Long userId) {

        // 1. 로그인 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "사용자를 찾을 수 없습니다."
                        )
                );

        String nickname = user.getName() == null
                ? ""
                : user.getName();

        // 2. 티켓 수 및 지출 합계
        var totals = ticketRepository.findTotalsByUserId(userId);

        long ticketTotal = value(totals.getTicketTotal());
        long culturePassTotal =
                value(totals.getCulturePassTotal());
        long mdTotal = value(totals.getMdTotal());

        long totalSpending =
                ticketTotal - culturePassTotal + mdTotal;

        // 3. 후기 유형별 개수
        long performanceCount = 0;
        long actorCount = 0;
        long pairCount = 0;

        for (Object[] result :
                reviewRepository.countByTypeForUser(userId)) {

            String type = result[0].toString();
            long count = ((Number) result[1]).longValue();

            switch (type) {
                case "PERFORMANCE" ->
                        performanceCount = count;

                case "ACTOR" ->
                        actorCount = count;

                case "PAIR" ->
                        pairCount = count;
            }
        }

        // 4. 실제 공연 장르별 티켓 개수
        long musicalCount = 0;
        long playCount = 0;
        long otherCount = 0;

        for (Object[] result :
                ticketRepository.countTicketsByGenre(userId)) {

            String genre = result[0] == null
                    ? "ETC"
                    : result[0].toString();

            long count = ((Number) result[1]).longValue();

            switch (genre) {
                case "MUSICAL" ->
                        musicalCount += count;

                case "PLAY" ->
                        playCount += count;

                default ->
                        otherCount += count;
            }
        }

        MySummaryResponse.TicketCounts ticketCounts =
                new MySummaryResponse.TicketCounts(
                        musicalCount,
                        playCount,
                        otherCount
                );

        // 5. 후기 개수
        MySummaryResponse.ReviewCounts reviewCounts =
                new MySummaryResponse.ReviewCounts(
                        performanceCount,
                        actorCount,
                        pairCount
                );

        // 6. 지출 합계
        MySummaryResponse.Expenses expenses =
                new MySummaryResponse.Expenses(
                        ticketTotal,
                        culturePassTotal,
                        mdTotal,
                        totalSpending
                );

        // 7. 취향 브리핑 조회
        // 취향 리포트가 없는 사용자는 빈 문자열 반환
        String tasteBriefing = tasteService
                .findBriefing(userId)
                .orElse("");

        return new MySummaryResponse(
                nickname,
                tasteBriefing,
                ticketCounts,
                reviewCounts,
                expenses
        );
    }

    private long value(Number number) {
        return number == null ? 0L : number.longValue();
    }
}
