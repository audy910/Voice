package com.example.voice;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

        topic = findViewById(R.id.saved_post_title);
        description = findViewById(R.id.saved_post_description);
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

    //creating display of saved groups with recyclerview
    private void displaySavedGroups() {
        savedPostsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild("Title"))
                {
                    final String description = snapshot.child("Description").getValue().toString();
                    final String title = snapshot.child("Title").getValue().toString();
                    FirebaseRecyclerOptions<Posts> options = new FirebaseRecyclerOptions.Builder<Posts>()
                            .setQuery(savedPostsRef, Posts.class)
                            .build();
                    FirebaseRecyclerAdapter<Posts, SavedGroupsHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Posts, SavedGroupsHolder>(options) {
                        @Override
                        protected void onBindViewHolder(@NonNull SavedGroupsHolder holder, int position, @NonNull Posts model) {
                            //use the SavedGroupsHolder variables set up the add parts of the
                            holder.setDescription(description);
                            holder.setTitle(title);
                        }

                        @NonNull
                        @Override
                        public SavedGroupsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.saved_groups, parent, false);
                            SavedGroupsHolder viewHolder = new SavedGroupsHolder(view);


                            return viewHolder;
                        }
                    };
                    savedGroupsList.setAdapter(firebaseRecyclerAdapter);
                    firebaseRecyclerAdapter.startListening();
                }
                else
                {
                    Toast.makeText(GroupsActivity.this, "Please save a group first", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

    public class SavedGroupsHolder extends RecyclerView.ViewHolder
    {
        View mView;
        Button goToGroupButton, removeGroupButton;

        public SavedGroupsHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            goToGroupButton = findViewById(R.id.goToGroupButton);
            removeGroupButton = findViewById(R.id.removeGroupButton);
            //define upper variables
        }
        public void setDescription(String description)
        {
            TextView postDescription = mView.findViewById(R.id.saved_post_description);
            postDescription.setText(description);
        }
        public void setTitle(String title)
        {
            TextView postTitle = mView.findViewById(R.id.saved_post_title);
            postTitle.setText(title);
        }
        //going to set the title and the description using the Posts class
        //create a Ref to access the saved posts with the other variables - use that to access what goes into the title
        //^ may go in the BinderViewHolder
    }


}