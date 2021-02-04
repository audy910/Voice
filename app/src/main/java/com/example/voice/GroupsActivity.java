package com.example.voice;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class GroupsActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private RecyclerView savedGroupsList;

    private TextView topic, description;
    private String currentUserId;

    private DatabaseReference savedPostsRef;
    private FirebaseAuth mAuth;



    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

//        topic = findViewById(R.id.saved_topic);
//        description = findViewById(R.id.saved_solution);
        mAuth = FirebaseAuth.getInstance();
        currentUserId  = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        savedPostsRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId).child("Saved");


        mToolbar = findViewById(R.id.groups_toolbar);
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Groups");

        savedGroupsList = findViewById(R.id.saved_group_list);
        savedGroupsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        savedGroupsList.setLayoutManager(linearLayoutManager);

        displaySavedGroups();
    }
    private void displaySavedGroups () {

        FirebaseRecyclerOptions<savedGroups> options = new FirebaseRecyclerOptions.Builder<savedGroups>()
                .setQuery(savedPostsRef, savedGroups.class) //savedPostRef accessing posts saved by user, declared in Create
                .build();
        FirebaseRecyclerAdapter<savedGroups, SavedGroupsHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<savedGroups, GroupsActivity.SavedGroupsHolder>
                (options) {
            @Override
            protected void onBindViewHolder(@NonNull SavedGroupsHolder holder, int position, @NonNull final savedGroups model) {
                final String PostKey = getRef(position).getKey();

//                holder.setFullname(model.getFullname());
//                holder.setTime(model.getTime());
//                holder.setDate(model.getDate());
                Log.i("info","model should now have".concat( model.getDescription()));
                holder.setDescription(model.getDescription());
                holder.setTitle(model.getTitle()); //log.i
            }
            @NonNull
            @Override
            public SavedGroupsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.saved_groups_layout, parent, false);
                SavedGroupsHolder viewHolder = new SavedGroupsHolder(view);


                return viewHolder;
            }

        };

        savedGroupsList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

    }


    public class SavedGroupsHolder extends RecyclerView.ViewHolder
    {
        View mView;
        Button goToGroupButton, removeGroupButton;

        public SavedGroupsHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            goToGroupButton = findViewById(R.id.go_to_group);
            //removeGroupButton = findViewById(R.id.removeGroupButton);
            //define upper variables
        }
        public void setDescription(String description)
        {
            TextView postDescription = mView.findViewById(R.id.saved_solution);
            postDescription.setText(description);
        }
        public void setTitle(String title)
        {
            TextView postTitle = mView.findViewById(R.id.saved_topic);
            postTitle.setText(title);
        }
        //going to set the title and the description using the savedGroupds class
        //create a Ref to access the saved posts with the other variables - use that to access what goes into the title
        //^ may go in the BinderViewHolder
    }


}