package com.thealteria.gabbychat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.thealteria.gabbychat.Account.AccountSettingsActivity;
import com.thealteria.gabbychat.Account.ProfileActivity;
import com.thealteria.gabbychat.Model.Users;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView recyclerView;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String currentUserId;
    private FirebaseRecyclerAdapter<Users, UsersViewHolder> firebaseRecyclerAdapter;
    private DatabaseReference userRef;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        mToolbar = findViewById(R.id.user_appbar);
        swipeRefreshLayout = findViewById(R.id.pullToRefresh);

        //mUserDatabase.keepSynced(true);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshContent();
                //swipeRefreshLayout.setRefreshing(true);
            }
        });

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        recyclerView = findViewById(R.id.users_list);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayout = new LinearLayoutManager(UsersActivity.this);
        recyclerView.setLayoutManager(linearLayout);

        DividerItemDecoration mDividerItemDecoration = new
                DividerItemDecoration(recyclerView.getContext(), linearLayout.getOrientation());
        recyclerView.addItemDecoration(mDividerItemDecoration);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        if (mAuth.getCurrentUser() != null) {
            userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        }

    }

    private void refreshContent(){

        new Handler().postDelayed(new Runnable() {
            @Override public void run() {
                swipeRefreshLayout.setRefreshing(false);
            }
        }, 3000);

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Users> options =
                new FirebaseRecyclerOptions.Builder<Users>()
                        .setQuery(mUserDatabase, Users.class)
                        .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull UsersViewHolder holder, int position, @NonNull Users users) {

                holder.setName(users.getName());
                holder.setStatus(users.getStatus());
                holder.setImage(users.getImage());

                final String uid = getRef(position).getKey();

                holder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if(uid.equals(currentUserId)) {
                            Intent profile_intent = new Intent(getApplicationContext(), AccountSettingsActivity.class);
                                    startActivity(profile_intent);
                        }

                        else {
                            Intent profile_intent = new Intent(getApplicationContext(), ProfileActivity.class);
                            profile_intent.putExtra("user_id", uid);
                            startActivity(profile_intent);
                            //Toast.makeText(getApplicationContext(), uid, Toast.LENGTH_LONG).show();
                            Log.d("USER_ID: ", uid);
                        }
                    }
                });
            }

            @NonNull
            @Override
            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.users_single_layout, parent, false);

                return new UsersViewHolder(view);
            }

            @Override
            public int getItemCount() {
                return super.getItemCount();
            }
        };

        recyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

    }

    public class UsersViewHolder extends RecyclerView.ViewHolder {

        View view;

        UsersViewHolder(View itemView) {
            super(itemView);

            view = itemView;
        }

        public void setName(String name) {

            TextView mName = view.findViewById(R.id.singleName);
            mName.setText(name);

        }

        public void setStatus(String status) {

            TextView mStatus = view.findViewById(R.id.userStatus);
            mStatus.setText(status);

        }

        public void setImage(final String thumb_image) {

            final CircleImageView circleImageView = view.findViewById(R.id.userImage);
            //Picasso.get().load(thumb_image).placeholder(R.drawable.boy).error(R.drawable.boy).into(circleImageView);

            Picasso.get().load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.boy).error(R.drawable.boy).into(circleImageView, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(Exception e) {

                    Picasso.get().load(thumb_image).placeholder(R.drawable.boy).error(R.drawable.boy).into(circleImageView);
                }
            });
        }

    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//
//        currentUser = mAuth.getCurrentUser();
//        if(currentUser != null) {
//            userRef.child("online").setValue(ServerValue.TIMESTAMP);
//        }
//
//    }
}
