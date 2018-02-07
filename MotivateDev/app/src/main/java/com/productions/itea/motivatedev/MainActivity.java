package com.productions.itea.motivatedev;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Comment;

import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    // Choose an arbitrary request code value
    private static final int RC_SIGN_IN = 123;
    // Database
    private FirebaseDatabase myDb;

    public static Intent createIntent(Context context) {
        return new Intent(context, SignInActivity.class);
    }

    private void showSnackbar(int id) {
        Snackbar.make(findViewById(R.id.container), getResources().getString(id), Snackbar.LENGTH_LONG).show();
    }

    class TaskAdapter {
        DatabaseReference myRef;
        Context mContext;

        private List<String> myTaskIds = new ArrayList<>();
        private List<myTask> myTasks = new ArrayList<>();


        public TaskAdapter(Context context, DatabaseReference ref) {

            myRef = ref;
            mContext = context;

            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                    // A new task has been added, add it to the displayed list
                    myTask mytask = dataSnapshot.getValue(myTask.class);

                    // Update
                    myTaskIds.add(dataSnapshot.getKey());
                    myTasks.add(mytask);

                    //Update Listview

                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());

                    // A task has changed, use the key to determine if we are displaying this
                    // task and if so displayed the changed task.
                    myTask mytask = dataSnapshot.getValue(myTask.class);

                    String taskKey = dataSnapshot.getKey();

                    int taskIndex = myTaskIds.indexOf(taskKey);
                    if (taskIndex > -1) {
                        // Replace with the new data
                        myTasks.set(taskIndex, mytask);

                        //Update Listview

                    } else {
                        Log.w(TAG, "onChildChanged:unknown_child:" + taskKey);
                    }
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

                    // A task has changed, use the key to determine if we are displaying this
                    // task and if so remove it.
                    String taskKey = dataSnapshot.getKey();

                    int taskIndex = myTaskIds.indexOf(taskKey);
                    if (taskIndex > -1) {
                        // Remove data from the list
                        myTasks.remove(taskIndex);
                        myTaskIds.remove(taskIndex);

                        //Update Listview

                    } else {
                        Log.w(TAG, "onChildChanged:unknown_child:" + taskKey);
                    }
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());

                    // A task has changed, use the key to determine if we are displaying this
                    // task and if so move it.
                    myTask mytask = dataSnapshot.getValue(myTask.class);
                    String taskKey = dataSnapshot.getKey();

                    // ...
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "postComments:onCancelled", databaseError.toException());
                    Toast.makeText(mContext, "Failed to load tasks.", Toast.LENGTH_SHORT).show();
                }
            };
            ref.addChildEventListener(childEventListener);
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser curUser = mAuth.getCurrentUser();

        if (curUser != null) { //if signed in

            // Instance of database
            myDb = FirebaseDatabase.getInstance();
            DatabaseReference userRef  = myDb.getReference("users");

            // User Info
            String username = "";
            String email = "";
            Uri photoUrl = null;
            String uid = "";

            //get info from another provider
            for (UserInfo profile : curUser.getProviderData()) {
                String providerId  = profile.getProviderId();
                Log.d("PROVIDERID",providerId);
                if (providerId.equals("firebase")) {
                    uid = profile.getUid();
                    username = profile.getDisplayName();
                    email = profile.getEmail();
                    photoUrl = profile.getPhotoUrl();
                }
                if (providerId.equals("google.com")) {
                    username = profile.getDisplayName();
                    photoUrl = profile.getPhotoUrl();
                }
            }

            //add cur user to db
            myUser user = new myUser(username, email, photoUrl);
            userRef.child(uid).setValue(user);

        } else {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
        }
        setContentView(R.layout.activity_main);
    }

    List<String> myTaskIds = new ArrayList<>();
    List<myTask> myTasks = new ArrayList<>();
    private Context mContext;
    @Override
    public void onStart() {
        super.onStart();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser curUser = mAuth.getCurrentUser();
        if (curUser != null) { //if signed in
            String uid = curUser.getUid();

            //Current tasks
            DatabaseReference curTasksRef = myDb.getReference("curr_tasks").child(uid);
            TaskAdapter curTaskAdapter = new TaskAdapter(this, curTasksRef);

            //Ended tasks
            DatabaseReference endedTasksRef = myDb.getReference("ended_tasks").child(uid);
            TaskAdapter endedTaskAdapter = new TaskAdapter(this, endedTasksRef);

        }
        else {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
        }
    }


    public void signOut(View view) {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            startActivity(MainActivity.createIntent(MainActivity.this));
                            finish();
                        } else {
                            showSnackbar(R.string.sign_out_failed);
                        }
                    }
                });



    }


    }


