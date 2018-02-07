package com.example.root.motivationapp;


import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class BaseEmul {

    public static Map<String, TaskData> myTasks = new HashMap<String, TaskData>();
    public static TaskData defaultTask = new TaskData("New task", "null descr", new Date(1980, 01, 01));

}

