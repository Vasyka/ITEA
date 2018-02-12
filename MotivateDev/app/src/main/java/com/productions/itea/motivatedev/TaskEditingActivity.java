package com.productions.itea.motivatedev;

import android.content.Intent;
import android.nfc.FormatException;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Calendar;
import java.util.Date;

public class TaskEditingActivity extends AppCompatActivity {//implements CompoundButton.OnCheckedChangeListener {

    static final String EXTRA_TASK_STATE = "Add";
    String name;
    TextView title;
    EditText description;
    EditText date;
    myTask data;
    Button save;
    String task_state;
    String uid;
    //ToggleButton edit;

    DatabaseReference tasksRef;


    RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_editing);

        // Instance of database
        FirebaseDatabase myDb = FirebaseDatabase.getInstance();
        tasksRef = myDb.getReference("curr_tasks");

        task_state = getIntent().getStringExtra(EXTRA_TASK_STATE);
        name = getIntent().getStringExtra("taskName");
        uid = getIntent().getStringExtra("uid");
        title = (TextView) findViewById(R.id.titleView);
        description = (EditText) findViewById(R.id.notesTextView);
        date = (EditText) findViewById(R.id.dateView);
        loadData(name);
        setWriteble(true);

        save = (Button) findViewById(R.id.saveBtn);
        save.setOnClickListener(saveButtonClickListener);

        //edit = (ToggleButton) findViewById(R.id.toggleEdit);
        //edit.setOnCheckedChangeListener(this);

    }

    View.OnClickListener saveButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Toast.makeText(TaskEditingActivity.this,"Saved!",Toast.LENGTH_SHORT).show();

            data.task_name = title.getText().toString();
            data.description = description.getText().toString();
            data.date = date.getText().toString();
            data.photoUrl = "";
            DatabaseReference newTaskRef = tasksRef.child(uid).push();
            newTaskRef.setValue(data);

            Intent intent = new Intent(TaskEditingActivity.this, MainActivity.class);
            intent.putExtra("taskName", data.task_name);
            startActivity(intent);
        }
    };

    private void loadData(String name) {

        if (task_state.equals("Add")) {
            data = BaseEmul.defaultTask;
        } else {
            data = BaseEmul.myTasks.get(name);
        }

        title.setText(data.task_name);
        description.setText(data.description);
        date.setText(data.date);
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




    /*@Override
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

    }*/

}
