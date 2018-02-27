package com.productions.itea.motivatedev;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class MyTasksFragment extends Fragment implements TaskAdapter.OnTaskItemClickListener {

    private OnMyTasksFragmentInteractionListener mListener;
    public RecyclerView mRecyclerView;

    public TaskAdapter curTaskAdapter;
    private FirebaseDatabase myDb; // Database


    public MyTasksFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_my_tasks, container, false);


        // Firebase Auth
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser curUser = mAuth.getCurrentUser();

        // Check if user is signed in
        if (curUser != null) {



            Button createBtn = (Button) v.findViewById(R.id.createBtn);
            createBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    mListener.onBtnCreateTaskPressed();
                }
            });
            mRecyclerView = (RecyclerView) v.findViewById(R.id.rec_view);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()) {
                @Override
                public void onLayoutChildren(RecyclerView.Recycler arg0, RecyclerView.State arg1) {
                    try {
                        super.onLayoutChildren(arg0, arg1);
                    } catch (Exception e) {
                        Log.d("MyTaskFragment","onLayoutChildren :" + e.toString());
                    }
                }
            });


            //Current tasks
            String uid = curUser.getUid();
            myDb = FirebaseDatabase.getInstance();
            DatabaseReference curTasksRef = myDb.getReference("curr_tasks").child(uid);
            DatabaseReference groupTasksRef = myDb.getReference("group_tasks_user").child(uid);
            curTaskAdapter = new TaskAdapter(getActivity(), curTasksRef, groupTasksRef, this);
            mRecyclerView.setAdapter(curTaskAdapter);
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

    @Override
    public void onItemClicked(View v, String descr) {
        Bundle bnd = new Bundle();
        bnd.putString("description", descr);
        GroupTaskDialog dialog = new GroupTaskDialog();
        dialog.setArguments(bnd);
        dialog.show(getActivity().getFragmentManager(), "Dialog");
    }

    public interface OnMyTasksFragmentInteractionListener {
        void onBtnCreateTaskPressed();
    }
}
