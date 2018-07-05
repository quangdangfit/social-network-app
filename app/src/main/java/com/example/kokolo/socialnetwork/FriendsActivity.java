package com.example.kokolo.socialnetwork;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class FriendsActivity extends AppCompatActivity {

    RecyclerView friendsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        friendsList = findViewById(R.id.friends_list);
        friendsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        friendsList.setLayoutManager(linearLayoutManager);
    }

    public static class FriendViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public FriendViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }
    }
}
