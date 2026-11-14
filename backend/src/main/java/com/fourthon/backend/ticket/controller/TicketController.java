package com.fourthon.backend.ticket.controller;
import com.fourthon.backend.auth.resolver.CurrentUser;
import com.fourthon.backend.ticket.dto.TicketRequest;
import com.fourthon.backend.ticket.dto.TicketResponse;
import com.fourthon.backend.ticket.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@Tag(name="티켓·후기 API",description="관람 기록 등록, 후기, 조회, 수정, 삭제")
@RestController @RequestMapping("/api/tickets")
public class TicketController {
    private final TicketService tickets;

    public TicketController(TicketService tickets){this.tickets=tickets;}
    @Operation(summary="티켓 및 후기 등록")
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public TicketResponse.Saved create(@Parameter(hidden=true) @CurrentUser Long userId,
        @Valid @RequestBody TicketRequest request){return tickets.create(userId,request);}


    @Operation(summary="나의 관람 기록 목록 (관극일시 순)")
    @GetMapping
    public List<TicketResponse.Summary> list(@Parameter(hidden=true) @CurrentUser Long userId,
        @RequestParam(defaultValue="latest") String sort){
        if(!sort.equals("latest")&&!sort.equals("oldest")) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST,"sort는 latest 또는 oldest입니다");
        return tickets.list(userId,sort.equals("latest"));
    }


    @Operation(summary="관람 기록 상세 및 후기 조회")
    @GetMapping("/{id}")
    public TicketResponse.Detail detail(@Parameter(hidden=true) @CurrentUser Long userId,@PathVariable Long id){return tickets.detail(userId,id);}


    @Operation(summary="관람 기록 및 후기 전체 수정")
    @PutMapping("/{id}")
    public TicketResponse.Saved update(@Parameter(hidden=true) @CurrentUser Long userId,
        @PathVariable Long id,@Valid @RequestBody TicketRequest request){return tickets.update(userId,id,request);}


    @Operation(summary="관람 기록 및 연결된 후기·좋아요 삭제")
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@Parameter(hidden=true) @CurrentUser Long userId,@PathVariable Long id){tickets.delete(userId,id);}
}
