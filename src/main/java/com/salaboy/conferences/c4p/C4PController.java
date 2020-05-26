package com.salaboy.conferences.c4p;

import com.salaboy.conferences.c4p.model.AgendaItem;
import com.salaboy.conferences.c4p.model.Proposal;
import com.salaboy.conferences.c4p.model.ProposalDecision;
import com.salaboy.conferences.c4p.model.ProposalStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import lombok.extern.slf4j.Slf4j;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class C4PController {

    @Value("${AGENDA_SERVICE:http://fmtok8s-agenda}")
    private String AGENDA_SERVICE;
    @Value("${EMAIL_SERVICE:http://fmtok8s-email}")
    private String EMAIL_SERVICE;

    @Value("${version:0.0.0}")
    private String version;

    private RestTemplate restTemplate = new RestTemplate();

    private Set<Proposal> proposals = new HashSet<>();



    @GetMapping("/info")
    public String infoWithVersion() {
        return "{ \"name\" : \"C4P Service - No Workflow\", \"version\" : \"" + version + "\", \"source\": \"https://github.com/salaboy/fmtok8s-c4p/releases/tag/v"+version+"\" }";
    }




    @PostMapping()
    public Proposal newProposal(@RequestBody Proposal proposal) {
        proposals.add(proposal);
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

//          Only if it is Approved create a new Agenda Item into the Agenda Service
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
        String[] days = {"Monday", "Tuesday"};
        String[] times = {"9:00 am", "10:00 am", "11:00 am", "1:00 pm", "2:00 pm", "3:00 pm", "4:00 pm", "5:00 pm"};
        Random random = new Random();
        int day = random.nextInt(2);
        int time = random.nextInt(8);
        HttpEntity<AgendaItem> requestAgenda = new HttpEntity<>(new AgendaItem(proposal.getTitle(), proposal.getAuthor(), days[day], times[time]));
        restTemplate.postForEntity(AGENDA_SERVICE, requestAgenda, String.class);
    }

    private void notifySpeakerByEmail(@RequestBody ProposalDecision decision, Proposal proposal) {
        emitEvent("> Notify Speaker Event (via email: " + proposal.getEmail() + " -> " + ((decision.isApproved()) ? "Approved" : "Rejected") + ")");
        HttpEntity<Proposal> requestEmail = new HttpEntity<>(proposal);
        restTemplate.postForEntity(EMAIL_SERVICE + "/notification", requestEmail, String.class);
    }

    private void emitEvent(String content) {
        log.info(content);
    }
}
