package com.example.kokolo.socialnetwork;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    EditText UserName, FullName, Country;
    Button SaveInfomationButton;
    CircleImageView profileImage;
    ProgressDialog loadingBar;
    CropImage.ActivityResult imageCropResult;

    FirebaseAuth mAuth;
    DatabaseReference userRef;
    StorageReference userProfileImageRef;

    String currentUserId;

    final static int Gallery_Pick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        loadingBar = new ProgressDialog(this);

        UserName = findViewById(R.id.setup_username);
        FullName = findViewById(R.id.setup_full_name);
        Country = findViewById(R.id.setup_country);
        SaveInfomationButton = findViewById(R.id.setup_information_button);
        profileImage = findViewById(R.id.setup_profile_image);

        SaveInfomationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAccountSetupInformation();
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_Pick);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            if (resultCode == RESULT_OK) {
                imageCropResult = CropImage.getActivityResult(data);
                profileImage.setImageURI(imageCropResult.getUri());
            }
        }
    }

    private void setAccountSetupInformation() {
        String name = UserName.getText().toString();
        String fullName = FullName.getText().toString();
        String country = Country.getText().toString();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Please insert username", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(fullName)) {
            Toast.makeText(this, "Please insert full name", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(country)) {
            Toast.makeText(this, "Please insert country", Toast.LENGTH_SHORT).show();
        }
        else{
            loadingBar.setTitle("Saving information");
            loadingBar.setMessage("Please wait...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(false);

            HashMap userMap = new HashMap();

            userMap.put("username", name);
            userMap.put("fullname", fullName);
            userMap.put("country", country);
            userMap.put("status", "none");
            userMap.put("gender", "none");
            userMap.put("dob", "none");
            userMap.put("relationshipstatus", "none");
            userMap.put("profileimage", "none");

            userRef.updateChildren(userMap)
                    .addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful())
                            {
                            }
                            else{
                                String message = task.getException().getMessage();
                                Toast.makeText(SetupActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                            loadingBar.dismiss();
                        }
                    });

            if (imageCropResult != null)
                addProfileImage(imageCropResult);

            sendUserToMainActivity();
        }
    }

    private void addProfileImage(CropImage.ActivityResult imageCropResult) {
        final Uri resultUri = imageCropResult.getUri();
        StorageReference filePath = userProfileImageRef.child(currentUserId+".jpg");
        filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful())
                {
                    final String downloadUrl = task.getResult().getDownloadUrl().toString();
                    userRef.child("profileimage").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful())
                            {
                                Toast.makeText(SetupActivity.this, "Update profile  Successfully!", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                String message = task.getException().getMessage();
                                Toast.makeText(SetupActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else{
                    String message = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendUserToMainActivity() {
        Intent mainIntetn = new Intent(SetupActivity.this, MainActivity.class);
        mainIntetn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntetn);
        finish();
    }
}
