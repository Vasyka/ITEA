package com.productions.itea.motivatedev;

import android.net.Uri;
import android.util.Log;

import java.util.Date;

public class myGroupTask extends myTask{
    public Integer complexity;

    public myGroupTask() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public myGroupTask(String task_name, String description, Date date, Uri photoUrl, String complexity) {
       super(task_name, description, date, photoUrl);
        try
        {
            this.complexity = Integer.parseInt(complexity);
        }
        catch (NumberFormatException nfe)
        {
            Log.d("MyGroupTask", "NumberFormatException: " + nfe.getMessage());
        }
    }

    public String toString(){
        return super.toString();
    }
}
