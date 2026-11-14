package com.fourthon.backend.ticket.controller;
import com.fourthon.backend.ticket.dto.CatalogResponse;
import com.fourthon.backend.ticket.service.CatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;



@Tag(name="공연·회차 조회 API",description="티켓 등록 1단계 공연 검색, 날짜/회차, 상세 및 캐스팅")
@RestController @RequestMapping("/api/performances")
public class CatalogController {
    private final CatalogService catalog;
    public CatalogController(CatalogService catalog){this.catalog=catalog;}

    @Operation(summary="공연 검색") @GetMapping
    public List<CatalogResponse.Performance> search(@RequestParam(defaultValue="") String keyword){return catalog.search(keyword);}

    @Operation(summary="관람 가능한 날짜 조회") @GetMapping("/{id}/dates")
    public CatalogResponse.Dates dates(@PathVariable Long id){return catalog.dates(id);}

    @Operation(summary="날짜별 공연 회차 시간 조회") @GetMapping("/{id}/sessions")
    public List<CatalogResponse.Session> sessions(@PathVariable Long id,@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate date){return catalog.sessions(id,date);}

    @Operation(summary="공연장·러닝타임·회차 캐스팅 조회") @GetMapping("/{id}/details")
    public CatalogResponse.Details details(@PathVariable Long id,@RequestParam Long sessionId){return catalog.details(id,sessionId);}
}
