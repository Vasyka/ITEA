package com.productions.itea.motivatedev;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.support.v7.app.AlertDialog;
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

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.tylersuehr.chips.ChipView;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;

public class ImportantAdapter extends RecyclerView.Adapter<ImportantAdapter.ImportantViewHolder> {

    private static final String TAG = "ImportantAdapter";
    private OnTaskItemClickListener listener;

    public interface OnTaskItemClickListener {
        void onItemClicked(View v, String descr);
    }

    static class ImportantViewHolder extends RecyclerView.ViewHolder {
        TextView taskView;
        ImageButton menuImageButton;
        ChipView mChipsView;

        ImportantViewHolder(View itemView) {
            super(itemView);
            taskView = itemView.findViewById(R.id.my_text_view_solved);
            menuImageButton = itemView.findViewById(R.id.task_menu);
            mChipsView = (ChipView) itemView.findViewById(R.id.solved_chip);
        }
    }


    private Context mContext;
    private DatabaseReference userTaskRef;
    private DatabaseReference groupTaskRef;

    private List<String> importantIds = new ArrayList<>();
    private List<myTask> myImportant = new ArrayList<>();

    private List<myGroup> myGroups = new ArrayList<>();


    ImportantAdapter(Context context, DatabaseReference solvedRef, DatabaseReference groupRef, OnTaskItemClickListener listener) {

        mContext = context;
        userTaskRef = solvedRef;
        groupTaskRef = groupRef;
        this.listener = listener;

        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                Log.d(TAG, "onChildAdded:" + dataSnapshot.child("important").getValue(Boolean.class));

                // Check that new task is important for user
                Boolean important = dataSnapshot.child("important").getValue(Boolean.class);

                if (important) {

                // A new task has been added, add it to the displayed list
                myTask mytask = dataSnapshot.getValue(myTask.class);

                    // Update
                    importantIds.add(dataSnapshot.getKey());
                    myImportant.add(mytask);
                    myGroups.add(new myGroup("", "", null));

                    //Update Recycleview
                    notifyItemInserted(myImportant.size() - 1);
                }
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());

                // A task has changed, use the key to determine if we are displaying this
                // task and is it still important if so displayed the changed task.

                String taskKey = dataSnapshot.getKey();
                Boolean important = dataSnapshot.child("important").getValue(Boolean.class);

                Log.d(TAG, "onChildAdded:" + important);

                int taskIndex = importantIds.indexOf(taskKey);
                if (taskIndex > -1) {

                    // Check that this task is still important for user
                    if (important) {

                        myTask mytask = dataSnapshot.getValue(myTask.class);

                        // Replace with the new data
                        myImportant.set(taskIndex, mytask);

                        //Update Recycleview
                        notifyItemChanged(taskIndex);
                    }
                    else { // task isn't important now

                        // Remove data from the list
                        myImportant.remove(taskIndex);
                        importantIds.remove(taskIndex);
                        myGroups.remove(taskIndex);

                        //Update Recycleview
                        notifyItemRemoved(taskIndex);
                     }

                } else {
                    // Check if this task is now important for user
                    if (important) {

                        // A new task has been added, add it to the displayed list
                        myTask mytask = dataSnapshot.getValue(myTask.class);

                        // Update
                        importantIds.add(dataSnapshot.getKey());
                        myImportant.add(mytask);
                        myGroups.add(new myGroup("", "", null));

                        //Update Recycleview
                        notifyItemInserted(myImportant.size() - 1);
                    }
                    else {
                            Log.w(TAG, "onChildChanged:unknown_child:" + taskKey);
                        }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

                // A task has changed, use the key to determine if we are displaying this
                // task and if so remove it.
                String taskKey = dataSnapshot.getKey();

                int taskIndex = importantIds.indexOf(taskKey);
                if (taskIndex > -1) {
                    // Remove data from the list
                    myImportant.remove(taskIndex);
                    importantIds.remove(taskIndex);
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
            }
        };

        ChildEventListener groupChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                final String groupTaskKey = dataSnapshot.getKey();

                Log.d(TAG, "onSolvedGroupTaskChildAdded:" + groupTaskKey);

                // A new task has been added, add it to the displayed list

                final String groupKey = dataSnapshot.child("group").getValue(String.class);
                Log.d(TAG, "onSolvedGroupTaskChildAdded:group" + groupKey);

                final Boolean important = dataSnapshot.child("important").getValue(Boolean.class);
                Log.d(TAG, String.valueOf(important));

                if (important) {

                    // get group task's information
                    DatabaseReference myGroupRef = groupTaskRef.getRoot().child("group_tasks").child(groupKey).child(groupTaskKey);
                    myGroupRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                myGroupTask task = dataSnapshot.getValue(myGroupTask.class);
                                task.important = important;
                                myImportant.add(task);
                                importantIds.add(groupTaskKey);
                                notifyDataSetChanged();
                            } else
                                Log.d(TAG, "Oops. Group task with id " + groupTaskKey + " doesn't exist now");
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d(TAG, "getSolvedGroupTask:onCancelled:", databaseError.toException());
                        }
                    });

                    // get group info
                    DatabaseReference groupRef = groupTaskRef.getRoot().child("groups").child(groupKey);
                    groupRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                myGroup group = dataSnapshot.getValue(myGroup.class);
                                myGroups.add(group);
                                notifyDataSetChanged();
                            } else
                                Log.d(TAG, "Oops. Group with id " + groupKey + " doesn't exist now");
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d(TAG, "getGroupTask:onCancelled:", databaseError.toException());
                        }
                    });


                    //Update Recycleview
                    notifyItemInserted(myImportant.size() - 1);
                    Log.d(TAG, "ok");
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onGroupTaskChildChanged:" + dataSnapshot.getKey());

                // A task has changed, use the key to determine if we are displaying this
                // task and if so displayed the changed task.

                final String groupKey = dataSnapshot.child("group").getValue(String.class);
                Log.d(TAG, "onSolvedGroupTaskChildAdded:group" + groupKey);

                final String groupTaskKey = dataSnapshot.getKey();
                final Boolean important = dataSnapshot.child("important").getValue(Boolean.class);
                

                int taskIndex = importantIds.indexOf(groupTaskKey);

                if (taskIndex > -1) {

                    // Check that this task is still important for user
                    if (important) {

                        myImportant.get(taskIndex).important = important;

                        //Update Recycleview
                        notifyItemChanged(taskIndex);
                    } else { // task isn't important now

                        // Remove data from the list
                        myImportant.remove(taskIndex);
                        importantIds.remove(taskIndex);
                        myGroups.remove(taskIndex);

                        //Update Recycleview
                        notifyItemRemoved(taskIndex);
                    }
                } else {
                    // Check if this task is now important for user
                    if (important) {

                        // get group task's information
                        DatabaseReference myGroupRef = groupTaskRef.getRoot().child("group_tasks").child(groupKey).child(groupTaskKey);
                        myGroupRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    myGroupTask task = dataSnapshot.getValue(myGroupTask.class);
                                    task.important = important;
                                    myImportant.add(task);
                                    importantIds.add(groupTaskKey);
                                    notifyDataSetChanged();
                                }
                                else
                                    Log.d(TAG,"Oops. Group task with id " + groupTaskKey + " doesn't exist now");
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.d(TAG, "getSolvedGroupTask:onCancelled:", databaseError.toException() );
                            }
                        });

                        // get group info
                        DatabaseReference groupRef = groupTaskRef.getRoot().child("groups").child(groupKey);
                        groupRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    myGroup group = dataSnapshot.getValue(myGroup.class);
                                    myGroups.add(group);
                                    notifyDataSetChanged();
                                }
                                else
                                    Log.d(TAG,"Oops. Group with id " + groupKey + " doesn't exist now");
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.d(TAG, "getGroupTask:onCancelled:", databaseError.toException() );
                            }
                        });

                        //Update Recycleview
                        notifyItemInserted(myImportant.size() - 1);
                    } else {
                        Log.w(TAG, "onGroupTaskChildChanged:unknown_child:" + groupTaskKey);
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onGroupTaskChildRemoved:" + dataSnapshot.getKey());

                // A task has changed, use the key to determine if we are displaying this
                // task and if so remove it.
                String groupTaskKey = dataSnapshot.getKey();

                int taskIndex = importantIds.indexOf(groupTaskKey);
                if (taskIndex > -1) {
                    // Remove data from the list
                    myImportant.remove(taskIndex);
                    importantIds.remove(taskIndex);
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
            }
        };

        groupTaskRef.addChildEventListener(groupChildEventListener);
        userTaskRef.addChildEventListener(childEventListener);
    }

    @Override
    public void onBindViewHolder(final ImportantViewHolder holder, int position) {

        holder.taskView.setText(myImportant.get(holder.getAdapterPosition()).task_name);

        if (myImportant.get(holder.getAdapterPosition()) instanceof myGroupTask) {
            holder.mChipsView.setVisibility(View.VISIBLE);
            holder.mChipsView.setTitle(myGroups.get(holder.getAdapterPosition()).group_name);
            holder.mChipsView.setHasAvatarIcon(false);
            holder.mChipsView.setDeletable(false);

            // Avatar!
            //holder.mChipsView.setAvatarIcon(Uri.parse("android.resource://com.productions.itea.motivatedev/" + R.mipmap.cat_tea));
        }
        else {
            holder.mChipsView.setVisibility(GONE);
        }


        // Menu
        holder.menuImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(mContext, holder.menuImageButton);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.imp_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    // Handle clicks on menu items
                    public boolean onMenuItemClick (MenuItem menuItem){
                        switch (menuItem.getItemId()) {
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
                                                String task_id = importantIds.get(holder.getAdapterPosition());
                                                if (myImportant.get(holder.getAdapterPosition()) instanceof myGroupTask)
                                                    groupTaskRef.child(task_id).removeValue();
                                                else
                                                    userTaskRef.child(task_id).removeValue();
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
                            case R.id.un_important_task: // Make the task unimportant if it is important
                                if (myImportant.get(holder.getAdapterPosition()).important) {
                                    if (myImportant.get(holder.getAdapterPosition()) instanceof myGroupTask)
                                        groupTaskRef.child(importantIds.get(holder.getAdapterPosition())).child("important").setValue(false);
                                    else
                                        userTaskRef.child(importantIds.get(holder.getAdapterPosition())).child("important").setValue(false);
                                }

                            default:
                                return false;
                        }
                    }
                });
                popup.show();
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClicked(v, myImportant.get(holder.getAdapterPosition()).description);
            }
        });
    }

    @Override
    public int getItemCount() {
        return myImportant.size();
    }

    @Override
    public ImportantViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.solved_layout, parent, false);

        return new ImportantAdapter.ImportantViewHolder(view);
    }

}
