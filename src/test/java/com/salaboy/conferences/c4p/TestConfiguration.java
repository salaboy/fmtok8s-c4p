package com.salaboy.conferences.c4p;

import io.zeebe.spring.client.ZeebeClientLifecycle;
import org.mockito.Answers;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class TestConfiguration {

    @MockBean(answer = Answers.RETURNS_DEEP_STUBS)
    private ZeebeClientLifecycle clientLifecycle;

    @Bean
    @Primary
    public ZeebeClientLifecycle getClientLifecyle() {
        return clientLifecycle;
    }



}
