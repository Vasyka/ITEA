package com.productions.itea.motivatedev;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.net.sip.SipSession;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class GroupTaskAdapter extends RecyclerView.Adapter<GroupTaskAdapter.GroupTaskViewHolder>{

    private static final String TAG = "GroupTaskAdapter";
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClicked(View v, String descr);
    }

    static class GroupTaskViewHolder extends RecyclerView.ViewHolder {
        TextView groupTaskView;
        CheckBox getTaskCheckBox;

        GroupTaskViewHolder(View itemView) {
            super(itemView);
            groupTaskView = (TextView)itemView.findViewById(R.id.group_task_view);
            getTaskCheckBox = (CheckBox) itemView.findViewById((R.id.get_task));
        }
    }

    private Context mContext;
    private DatabaseReference mRef;

    private List<String> myGroupTaskIds = new ArrayList<>();
    private List<myGroupTask> myGroupTasks = new ArrayList<>();


    public GroupTaskAdapter(Context context, DatabaseReference ref, OnItemClickListener listener) {
        this.listener = listener;
        mContext = context;
        mRef = ref;


        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                // A new task has been added, add it to the displayed list
                myGroupTask mygrouptask = dataSnapshot.getValue(myGroupTask.class);

                // Update
                myGroupTaskIds.add(dataSnapshot.getKey());
                myGroupTasks.add(mygrouptask);

                //Update Recycleview
                notifyItemInserted(myGroupTasks.size() - 1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());

                // A task has changed, use the key to determine if we are displaying this
                // task and if so displayed the changed task.
                myGroupTask mygrouptask = dataSnapshot.getValue(myGroupTask.class);

                String taskKey = dataSnapshot.getKey();

                int taskIndex = myGroupTaskIds.indexOf(taskKey);
                if (taskIndex > -1) {
                    // Replace with the new data
                    myGroupTasks.set(taskIndex, mygrouptask);

                    //Update Recycleview
                    notifyItemChanged(taskIndex);

                } else {
                    Log.w(TAG, "onChildChanged:unknown_child:" + taskKey);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

                // A task has changed, use the key to determine if we are displaying this
                // task and if so remove it.
                String taskKey = dataSnapshot.getKey();

                int taskIndex = myGroupTaskIds.indexOf(taskKey);
                if (taskIndex > -1) {
                    // Remove data from the list
                    myGroupTasks.remove(taskIndex);
                    myGroupTaskIds.remove(taskIndex);

                    //Update Recycleview
                    notifyItemRemoved(taskIndex);

                } else {
                    Log.w(TAG, "onChildChanged:unknown_child:" + taskKey);
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());

                // A task has changed, use the key to determine if we are displaying this
                // task and if so move it.
                myGroupTask mygrouptask = dataSnapshot.getValue(myGroupTask.class);
                String taskKey = dataSnapshot.getKey();

                // ...
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "GroupTasks:onCancelled", databaseError.toException());
                Toast.makeText(mContext, "Failed to load group's tasks.", Toast.LENGTH_SHORT).show();
            }
        };
        ref.addChildEventListener(childEventListener);
    }

    public int getItemCount() {
        return myGroupTasks.size();
    }

    // Place item[position] in holder
    public void onBindViewHolder(final GroupTaskViewHolder holder, final int position) {
        holder.groupTaskView.setText(myGroupTasks.get(position).task_name);

        // Get task
        holder.getTaskCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {

                    // Add the group task to the user's space
                    final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    final DatabaseReference grouptaskuser = mRef.getRoot().child("group_tasks_user");

                    miniTask miniTask = new miniTask(mRef.getKey(), myGroupTasks.get(position).important);
                    final HashMap <String, Object> task = new HashMap<>();
                    task.put(myGroupTaskIds.get(position), miniTask);

                    // Check user's existence in group_tasks_user table
                    grouptaskuser.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.child(uid).exists()) { // If it's not the first group task for user
                                grouptaskuser.child(uid).updateChildren(task);
                            }
                            else { // If it's the first group task for user
                                HashMap <String, Object> usertask = new HashMap<>();
                                usertask.put(uid,task);
                                grouptaskuser.updateChildren(usertask);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d(TAG,"Couldn't add group task");
                        }
                    });






                    Toast.makeText(mContext, "You got a new task", Toast.LENGTH_SHORT).show();
                }

                else {
                    Toast.makeText(mContext,"You removed the task",Toast.LENGTH_SHORT).show();

                    /*// Deleting the task from current tasks db and adding to solved
                    String curUser = mRef.getKey();
                    DatabaseReference mainRef = mRef.getRoot();
                    DatabaseReference solvedTasksRef = mainRef.child("solved_tasks").child(curUser);
                    solvedTasksRef.push().setValue(myGroupTasks.get(holder.getAdapterPosition()));
                    mRef.child(myGroupTaskIds.get(holder.getAdapterPosition())).removeValue();
                    // getAdapterPosition() can cause some errors:(*/
                }
            }
        });


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClicked(v, myGroupTasks.get(holder.getAdapterPosition()).description);
            }
        });

    }




    // Create new views (invoked by the layout manager)
    public GroupTaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.group_task_layout, parent, false);

        // set the view's size, margins, paddings and layout parameters

        return new GroupTaskViewHolder(view);
    }

}

