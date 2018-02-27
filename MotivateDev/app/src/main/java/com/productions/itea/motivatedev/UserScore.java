package com.productions.itea.motivatedev;


import android.net.Uri;

public class UserScore {
    public String username;
    public Integer score;
    public String photoUrl;

    public UserScore() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    UserScore (String name, Integer score, Uri photoUrl) {
        this.username = name;
        this.score = score;
        this.photoUrl = photoUrl != null ? photoUrl.toString() : "";
    }
    UserScore (String name, Integer score, String photoUrl) {
        this.username = name;
        this.score = score;
        this.photoUrl = photoUrl;
    }
}
