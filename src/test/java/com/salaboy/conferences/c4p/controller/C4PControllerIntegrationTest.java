package com.salaboy.conferences.c4p.controller;

import com.salaboy.conferences.c4p.C4PApplication;
import com.salaboy.conferences.c4p.ProposalRepository;
import com.salaboy.conferences.c4p.TestConfiguration;
import com.salaboy.conferences.c4p.model.Proposal;
import com.salaboy.conferences.c4p.model.ProposalDecision;
import com.salaboy.conferences.c4p.model.ProposalStatus;
import io.zeebe.spring.util.ZeebeAutoStartUpLifecycle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@ContextConfiguration(classes = C4PApplication.class)
@AutoConfigureWebTestClient
@Import(TestConfiguration.class)
public class C4PControllerIntegrationTest {


    @Autowired
    private ApplicationContext context;

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ProposalRepository proposalRepository;

    @MockBean
    private ZeebeAutoStartUpLifecycle zeebeAutoStartUpLifecycle;

    @BeforeEach
    public void beforeAll() {
        deleteProposals();
    }

    private Proposal createProposal() {

        var requestProposal =
                new Proposal("Title", "Description", "Author", "email@email.com");

        return webTestClient.post()
                .uri("/")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(requestProposal))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Proposal.class)
                .returnResult()
                .getResponseBody();
    }

    private List<Proposal> getAllProposals() {

        return webTestClient.get()
                .uri("/")
                .exchange()
                .expectBodyList(Proposal.class)
                .returnResult()
                .getResponseBody();
    }

    @Test
    public void newProposal_ShouldBeCreateAProposal() {

        // action
        var proposal = createProposal();

        // assert
        assertThat(proposal.getId()).isNotBlank().isNotNull().isNotEmpty();
    }

    @Test
    public void deleteProposal_ShouldDeleteProposalById() {

        // arrange
        var proposal = createProposal();

        // action, assert
        webTestClient.delete()
                .uri("/" + proposal.getId())
                .exchange()
                .expectStatus()
                .isOk();

        assertThatThereAreNotProposals();
    }

    @Test
    public void deleteProposals_ShouldDeleteAllProposals() {

        // arrange
        createProposal();
        createProposal();

        // action, assert
        deleteProposals().expectStatus().isOk();

        assertThatThereAreNotProposals();
    }

    @Test
    public void decide_ShouldBeDecidedProposal() {

        var proposal = createProposal();
        var decision = new ProposalDecision(true);

        webTestClient.post()
                .uri("/" + proposal.getId() + "/decision")
                .body(BodyInserters.fromValue(decision))
                .exchange()
                .expectStatus()
                .isOk();

        getAllProposals().forEach(item -> {
            assertThat(item.getStatus()).isEqualTo(ProposalStatus.DECIDED);
        });
    }

    private void assertThatThereAreNotProposals() {
        assertThat(getAllProposals()).hasSize(0);
    }

    private WebTestClient.ResponseSpec deleteProposals() {
        return webTestClient.delete()
                .uri("/")
                .exchange();
    }
}

