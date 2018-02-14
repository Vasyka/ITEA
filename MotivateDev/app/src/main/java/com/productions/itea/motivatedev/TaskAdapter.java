package com.productions.itea.motivatedev;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;


class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder>{

    private static final String TAG = "MainActivity";
    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskView;
        Button doneButton;

        TaskViewHolder(View itemView) {
            super(itemView);
            taskView = itemView.findViewById(R.id.my_text_view);
            doneButton = itemView.findViewById(R.id.done_button);
        }
    }

    private Context mContext;
    private DatabaseReference mRef;

    private List<String> myTaskIds = new ArrayList<>();
    private List<myTask> myTasks = new ArrayList<>();


    public TaskAdapter(Context context, DatabaseReference ref) {

        mContext = context;
        mRef = ref;


        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                // A new task has been added, add it to the displayed list
                myTask mytask = dataSnapshot.getValue(myTask.class);

                // Update
                myTaskIds.add(dataSnapshot.getKey());
                myTasks.add(mytask);

                //Update Recycleview
                notifyItemInserted(myTasks.size() - 1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());

                // A task has changed, use the key to determine if we are displaying this
                // task and if so displayed the changed task.
                myTask mytask = dataSnapshot.getValue(myTask.class);

                String taskKey = dataSnapshot.getKey();

                int taskIndex = myTaskIds.indexOf(taskKey);
                if (taskIndex > -1) {
                    // Replace with the new data
                    myTasks.set(taskIndex, mytask);

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

                int taskIndex = myTaskIds.indexOf(taskKey);
                if (taskIndex > -1) {
                    // Remove data from the list
                    myTasks.remove(taskIndex);
                    myTaskIds.remove(taskIndex);

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
                myTask mytask = dataSnapshot.getValue(myTask.class);
                String taskKey = dataSnapshot.getKey();

                // ...
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Tasks:onCancelled", databaseError.toException());
                Toast.makeText(mContext, "Failed to load tasks.", Toast.LENGTH_SHORT).show();
            }
        };
        ref.addChildEventListener(childEventListener);
    }

    public int getItemCount() {
        return myTasks.size();
    }

    // Place item[position] in holder
    public void onBindViewHolder(TaskViewHolder holder, int position) {
        holder.taskView.setText(myTasks.get(position).task_name);

        holder.doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Congratulations dialog
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
                dialogBuilder.setMessage("Task was done!").
                        setTitle("Congratulations!!").setIcon(R.mipmap.confetti1);
                dialogBuilder.create().show();

                // Deleting the task from current tasks db and adding to solved
                String curUser = mRef.getKey();
                DatabaseReference mainRef = mRef.getRoot();
                DatabaseReference solvedTasksRef = mainRef.child("solved_tasks").child(curUser);
                //myTask task = solvedTasksRef.
            }
        });

    }

    // Create new views (invoked by the layout manager)
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.task_layout, parent, false);

        // set the view's size, margins, paddings and layout parameters

        return new TaskViewHolder(view);
    }
}