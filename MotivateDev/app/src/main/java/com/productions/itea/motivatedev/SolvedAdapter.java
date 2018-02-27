package com.productions.itea.motivatedev;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import com.tylersuehr.chips.ChipView;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;

public class SolvedAdapter extends RecyclerView.Adapter<SolvedAdapter.SolvedViewHolder> {

    private static final String TAG = "SolvedTaskAdapter";
    static final String EXTRA_TASK_STATE = "task_state";
    private OnTaskItemClickListener listener;

    public interface OnTaskItemClickListener {
        void onItemClicked(View v, String descr);
    }
    static class SolvedViewHolder extends RecyclerView.ViewHolder {

        TextView solvedView;
        public ImageButton menuImageButton;
        ChipView mChipsView;


        SolvedViewHolder(View itemView) {
            super(itemView);
            solvedView = itemView.findViewById(R.id.my_text_view_solved);
            menuImageButton = itemView.findViewById(R.id.task_menu);
            mChipsView = (ChipView) itemView.findViewById(R.id.solved_chip);
        }
    }



    private Context mContext;
    private DatabaseReference userTaskRef;
    private DatabaseReference groupTaskRef;

    private List<String> solvedIds = new ArrayList<>();
    private List<myTask> mySolved = new ArrayList<>();

    private List<myGroup> myGroups = new ArrayList<>();

    public SolvedAdapter(Context context, DatabaseReference solvedRef, DatabaseReference groupRef, OnTaskItemClickListener listener) {

        mContext = context;
        userTaskRef = solvedRef;
        groupTaskRef = groupRef;
        this.listener = listener;


        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                // A new task has been added, add it to the displayed list
                myTask mytask = dataSnapshot.getValue(myTask.class);

                // Update
                solvedIds.add(dataSnapshot.getKey());
                mySolved.add(mytask);
                myGroups.add(new myGroup("","",null));

                //Update Recycleview
                notifyItemInserted(mySolved.size() - 1);
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());

                // A task has changed, use the key to determine if we are displaying this
                // task and if so displayed the changed task.
                myTask mytask = dataSnapshot.getValue(myTask.class);

                String taskKey = dataSnapshot.getKey();

                int taskIndex = solvedIds.indexOf(taskKey);
                if (taskIndex > -1) {
                    // Replace with the new data
                    mySolved.set(taskIndex, mytask);

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

                int taskIndex = solvedIds.indexOf(taskKey);
                if (taskIndex > -1) {
                    // Remove data from the list
                    mySolved.remove(taskIndex);
                    solvedIds.remove(taskIndex);
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

                final Boolean important = dataSnapshot.child("important").getValue(Boolean.class);

                final String groupKey = dataSnapshot.child("group").getValue(String.class);
                Log.d(TAG, "onSolvedGroupTaskChildAdded:group" + groupKey);

                // get group task's information
                DatabaseReference myGroupRef = groupTaskRef.getRoot().child("group_tasks").child(groupKey).child(groupTaskKey);
                myGroupRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            myGroupTask task = dataSnapshot.getValue(myGroupTask.class);
                            task.important = important;
                            mySolved.add(task);
                            solvedIds.add(groupTaskKey);
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
                notifyItemInserted(mySolved.size() - 1);
                Log.d(TAG,"ok");
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onGroupTaskChildChanged:" + dataSnapshot.getKey());

                // A task has changed, use the key to determine if we are displaying this
                // task and if so displayed the changed task.

                String groupTaskKey = dataSnapshot.getKey();
                Boolean important = dataSnapshot.child("important").getValue(Boolean.class);


                int taskIndex = solvedIds.indexOf(groupTaskKey);
                if (taskIndex > -1) {


                    mySolved.get(taskIndex).important = important;

                    String groupKey = dataSnapshot.child(groupTaskKey).child("group").getValue(String.class);
                    //if (!myGroups.get(taskIndex).equals(groupKey))
                    //    Log.w(TAG, "onGroupTaskChildChanged::unknown_group_key" + groupKey);

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

                int taskIndex = solvedIds.indexOf(groupTaskKey);
                if (taskIndex > -1) {
                    // Remove data from the list
                    mySolved.remove(taskIndex);
                    solvedIds.remove(taskIndex);
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
    public SolvedViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.solved_layout, parent, false);

        // set the view's size, margins, paddings and layout parameters

        return new SolvedAdapter.SolvedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final SolvedViewHolder holder, int position) {
        holder.solvedView.setText(mySolved.get(holder.getAdapterPosition()).task_name);


        if (mySolved.get(holder.getAdapterPosition()) instanceof myGroupTask) {
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
            public void onClick(final View view) {
                PopupMenu popup = new PopupMenu(mContext, holder.menuImageButton);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.solved_task_menu, popup.getMenu());

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
                                                String task_id = solvedIds.get(holder.getAdapterPosition());
                                                if (mySolved.get(holder.getAdapterPosition()) instanceof myGroupTask)
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
                            case R.id.important_task: // Make the task important if it isn't important
                                Log.d(TAG,String.valueOf(holder.getAdapterPosition()));
                                Log.d(TAG,solvedIds.get(holder.getAdapterPosition()));
                                Log.d(TAG,mySolved.get(holder.getAdapterPosition()).task_name);
                                Log.d(TAG,String.valueOf(mySolved.get(holder.getAdapterPosition()).important));
                                if (mySolved.get(holder.getAdapterPosition()) == null)
                                    Log.d(TAG, "FJSJDKGH");
                                if (!mySolved.get(holder.getAdapterPosition()).important) {
                                    if (mySolved.get(holder.getAdapterPosition()) instanceof myGroupTask)
                                        groupTaskRef.child(solvedIds.get(holder.getAdapterPosition())).child("important").setValue(true);
                                    else
                                        userTaskRef.child(solvedIds.get(holder.getAdapterPosition())).child("important").setValue(true);
                                }
                                return true;
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
                listener.onItemClicked(v, mySolved.get(holder.getAdapterPosition()).description);
            }
        });
    }

    @Override
    public int getItemCount() {
      return mySolved.size();
    }



}
