package com.fourthon.backend.internal.aop;

import com.fourthon.backend.internal.event.TicketChangeType;
import com.fourthon.backend.internal.event.TicketChangedEvent;
import com.fourthon.backend.ticket.dto.TicketResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class TicketChangeAspect implements MethodInterceptor {

    static final String CREATE = "create";
    static final String UPDATE = "update";
    static final String DELETE = "delete";

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Object result = invocation.proceed();
        try {
            toEvent(invocation.getMethod().getName(), invocation.getArguments(), result)
                    .ifPresent(eventPublisher::publishEvent);
        } catch (RuntimeException e) {
            log.error("티켓 변경 이벤트 발행 실패: {}", invocation.getMethod().getName(), e);
        }
        return result;
    }

    Optional<TicketChangedEvent> toEvent(String methodName, Object[] arguments, Object result) {
        if (CREATE.equals(methodName)
                && arguments.length == 2
                && arguments[0] instanceof Long userId
                && result instanceof TicketResponse.Saved saved) {
            return Optional.of(new TicketChangedEvent(userId, saved.id(), TicketChangeType.CREATED));
        }
        if (arguments.length >= 2
                && arguments[0] instanceof Long userId
                && arguments[1] instanceof Long ticketId) {
            if (UPDATE.equals(methodName)) {
                return Optional.of(new TicketChangedEvent(userId, ticketId, TicketChangeType.UPDATED));
            }
            if (DELETE.equals(methodName)) {
                return Optional.of(new TicketChangedEvent(userId, ticketId, TicketChangeType.DELETED));
            }
        }
        return Optional.empty();
    }
}
