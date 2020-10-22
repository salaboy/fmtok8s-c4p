package com.salaboy.conferences.c4p.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Objects;

@Entity(name = "proposal")
public class Proposal {

    @Id
    @GenericGenerator(name = "system-uuid", strategy = "uuid2")
    @GeneratedValue(generator = "system-uuid")
    private String id;
    private String title;
    private String description;
    private String author;
    private String email;
    private boolean approved = false;
    private ProposalStatus status = ProposalStatus.PENDING;

    protected Proposal() {
    }

    public Proposal(String title, String description, String author, String email) {
        this.title = title;
        this.description = description;
        this.author = author;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getAuthor() {
        return author;
    }

    public String getEmail() {
        return email;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public ProposalStatus getStatus() {
        return status;
    }

    public void approve() {
        this.approved = true;
        this.changeStatusToDecided();
    }

    public void reject() {
        this.approved = false;
        this.changeStatusToDecided();
    }

    private void changeStatusToDecided() {
        this.status = ProposalStatus.DECIDED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Proposal proposal = (Proposal) o;
        return Objects.equals(id, proposal.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Proposal{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", author='" + author + '\'' +
                ", email='" + email + '\'' +
                ", approved=" + approved + '\'' +
                ", status=" + status + '\'' +
                '}';
    }
}
