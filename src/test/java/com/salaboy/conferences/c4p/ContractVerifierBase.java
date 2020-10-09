package com.salaboy.conferences.c4p;

import io.restassured.RestAssured;
import io.zeebe.spring.util.ZeebeAutoStartUpLifecycle;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "server.port=0")
public abstract class ContractVerifierBase {

    @LocalServerPort
    int port;

    @MockBean
    private ZeebeAutoStartUpLifecycle zeebeAutoStartUpLifecycle;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        RestAssured.baseURI = "http://localhost:" + this.port;
    }
}
