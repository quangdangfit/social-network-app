package com.example.kokolo.socialnetwork;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsActivity extends AppCompatActivity {

    RecyclerView friendsList;
    DatabaseReference friendsRef, usersRef;
    FirebaseAuth mAuth;
    String onlineUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        mAuth = FirebaseAuth.getInstance();
        onlineUserId = mAuth.getCurrentUser().getUid();
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(onlineUserId);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        friendsList = findViewById(R.id.friends_list);
        friendsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        friendsList.setLayoutManager(linearLayoutManager);


        DisplayAllFriends();
    }

    private void DisplayAllFriends() {
        FirebaseRecyclerAdapter<Friends, FriendViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Friends, FriendViewHolder>
                (
                        Friends.class,
                        R.layout.all_users_display_display,
                        FriendViewHolder.class,
                        friendsRef

                ){
            @Override
            protected void populateViewHolder(final FriendViewHolder viewHolder, Friends model, int position) {
                viewHolder.setDate(model.getDate());

                final String usersId = getRef(position).getKey();

                usersRef.child(usersId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            final String userName = dataSnapshot.child("fullname").getValue().toString();
                            final String profileImage = dataSnapshot.child("profileimage").getValue().toString();

                            viewHolder.setFullname(userName);
                            viewHolder.setProfileimage(profileImage);

                            viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    CharSequence options[] = new CharSequence[]
                                            {
                                                    userName + "'s Profile",
                                                    "Send Message"
                                            };
                                    AlertDialog.Builder builder = new AlertDialog.Builder(FriendsActivity.this);
                                    builder.setTitle("Select Options");

                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (which == 0){
                                                SendUserToProfileActivity(usersId);
                                            }
                                            if (which == 1){
                                                SendUserToChatActivity(usersId, userName);
                                            }
                                        }
                                    });
                                    builder.show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };

        friendsList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class FriendViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public FriendViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setProfileimage(String profileimage){
            CircleImageView image = mView.findViewById(R.id.all_users_profile_image);
            Picasso.get().load(profileimage).placeholder(R.drawable.profile).into(image);
        }

        public void setFullname(String fullname){
            TextView username = mView.findViewById(R.id.all_users_profile_full_name);
            username.setText(fullname);
        }
        public void setDate(String date){
            TextView friendDate = mView.findViewById(R.id.all_users_profile_status);
            friendDate.setText("Friend since: " + date);
        }
    }

    private void SendUserToProfileActivity(String userId) {
        Intent profileIntent = new Intent(FriendsActivity.this, ProfileActivity.class);
        profileIntent.putExtra("userId", userId);
        startActivity(profileIntent);
    }

    private void SendUserToChatActivity(String userId, String userName) {
        Intent chatIntent = new Intent(FriendsActivity.this, ChatActivity.class);
        chatIntent.putExtra("userId", userId);
        chatIntent.putExtra("userName", userName);
        startActivity(chatIntent);
    }
}
