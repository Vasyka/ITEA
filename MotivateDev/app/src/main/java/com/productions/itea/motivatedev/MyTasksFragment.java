package com.productions.itea.motivatedev;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class MyTasksFragment extends Fragment {

    private OnMyTasksFragmentInteractionListener mListener;
    public RecyclerView mRecyclerView;

    public MainActivity.TaskAdapter curTaskAdapter;
    private FirebaseDatabase myDb; // Database


    public MyTasksFragment() {
        // Required empty public constructor
    }

    public void SetAdapter(MainActivity.TaskAdapter adapter){
        mRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_my_tasks, container, false);

        Button createBtn = (Button) v.findViewById(R.id.createBtn);
        createBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mListener.onBtnPressed();
            }
        });
        mRecyclerView = (RecyclerView) v.findViewById(R.id.rec_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));




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
            curTaskAdapter = new MainActivity.TaskAdapter(getActivity(), curTasksRef);
            SetAdapter(curTaskAdapter);

            //curTasksRef.child("1").setValue(new myTask("jjj","kkk",null));
            /*//Ended tasks
            DatabaseReference endedTasksRef = myDb.getReference("ended_tasks").child(uid);
            TaskAdapter endedTaskAdapter = new TaskAdapter(this, endedTasksRef);
            mRecyclerView1.setAdapter(endedTaskAdapter);*/

        }






        return v;

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnMyTasksFragmentInteractionListener) {
            mListener = (OnMyTasksFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnMyTasksFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }



    public interface OnMyTasksFragmentInteractionListener {
        void onBtnPressed();
    }
}
