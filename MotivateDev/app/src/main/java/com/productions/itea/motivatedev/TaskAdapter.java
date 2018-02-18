package com.productions.itea.motivatedev;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder>{

    private static final String TAG = "TaskAdapter";
    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskView;
        Button doneButton;
        ImageButton menuImageButton;

        TaskViewHolder(View itemView) {
            super(itemView);
            taskView = itemView.findViewById(R.id.my_text_view);
            doneButton = itemView.findViewById(R.id.done_button);
            menuImageButton = itemView.findViewById(R.id.task_menu);
        }
    }

    private Context mContext;
    private DatabaseReference mRef;

    private List<String> myTaskIds = new ArrayList<>();
    private List<myTask> myTasks = new ArrayList<>();


    TaskAdapter(Context context, DatabaseReference ref) {

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
    public void onBindViewHolder(final TaskViewHolder holder, final int position) {
        holder.taskView.setText(myTasks.get(position).task_name);

        // "Done"
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
                solvedTasksRef.push().setValue(myTasks.get(holder.getAdapterPosition()));
                mRef.child(myTaskIds.get(holder.getAdapterPosition())).removeValue();
                // getAdapterPosition() can cause some errors:(
            }
        });

        // Menu
        holder.menuImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                PopupMenu popup = new PopupMenu(mContext, holder.menuImageButton);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.task_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    // Handle clicks on menu items
                    public boolean onMenuItemClick (MenuItem menuItem){
                        switch (menuItem.getItemId()) {
                            case R.id.edit_task:

                                Intent intent = new Intent(mContext, TaskEditingActivity.class);
                                String taskID = myTaskIds.get(holder.getAdapterPosition());
                                intent.putExtra("task_uid", taskID);
                                intent.putExtra("Add", "");
                                intent.putExtra("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                view.getContext().startActivity(intent);

                                return true;
                            case R.id.delete_task:
                                return true;
                            case R.id.important_task: // Make the task important if it isn't important
                                if (!myTasks.get(position).important)
                                    mRef.child(myTaskIds.get(position)).child("important").setValue(true);
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                popup.show();
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
