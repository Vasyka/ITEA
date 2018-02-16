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

    private List<String> myUsersIds = new ArrayList<>();
    private List<myUser> myUsers = new ArrayList<>();
    //idk how to do it
    private List<String> userNames = new ArrayList<>();
    private List<Integer> userScores = new ArrayList<>();



    public RatingAdapter(Context context, DatabaseReference ref) {

        mContext = context;
        mRef = ref;

    }


    @Override
    public RatingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.rating_layout, parent, false);

        return new RatingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RatingViewHolder holder, int position) {
        holder.name.setText(userNames.get(position));
        holder.score.setText(userScores.get(position));
        String photo_url = myUsers.get(position).photoUrl;
        if (photo_url != null)
            holder.avatar.setImageURI(Uri.parse(myUsers.get(position).photoUrl));
        else
            Log.d(TAG, "OOOOO");
    }

    @Override
    public int getItemCount() {
        return myUsersIds.size();
    }








}
