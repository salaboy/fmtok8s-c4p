package com.salaboy.conferences.c4p;

import com.salaboy.conferences.c4p.model.AgendaItem;
import com.salaboy.conferences.c4p.model.Proposal;
import com.salaboy.conferences.c4p.model.ProposalDecision;
import com.salaboy.conferences.c4p.model.ProposalStatus;
// import io.zeebe.spring.client.EnableZeebeClient;
// import io.zeebe.spring.client.ZeebeClientLifecycle;
// import io.zeebe.spring.client.annotation.ZeebeDeployment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@SpringBootApplication
@RestController
// @EnableZeebeClient
// @ZeebeDeployment(classPathResource = "c4p-orchestration.bpmn")
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    private static final String AGENDA_SERVICE = "http://fmtok8s-agenda";
    private static final String EMAIL_SERVICE = "http://fmtok8s-email";

    @Value("${version:0.0.0}")
    private String version;

    private RestTemplate restTemplate = new RestTemplate();

    private Set<Proposal> proposals = new HashSet<>();

    // @Autowired
    // private ZeebeClientLifecycle client;

    @GetMapping("/info")
    public String infoWithVersion() {
        return "C4P v" + version;
    }


    @PostMapping()
    public void newProposal(@RequestBody Proposal proposal) {
        proposals.add(proposal);
        // client.newCreateInstanceCommand()
        //         .bpmnProcessId("C4P")
        //         .latestVersion()
        //         .variables(Collections.singletonMap("proposal", proposal))
        //         .send();
        emitEvent("> New Proposal Received Event ( " + proposal + ")");
    }

    @GetMapping()
    public Set<Proposal> getAll() {
        return proposals;
    }

    @GetMapping("/{id}")
    public Optional<Proposal> getById(@PathVariable("id") String id) {
        return proposals.stream().filter(p -> p.getId().equals(id)).findFirst();
    }

    @PostMapping(value = "/{id}/decision")
    public void decide(@PathVariable("id") String id, @RequestBody ProposalDecision decision) {
        emitEvent("> Proposal Approved Event ( " + ((decision.isApproved()) ? "Approved" : "Rejected") + ")");
        Optional<Proposal> proposalOptional = proposals.stream().filter(p -> p.getId().equals(id)).findFirst();
        if (proposalOptional.isPresent()) {
            Proposal proposal = proposalOptional.get();

            // Apply Decision to Proposal
            proposal.setApproved(decision.isApproved());
            proposal.setStatus(ProposalStatus.DECIDED);
            proposals.add(proposal);

            // client.newPublishMessageCommand()
            //         .messageName("DecisionMade").correlationKey(proposal.getId())
            //         .variables(Collections.singletonMap("proposal", proposal)).send();
           //Only if it is Approved create a new Agenda Item into the Agenda Service
           if (decision.isApproved()) {
               createAgendaItem(proposal);
           }

           // Notify Potential Speaker By Email
           notifySpeakerByEmail(decision, proposal);
        } else {
            emitEvent(" Proposal Not Found Event (" + id + ")");
        }

    }

   private void createAgendaItem(Proposal proposal) {
       emitEvent("> Add Proposal To Agenda Event ");
       HttpEntity<AgendaItem> requestAgenda = new HttpEntity<>(new AgendaItem(proposal.getTitle(), proposal.getAuthor(), new Date()));
       restTemplate.postForEntity(AGENDA_SERVICE, requestAgenda, String.class);
   }

   private void notifySpeakerByEmail(@RequestBody ProposalDecision decision, Proposal proposal) {
       emitEvent("> Notify Speaker Event (via email: " + proposal.getEmail() + " -> " + ((decision.isApproved()) ? "Approved" : "Rejected") + ")");
       HttpEntity<Proposal> requestEmail = new HttpEntity<>(proposal);
       restTemplate.postForEntity(EMAIL_SERVICE, requestEmail, String.class);
   }

    private void emitEvent(String content) {
        System.out.println(content);
    }

}
