package com.salaboy.conferences.c4p.model;

public class ProposalDecision {
    private boolean approved;
    private String dayTime;


    public ProposalDecision() {
    }

    public ProposalDecision(boolean approved, String dayTime) {
        this.approved = approved;
        this.dayTime = dayTime;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public String getDayTime() {
        return dayTime;
    }

    public void setDayTime(String dayTime) {
        this.dayTime = dayTime;
    }

    @Override
    public String toString() {
        return "ProposalDecision{" +
                "approved=" + approved +
                ", dayTime='" + dayTime + '\'' +
                '}';
    }
}
