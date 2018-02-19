package com.productions.itea.motivatedev;

import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.data.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;


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
    public ImportantFragment mTrophiesFrag;
    public SolvedTasksFragment mSolvedTasksFrag;

    private FirebaseDatabase myDb; // Database

    static final String EXTRA_TASK_STATE = "task_state";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Firebase Auth
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final FirebaseUser curUser = mAuth.getCurrentUser();

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
            HashMap<String, Boolean> groups = null;

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
            final myUser user = new myUser(username, email, photoUrl, groups);
            final DatabaseReference curUserRef = userRef.child(uid);

            // Check user's existence in database
            userRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // If user exits
                    if(dataSnapshot.exists()){
                        Log.d(TAG, "User exists");
                        // Load user's groups
                        /*for (DataSnapshot groupSnapshot: dataSnapshot.child("groups").getChildren()) {
                            Log.d(TAG, groupSnapshot.getKey() != null ? "JJ" : "LL");
                            String groupKey = groupSnapshot.getKey();
                            //Boolean value = (Boolean) groupSnapshot.child(groupKey).getValue();
                            groups.put(groupKey, true);
                        }*/

                    } else { // User does not exist
                        // Add current user info to database
                        curUserRef.setValue(user);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "Users:onCancelled", databaseError.toException());
                }
            });



            // Slider and tab
            tab = (TabLayout) findViewById(R.id.tab_id);
            mPager = (ViewPager) findViewById(R.id.pager);
            mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
            mPagerAdapter.AddFragment(new MyTasksFragment(), "My Tasks");
            mPagerAdapter.AddFragment(new MyGroupsFragment(), "My Groups");
            mPagerAdapter.AddFragment(new ImportantFragment(), "My Trophies");
            mPagerAdapter.AddFragment(new SolvedTasksFragment(), "Solved Tasks");
            tab.setupWithViewPager(mPager);
            mPager.setAdapter(mPagerAdapter);

            //Fragments references
            mTasksFrag = (MyTasksFragment) mPagerAdapter.getItem(0);
            myGroupsFrag = (MyGroupsFragment) mPagerAdapter.getItem(1);
            mTrophiesFrag = (ImportantFragment) mPagerAdapter.getItem(2);
            mSolvedTasksFrag = (SolvedTasksFragment) mPagerAdapter.getItem(3);

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
    public void onBtnCreateTaskPressed() {
        Intent intent = new Intent(MainActivity.this, TaskEditingActivity.class);
//        intent.putExtra("taskName", "default");
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String uid = mAuth.getCurrentUser().getUid();
        intent.putExtra("uid", uid);
        intent.putExtra("task_id", "");
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


    @Override
    public void onBtnSearchPressed() {
        Intent intent = new Intent(MainActivity.this, GroupsSearchActivity.class);
        startActivity(intent);
    }
}


