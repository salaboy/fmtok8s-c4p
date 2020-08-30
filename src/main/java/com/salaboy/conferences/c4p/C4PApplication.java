package com.salaboy.conferences.c4p;

import io.zeebe.spring.client.EnableZeebeClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableZeebeClient
public class C4PApplication {

    public static void main(String[] args) {
        SpringApplication.run(C4PApplication.class, args);
    }


}
