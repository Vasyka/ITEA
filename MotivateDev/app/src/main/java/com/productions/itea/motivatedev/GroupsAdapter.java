package com.productions.itea.motivatedev;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;


class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.GroupViewHolder>{

private static final String TAG = "MyGroupsFragment";

static class GroupViewHolder extends RecyclerView.ViewHolder {

    TextView groupView;
    ImageView groupPhotoView;

    GroupViewHolder(View itemView) {
        super(itemView);
        groupView = itemView.findViewById(R.id.group_name);
        groupPhotoView = itemView.findViewById(R.id.group_photo);
    }
}

    private Context mContext;
    public DatabaseReference mRef;

    private List<String> myGroupsIds = new ArrayList<>();
    private List<myGroup> myGroups = new ArrayList<>();


    public GroupsAdapter(Context context, DatabaseReference ref) {

        mContext = context;
        mRef = ref;


        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                // A new group has been added, add it to the displayed list
                String groupKey = dataSnapshot.getKey();
                myGroupsIds.add(groupKey);

                DatabaseReference myGroupRef = mRef.getRoot().child("groups").child(groupKey);
                myGroupRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        myGroup mygroup = dataSnapshot.getValue(myGroup.class);
                        myGroups.add(mygroup);
                        notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, "getGroup:onCancelled:", databaseError.toException() );
                    }
                });

                //Update Recycleview
                notifyItemInserted(myGroups.size() - 1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());

                // A group has changed, use the key to determine if we are displaying this
                // group and if so displayed the changed group.
                String groupKey = dataSnapshot.getKey();

                final int groupIndex = myGroupsIds.indexOf(groupKey);
                if (groupIndex > -1) {

                    // Replace with the new data
                    DatabaseReference myGroupRef = mRef.getRoot().child("groups").child(groupKey);
                    myGroupRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            myGroup mygroup = dataSnapshot.getValue(myGroup.class);
                            myGroups.set(groupIndex, mygroup);
                            notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d(TAG, "getGroup:onCancelled:", databaseError.toException());
                        }
                    });

                    //Update Recycleview
                    notifyItemChanged(groupIndex);

                } else {
                    Log.w(TAG, "onChildChanged:unknown_child:" + groupKey);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

                // A group has changed, use the key to determine if we are displaying this
                // group and if so remove it.
                String groupKey = dataSnapshot.getKey();

                int groupIndex = myGroupsIds.indexOf(groupKey);
                if (groupIndex > -1) {
                    // Remove data from the list
                    myGroups.remove(groupIndex);
                    myGroupsIds.remove(groupIndex);

                    //Update Recycleview
                    notifyItemRemoved(groupIndex);

                } else {
                    Log.w(TAG, "onChildChanged:unknown_child:" + groupKey);
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());

                // A group has changed, use the key to determine if we are displaying this
                // group and if so move it.
                myGroup mygroup = dataSnapshot.getValue(myGroup.class);
                String groupKey = dataSnapshot.getKey();

                // ...
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Groups:onCancelled", databaseError.toException());
                Toast.makeText(mContext, "Failed to load groups.", Toast.LENGTH_SHORT).show();
            }
        };
        ref.addChildEventListener(childEventListener);
    }

    public int getItemCount() {
        return myGroups.size();
    }

    // Place item[position] in holder
    public void onBindViewHolder(GroupsAdapter.GroupViewHolder holder, int position) {
        holder.groupView.setText(myGroups.get(position).group_name);
        String photo_url = myGroups.get(position).photoUrl;
        if (photo_url != null)
            holder.groupPhotoView.setImageURI(Uri.parse(myGroups.get(position).photoUrl));
        else
            Log.d(TAG, "OOOOO");
    }

    // Create new views (invoked by the layout manager)
    public GroupsAdapter.GroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.group_layout, parent, false);

        // set the view's size, margins, paddings and layout parameters

        return new GroupViewHolder(view);
    }

}