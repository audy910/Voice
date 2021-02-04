package com.example.voice;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
//import android.support.v4.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
//import android.widget.Toolbar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.widget.Toast.*;

public class MainActivity extends AppCompatActivity {


//declaring global variables
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private RecyclerView postList;
    private Toolbar mToolbar;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    private CircleImageView NavProfileImage;
    private TextView NavProfileUserName;
    private ImageButton addNewPostButton;
    private Button addGroup;

    private FirebaseAuth mAuth;
    private DatabaseReference UserRef, postRef, userGroupRef;


    private String currentUserID;
    private boolean LikeChecker = false;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initializing global variables
        mAuth = FirebaseAuth.getInstance();
        currentUserID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        userGroupRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID).child("Saved");

        mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Home");
        addNewPostButton = findViewById(R.id.add_new_post_button);


        drawerLayout = findViewById(R.id.drawable_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView =  findViewById(R.id.navigation_view);
        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);

        postList = findViewById(R.id.all_users_post_list);
        postList.setHasFixedSize(true);
        LinearLayoutManager  linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);

        NavProfileImage = navView.findViewById(R.id.nav_profile_image);
        NavProfileUserName = navView.findViewById(R.id.nav_user_full_name);




        UserRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    if(snapshot.hasChild("fullname"))
                    {
                        String fullname = Objects.requireNonNull(snapshot.child("fullname").getValue()).toString();
                        NavProfileUserName.setText(fullname);
                    }
                    if(snapshot.hasChild("profileimage"))
                    {
                        String image = Objects.requireNonNull(snapshot.child("profileimage").getValue()).toString();

                        Picasso.get().load(image).placeholder(R.drawable.profile).into(NavProfileImage);
                    }
                    else
                    {
                        Toast.makeText(MainActivity.this, "Profile name does not exist", LENGTH_SHORT).show();
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        //checking for navigation button press
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                UserMenuSelector(item);
                return false;
            }

        });

        addNewPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToPostActivity();
            }
        });


        DisplayAllUserPosts();
    }//end on create




    private void DisplayAllUserPosts () {

            FirebaseRecyclerOptions<Posts> options = new FirebaseRecyclerOptions.Builder<Posts>()
                    .setQuery(postRef, Posts.class)
                    .build();
            FirebaseRecyclerAdapter<Posts, PostsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Posts, PostsViewHolder>
                    (options) {
                @Override
                protected void onBindViewHolder(@NonNull PostsViewHolder holder, int position, @NonNull final Posts model) {
                    final String PostKey = getRef(position).getKey();

                    holder.setFullname(model.getFullname());
                    holder.setTime(model.getTime());
                    holder.setDate(model.getDate());
                    holder.setDescription(model.getDescription());
                    holder.setTitle(model.getTitle());
                    holder.setProfileimage(getApplicationContext(), model.getProfileimage());
                    //holder.setPostimage(getApplicationContext(), model.getPostimage());
//                    holder.mView.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            Intent clickPostIntent = new Intent(MainActivity.this,)
//                        }
//                    });
                    holder.addGroupButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            LikeChecker = true;
                            userGroupRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    HashMap<String, Object> postmap = new HashMap<>();
                                    postmap.put("Title", model.getTitle());
                                    postmap.put("Description", model.getDescription());
                                    userGroupRef.updateChildren(postmap);
                                    LikeChecker = false;
                                    Toast.makeText(MainActivity.this, "This group has been saved", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    });


                }


                @NonNull
                @Override
                public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_posts_layout, parent, false);
                    PostsViewHolder viewHolder = new PostsViewHolder(view);


                    return viewHolder;
                }

            };

            postList.setAdapter(firebaseRecyclerAdapter);
            firebaseRecyclerAdapter.startListening();

        }


    public class PostsViewHolder extends RecyclerView.ViewHolder
    {

        View mView;
        Button addGroupButton;

        public PostsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            addGroupButton = mView.findViewById(R.id.add_group_button);

        }
//        public void setAddGroup(Button addGroup){
//            addGroup  = mView.findViewById(R.id.add_group_button);
//
//            addGroup.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    LikeChecker = true;
//                }
//            });
//        }
        public void setFullname(String fullname)
        {
            TextView username = mView.findViewById(R.id.post_username);
            username.setText(fullname);
        }
        public void setProfileimage(Context ctx, String profileimage)
        {
            CircleImageView image = mView.findViewById(R.id.post_profile_image);
            Picasso.get().load(profileimage).placeholder(R.drawable.profile).into(image);
        }
        public void setTime(String time)
        {
            TextView postTime = mView.findViewById(R.id.post_time);
            postTime.setText("  " +time);
        }
        public void setDate(String date)
        {
            TextView postDate = mView.findViewById(R.id.post_date);
            postDate.setText(" " +date);
        }
        public void setDescription(String description)
        {
            TextView postDescription = mView.findViewById(R.id.post_description);
            postDescription.setText(description);
        }
//        public void setPostimage(Context ctx1, String postimage)
//        {
//            ImageView postImage = (ImageView) mView.findViewById(R.id.post_image);
//            Picasso.get().load(postimage).into(postImage);
//        }
        public void setTitle(String title)
    {
        TextView postTitle = mView.findViewById(R.id.post_topic);
        postTitle.setText(title);
    }
    }

    private void sendUserToPostActivity() {
        Intent addNewPostIntent = new Intent(MainActivity.this, PostActivity.class);
        startActivity(addNewPostIntent);
    }

    //checking if user has an account and if account is set up
    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null)
        {
            SendUserTologinActivity();
        }
        else
        {
            CheckUserExistence();
        }
    }
//method for second step of verification, profile set up?
    private void CheckUserExistence() {
        final String current_user_id = mAuth.getCurrentUser().getUid();

        UserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
               if(!snapshot.hasChild(current_user_id))
               {
                    SendUserToSetupActivity();
               }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

//method for to send to setup page
    private void SendUserToSetupActivity() {
        Intent SetupIntent = new Intent(MainActivity.this, SetupActivity.class);
        SetupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(SetupIntent);
        finish();
    }
//method to send to login page
    private void SendUserTologinActivity() {
        Intent loginIntent = new Intent(MainActivity.this, loginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }
    private void SendUserToGroupActivity() {
        Intent groupIntent = new Intent(MainActivity.this, GroupsActivity.class);
        groupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(groupIntent);
        finish();
    }
//checking for buttons pressed on the navigation bar page
    @Override
            public boolean onOptionsItemSelected(MenuItem item)
            {
                if(actionBarDrawerToggle.onOptionsItemSelected(item))
                {
                    return true;
                }
                return super.onOptionsItemSelected(item);
            }

            private void UserMenuSelector(MenuItem item) {
                switch (item.getItemId())
                {
                    case R.id.nav_post:
                        sendUserToPostActivity();
                        break;
                    case R.id.nav_profile:
                        Toast.makeText(MainActivity.this,"Profile", Toast.LENGTH_SHORT).show();
                        break;

                    case R.id.nav_home:
                        Toast.makeText(MainActivity.this,"home", Toast.LENGTH_SHORT).show();
                        break;

                    case R.id.nav_find_friends:
                        Toast.makeText(MainActivity.this,"find friend", Toast.LENGTH_SHORT).show();
                        break;

                    case R.id.nav_friends:
                        SendUserToGroupActivity();
                        break;

                    case R.id.nav_messages:
                        Toast.makeText(MainActivity.this,"messages", Toast.LENGTH_SHORT).show();
                        break;

                    case R.id.nav_logout:
                        mAuth.signOut();
                        SendUserTologinActivity();
                        break;

                }

    }
}