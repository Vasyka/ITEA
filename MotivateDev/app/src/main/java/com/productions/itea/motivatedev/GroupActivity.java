package com.productions.itea.motivatedev;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
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
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;


public class GroupActivity extends AppCompatActivity implements GroupTaskAdapter.OnItemClickListener {

    public RecyclerView mRecyclerView;
    private FirebaseDatabase myDb;
    private static final String TAG = "GroupActivity";

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
        final FirebaseUser curUser = mAuth.getCurrentUser();





        if (curUser != null) {
            String uid = curUser.getUid();
            myDb = FirebaseDatabase.getInstance();
            final String group_id = getIntent().getStringExtra("groupId");

            ImageButton createBtn = (ImageButton) findViewById(R.id.rating_image);

            createBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), RatingActivity.class);
                    intent.putExtra("groupId", group_id);
                   startActivity(intent);
                }
            });

            ImageButton joinBtn = findViewById(R.id.members_image);
            final DatabaseReference userRef = myDb.getReference().child("users").child(uid);

            joinBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            myUser userClass = dataSnapshot.getValue(myUser.class);

                            AlertDialog.Builder builder;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                builder = new AlertDialog.Builder(GroupActivity.this, android.R.style.Theme_Material_Dialog_Alert);
                            } else {
                                builder = new AlertDialog.Builder(GroupActivity.this);
                            }

                            if(userClass.groups!=null && userClass.groups.containsKey(group_id) && userClass.groups.get(group_id)) {

                                builder.setTitle("Выйти")
                                .setMessage("Выйти из группы?")
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {

                                        userRef.child("groups").child(group_id).removeValue();

                                        Log.d(TAG,"АТБАВЛЕНА");
                                        Toast.makeText(GroupActivity.this, "Вы покинули группу", Toast.LENGTH_SHORT).show();

                                        DatabaseReference tasks_to_delete = userRef.getRoot()
                                                .child("group_tasks")
                                                .child(group_id);

                                        tasks_to_delete.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                for (DataSnapshot childTask:
                                                     dataSnapshot.getChildren()) {
                                                    userRef.getRoot()
                                                            .child("group_tasks_user")
                                                            .child(userRef.getKey())
                                                            .child(childTask.getKey())
                                                            .removeValue();
                                                }
                                            }
                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                            }
                                        });
                                        groupTaskAdapter.notifyDataSetChanged();
                                    }
                                })
                                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                // do nothing
                                            }
                                        })
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .show();
                            }
                            else {
                                builder.setTitle("Вступить")
                                        .setMessage("Вступить в группу?")
                                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {

                                                userRef.child("groups").child(group_id).setValue(true);

                                                Log.d(TAG,"ДАБАВЛЕНА");
                                                Toast.makeText(GroupActivity.this, "Вы вступили в группу", Toast.LENGTH_SHORT).show();
                                                groupTaskAdapter.notifyDataSetChanged();

                                            }
                                        })
                                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                // do nothing
                                            }
                                        })
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            });


            mRecyclerView = (RecyclerView) findViewById(R.id.group_task_rec);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));


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

