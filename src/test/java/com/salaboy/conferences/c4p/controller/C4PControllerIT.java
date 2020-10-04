package com.salaboy.conferences.c4p.controller;

import com.salaboy.conferences.c4p.C4PController;
import com.salaboy.conferences.c4p.ProposalRepository;
import com.salaboy.conferences.c4p.model.Proposal;
import io.zeebe.client.ZeebeClientBuilder;
import io.zeebe.spring.client.ZeebeClientLifecycle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import static org.mockito.Answers.RETURNS_DEEP_STUBS;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = C4PController.class)
public class C4PControllerIT {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ProposalRepository proposalRepository;

    @MockBean(answer = RETURNS_DEEP_STUBS)
    private ZeebeClientLifecycle zeebeClientLifecycle;

    @MockBean(answer = RETURNS_DEEP_STUBS)
    private ZeebeClientBuilder zeebeClientBuilder;

    private WebTestClient.ResponseSpec createProposalRequest() {

        var requestProposal =
                new Proposal("Title", "Description", "Author", "email@email.com");

        Mockito.when(proposalRepository.save(requestProposal)).thenReturn(requestProposal);

        return webTestClient.post()
                .uri("/")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(requestProposal))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    public void newProposal_ShouldBeCreateAProposal() {

        // action
        var responseSpec = createProposalRequest();

        // assert
        responseSpec.expectBody(Proposal.class);
    }

    @Test
    public void deleteProposal_ShouldDeleteProposalById() {

        // arrange
        var responseSpec = createProposalRequest();
        var proposal = responseSpec.expectBody(Proposal.class).returnResult().getResponseBody();

        // action, assert
        webTestClient.delete()
                .uri("/" + proposal.getId())
                .exchange()
                .expectStatus().isOk();
    }
}
