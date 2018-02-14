package com.productions.itea.motivatedev;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class MyGroupsFragment extends Fragment {


    private OnMyGroupsFragmentInteractionListener mListener;
    public RecyclerView mRecyclerView;

    private FirebaseDatabase myDb;
    public GroupsAdapter groupsAdapter;

    public MyGroupsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_my_groups, container, false);

        // Firebase Auth
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser curUser = mAuth.getCurrentUser();
        // Check if user is signed in
        if (curUser != null) {

            Button createBtn = (Button) v.findViewById(R.id.search_btn);
            createBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    mListener.onBtnSearchPressed();
                }
            });
            mRecyclerView = (RecyclerView) v.findViewById(R.id.groups_rec);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

            //Current tasks
            String uid = curUser.getUid();
            myDb = FirebaseDatabase.getInstance();
            Log.d("DBDBDBDBD",myDb != null ? "OK" : "Oops" );
            DatabaseReference curTasksRef = myDb.getReference("groups").child(uid);
            groupsAdapter = new GroupsAdapter(getActivity(), curTasksRef);
            mRecyclerView.setAdapter(groupsAdapter);
        }

        return v;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnMyGroupsFragmentInteractionListener) {
            mListener = (OnMyGroupsFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnMyGroupsFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnMyGroupsFragmentInteractionListener {
        void onBtnSearchPressed();
    }
}
