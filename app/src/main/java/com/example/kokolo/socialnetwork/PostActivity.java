package com.example.kokolo.socialnetwork;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

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
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class PostActivity extends AppCompatActivity {

    Toolbar mToolbar;
    ImageButton SelectPostImage;
    Button UpdatePostButton;
    EditText PostDescription;

    static final int Gallery_Pick = 1;
    Uri imageUri;
    String description;

    StorageReference PostImageRef;
    DatabaseReference UsersRef, PostRef;
    FirebaseAuth mAuth;

    String saveCurrentTime, saveCurrentDate, postRandomName, downloadURL, current_user_id;

    ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mAuth = FirebaseAuth.getInstance();
        PostImageRef = FirebaseStorage.getInstance().getReference();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        current_user_id = mAuth.getCurrentUser().getUid();

        mToolbar = findViewById(R.id.update_post_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Update Post");

        loadingBar = new ProgressDialog(this);

        SelectPostImage = findViewById(R.id.select_post_image);
        UpdatePostButton = findViewById(R.id.update_post_button);
        PostDescription = findViewById(R.id.post_description);

        SelectPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        UpdatePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validatePostInfo();
            }
        });

    }

    private void validatePostInfo() {
        description = PostDescription.getText().toString();

        if (imageUri == null){
            Toast.makeText(this, "Please select post image", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(description)){
            Toast.makeText(this, "Please say something about your image", Toast.LENGTH_SHORT).show();
        }
        else{
            loadingBar.setTitle("Updating your post");
            loadingBar.setMessage("Please wait...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(false);

            storeImageToFirebaseStorage();

        }
    }

    private void storeImageToFirebaseStorage() {
        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currentDate1 = new SimpleDateFormat("dd-MMMM-yyyy", Locale.US);
        String saveCurrentDateType1 = currentDate1.format(calFordDate.getTime());

        SimpleDateFormat currentDate2 = new SimpleDateFormat("dd/MM/yyyy");
        saveCurrentDate = currentDate2.format(calFordDate.getTime());

        Calendar calFordTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(calFordTime.getTime());

        postRandomName = saveCurrentDateType1 + saveCurrentTime;

        StorageReference filePath = PostImageRef.child("Post Images").child(imageUri.getLastPathSegment()+ postRandomName + ".jpg");
        filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful())
                {
                    downloadURL = task.getResult().getDownloadUrl().toString();
                    savePostInformationToDatabase();
                    sendUserToMainActivity();
                    Toast.makeText(PostActivity.this, "Post Successfully!", Toast.LENGTH_SHORT).show();
                }
                else{
                    String message = task.getException().getMessage();
                    Toast.makeText(PostActivity.this, message, Toast.LENGTH_SHORT).show();
                }

                loadingBar.dismiss();
            }
        });
    }

    private void savePostInformationToDatabase() {

        UsersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String userFullName = dataSnapshot.child("fullname").getValue().toString();
                    String userProfileImage = dataSnapshot.child("profileimage").getValue().toString();

                    HashMap postsMap = new HashMap();
                    postsMap.put("uid", current_user_id);
                    postsMap.put("date", saveCurrentDate);
                    postsMap.put("time", saveCurrentTime);
                    postsMap.put("description", description);
                    postsMap.put("postimage", downloadURL);
                    postsMap.put("profileimage", userProfileImage);
                    postsMap.put("fullname", userFullName);

                    PostRef.child(current_user_id + postRandomName).updateChildren(postsMap)
                            .addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful())
                                    {

                                    }
                                    else{
                                        String message = task.getException().getMessage();
                                        Toast.makeText(PostActivity.this, message, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void openGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, Gallery_Pick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null){
            imageUri = data.getData();
            SelectPostImage.setImageURI(imageUri);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home){
            sendUserToMainActivity();
        }

        return super.onOptionsItemSelected(item);
    }




    private void sendUserToMainActivity() {
        Intent mainIntetn = new Intent(PostActivity.this, MainActivity.class);
        startActivity(mainIntetn);
        finish();
    }
}
