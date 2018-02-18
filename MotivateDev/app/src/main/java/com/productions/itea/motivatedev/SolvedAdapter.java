package com.productions.itea.motivatedev;


import android.content.Context;
import android.support.v4.app.FragmentActivity;
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

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

public class SolvedAdapter extends RecyclerView.Adapter<SolvedAdapter.SolvedViewHolder> {

    private static final String TAG = "SolvedTaskAdapter";
    static class SolvedViewHolder extends RecyclerView.ViewHolder {

        TextView solvedView;
        public ImageButton menuImageButton;


        SolvedViewHolder(View itemView) {
            super(itemView);
            solvedView = itemView.findViewById(R.id.my_text_view_solved);
            menuImageButton = itemView.findViewById(R.id.task_menu);
        }
    }



    private Context mContext;
    private DatabaseReference mRef;

    private List<String> solvedIds = new ArrayList<>();
    private List<myTask> mySolved = new ArrayList<>();


    public SolvedAdapter(Context context, DatabaseReference solvedRef) {

        mContext = context;
        mRef = solvedRef;


        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                // A new task has been added, add it to the displayed list
                myTask mytask = dataSnapshot.getValue(myTask.class);

                // Update
                solvedIds.add(dataSnapshot.getKey());
                mySolved.add(mytask);

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
        solvedRef.addChildEventListener(childEventListener);


    }

    @Override
    public SolvedViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.solved_layout, parent, false);

        // set the view's size, margins, paddings and layout parameters

        return new SolvedAdapter.SolvedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final SolvedViewHolder holder, final int position) {
        holder.solvedView.setText(mySolved.get(position).task_name);

        // Menu
        holder.menuImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(mContext, holder.menuImageButton);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.task_menu, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    // Handle clicks on menu items
                    public boolean onMenuItemClick (MenuItem menuItem){
                        switch (menuItem.getItemId()) {
                            case R.id.edit_task:
                                return true;
                            case R.id.delete_task:
                                return true;
                            case R.id.important_task: // Make the task important if it isn't important
                                if (!mySolved.get(position).important)
                                    mRef.child(solvedIds.get(position)).child("important").setValue(true);
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

    @Override
    public int getItemCount() {
      return mySolved.size();
    }



}
