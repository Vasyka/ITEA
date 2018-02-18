package com.productions.itea.motivatedev;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RatingActivity extends AppCompatActivity {


    public RecyclerView mRecyclerView;

    private FirebaseDatabase myDb;
    public RatingAdapter ratingAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser curUser = mAuth.getCurrentUser();
        if (curUser != null) {

            mRecyclerView = (RecyclerView) findViewById(R.id.rating_rec);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

            String group_id = getIntent().getStringExtra("groupId");
            String uid = curUser.getUid();
            myDb = FirebaseDatabase.getInstance();

            DatabaseReference ratingRef = myDb.getReference("rating").child(group_id);
            ratingAdapter = new RatingAdapter(this, ratingRef);
            mRecyclerView.setAdapter(ratingAdapter);
        }
    }
}
