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
            avatar = itemView.findViewById(R.id.avatar_image);

        }
    }


    private Context mContext;
    public DatabaseReference mRef;

    private List<String> myUserScoreIds = new ArrayList<>();
    private List<UserScore> myUserScores = new ArrayList<>();


    public RatingAdapter(Context context, DatabaseReference ref) {

        mContext = context;
        mRef = ref;


        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {

                final String uid = dataSnapshot.getKey();
                Log.d(TAG, "onRatingChildAdded:" + uid);

                // A new score has been added, add it to the displayed list
                final Integer score = dataSnapshot.getValue(Integer.class);
                Log.d(TAG, "onRatingChildAdded:" + score);

                DatabaseReference curUserRef = mRef.getRoot().child("users").child(uid);
                curUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        myUser myuser = dataSnapshot.getValue(myUser.class);

                        // Update
                        myUserScoreIds.add(uid);
                        myUserScores.add(new UserScore(myuser.username, score, myuser.photoUrl));

                        notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, "getRating:onCancelled:", databaseError.toException() );
                    }
                });

                //Update Recycleview
                notifyItemInserted(myUserScores.size() - 1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                final String uid = dataSnapshot.getKey();
                Log.d(TAG, "onRatingChildChanged:" + uid);

                // A task has changed, use the key to determine if we are displaying this
                // task and if so displayed the changed task.
                final Integer score = dataSnapshot.getValue(Integer.class);
                Log.d(TAG, "onRatingChildChanged:" + score);

                final int taskIndex = myUserScoreIds.indexOf(uid);
                if (taskIndex > -1) {
                    // Replace with the new data

                    DatabaseReference curUserRef = mRef.getRoot().child("users").child(uid);
                    curUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            myUser myuser = dataSnapshot.getValue(myUser.class);

                            // Update
                            myUserScoreIds.add(uid);
                            myUserScores.add(new UserScore(myuser.username, score, myuser.photoUrl));

                            notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d(TAG, "getRating:onCancelled:", databaseError.toException() );
                        }
                    });

                    //Update Recycleview
                    notifyItemChanged(taskIndex);

                } else {
                    Log.w(TAG, "onRatingChildChanged:unknown_child:" + uid);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onRatingChildRemoved:" + dataSnapshot.getKey());

                // A task has changed, use the key to determine if we are displaying this
                // task and if so remove it.
                String uid = dataSnapshot.getKey();

                int scoreIndex = myUserScoreIds.indexOf(uid);
                if (scoreIndex > -1) {
                    // Remove data from the list
                    myUserScores.remove(scoreIndex);
                    myUserScoreIds.remove(scoreIndex);

                    //Update Recycleview
                    notifyItemRemoved(scoreIndex);

                } else {
                    Log.w(TAG, "onChildChanged:unknown_child:" + uid);
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
        ref.orderByValue().addChildEventListener(childEventListener);
    }

    @Override
    public int getItemCount() {
        return myUserScores.size();
    }

    @Override
    public void onBindViewHolder(RatingViewHolder holder, int position) {

        holder.score.setText(String.valueOf(myUserScores.get(holder.getAdapterPosition()).score));
        holder.name.setText(myUserScores.get(holder.getAdapterPosition()).username);

        /*String photo_url = myUserScores.get(holder.getAdapterPosition()).photoUrl;
        if (photo_url != null)
            holder.avatar.setImageURI(Uri.parse(myUserScores.get(holder.getAdapterPosition()).photoUrl));
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
