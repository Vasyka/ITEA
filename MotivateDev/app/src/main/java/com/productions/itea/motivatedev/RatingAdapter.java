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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class RatingAdapter extends RecyclerView.Adapter<RatingAdapter.RatingViewHolder> {

    private static final String TAG = "RatingActivity";


    class RatingViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView score;
        ImageView avatar;


        public RatingViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.user_name_id);
            score = itemView.findViewById(R.id.user_score_id);
            //avatar = itemView.findViewById(R.id.avatar_image);

        }
    }




    private Context mContext;
    public DatabaseReference mRef;

    private List<String> myUserScoreIds = new ArrayList<>();
    private List<Long> myUserScores = new ArrayList<>();


    public RatingAdapter(Context context, DatabaseReference ref) {

        mContext = context;
        mRef = ref;


        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                // A new score has been added, add it to the displayed list
                Long myscore = dataSnapshot.getValue(Long.class);
                Log.d(TAG, "onChildAdded:" + myscore);

                // Update
                myUserScoreIds.add(dataSnapshot.getKey());
                myUserScores.add(myscore);

                //Update Recycleview
                notifyItemInserted(myUserScores.size() - 1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());

                // A task has changed, use the key to determine if we are displaying this
                // task and if so displayed the changed task.
                Long myscore = dataSnapshot.getValue(Long.class);

                String scoreKey = dataSnapshot.getKey();

                int taskIndex = myUserScoreIds.indexOf(scoreKey);
                if (taskIndex > -1) {
                    // Replace with the new data
                    myUserScores.set(taskIndex, myscore);

                    //Update Recycleview
                    notifyItemChanged(taskIndex);

                } else {
                    Log.w(TAG, "onChildChanged:unknown_child:" + scoreKey);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

                // A task has changed, use the key to determine if we are displaying this
                // task and if so remove it.
                String scoreKey = dataSnapshot.getKey();

                int scoreIndex = myUserScoreIds.indexOf(scoreKey);
                if (scoreIndex > -1) {
                    // Remove data from the list
                    myUserScores.remove(scoreIndex);
                    myUserScoreIds.remove(scoreIndex);

                    //Update Recycleview
                    notifyItemRemoved(scoreIndex);

                } else {
                    Log.w(TAG, "onChildChanged:unknown_child:" + scoreKey);
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());

                // A task has changed, use the key to determine if we are displaying this
                // task and if so move it.
                Long myscore = dataSnapshot.getValue(Long.class);
                String scoreKey = dataSnapshot.getKey();

                // ...
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Rating:onCancelled", databaseError.toException());
                Toast.makeText(mContext, "Failed to load scores.", Toast.LENGTH_SHORT).show();
            }
        };
        ref.addChildEventListener(childEventListener);
    }

    @Override
    public int getItemCount() {
        return myUserScores.size();
    }

    @Override
    public void onBindViewHolder(RatingViewHolder holder, int position) {
        String str = "Cat_" + position;
        holder.name.setText(str);
        //holder.name.setText(userNames.get(position));
        holder.score.setText(String.valueOf(myUserScores.get(position)));
        /*String photo_url = myUsers.get(position).photoUrl;
        if (photo_url != null)
            holder.avatar.setImageURI(Uri.parse(myUsers.get(position).photoUrl));
        else
            Log.d(TAG, "OOOOO");*/
    }
    @Override
    public RatingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.rating_layout, parent, false);

        return new RatingViewHolder(view);
    }



}
