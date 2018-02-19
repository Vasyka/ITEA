package com.productions.itea.motivatedev;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class SolvedTasksFragment extends Fragment {



    public RecyclerView mRecyclerView;

    private FirebaseDatabase myDb;
    public SolvedAdapter solvedAdapter;


    public SolvedTasksFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_solved_tasks, container, false);
        // Firebase Auth
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser curUser = mAuth.getCurrentUser();
        if (curUser != null){

            mRecyclerView = (RecyclerView) v.findViewById(R.id.solved_rec);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity())
            {
                @Override
                public void onLayoutChildren(RecyclerView.Recycler arg0, RecyclerView.State arg1) {
                    try {
                        super.onLayoutChildren(arg0, arg1);
                    } catch (Exception e) {
                        Log.d("SolvedTaskFragment","onLayoutChildren :" + e.toString());
                    }
                }
            });

            String uid = curUser.getUid();
            myDb = FirebaseDatabase.getInstance();
            Log.d("DBDBDBDBD",myDb != null ? "OK" : "Oops" );
            DatabaseReference solvedTasksRef = myDb.getReference("solved_tasks").child(uid);
            DatabaseReference groupTasksRef = myDb.getReference("solved_group_tasks_user").child(uid);
            solvedAdapter = new SolvedAdapter(getActivity(), solvedTasksRef, groupTasksRef);
            mRecyclerView.setAdapter(solvedAdapter);

        }



        return v;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);


    }

    @Override
    public void onDetach() {
        super.onDetach();
    }



}
