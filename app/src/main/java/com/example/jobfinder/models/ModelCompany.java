package com.example.jobfinder.models;

public class ModelCompany {

    String id, company, uid;
    long timestamp;

    // constructor empty required for firebase
    public ModelCompany() {
    }

    // parametrized constructor
    public ModelCompany(String id, String company, String uid, long timestamp) {
        this.id = id;
        this.company = company;
        this.uid = uid;
        this.timestamp = timestamp;
    }

    /* Getters and Setters*/

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
