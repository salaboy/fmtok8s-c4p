package com.salaboy.conferences.c4p;

import com.salaboy.conferences.c4p.model.Proposal;
import com.salaboy.conferences.c4p.model.ProposalDecision;
import com.salaboy.conferences.c4p.model.ProposalStatus;
import io.zeebe.client.api.response.DeploymentEvent;
import io.zeebe.client.api.response.Workflow;
import io.zeebe.spring.client.ZeebeClientLifecycle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.*;

@RestController
@Slf4j
public class C4PController {

    private final Map<String, Long> proposalsWorkflowKeys = new HashMap<>();

    private final ZeebeClientLifecycle client;
    private final ProposalRepository proposalRepository;

    public C4PController(ZeebeClientLifecycle client, ProposalRepository proposalRepository) {
        this.client = client;
        this.proposalRepository = proposalRepository;
    }

    @EventListener(classes = {ApplicationReadyEvent.class})
    public void handleMultipleEvents() {
        log.info("C4P Service Started!");
        try {
            DeploymentEvent deploymentEvent = client.newDeployCommand().addResourceFromClasspath("c4p-orchestration.bpmn").send().join();
            log.info("Deploying Workflow... " + deploymentEvent.getKey());
            for (Workflow w : deploymentEvent.getWorkflows()) {
                log.info("> processId: " + w.getBpmnProcessId());
                log.info("> resourceName: " + w.getResourceName());
                log.info("> workflowKey: " + w.getWorkflowKey());
            }
        }catch(Exception e){
            log.error("Deploy Workflow Failed, needs retry...");
        }
    }

    @PostMapping
    public ResponseEntity<Proposal> newProposal(@RequestBody Proposal proposal) {

        var saved = proposalRepository.save(proposal);

        var workflowInstanceEvent = client.newCreateInstanceCommand()
                .bpmnProcessId("C4P")
                .latestVersion()
                .variables(Collections.singletonMap("proposal", saved))
                .send().join();

        proposalsWorkflowKeys.put(saved.getId(), workflowInstanceEvent.getWorkflowInstanceKey());

        emitEvent("> New Proposal Received Event ( " + proposal + ")");

        return ResponseEntity.ok().body(saved);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity deleteProposal(@PathVariable("id") String id) {

        var optionalProposal = proposalRepository.findById(id);

        if (optionalProposal.isPresent()) {

            proposalRepository.deleteById(id);

            return new ResponseEntity<>(HttpStatus.OK);

        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/")
    public ResponseEntity<Void> deleteProposals() {

        for(String proposalId : proposalsWorkflowKeys.keySet()) {
            log.info("Cancelling Proposal Id: " + proposalId + " with workflowInstanceKey: " + proposalsWorkflowKeys.get(proposalId));
            client.newCancelInstanceCommand(proposalsWorkflowKeys.get(proposalId)).send();
        }

        proposalRepository.deleteAll();

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping()
    public Set<Proposal> getAll(@RequestParam(value = "pending", defaultValue = "false", required = false) boolean pending) {

        if (!pending) {
            return new HashSet<>(proposalRepository.findAll());
        } else {
            return new HashSet<>(proposalRepository.findAllByStatus(ProposalStatus.PENDING));
        }
    }

    @GetMapping("/{id}")
    public Optional<Proposal> getById(@PathVariable("id") final String id) {

        return proposalRepository.findById(id);
    }

    @PostMapping(value = "/{id}/decision")
    public void decide(@PathVariable("id") String id, @RequestBody ProposalDecision decision) {

        emitEvent("> Proposal Approved Event ( " + ((decision.isApproved()) ? "Approved" : "Rejected") + ")");

        var proposalOptional = proposalRepository.findById(id);

        if (proposalOptional.isPresent()) {

            var proposal = proposalOptional.get();

            proposal.approve();

            proposalRepository.save(proposal);

            // Notify the workflow that a decision was made
            client.newPublishMessageCommand()
                    .messageName("DecisionMade").correlationKey(proposal.getId())
                    .variables(Collections.singletonMap("proposal", proposal)).send().join();
        } else {

            emitEvent(" Proposal Not Found Event (" + id + ")");
        }
    }

    private void emitEvent(String content) {
        log.info(content);
    }
}
