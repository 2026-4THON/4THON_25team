
package com.fourthon.backend.my.service;

import com.fourthon.backend.my.dto.MySummaryResponse;
import com.fourthon.backend.my.repository.MyReviewSummaryRepository;
import com.fourthon.backend.my.repository.MyTicketSummaryRepository;
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

        // 2. 티켓 수와 지출 금액 집계
        var totals = ticketRepository
                .findTotalsByUserId(userId);

        long ticketCount = value(totals.getTicketCount());
        long ticketTotal = value(totals.getTicketTotal());
        long culturePassTotal =
                value(totals.getCulturePassTotal());
        long mdTotal = value(totals.getMdTotal());

        long totalSpending =
                ticketTotal - culturePassTotal + mdTotal;

        // 3. 후기 유형별 집계
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

        // 4. 장르별 티켓 개수
        // TODO: 실제 공연 장르 엔티티 연동
        // 현재는 임시로 모든 티켓을 기타에 포함
        MySummaryResponse.TicketCounts ticketCounts =
                new MySummaryResponse.TicketCounts(
                        0,
                        0,
                        ticketCount
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

        // 7. 취향 브리핑
        // 아직 분석 기능을 연결하지 않았으므로 빈 문자열
        String tasteBriefing = "";

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
