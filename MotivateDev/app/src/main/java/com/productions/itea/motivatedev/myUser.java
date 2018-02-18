package com.productions.itea.motivatedev;

import android.net.Uri;

import java.util.HashMap;

public class myUser {
    public String username;
    public String email;
    public String photoUrl;
    public HashMap<String, Boolean> groups;


    public myUser() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public myUser(String username, String email, Uri photoUrl, HashMap<String, Boolean> groups) {
        this.username = username;
        this.email = email;
        this.photoUrl = photoUrl != null ? photoUrl.toString() : "";
        this.groups = groups;
    }
}
