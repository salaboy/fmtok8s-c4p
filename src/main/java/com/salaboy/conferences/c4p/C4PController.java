package com.salaboy.conferences.c4p;

import com.salaboy.conferences.c4p.model.Proposal;
import com.salaboy.conferences.c4p.model.ProposalDecision;
import com.salaboy.conferences.c4p.model.ProposalStatus;
import io.zeebe.client.api.response.DeploymentEvent;
import io.zeebe.client.api.response.Workflow;
import io.zeebe.spring.client.ZeebeClientLifecycle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class C4PController {


    @Value("${version:0.0.0}")
    private String version;

    private RestTemplate restTemplate = new RestTemplate();

    private Set<Proposal> proposals = new HashSet<>();

    @Autowired
    private ZeebeClientLifecycle client;

    @GetMapping("/info")
    public String infoWithVersion() {
        return "{ \"name\" : \"C4P Service\", \"version\" : \"" + version + "\" }";
    }


    @EventListener(classes = {ApplicationReadyEvent.class})
    public void handleMultipleEvents() {
        log.info("C4P Service Started!");

        DeploymentEvent deploymentEvent = client.newDeployCommand().addResourceFromClasspath("c4p-orchestration.bpmn").send().join();
        log.info("Deploying Workflow... " + deploymentEvent.getKey());
        for(Workflow w : deploymentEvent.getWorkflows()){
            log.info("> processId: " + w.getBpmnProcessId());
            log.info("> resourceName: " + w.getResourceName());
            log.info("> workflowKey: " + w.getWorkflowKey());
        }
    }

    @PostMapping()
    public Proposal newProposal(@RequestBody Proposal proposal) {
        proposals.add(proposal);
        client.newCreateInstanceCommand()
                .bpmnProcessId("C4P")
                .latestVersion()
                .variables(Collections.singletonMap("proposal", proposal))
                .send();
        emitEvent("> New Proposal Received Event ( " + proposal + ")");
        return proposal;
    }

    @DeleteMapping("/{id}")
    public void deleteProposal(@PathVariable("id") String id) {
        proposals.stream().filter(p -> p.getId().equals(id)).findFirst().ifPresent(p -> proposals.remove(p));
    }

    @GetMapping()
    public Set<Proposal> getAll(@RequestParam(value = "pending", defaultValue = "false", required = false) boolean pending) {
        if (!pending) {
            return proposals;
        } else {
            return proposals.stream().filter(p -> p.getStatus().equals(ProposalStatus.PENDING)).collect(Collectors.toSet());
        }
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
            // Notify the workflow that a decision was made
            client.newPublishMessageCommand()
                    .messageName("DecisionMade").correlationKey(proposal.getId())
                    .variables(Collections.singletonMap("proposal", proposal)).send();
        } else {
            emitEvent(" Proposal Not Found Event (" + id + ")");
        }

    }

    private void emitEvent(String content) {
        log.info(content);
    }
}
