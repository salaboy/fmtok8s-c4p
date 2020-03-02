package com.salaboy.conferences.c4p.model;

public class ProposalDecision {
    private boolean approved;


    public ProposalDecision() {
    }

    public ProposalDecision(boolean approved) {
        this.approved = approved;

    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    @Override
    public String toString() {
        return "ProposalDecision{" +
                "approved=" + approved +
                '}';
    }
}
