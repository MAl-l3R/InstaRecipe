package com.example.instarecipe.ui.recipe.comments;

import com.google.firebase.Timestamp;

public class Comment {
    private String comment;
    private String uid;
    private Timestamp timestamp;

    // Constructor
    public Comment(String comment, String uid, Timestamp timestamp) {
        this.comment = comment;
        this.uid = uid;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getComment() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUid() {
        return uid;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
