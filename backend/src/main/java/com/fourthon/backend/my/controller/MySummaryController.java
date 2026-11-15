
package com.fourthon.backend.my.controller;

import com.fourthon.backend.auth.resolver.CurrentUser;
import com.fourthon.backend.my.dto.MySummaryResponse;
import com.fourthon.backend.my.service.MySummaryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/api/my")
@RequiredArgsConstructor
@Tag(
        name = "마이페이지",
        description = "마이페이지 관련 API"
)
public class MySummaryController {

    private final MySummaryService mySummaryService;


    @GetMapping("/summary")
    @Operation(summary = "마이페이지 요약 조회")
    public MySummaryResponse getSummary(
            @Parameter(hidden = true)
            @CurrentUser Long userId
    ) {
        return mySummaryService.getSummary(userId);
    }

}
