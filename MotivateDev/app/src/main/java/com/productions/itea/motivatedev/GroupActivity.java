package com.productions.itea.motivatedev;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;



public class GroupActivity extends AppCompatActivity {

    public RecyclerView mRecyclerView;
    private FirebaseDatabase myDb;

    public GroupTaskAdapter groupTaskAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser curUser = mAuth.getCurrentUser();

        if (curUser != null) {
            final String group_id = getIntent().getStringExtra("groupId");
            ImageButton createBtn = (ImageButton) findViewById(R.id.rating_image);

            createBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), RatingActivity.class);
                    intent.putExtra("groupId", group_id);
                   startActivity(intent);
                }
            });

            mRecyclerView = (RecyclerView) findViewById(R.id.group_task_rec);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

            String uid = curUser.getUid();

            myDb = FirebaseDatabase.getInstance();

            DatabaseReference groupRef = myDb.getReference("group_tasks").child(group_id);
            groupTaskAdapter = new GroupTaskAdapter(this, groupRef);
            mRecyclerView.setAdapter(groupTaskAdapter);

        }

    }
}
