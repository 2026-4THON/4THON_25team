package com.fourthon.backend.internal.aop;

import com.fourthon.backend.internal.event.TicketChangeType;
import com.fourthon.backend.internal.event.TicketChangedEvent;
import com.fourthon.backend.ticket.dto.TicketResponse;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class TicketChangeAspectTest {

    private final TicketChangeAspect aspect = new TicketChangeAspect(mock(ApplicationEventPublisher.class));

    @Test
    void 등록은_반환된_티켓_ID로_이벤트를_만든다() {
        TicketResponse.Saved saved = new TicketResponse.Saved(10L, 1, "티켓 등록이 완료되었습니다");

        assertThat(aspect.toEvent("create", new Object[]{1L, new Object()}, saved))
                .contains(new TicketChangedEvent(1L, 10L, TicketChangeType.CREATED));
    }

    @Test
    void 수정과_삭제는_인자의_티켓_ID로_이벤트를_만든다() {
        assertThat(aspect.toEvent("update", new Object[]{1L, 10L, new Object()}, null))
                .contains(new TicketChangedEvent(1L, 10L, TicketChangeType.UPDATED));
        assertThat(aspect.toEvent("delete", new Object[]{1L, 10L}, null))
                .contains(new TicketChangedEvent(1L, 10L, TicketChangeType.DELETED));
    }

    @Test
    void 다른_메서드는_이벤트를_만들지_않는다() {
        assertThat(aspect.toEvent("list", new Object[]{1L, true}, null)).isEmpty();
    }
}
