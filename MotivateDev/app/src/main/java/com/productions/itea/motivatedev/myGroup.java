package com.productions.itea.motivatedev;

import android.net.Uri;

public class myGroup {
    public String group_name;
    public String description;
    public String photoUrl;

    public myGroup() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public myGroup(String group_name, String description, Uri photoUrl) {
        this.group_name = group_name;
        this.description = description;
        this.photoUrl = photoUrl != null ? photoUrl.toString() : "";
    }
}