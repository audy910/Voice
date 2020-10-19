package com.example.voice;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

public class PostActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ProgressDialog loadingBar;

    private ImageButton SelectPostImage;
    private Button UpdatePostButton;
    private EditText PostDescription;
    private EditText PostTitle;

    private static final int Gallery_Pic = 1;
    private Uri ImageURI;

    private String Description;
    private String saveCurrentDate, saveCurrentTime, postRandomName, current_user_id, Title;
    private String downloadUrl;

   // private StorageReference PostImagesRef;
    private DatabaseReference usersRef, postRef;
    private FirebaseAuth mAuth;


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        //PostImagesRef = FirebaseStorage.getInstance().getReference();
        usersRef  = FirebaseDatabase.getInstance().getReference().child("Users");
        postRef  = FirebaseDatabase.getInstance().getReference().child("Posts");
        mAuth = FirebaseAuth.getInstance();
        current_user_id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        //SelectPostImage = findViewById(R.id.select_post_image);
        UpdatePostButton = findViewById(R.id.update_post_button);
        PostDescription = findViewById(R.id.update_post_description);
        PostTitle = findViewById(R.id.update_post_topic);

        mToolbar = findViewById(R.id.update_post_toolbar);
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Create Post");

        loadingBar = new ProgressDialog(this);

//        SelectPostImage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                OpenGallery();
//            }
//        });

        UpdatePostButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                ValidatePostInfo();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void ValidatePostInfo() {

        Description = PostDescription.getText().toString();
        Title = PostTitle.getText().toString();

        if(TextUtils.isEmpty(Title))
        {
            Toast.makeText(this, "Please write a topic...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(Description))
        {
            Toast.makeText(this, "Please write a description...", Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingBar.setTitle("Add New Post");
            loadingBar.setMessage("Please wait, while we are updating your new post...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);
            //StoringImageToFirebase();
            SavingPostInformationToDatabase();
        }

    }

//    @RequiresApi(api = Build.VERSION_CODES.N)
//    private void StoringImageToFirebase() {
//        Calendar callForDate = Calendar.getInstance();
//        SimpleDateFormat currentData = new SimpleDateFormat("dd-MMMM-yyyy");
//        saveCurrentDate = currentData.format(callForDate.getTime());
//
//        Calendar callForTime = Calendar.getInstance();
//        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
//        saveCurrentTime = currentTime.format(callForTime.getTime());
//
//        postRandomName = saveCurrentDate+saveCurrentTime;
//
//        final StorageReference filePath  = PostImagesRef.child(ImageURI.getLastPathSegment() + postRandomName + ".jpg");
//
//        filePath.putFile(ImageURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//                if(task.isSuccessful())
//                {
//                    downloadUrl = task.getResult().getStorage().getDownloadUrl().toString();
//
//                    Toast.makeText(PostActivity.this, "Image has uploaded successfully to storage...",  Toast.LENGTH_SHORT).show();
//
//                    SavingPostInformationToDatabase();
//                }
//                else
//                {
//                    String message = Objects.requireNonNull(task.getException()).getMessage();
//                    Toast.makeText(PostActivity.this, "Error occurred:" + message,  Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void SavingPostInformationToDatabase()
    {
        Calendar callForDate = Calendar.getInstance();
        SimpleDateFormat currentData = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentData.format(callForDate.getTime());

        Calendar callForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
        saveCurrentTime = currentTime.format(callForTime.getTime());
        postRandomName = saveCurrentDate+saveCurrentTime;


        usersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String UserFullname = Objects.requireNonNull(snapshot.child("fullname").getValue()).toString();
                    String userProfileImage;
//
                        userProfileImage = snapshot.child("profileimage").getValue().toString();
//



                    HashMap<String, Object > postMap = new HashMap<>();
                    postMap.put("uid", current_user_id);
                    postMap.put("date", saveCurrentDate);
                    postMap.put("time", saveCurrentTime);
                    postMap.put("description", Description);
                  //  postMap.put("postimage", downloadUrl);

                    if(!userProfileImage.isEmpty()) {
                        postMap.put("profileimage", userProfileImage);
                    }
                    postMap.put("fullname", UserFullname);
                    postMap.put("title", Title);
                    postRef.child(current_user_id + postRandomName).updateChildren(postMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                SendUserToMainActivity();
                                Toast.makeText(PostActivity.this, "Post is updated successfully...", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                            else
                            {
                                Toast.makeText(PostActivity.this, "Error Occurred while updating...", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void OpenGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, Gallery_Pic);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode ==Gallery_Pic && resultCode == RESULT_OK && data != null)
        {
            ImageURI = data.getData();
            SelectPostImage.setImageURI(ImageURI);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home)
        {
            SendUserToMainActivity();
        }

        return super.onOptionsItemSelected(item);
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(PostActivity.this, MainActivity.class);
        startActivity(mainIntent);
    }
}