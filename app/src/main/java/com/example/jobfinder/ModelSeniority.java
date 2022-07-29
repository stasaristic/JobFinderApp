package com.example.jobfinder;

public class ModelSeniority {
    String id, seniority, uid;
    long timestamp;

    public ModelSeniority() {
    }

    public ModelSeniority(String id, String seniority, String uid, long timestamp) {
        this.id = id;
        this.seniority = seniority;
        this.uid = uid;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSeniority() {
        return seniority;
    }

    public void setSeniority(String seniority) {
        this.seniority = seniority;
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
