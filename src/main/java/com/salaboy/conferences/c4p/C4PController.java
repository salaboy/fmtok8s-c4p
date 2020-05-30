package com.salaboy.conferences.c4p;

import com.salaboy.conferences.c4p.model.Proposal;
import com.salaboy.conferences.c4p.model.ProposalDecision;
import com.salaboy.conferences.c4p.model.ProposalStatus;
import com.salaboy.conferences.c4p.services.AgendaService;
import com.salaboy.conferences.c4p.services.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;
import java.util.Set;

@RestController
@Slf4j
public class C4PController {

    @Value("${version:0.0.0}")
    private String version;

    @Autowired
    private ProposalStorageService proposalStorageService;

    @Autowired
    private AgendaService agendaService;

    @Autowired
    private EmailService emailService;

    @GetMapping("/info")
    public String infoWithVersion() {
        return "{ \"name\" : \"C4P Service - No Workflow\", \"version\" : \"" + version + "\", \"source\": \"https://github.com/salaboy/fmtok8s-c4p/releases/tag/v"+version+"\" }";
    }

    @PostMapping()
    public Proposal newProposal(@RequestBody Proposal proposal) {
        proposalStorageService.add(proposal);
        emitEvent("> New Proposal Received Event ( " + proposal + ")");
        return proposal;
    }

    @DeleteMapping("/{id}")
    public void deleteProposal(@PathVariable("id") String id) {
        proposalStorageService.delete(id);
    }

    @GetMapping()
    public Set<Proposal> getAll(@RequestParam(value = "pending", defaultValue = "false", required = false) boolean pending) {
        return proposalStorageService.getProposals(pending);

    }

    @GetMapping("/{id}")
    public Optional<Proposal> getById(@PathVariable("id") String id) {
        return proposalStorageService.getProposalById(id);
    }

    @PostMapping(value = "/{id}/decision")
    public void decide(@PathVariable("id") String id, @RequestBody ProposalDecision decision) {
        emitEvent("> Proposal Approved Event ( " + ((decision.isApproved()) ? "Approved" : "Rejected") + ")");
        Optional<Proposal> proposalOptional = proposalStorageService.getProposalById(id);
        if (proposalOptional.isPresent()) {
            Proposal proposal = proposalOptional.get();

            // Apply Decision to Proposal
            proposal.setApproved(decision.isApproved());
            proposal.setStatus(ProposalStatus.DECIDED);
            proposalStorageService.add(proposal);

//          Only if it is Approved create a new Agenda Item into the Agenda Service
            if (decision.isApproved()) {
                agendaService.createAgendaItem(proposal);
            }

            // Notify Potential Speaker By Email
            emailService.notifySpeakerByEmail(decision, proposal);
        } else {
            emitEvent(" Proposal Not Found Event (" + id + ")");
        }

    }

    private void emitEvent(String content) {
        log.info(content);
    }
}
