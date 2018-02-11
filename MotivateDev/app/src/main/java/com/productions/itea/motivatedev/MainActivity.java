package com.productions.itea.motivatedev;

import android.app.Activity;
import android.app.ActivityGroup;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
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

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends FragmentActivity
        implements
        MyTasksFragment.OnMyTasksFragmentInteractionListener,
        MyGroupsFragment.OnMyGroupsFragmentInteractionListener{

    private static final String TAG = "MainActivity";
    private static final int RC_SIGN_IN = 123;
    //PageViewer
    private ViewPager mPager;
    public ScreenSlidePagerAdapter mPagerAdapter;
    private TabLayout tab;
    //Fragments of PageViewer
    public MyTasksFragment mTasksFrag;
    public MyGroupsFragment myGroupsFrag;
    public TrophiesFragment mTrophiesFrag;
    public SolvedTasksFragment mSolvedTasksFrag;


    private TaskAdapter curTaskAdapter;
    private FirebaseDatabase myDb; // Database

    static final String EXTRA_TASK_STATE = "Add";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Firebase Auth
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser curUser = mAuth.getCurrentUser();

        // Check if user is signed in
        if (curUser != null) {

            // Instance of database
            myDb = FirebaseDatabase.getInstance();
            DatabaseReference userRef  = myDb.getReference("users");

            // User Info
            String username = "";
            String email = "";
            Uri photoUrl = null;
            String uid = "";

            // Get current user info from different providers
            for (UserInfo profile : curUser.getProviderData()) {
                String providerId  = profile.getProviderId();

                if (providerId.equals("firebase")) {
                    uid = profile.getUid();
                    username = profile.getDisplayName();
                    email = profile.getEmail();
                    photoUrl = profile.getPhotoUrl();
                }
                else {
                    if (providerId.equals("google.com")) {
                        username = profile.getDisplayName();
                        photoUrl = profile.getPhotoUrl();
                    }
                }
            }

            // Add current user info to database
            myUser user = new myUser(username, email, photoUrl);
            userRef.child(uid).setValue(user);

            // Slider and tab
            tab = (TabLayout) findViewById(R.id.tab_id);
            mPager = (ViewPager) findViewById(R.id.pager);
            mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
            mPagerAdapter.AddFragment(new MyTasksFragment(), "My Tasks");
            mPagerAdapter.AddFragment(new MyGroupsFragment(), "My Groups");
            mPagerAdapter.AddFragment(new TrophiesFragment(), "My Trophies");
            mPagerAdapter.AddFragment(new SolvedTasksFragment(), "Solved Tasks");
            tab.setupWithViewPager(mPager);
            mPager.setAdapter(mPagerAdapter);

            //Fragments references
            mTasksFrag = (MyTasksFragment) mPagerAdapter.getItem(0);
            myGroupsFrag = (MyGroupsFragment) mPagerAdapter.getItem(1);
            mTrophiesFrag = (TrophiesFragment) mPagerAdapter.getItem(2);
            mSolvedTasksFrag = (SolvedTasksFragment) mPagerAdapter.getItem(3);


            // adding sample task
            myDb.getReference("curr_tasks").child(uid).child("2").setValue(new myTask("project","Android project",null,null));

            // View for tasks
//            LayoutInflater inflater = getLayoutInflater();
//            View rootView = inflater.inflate(R.layout.fragment_my_tasks, null,false);
//            RecyclerView mRecyclerView = (RecyclerView)rootView.findViewById(R.id.rec_view);
//            Log.d("HHHHHHHHHH", mRecyclerView == null ? "0000000" : "1111111");
//            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(MainActivity.this);
//
//            mRecyclerView.setLayoutManager(mLayoutManager);

            //Current tasks
            DatabaseReference curTasksRef = myDb.getReference("curr_tasks").child(uid);
            curTaskAdapter = new TaskAdapter(this, curTasksRef);

//            mTasksFrag.SetAdapter(curTaskAdapter);

            //curTasksRef.child("1").setValue(new myTask("jjj","kkk",null));
            /*//Ended tasks
            DatabaseReference endedTasksRef = myDb.getReference("ended_tasks").child(uid);
            TaskAdapter endedTaskAdapter = new TaskAdapter(this, endedTasksRef);
            mRecyclerView1.setAdapter(endedTaskAdapter);*/

        } else {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    @Override
    public void onBtnPressed() {
        Intent intent = new Intent(MainActivity.this, TaskEditingActivity.class);
        intent.putExtra("taskName", "default");
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String uid = mAuth.getCurrentUser().getUid();
        intent.putExtra("uid",uid);
        intent.putExtra(EXTRA_TASK_STATE,"Add");
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onResume(){
        super.onResume();
        if (mTasksFrag.curTaskAdapter!=null)
            mTasksFrag.curTaskAdapter.notifyDataSetChanged();
        mPagerAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mTasksFrag.curTaskAdapter!=null)
            mTasksFrag.curTaskAdapter.notifyDataSetChanged();
        mPagerAdapter.notifyDataSetChanged();
    }

    public void signOut(View view) {
        AuthUI.getInstance().signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(MainActivity.this, SignInActivity.class));
                            finish();
                        } else {
                            Snackbar.make(findViewById(R.id.container), getResources().
                                    getString(R.string.sign_out_failed), Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void updateShiet() {
        mTasksFrag.curTaskAdapter.notifyDataSetChanged();

    }


//    private static class TaskViewHolder extends RecyclerView.ViewHolder {
//        public TextView taskView;
//
//        public TaskViewHolder(View itemView) {
//            super(itemView);
//            taskView = itemView.findViewById(R.id.my_text_view);
//        }
//    }

    static class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder>{

        public static class TaskViewHolder extends RecyclerView.ViewHolder {
            public TextView taskView;

            public TaskViewHolder(View itemView) {
                super(itemView);
                taskView = itemView.findViewById(R.id.my_text_view);
            }
        }

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

                    //Update Recycleview
                    notifyItemInserted(myTasks.size() - 1);
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

                        //Update Recycleview
                        notifyItemChanged(taskIndex);

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

                        //Update Recycleview
                        notifyItemRemoved(taskIndex);

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
                    Log.w(TAG, "Tasks:onCancelled", databaseError.toException());
                    Toast.makeText(mContext, "Failed to load tasks.", Toast.LENGTH_SHORT).show();
                }
            };
            ref.addChildEventListener(childEventListener);
        }

        public int getItemCount() {
            return myTasks.size();
        }

        // Place item[position] in holder
        public void onBindViewHolder(TaskViewHolder holder, int position) {
            holder.taskView.setText(myTasks.get(position).task_name);
            //holder.taskView.setText("LLLLL");
        }

        // Create new views (invoked by the layout manager)
        public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.task_layout, parent, false);

            // set the view's size, margins, paddings and layout parameters

            return new TaskViewHolder(view);
        }
    }

}


