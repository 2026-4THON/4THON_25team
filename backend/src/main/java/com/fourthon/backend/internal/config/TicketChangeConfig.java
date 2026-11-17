package com.fourthon.backend.internal.config;

import com.fourthon.backend.internal.aop.TicketChangeAspect;
import com.fourthon.backend.ticket.service.TicketService;

import org.springframework.aop.Advisor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.NameMatchMethodPointcut;
import org.springframework.aop.support.RootClassFilter;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.core.Ordered;

@Configuration
public class TicketChangeConfig {

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public static Advisor ticketChangeAdvisor(ApplicationEventPublisher eventPublisher) {
        NameMatchMethodPointcut pointcut = new NameMatchMethodPointcut();
        pointcut.setMappedNames("create", "update", "delete");
        pointcut.setClassFilter(new RootClassFilter(TicketService.class));
        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor(pointcut, new TicketChangeAspect(eventPublisher));
        advisor.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return advisor;
    }
}
