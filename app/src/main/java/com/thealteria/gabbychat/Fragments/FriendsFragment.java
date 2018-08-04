package com.thealteria.gabbychat.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.thealteria.gabbychat.Account.ChatActivity;
import com.thealteria.gabbychat.Account.ProfileActivity;
import com.thealteria.gabbychat.R;
import com.thealteria.gabbychat.Model.Friends;
import com.thealteria.gabbychat.UsersActivity;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsFragment extends Fragment {
    private RecyclerView friendList;
    private TextView noFriends;

    private DatabaseReference usersDatabase;
    private DatabaseReference friendsDatabase;

    private String currentUserId;
    private View view;
    private String userId;

    private FirebaseRecyclerAdapter<Friends, FriendsViewHolder> firebaseRecyclerAdapter;


    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_friends, container, false);

        friendList = view.findViewById(R.id.friends_list);
        noFriends = view.findViewById(R.id.noFriends);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        currentUserId = mAuth.getCurrentUser().getUid();
        friendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        usersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        friendList.setHasFixedSize(true);
        LinearLayoutManager linearLayout = new LinearLayoutManager(getContext());
        friendList.setLayoutManager(linearLayout);

        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(friendList.getContext(),
                linearLayout.getOrientation());
        friendList.addItemDecoration(mDividerItemDecoration);

        friendsDatabase.keepSynced(true);
        usersDatabase.keepSynced(true);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        friendsDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(currentUserId)) {

                    noFriends.setVisibility(View.GONE);

                    FirebaseRecyclerOptions<Friends> options =
                            new FirebaseRecyclerOptions.Builder<Friends>()
                                    .setQuery(friendsDatabase.child(currentUserId), Friends.class)
                                    .build();

                    firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(options) {
                        @Override
                        protected void onBindViewHolder(@NonNull final FriendsViewHolder holder,
                                                        int position, @NonNull Friends friends) {

//                            holder.setDate("Friends since: " + friends.getDate() + "\n" + "\t" + "\t" + "\t" + "\t" +
//                                    "\t" + "\t" + "\t" + "\t" + "\t" + friends.getTime());

                            holder.setDate("Friends since: " + friends.getDate());

                            userId = getRef(position).getKey();

                            if (userId != null) {

                                usersDatabase.child(userId).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        final String name = dataSnapshot.child("name").getValue().toString();
                                        final String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                                        if (dataSnapshot.hasChild("online")) {
                                            String userOnline = dataSnapshot.child("online").getValue().toString();
                                            holder.setUserOnline(userOnline);
                                        }

                                        holder.setName(name);
                                        holder.setImage(thumb_image);

                                        holder.view.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                CharSequence options[] = new CharSequence[]{"Open Profile", "Send message"};
                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                                                builder.setTitle("Select Options");
                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {

                                                        if (which == 0) {
                                                            Intent intent = new Intent(getContext(), ProfileActivity.class);
                                                            intent.putExtra("user_id", userId);
                                                            startActivity(intent);
                                                        }

                                                        if (which == 1) {
                                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                                            chatIntent.putExtra("user_id", userId);
                                                            chatIntent.putExtra("chat_name", name);
                                                            chatIntent.putExtra("chat_image", thumb_image);
                                                            startActivity(chatIntent);
                                                        }
                                                    }
                                                });

                                                builder.show();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.d("FRIENDS_FRAGMENT", databaseError.getMessage());
                                    }
                                });

                            }
                        }

                        @NonNull
                        @Override
                        public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                            view = LayoutInflater.from(parent.getContext()).
                                    inflate(R.layout.users_single_layout, parent, false);

                            return new FriendsFragment.FriendsViewHolder(view);
                        }
                    };

                    friendList.setAdapter(firebaseRecyclerAdapter);
                    firebaseRecyclerAdapter.startListening();
                }

                else {
                    noFriends.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private class FriendsViewHolder extends RecyclerView.ViewHolder {

        View view;

        FriendsViewHolder(View itemView) {
            super(itemView);

            view = itemView;
        }

        public void setDate(String date) {

            TextView mDate = view.findViewById(R.id.userStatus);
            mDate.setText(date);
        }

//        public void setTime(String time) {
//
//            TextView mTime = view.findViewById(R.id.userStatus);
//            mTime.setText(time);
//        }

        public void setName(String name) {

            TextView mName = view.findViewById(R.id.singleName);
            mName.setText(name);

        }

        public void setImage(final String thumb_image) {

            final CircleImageView circleImageView = view.findViewById(R.id.userImage);

            Picasso.get().load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.boy).error(R.drawable.boy).into(circleImageView, new Callback() {
                @Override
                public void onSuccess() {


                }

                @Override
                public void onError(Exception e) {
                    Picasso.get().load(thumb_image)
                            .placeholder(R.drawable.boy).error(R.drawable.boy).into(circleImageView);
                }
            });
        }

        public void setUserOnline (String ifOnline) {
            ImageView onlineImage = view.findViewById(R.id.onlineStatus);
            onlineImage.setVisibility(View.VISIBLE);

            if (ifOnline.equals("true")) {
                onlineImage.setImageResource(R.drawable.draw_online);
            }
            else {
                onlineImage.setImageResource(R.drawable.draw_offline);
            }
        }
    }
}
