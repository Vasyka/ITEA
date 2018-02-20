package com.productions.itea.motivatedev;


import android.util.Log;

import com.google.android.gms.tasks.Task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BaseEmul {

    public static Map<String, myTask> myTasks = new HashMap<String, myTask>();
    public static myTask defaultTask = new myTask("Новая задача", "Описание задчи",
            new Date(1980, 01, 01), null, false);

}

