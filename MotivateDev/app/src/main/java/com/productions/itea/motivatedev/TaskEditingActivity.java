package com.productions.itea.motivatedev;

import android.nfc.FormatException;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.tasks.Task;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Calendar;
import java.util.Date;

public class TaskEditingActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    String name;
    TextView title;
    EditText description;
    EditText date;
    myTask data;
    ToggleButton edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_editing);

        name = getIntent().getStringExtra("taskName");
        title = (TextView) findViewById(R.id.titleView);
        description = (EditText) findViewById(R.id.notesTextView);
        date = (EditText) findViewById(R.id.dateView);
        loadData(name);
        setWriteble(false);

        edit = (ToggleButton) findViewById(R.id.toggleEdit);
        edit.setOnCheckedChangeListener(this);

    }

    private void loadData(String name) {

        if (name.equals("default")) {
            data = BaseEmul.defaultTask;
        } else {
            data = BaseEmul.myTasks.get(name);
        }

        title.setText(data.task_name);
        description.setText(data.description);
        date.setText(data.toString());
    }

    void setWriteble(boolean state) {
        title.setFocusable(state);
        title.setLongClickable(state);
        title.setCursorVisible(state);
        description.setFocusable(state);
        description.setLongClickable(state);
        description.setCursorVisible(state);
        date.setFocusable(state);
        date.setLongClickable(state);
        date.setCursorVisible(state);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked)
        {
            setWriteble(true);
        }
        else{
            setWriteble(false);
            data.task_name = title.getText().toString();
            data.description = description.getText().toString();
            try {
                Date today = Calendar.getInstance().getTime();
                data.date = (new SimpleDateFormat("MMMM d, yyyy", Locale.ROOT)).format(today);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (BaseEmul.myTasks.containsKey(data.task_name)){
                BaseEmul.myTasks.put(data.task_name, data);
            }else{
                BaseEmul.myTasks.put(data.task_name, data);
                //MainActivity.tasks.add(name);
            }


        }

    }

}
