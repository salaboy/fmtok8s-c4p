package com.salaboy.conferences.c4p.services;

import com.salaboy.conferences.c4p.model.Proposal;
import com.salaboy.conferences.c4p.model.ProposalDecision;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class EmailService {

    @Value("${EMAIL_SERVICE:http://fmtok8s-email}")
    private String EMAIL_SERVICE;

    private RestTemplate restTemplate = new RestTemplate();


    private void emitEvent(String content) {
        log.info(content);
    }

    public void notifySpeakerByEmail(ProposalDecision decision, Proposal proposal) {
        emitEvent("> Notify Speaker Event (via email: " + proposal.getEmail() + " -> " + ((decision.isApproved()) ? "Approved" : "Rejected") + ")");
        HttpEntity<Proposal> requestEmail = new HttpEntity<>(proposal);
        restTemplate.postForEntity(EMAIL_SERVICE + "/notification", requestEmail, String.class);
    }
}
