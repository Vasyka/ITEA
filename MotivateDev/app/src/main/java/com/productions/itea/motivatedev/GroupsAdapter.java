package com.productions.itea.motivatedev;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;


class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.GroupViewHolder>{

private static final String TAG = "MainActivity";

static class GroupViewHolder extends RecyclerView.ViewHolder {

    TextView groupView;

    GroupViewHolder(View itemView) {
        super(itemView);
        groupView = itemView.findViewById(R.id.group_name_id);
    }
}

    private Context mContext;
    private DatabaseReference mRef;

    private List<String> myGroupsIds = new ArrayList<>();
    private List<myGroup> myGroups = new ArrayList<>();


    public GroupsAdapter(Context context, DatabaseReference ref) {

        mContext = context;
        mRef = ref;

    }

    public int getItemCount() {
        return myGroups.size();
    }

    // Place item[position] in holder
    public void onBindViewHolder(GroupsAdapter.GroupViewHolder holder, int position) {
        holder.groupView.setText(myGroups.get(position).group_name);


    }

    // Create new views (invoked by the layout manager)
    public GroupsAdapter.GroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.group_layout, parent, false);

        // set the view's size, margins, paddings and layout parameters

        return new GroupViewHolder(view);
    }

}