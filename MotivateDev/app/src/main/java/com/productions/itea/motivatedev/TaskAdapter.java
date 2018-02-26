package com.productions.itea.motivatedev;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
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

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.tylersuehr.chips.ChipView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.TRANSLATION_X;


class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder>{

    private static final String TAG = "TaskAdapter";
    static final String EXTRA_TASK_STATE = "task_state";

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskView;
        Button doneButton;
        ImageButton menuImageButton;
        ChipView mChipsView;


        TaskViewHolder(View itemView) {
            super(itemView);
            taskView = itemView.findViewById(R.id.my_text_view);
            doneButton = itemView.findViewById(R.id.done_button);
            menuImageButton = itemView.findViewById(R.id.task_menu);
            mChipsView = (ChipView) itemView.findViewById(R.id.chip);
        }
    }

    private Context mContext;
    private DatabaseReference curTaskRef;
    private DatabaseReference groupTaskRef;

    private List<String> myTaskIds = new ArrayList<>();
    private List<myTask> myTasks = new ArrayList<>();

    private List<myGroup> myGroups = new ArrayList<>();


    TaskAdapter(Context context, DatabaseReference curtaskref, DatabaseReference grouptaskref) {

        mContext = context;
        curTaskRef = curtaskref;
        groupTaskRef = grouptaskref;

        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                // A new task has been added, add it to the displayed list
                myTask mytask = dataSnapshot.getValue(myTask.class);

                // Update
                myTaskIds.add(dataSnapshot.getKey());
                myTasks.add(mytask);
                myGroups.add(new myGroup("","",null));

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
                    myGroups.remove(taskIndex);

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

        ChildEventListener groupChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                final String groupTaskKey = dataSnapshot.getKey();
                Log.d(TAG, "onGroupTaskChildAdded:" + groupTaskKey);

                // A new task has been added, add it to the displayed list

                String groupKey = dataSnapshot.child("group").getValue(String.class);
                Log.d(TAG, "onGroupTaskChildAdded:group" + groupKey);



                // get group task's information
                DatabaseReference myGroupRef = groupTaskRef.getRoot().child("group_tasks").child(groupKey).child(groupTaskKey);

                myGroupRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        myGroupTask task = dataSnapshot.getValue(myGroupTask.class);
                        myTasks.add(task);
                        myTaskIds.add(groupTaskKey);
                        notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, "getGroupTask:onCancelled:", databaseError.toException() );
                    }
                });

                // get group info
                DatabaseReference groupRef = groupTaskRef.getRoot().child("groups").child(groupKey);
                groupRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        myGroup group = dataSnapshot.getValue(myGroup.class);
                        myGroups.add(group);
                        notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, "getGroupTask:onCancelled:", databaseError.toException() );
                    }
                });

                //Update Recycleview
                notifyItemInserted(myTasks.size() - 1);
                Log.d(TAG,"ok");
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onGroupTaskChildChanged:" + dataSnapshot.getKey());

                // A task has changed, use the key to determine if we are displaying this
                // task and if so displayed the changed task.

                String groupTaskKey = dataSnapshot.getKey();

                int taskIndex = myTaskIds.indexOf(groupTaskKey);
                if (taskIndex > -1) {

                    myTasks.get(taskIndex).important = dataSnapshot.child(groupTaskKey).child("imprtant").getValue(Boolean.class);

                    String groupKey = dataSnapshot.child(groupTaskKey).child("group").getValue(String.class);
                    //if (!myGroups.get(taskIndex).equals(groupKey))
                    Log.w(TAG, "onGroupTaskChildChanged::unknown_group_key" + groupKey);

                    //Update Recycleview
                    notifyItemChanged(taskIndex);
                }
                else {
                    Log.w(TAG, "onGroupTaskChildChanged:unknown_child:" + groupTaskKey);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onGroupTaskChildRemoved:" + dataSnapshot.getKey());

                // A task has changed, use the key to determine if we are displaying this
                // task and if so remove it.
                String groupTaskKey = dataSnapshot.getKey();

                int taskIndex = myTaskIds.indexOf(groupTaskKey);
                if (taskIndex > -1) {
                    // Remove data from the list
                    myTasks.remove(taskIndex);
                    myTaskIds.remove(taskIndex);
                    myGroups.remove(taskIndex);

                    //Update Recycleview
                    notifyItemRemoved(taskIndex);

                } else {
                    Log.w(TAG, "onGroupTaskChildChanged:unknown_child:" + groupTaskKey);
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onGroupTaskChildMoved:" + dataSnapshot.getKey());

                // A task has changed, use the key to determine if we are displaying this
                // task and if so move it.
                myTask mytask = dataSnapshot.getValue(myGroupTask.class);
                String groupTaskKey = dataSnapshot.getKey();

                // ...
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "UserGroupTasks:onCancelled", databaseError.toException());
                Toast.makeText(mContext, "Failed to load user's group tasks.", Toast.LENGTH_SHORT).show();
            }
        };

        groupTaskRef.addChildEventListener(groupChildEventListener);
        curTaskRef.addChildEventListener(childEventListener);
    }

    public int getItemCount() {
        return myTasks.size();
    }

    // Place item[position] in holder
    public void onBindViewHolder(final TaskViewHolder holder, int position) {
        myTask task = myTasks.get(position);
        holder.taskView.setText(task.task_name);

        if (myTasks.get(position) instanceof myGroupTask) {
            holder.mChipsView.setVisibility(View.VISIBLE);
            holder.mChipsView.setTitle(myGroups.get(position).group_name);
            holder.mChipsView.setHasAvatarIcon(false);
            holder.mChipsView.setDeletable(false);

            // Avatar!
            //holder.mChipsView.setAvatarIcon(Uri.parse("android.resource://com.productions.itea.motivatedev/" + R.mipmap.cat_tea));
        }
        else {
            holder.mChipsView.setVisibility(GONE);
        }

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

                DatabaseReference mainRef = curTaskRef.getRoot();
                String curUser = curTaskRef.getKey();
                DatabaseReference solvedTasksRef = mainRef.child("solved_tasks").child(curUser);
                solvedTasksRef.push().setValue(myTasks.get(holder.getAdapterPosition()));

                // Check that the task is a group task
                if (myTasks.get(holder.getAdapterPosition()) instanceof myGroupTask) {
                    groupTaskRef.child(myTaskIds.get(holder.getAdapterPosition())).removeValue();

                    // Here must be rating counting!

                } else
                    curTaskRef.child(myTaskIds.get(holder.getAdapterPosition())).removeValue();

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
                                Log.d(TAG, String.valueOf(holder.getAdapterPosition()));

                                // Check that the task isn't a group task
                                if (!(myTasks.get(holder.getAdapterPosition()) instanceof myGroupTask)) {

                                    Log.d(TAG,curTaskRef.getKey());
                                    Log.d(TAG, myTaskIds.get(holder.getAdapterPosition()));
                                    Log.d(TAG,curTaskRef.child(myTaskIds.get(holder.getAdapterPosition())) == null ? "null" : "mya");

                                    Intent intent = new Intent(mContext, TaskEditingActivity.class);
                                    intent.putExtra(EXTRA_TASK_STATE, "Edit");

                                    String taskID = myTaskIds.get(holder.getAdapterPosition());
                                    intent.putExtra("task_id", taskID);

                                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                    intent.putExtra("path", "/curr_tasks/" + uid + "/");
                                    view.getContext().startActivity(intent);
                                }
                                else
                                    Toast.makeText(mContext, "Это групповое задние. Вы не можете его изменить!", Toast.LENGTH_SHORT).show();

                                return true;
                            case R.id.delete_task:
                                AlertDialog.Builder builder;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    builder = new AlertDialog.Builder(mContext, android.R.style.Theme_Material_Dialog_Alert);
                                } else {
                                    builder = new AlertDialog.Builder(mContext);
                                }
                                builder.setTitle("Удалить задачу")
                                        .setMessage("Вы уверены, что хотите удалить эту задачу?")
                                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                // continue with delete
                                                String task_id = myTaskIds.get(holder.getAdapterPosition());
                                                if (myTasks.get(holder.getAdapterPosition()) instanceof myGroupTask)
                                                    groupTaskRef.child(task_id).removeValue();
                                                else
                                                    curTaskRef.child(task_id).removeValue();
                                            }
                                        })
                                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                // do nothing
                                            }
                                        })
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .show();
                                return true;
                            case R.id.important_task: // Make the task important if it isn't important
                                if (!myTasks.get(holder.getAdapterPosition()).important)
                                    curTaskRef.child(myTaskIds.get(holder.getAdapterPosition())).child("important").setValue(true);
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
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.task_layout, parent, false);

        // set the view's size, margins, paddings and layout parameters

        return new TaskViewHolder(view);
    }

}
