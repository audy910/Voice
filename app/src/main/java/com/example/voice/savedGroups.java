package com.example.voice;

public class savedGroups {


    public String uid;
    public  String title;
    public String description;
    public String creator;

    public savedGroups(String uid, String title, String description, String creator) {
        this.uid = uid;
        this.title = title;
        this.description = description;
        this.creator = creator;
    }

    public savedGroups()
    {

    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }
}
