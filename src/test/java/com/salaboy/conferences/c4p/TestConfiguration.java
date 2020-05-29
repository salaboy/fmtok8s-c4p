package com.salaboy.conferences.c4p;

import com.salaboy.conferences.c4p.services.AgendaService;
import com.salaboy.conferences.c4p.services.EmailService;
import org.mockito.Answers;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class TestConfiguration {

    @MockBean
    private AgendaService agendaService;

    @MockBean
    private EmailService emailService;

    @Bean
    @Primary
    public EmailService getEmailService() {
        return emailService;
    }

    @Bean
    @Primary
    public AgendaService getAgendaService() {
        return agendaService;
    }

}
