package com.productions.itea.motivatedev;

import android.net.Uri;
import android.util.Log;

import java.util.Date;
import java.text.*;
import java.util.Locale;

public class myTask {
    public String task_name;
    public String description;
    public String photoUrl;
    public String date;
    public Boolean important;

    public myTask() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public myTask(String task_name, String description, String date, String photoUrl, Boolean important) {
        this.task_name = task_name;
        this.description = description;
        this.photoUrl = photoUrl;
        this.important = important;
        this.date = date;
    }


    public myTask(String task_name, String description, Date date, Uri photoUrl, Boolean important) {
        this.task_name = task_name;
        this.description = description;
        this.photoUrl = photoUrl != null ? photoUrl.toString() : "";
        this.important = important;
        SimpleDateFormat sdfr = new SimpleDateFormat("dd/MMM/yyyy", Locale.ROOT);
        try {
            this.date = date != null ? sdfr.format(date) : "";
        } catch (Exception ex) {
            Log.d("MyTask", "Error in date to string conversion");
        }

    }

    public String toString(){
        return task_name;
    }
}