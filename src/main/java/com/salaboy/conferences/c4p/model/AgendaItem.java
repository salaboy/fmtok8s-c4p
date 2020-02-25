package com.salaboy.conferences.c4p.model;

import java.util.Date;

public class AgendaItem {

    private String title;
    private String author;
    private Date talkTime;

    public AgendaItem(String title, String author, Date talkTime) {
        this.title = title;
        this.author = author;
        this.talkTime = talkTime;
    }


    public AgendaItem() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Date getTalkTime() {
        return talkTime;
    }

    public void setTalkTime(Date talkTime) {
        this.talkTime = talkTime;
    }
}
