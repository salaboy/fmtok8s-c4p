package com.salaboy.conferences.c4p;

import com.salaboy.conferences.c4p.model.Proposal;
import io.restassured.RestAssured;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "server.port=0")
public abstract class ContractVerifierBase {

    @LocalServerPort
    int port;

    @Autowired
    private ProposalStorageService proposalStorageService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        RestAssured.baseURI = "http://localhost:" + this.port;
        Proposal proposal = new Proposal();
        proposal.setId("ABC");
        proposal.setAuthor("salaboy");
        proposal.setTitle("My proposal");
        proposal.setDescription("some description here");
        proposalStorageService.add(proposal);

    }




}
