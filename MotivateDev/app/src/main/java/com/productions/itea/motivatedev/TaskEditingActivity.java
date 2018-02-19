package com.productions.itea.motivatedev;

import android.content.Intent;
import android.nfc.FormatException;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.sql.Ref;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Calendar;
import java.util.Date;

public class TaskEditingActivity extends AppCompatActivity {//implements CompoundButton.OnCheckedChangeListener {

    private static final String EXTRA_TASK_STATE = "task_state";
    private static final String TAG = "TaskEditingActivity";
//    String name;
    TextView title;
    EditText description;
    EditText date;
    myTask data;
    Button save;
    String task_state;
    String path, taskId;
    CheckBox checkBox;

    FirebaseDatabase myDb;
    DatabaseReference tasksRef;


    RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_editing);
        //Toolbar
        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        toolbar.inflateMenu(R.menu.toolbar_menu);

        task_state = getIntent().getStringExtra(EXTRA_TASK_STATE);
        taskId = getIntent().getStringExtra("task_id");
        path = getIntent().getStringExtra("path");

        // Instance of database
        myDb = FirebaseDatabase.getInstance();
        tasksRef = myDb.getReference().child(path);

        title = (TextView) findViewById(R.id.titleView);
        description = (EditText) findViewById(R.id.notesTextView);
        date = (EditText) findViewById(R.id.dateView);
        checkBox = (CheckBox) findViewById(R.id.important_check);

        loadData();
        setWriteble(true);

        save = (Button) findViewById(R.id.saveBtn);
        save.setOnClickListener(saveButtonClickListener);

    }

    View.OnClickListener saveButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) { // Save inputed values
            Toast.makeText(TaskEditingActivity.this,"Saved!",Toast.LENGTH_SHORT).show();

            data.task_name = title.getText().toString();
            data.description = description.getText().toString();
            data.date = date.getText().toString();
            data.photoUrl = "";
            data.important = checkBox.isChecked();

            DatabaseReference newTaskRef;
            if (taskId == null) {
                Log.d(TAG, "task_id is null");
                throw new NullPointerException();
            }
            if (taskId.trim().isEmpty()){
                newTaskRef = tasksRef.push();

            } else {
                newTaskRef = tasksRef.child(taskId);
            }
            newTaskRef.setValue(data);
            onBackPressed();
        }
    };

    private void loadData() {
        // Create new tenplate task
        if (task_state.equals("Add"))
            data = BaseEmul.defaultTask;
        else { // Load existing data

            DatabaseReference curTaskRef = tasksRef.child(taskId);

            curTaskRef.runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData mutableData) {
                    if (mutableData.getValue() != null) {
                        data = mutableData.getValue(myTask.class);
                        title.setText(data.task_name);
                        description.setText(data.description);
                        date.setText(data.date);
                        checkBox.setChecked(data.important);
                    }
                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(DatabaseError databaseError, boolean b,
                                       DataSnapshot dataSnapshot) {
                    // Transaction completed
                    Log.d(TAG, "myTaskTransaction:onComplete:" + databaseError);
                }
            });

        }
        title.setText(data.task_name);
        description.setText(data.description);
        date.setText(data.date);
        checkBox.setChecked(data.important);
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

    public void signOut() {
        AuthUI.getInstance().signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(TaskEditingActivity.this, SignInActivity.class));
                            finish();
                        } else {
                            Snackbar.make(findViewById(R.id.container), getResources().
                                    getString(R.string.sign_out_failed), Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.exit_menu:
                signOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
