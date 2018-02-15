package com.productions.itea.motivatedev;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class GroupsSearchActivity extends AppCompatActivity {


    public RecyclerView mRecyclerView;

    private FirebaseDatabase myDb;
    public GroupsAdapter searchAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups_search);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser curUser = mAuth.getCurrentUser();
        if (curUser != null) {

            mRecyclerView = (RecyclerView) findViewById(R.id.search_rec);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

            String uid = curUser.getUid();
            myDb = FirebaseDatabase.getInstance();
            Log.d("DBDBDBDBD", myDb != null ? "OK" : "Oops");
            DatabaseReference solvedTasksRef = myDb.getReference("groups");
            searchAdapter = new GroupsAdapter(this, solvedTasksRef);
            mRecyclerView.setAdapter(searchAdapter);
        }
    }
}
