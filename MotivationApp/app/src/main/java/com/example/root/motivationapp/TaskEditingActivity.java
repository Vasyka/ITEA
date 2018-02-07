package com.example.root.motivationapp;

import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TaskEditingActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    String name;
    TextView title;
    EditText description;
    EditText date;
    TaskData data;
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

    if(name.equals("default")){
        data = BaseEmul.defaultTask;

    }else{
        data = BaseEmul.myTasks.get(name);
    }
    title.setText(data.name);
    description.setText(data.description);
    date.setText(data.date.toString());
    }

    void setWriteble(boolean state){
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
            data.name = title.getText().toString();
            data.description = description.getText().toString();
            try {
                data.date = (new SimpleDateFormat("MMMM d, yyyy", Locale.ROOT)).parse(date.getText().toString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (BaseEmul.myTasks.containsKey(data.name)){
                BaseEmul.myTasks.put(data.name, data);
            }else{
                BaseEmul.myTasks.put(data.name, data);
                PagesActivity.tasks.add(name);
            }


        }

    }

}
