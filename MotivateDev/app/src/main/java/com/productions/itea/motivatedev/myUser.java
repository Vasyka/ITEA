package com.productions.itea.motivatedev;

import android.net.Uri;

public class myUser {
    public String username;
    public String email;
    public String photoUrl;

    public myUser() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public myUser(String username, String email, Uri photoUrl) {
        this.username = username;
        this.email = email;
        this.photoUrl = photoUrl != null ? photoUrl.toString() : "";
    }
}
