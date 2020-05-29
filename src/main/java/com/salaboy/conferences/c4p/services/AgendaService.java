package com.salaboy.conferences.c4p.services;

import com.salaboy.conferences.c4p.model.AgendaItem;
import com.salaboy.conferences.c4p.model.Proposal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Random;

@Service
@Slf4j
public class AgendaService {
    @Value("${AGENDA_SERVICE:http://fmtok8s-agenda}")
    private String AGENDA_SERVICE;

    private RestTemplate restTemplate = new RestTemplate();

    public void createAgendaItem(Proposal proposal) {
        emitEvent("> Add Proposal To Agenda Event ");
        String[] days = {"Monday", "Tuesday"};
        String[] times = {"9:00 am", "10:00 am", "11:00 am", "1:00 pm", "2:00 pm", "3:00 pm", "4:00 pm", "5:00 pm"};
        Random random = new Random();
        int day = random.nextInt(2);
        int time = random.nextInt(8);
        HttpEntity<AgendaItem> requestAgenda = new HttpEntity<>(new AgendaItem(proposal.getTitle(), proposal.getAuthor(), days[day], times[time]));
        restTemplate.postForEntity(AGENDA_SERVICE, requestAgenda, String.class);
    }

    private void emitEvent(String content) {
        log.info(content);
    }
}
