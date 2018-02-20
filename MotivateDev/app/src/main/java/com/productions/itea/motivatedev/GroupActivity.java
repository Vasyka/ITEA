package com.productions.itea.motivatedev;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class GroupActivity extends AppCompatActivity implements GroupTaskAdapter.OnItemClickListener {

    public RecyclerView mRecyclerView;
    private FirebaseDatabase myDb;

    public GroupTaskAdapter groupTaskAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        //Toolbar
        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        toolbar.inflateMenu(R.menu.toolbar_menu);


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
            DatabaseReference groupItself = myDb.getReference("groups").child(group_id);
            groupItself.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    ((TextView)findViewById(R.id.group_title)).setText(dataSnapshot.child("group_name").getValue().toString());
                    ((TextView)findViewById(R.id.group_description)).setText(dataSnapshot.child("description").getValue().toString());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            groupTaskAdapter = new GroupTaskAdapter(this, groupRef, this);
            mRecyclerView.setAdapter(groupTaskAdapter);

        }

    }

    @Override
    public void onItemClicked(View v, String descr) {
        Bundle bnd = new Bundle();
        bnd.putString("description", descr);
        GroupTaskDialog dialog = new GroupTaskDialog();
        dialog.setArguments(bnd);
        dialog.show(getFragmentManager(), "Dialog");
    }
    public void signOut() {
        AuthUI.getInstance().signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(GroupActivity.this, SignInActivity.class));
                            finish();
                        } else {
                            Snackbar.make(findViewById(R.id.container), getResources().
                                    getString(R.string.sign_out_failed), Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.exit_menu:
                signOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}


