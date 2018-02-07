package com.productions.itea.motivatedev;

import android.net.Uri;

public class myTask {
    public String task_name;
    public String description;
    public String photoUrl;

    public myTask() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public myTask(String task_name, String description, Uri photoUrl) {
        this.task_name = task_name;
        this.description = description;
        this.photoUrl = photoUrl != null ? photoUrl.toString() : "";
    }
}