package com.fourthon.backend.internal.controller;

import com.fourthon.backend.internal.dto.InternalResponse;
import com.fourthon.backend.kopis.service.KopisSyncService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Tag(
        name = "관리용 API",
        description = "동기화 수동 실행. X-Admin-Key 헤더 필요"
)
@RestController
@RequestMapping("/internal/batch")
@RequiredArgsConstructor
public class InternalBatchController {

    private final KopisSyncService kopisSyncService;

    @Operation(summary = "KOPIS 동기화 수동 실행")
    @PostMapping("/kopis-sync")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public InternalResponse.Started runKopisSync() {
        if (kopisSyncService.isRunning()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "KOPIS 동기화가 이미 실행 중입니다"
            );
        }
        kopisSyncService.runDailyInBackground();
        return new InternalResponse.Started("KOPIS 동기화를 시작했습니다");
    }
}
