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
import com.tylersuehr.chips.ChipView;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;

public class ImportantAdapter extends RecyclerView.Adapter<ImportantAdapter.ImportantViewHolder> {

    private static final String TAG = "ImportantAdapter";

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
    private DatabaseReference mRef;

    private List<String> myImportantIds = new ArrayList<>();
    private List<myTask> myImportant = new ArrayList<>();


    ImportantAdapter(Context context, DatabaseReference ref) {

        mContext = context;
        mRef = ref;


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
                    myImportantIds.add(dataSnapshot.getKey());
                    myImportant.add(mytask);

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

                int taskIndex = myImportantIds.indexOf(taskKey);
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
                        myImportantIds.remove(taskIndex);

                        //Update Recycleview
                        notifyItemRemoved(taskIndex);
                    }

                } else {
                    // Check if this task is now important for user
                    if (important) {

                        // A new task has been added, add it to the displayed list
                        myTask mytask = dataSnapshot.getValue(myTask.class);

                        // Update
                        myImportantIds.add(dataSnapshot.getKey());
                        myImportant.add(mytask);

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

                int taskIndex = myImportantIds.indexOf(taskKey);
                if (taskIndex > -1) {
                    // Remove data from the list
                    myImportant.remove(taskIndex);
                    myImportantIds.remove(taskIndex);

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
                Log.w(TAG, "ImportantTasks:onCancelled", databaseError.toException());
                Toast.makeText(mContext, "Failed to load important tasks.", Toast.LENGTH_SHORT).show();
            }
        };
        ref.addChildEventListener(childEventListener);
    }

    @Override
    public void onBindViewHolder(final ImportantViewHolder holder, final int position) {

        holder.taskView.setText(myImportant.get(position).task_name);

        holder.mChipsView.setVisibility(GONE);


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
                            case R.id.edit_task:
                                /// Check that the task isn't a group task
                                if (!(myImportant.get(holder.getAdapterPosition()) instanceof myGroupTask)) {
                                /*
                                    Intent intent = new Intent(mContext, TaskEditingActivity.class);
                                    intent.putExtra(EXTRA_TASK_STATE, "Edit");

                                    String taskID = solvedIds.get(holder.getAdapterPosition());
                                    intent.putExtra("task_id", taskID);

                                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                    intent.putExtra("path", "/solved_tasks/" + uid + "/");
                                    view.getContext().startActivity(intent); */
                                }
                                else
                                    Toast.makeText(mContext, "It is a group task. You can't change it!", Toast.LENGTH_SHORT).show();
                                return true;
                            case R.id.delete_task:
                                AlertDialog.Builder builder;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    builder = new AlertDialog.Builder(mContext, android.R.style.Theme_Material_Dialog_Alert);
                                } else {
                                    builder = new AlertDialog.Builder(mContext);
                                }
                                builder.setTitle("Delete task")
                                        .setMessage("Are you sure you want to delete this task?")
                                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                // continue with delete
                                                String task_id = myImportantIds.get(holder.getAdapterPosition());
                                                mRef.child(task_id).removeValue();

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
                                if (myImportant.get(position).important)
                                    mRef.child(myImportantIds.get(position)).child("important").setValue(false);
                            default:
                                return false;
                        }
                    }
                });
                popup.show();
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
