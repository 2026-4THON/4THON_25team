package com.fourthon.backend.map.controller;

import com.fourthon.backend.auth.resolver.CurrentUser;
import com.fourthon.backend.map.domain.MapMode;
import com.fourthon.backend.map.dto.MapResponse;
import com.fourthon.backend.map.service.MapService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(
        name = "지도 API",
        description = "공연장 지도와 공연장별 작품·관람 기록 조회"
)
@RestController
@RequestMapping("/api/map/venues")
@RequiredArgsConstructor
public class MapController {

    private final MapService mapService;

    @Operation(
            summary = "공연장 목록",
            description = "NOW는 진행중 공연이 있는 공연장, MINE은 내가 관람한 공연장. keyword로 공연장명 부분 일치 검색"
    )
    @GetMapping
    public List<MapResponse.VenueItem> findVenues(
            @Parameter(hidden = true) @CurrentUser Long userId,
            @RequestParam(defaultValue = "NOW") MapMode mode,
            @RequestParam(required = false) String keyword) {

        return mapService.findVenues(mode, keyword, userId);
    }

    @Operation(summary = "공연장의 진행중 작품")
    @GetMapping("/{venueId}/shows")
    public List<MapResponse.VenueShow> findOngoingShows(@PathVariable Long venueId) {
        return mapService.findOngoingShows(venueId);
    }

    @Operation(summary = "공연장의 내 관람 기록")
    @GetMapping("/{venueId}/my-tickets")
    public List<MapResponse.MyTicket> findMyTickets(
            @Parameter(hidden = true) @CurrentUser Long userId,
            @PathVariable Long venueId) {

        return mapService.findMyTickets(userId, venueId);
    }
}
