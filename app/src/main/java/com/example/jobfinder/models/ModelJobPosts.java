package com.example.jobfinder.models;

public class ModelJobPosts {
    // variables
    String uid, id, title, description, companyId, categoryId, typeId, seniorityId;
    String url;
    long timestamp;
    boolean interested;

    // constructors
    // empty constructor, required for firebase
    public ModelJobPosts() {
    }
    // constructor with parameters

    public ModelJobPosts(String uid, String id, String title, String description,
                         String companyId, String categoryId, String typeId,
                         String seniorityId, String url, long timestamp, boolean interested) {
        this.uid = uid;
        this.id = id;
        this.title = title;
        this.description = description;
        this.companyId = companyId;
        this.categoryId = categoryId;
        this.typeId = typeId;
        this.seniorityId = seniorityId;
        this.url = url;
        this.timestamp = timestamp;
        this.interested = interested;
    }


    /* Getters/Setter */

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public String getSeniorityId() {
        return seniorityId;
    }

    public void setSeniorityId(String seniorityId) {
        this.seniorityId = seniorityId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isInterested() {
        return interested;
    }

    public void setInterested(boolean interested) {
        this.interested = interested;
    }
}
