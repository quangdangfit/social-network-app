package com.example.kokolo.socialnetwork;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransitionImpl;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    TextView userName, userProfName, userStatus, userCountry, userGender, userRelation, userDOB;
    CircleImageView userProfileImage;
    RecyclerView postList;

    Button sendFriendRequestButton, declineFriendRequestButton;

    DatabaseReference profileUserRef, postsRef, likesRef, friendRequestRef;
    FirebaseAuth mAuth;

    static String currentUserId, userId, CURRENT_STATE;
    Boolean likeChecker = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        currentUserId = mAuth.getCurrentUser().getUid();
        profileUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("FriendRequests");

        userName = findViewById(R.id.my_username);
        userProfName = findViewById(R.id.my_profile_full_name);
        userStatus = findViewById(R.id.my_profile_status);
        userCountry = findViewById(R.id.my_country);
        userGender = findViewById(R.id.my_gender);
        userRelation = findViewById(R.id.my_relationship_status);
        userDOB = findViewById(R.id.my_dob);
        userProfileImage = findViewById(R.id.my_profile_pic);

        sendFriendRequestButton = findViewById(R.id.send_friend_request_button);
        declineFriendRequestButton = findViewById(R.id.decline_friend_request_button);

        postList = findViewById(R.id.all_current_users_post_list);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);
        postList.setFocusable(false);

        CURRENT_STATE = "not_friends";

        profileUserRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String myProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                    String myUserName = dataSnapshot.child("username").getValue().toString();
                    String myProfileName = dataSnapshot.child("fullname").getValue().toString();
                    String myProfileStatus = dataSnapshot.child("status").getValue().toString();
                    String myDOB = dataSnapshot.child("dob").getValue().toString();
                    String myCountry = dataSnapshot.child("country").getValue().toString();
                    String myGender = dataSnapshot.child("gender").getValue().toString();
                    String myRelationshipStatus = dataSnapshot.child("relationshipstatus").getValue().toString();

                    Picasso.get().load(myProfileImage).placeholder(R.drawable.profile).into(userProfileImage);

                    userName.setText("@" + myUserName);
                    userProfName.setText(myProfileName);
                    userStatus.setText(myProfileStatus);
                    userDOB.setText("DOB: " + myDOB);
                    userCountry.setText("Country: "+ myCountry);
                    userGender.setText("Gender: " + myGender);
                    userRelation.setText("Relationship: " + myRelationshipStatus);

                    MaintananceOfButtons();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        displayAllCurrentUsersPosts();

        declineFriendRequestButton.setVisibility(View.INVISIBLE);
        declineFriendRequestButton.setEnabled(false);

        if (!currentUserId.equals(userId)){
            sendFriendRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendFriendRequestButton.setEnabled(false);

                    if (CURRENT_STATE.equals("not_friends")){
                        sendFriendRequestButton.setText("Send Friend Request");
                        SendFriendRequest();
                    }else if (CURRENT_STATE.equals("request_sent")){
                        sendFriendRequestButton.setText("Cancel Friend Request");
                        CancelFriendRequest();
                    }
                }
            });
        }else {
            declineFriendRequestButton.setVisibility(View.INVISIBLE);
            sendFriendRequestButton.setVisibility(View.INVISIBLE);
        }
    }

    private void CancelFriendRequest() {
        friendRequestRef.child(currentUserId).child(userId).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            friendRequestRef.child(userId).child(currentUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                sendFriendRequestButton.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                sendFriendRequestButton.setText("Send Friend Request");

                                                declineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                declineFriendRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void MaintananceOfButtons() {
        friendRequestRef.child(currentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(userId)){
                            String request_type = dataSnapshot.child(userId).child("request_type")
                                    .getValue().toString();
                            if (request_type.equals("sent")){
                                CURRENT_STATE = "request_sent";
                                sendFriendRequestButton.setText("Cancel Friend Request");

                                declineFriendRequestButton.setVisibility(View.INVISIBLE);
                                declineFriendRequestButton.setEnabled(false);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void SendFriendRequest() {
        friendRequestRef.child(currentUserId).child(userId).child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            friendRequestRef.child(userId).child(currentUserId)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                sendFriendRequestButton.setEnabled(true);
                                                CURRENT_STATE = "request_sent";
                                                sendFriendRequestButton.setText("Cancel Friend Request");

                                                declineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                declineFriendRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void displayAllCurrentUsersPosts() {

        Query searchFriendsQuery = postsRef.orderByChild("uid").equalTo(userId);
        FirebaseRecyclerAdapter<Posts, ProfileActivity.PostsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Posts, ProfileActivity.PostsViewHolder>
                        (
                                Posts.class,
                                R.layout.all_post_layout,
                                ProfileActivity.PostsViewHolder.class,
                                searchFriendsQuery
                        )
                {
                    @Override
                    protected void populateViewHolder(ProfileActivity.PostsViewHolder viewHolder, Posts model, int position)
                    {
                        final String PostKey = getRef(position).getKey();

                        viewHolder.setFullname(model.getFullname());
                        viewHolder.setTime(model.getTime());
                        viewHolder.setDate(model.getDate());
                        viewHolder.setDescription(model.getDescription());
                        viewHolder.setPostimage(model.getPostimage());

                        viewHolder.setLikeButtonStatus(PostKey);

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent clickPostIntent =  new Intent(ProfileActivity.this, ClickPostActivity.class);
                                clickPostIntent.putExtra("PostKey", PostKey);
                                startActivity(clickPostIntent);
                            }
                        });

                        if (!model.profileimage.equals("none"))
                        {
                            viewHolder.setProfileimage(model.getProfileimage());
                        }
                        else{
                            viewHolder.setDefaultProfileimage();
                        }
                        viewHolder.likePostButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                likeChecker = true;

                                likesRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (likeChecker.equals(true)){
                                            if (dataSnapshot.child(PostKey).hasChild(currentUserId)){
                                                likesRef.child(PostKey).child(currentUserId).removeValue();
                                                likeChecker = false;
                                            }
                                            else{
                                                likesRef.child(PostKey).child(currentUserId).setValue(true);
                                                likeChecker = false;
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        });

                        viewHolder.commentPostButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent commentPostIntent =  new Intent(ProfileActivity.this, CommentsActivity.class);
                                commentPostIntent.putExtra("PostKey", PostKey);
                                startActivity(commentPostIntent);
                            }
                        });

                    }
                };

        postList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class PostsViewHolder extends RecyclerView.ViewHolder{

        View mView;
        ImageButton likePostButton, commentPostButton;
        CircleImageView postProfileImage;
        TextView displayNoOfLikes;
        int countLikes;
        DatabaseReference LikesRef;

        public void setLikeButtonStatus(final String PostKey){
            LikesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                    if (dataSnapshot.child(PostKey).hasChild(currentUserId)){
                        likePostButton.setImageResource(R.drawable.like);
                        if (countLikes > 1){
                            displayNoOfLikes.setText(Integer.toString(countLikes) + " Likes");
                        }else {
                            displayNoOfLikes.setText(Integer.toString(countLikes) + " Like");
                        }
                    }
                    else {
                        likePostButton.setImageResource(R.drawable.dislike);
                        if (countLikes > 1){
                            displayNoOfLikes.setText(Integer.toString(countLikes) + " Likes");
                        }else {
                            displayNoOfLikes.setText(Integer.toString(countLikes) + " Like");
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        public PostsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            likePostButton = mView.findViewById(R.id.like_button);
            commentPostButton = mView.findViewById(R.id.comment_button);
            displayNoOfLikes = mView.findViewById(R.id.display_no_of_likes);
            postProfileImage = mView.findViewById(R.id.post_profile_image);

            LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        }

        public void setFullname(String fullname){
            TextView username = mView.findViewById(R.id.post_user_name);
            username.setText(fullname);
        }

        public void setProfileimage(String profileimage){
            CircleImageView image = mView.findViewById(R.id.post_profile_image);
            Picasso.get().load(profileimage).into(image);
        }

        public void setDefaultProfileimage(){
            CircleImageView image = mView.findViewById(R.id.post_profile_image);
            image.setImageResource(R.drawable.profile);
        }

        public void setDescription(String description) {
            TextView postDescription = mView.findViewById(R.id.post_description);
            postDescription.setText(description);
        }

        public void setPostimage(String postimage){
            ImageView postImage = mView.findViewById(R.id.post_image);
            Picasso.get().load(postimage).into(postImage);
        }

        public void setDate(String date){
            TextView postDate = mView.findViewById(R.id.post_date);
            postDate.setText("   " + date);
        }

        public void setTime(String time){
            TextView postTime = mView.findViewById(R.id.post_time);
            postTime.setText("   " + time);
        }

    }
}
