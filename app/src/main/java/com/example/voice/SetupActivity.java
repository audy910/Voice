package com.example.voice;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity
{

    private EditText UserName, FullName, CountryName;
    private Button SaveInformationButton;
    private CircleImageView ProfileImage;
    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;
    private StorageReference UserProfileImageRef;
    private DatabaseReference UserRef;

    String currentUserId;
    final static int Gallery_Pic = 1;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        UserName = findViewById(R.id.setup_username);
        FullName = findViewById(R.id.setup_fullname);
        CountryName = findViewById(R.id.setup_country);
        SaveInformationButton = findViewById(R.id.setup_information_button);
        ProfileImage = findViewById(R.id.setup_profile_image);

        loadingBar = new ProgressDialog(this);


        SaveInformationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveAccountSetupInformation();
            }
        });

        ProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_Pic);
            }
        });

        UserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    if(snapshot.hasChild("profileimage")) {
                        String image = Objects.requireNonNull(snapshot.child("profileimage").getValue()).toString();

                        Picasso.get().load(image).placeholder(R.drawable.profile).into(ProfileImage);
                    }
                    else
                    {
                        Toast.makeText(SetupActivity.this, "Please select profile image", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    Toast.makeText(SetupActivity.this, "Please select a photo", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Gallery_Pic && resultCode == RESULT_OK && data != null) {
            Uri ImageUri = data.getData();

            CropImage.activity(ImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("Please wait, while we are updating your profile image.");
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(true);
                assert result != null;

                Uri resultUri = result.getUri();

                final StorageReference filePath = UserProfileImageRef.child(currentUserId + ".jpg");

                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot snapshot) {

                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                final String downloadUrl = uri.toString();
                                UserRef.child("profileimage").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {

                                            Intent selfIntent = new Intent(SetupActivity.this, SetupActivity.class);
                                            startActivity(selfIntent);
                                            Toast.makeText(SetupActivity.this, "Profile Image save to database successfully.", Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();
                                        } else {
                                            String message = Objects.requireNonNull(task.getException()).getMessage();
                                            Toast.makeText(SetupActivity.this, "Error occured: " + message, Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();
                                        }
                                    }
                                });
                            }
                        });
                    }
                });
            } else {
                Toast.makeText(this, "Error Occurred: Image can't be cropped, try again", Toast.LENGTH_SHORT).show();
                // loadingBar.dismiss();
            }
        }
    }




    private void SaveAccountSetupInformation() {
        String username = UserName.getText().toString();
        String fullname = FullName.getText().toString();
        String country = CountryName.getText().toString();

        if(TextUtils.isEmpty(username))
        {
            Toast.makeText(this, "Please write your username...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(fullname))
        {
            Toast.makeText(this, "Please write your full name...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(country))
        {
            Toast.makeText(this, "Please write your country...", Toast.LENGTH_SHORT).show();
        }
        else if(UserProfileImageRef == null)
        {
            Toast.makeText(this, "Please select a profile image...", Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingBar.setTitle("Saving Information");
        loadingBar.setMessage("Please wait, while we finish creating your account");
        loadingBar.show();
        loadingBar.setCanceledOnTouchOutside(true);

            HashMap<String, Object> usermap = new HashMap<>();
            usermap.put("username", username);
            usermap.put("fullname", fullname);
            usermap.put("country", country);
//            usermap.put("status: ", "default");
//            usermap.put("gender: ", "none");
//            usermap.put("DoB: ", "default");
            UserRef.updateChildren(usermap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful())
                    {
                        sendUserToMainActivity();
                        Toast.makeText(SetupActivity.this, "Your account has been successfully created...", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();

                    }
                    else
                    {
                        String messafe = Objects.requireNonNull(task.getException()).getMessage();
                        Toast.makeText(SetupActivity.this, "Error Occurred: "+ messafe, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });
        }
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}