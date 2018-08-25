package com.thealteria.gabbychat.Fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.thealteria.gabbychat.Account.ChatActivity;
import com.thealteria.gabbychat.Model.Conv;
import com.thealteria.gabbychat.Model.Messages;
import com.thealteria.gabbychat.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatFragment extends Fragment {

    private static final String TAG = "ChatFragment";

    private DatabaseReference chatDatabase, messageDB, userDB, rootRef;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private TextView noChat;
    private RecyclerView chatList;
    private View view;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;

    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_chat, container, false);

        noChat = view.findViewById(R.id.noChat);
        chatList = view.findViewById(R.id.chat_list);

        mAuth = FirebaseAuth.getInstance();

        rootRef = FirebaseDatabase.getInstance().getReference();
        chatDatabase = FirebaseDatabase.getInstance().getReference().child("Chat");
        chatDatabase.keepSynced(true);
        currentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        userDB = FirebaseDatabase.getInstance().getReference().child("Users");
        messageDB = FirebaseDatabase.getInstance().getReference().child("messages").child(currentUserId);

        chatList.setHasFixedSize(true);
        LinearLayoutManager linearLayout = new LinearLayoutManager(getContext());
        linearLayout.setReverseLayout(true);
        linearLayout.setStackFromEnd(true);
        chatList.setLayoutManager(linearLayout);

        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(chatList.getContext(),
                linearLayout.getOrientation());
        chatList.addItemDecoration(mDividerItemDecoration);

        return view;

    }

    @Override

    public void onStart() {
        super.onStart();

        chatDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(currentUserId)) {

                    noChat.setVisibility(View.GONE);

                    FirebaseRecyclerOptions<Conv> options =
                            new FirebaseRecyclerOptions.Builder<Conv>()
                                    .setQuery(chatDatabase.child(currentUserId).orderByChild("timestamp"), Conv.class)
                                    .build();
                    Log.d(TAG, "onDataChange: options: " + options);

                    firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Conv, ConvViewHolder>(options) {
                        @Override
                        protected void onBindViewHolder(@NonNull final ConvViewHolder holder,
                                                        int position, @NonNull final Conv conv) {

                            final String userId = getRef(position).getKey();
                            Log.d(TAG, "onBindViewHolder: userid: " + userId);

                            DatabaseReference userMsgPush = rootRef.child("messages")
                                    .child(currentUserId).child(userId).push();

                            Query lastMessage = messageDB.child(userId).limitToLast(1);

                            Log.d(TAG, "order: " + lastMessage);

                            lastMessage.addChildEventListener(new ChildEventListener() {
                                @Override
                                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                                        String type = Objects.requireNonNull(dataSnapshot.child("type")
                                                .getValue()).toString();
                                        String data = Objects.requireNonNull(dataSnapshot.child("message")
                                                .getValue()).toString();
                                    String time = Objects.requireNonNull(dataSnapshot.child("time").getValue()).toString();

                                    Log.d(TAG, "onChildAdded: msg: " + data + "\n" + time);

                                        switch (type) {
                                            case "text":
                                                holder.setMessage(data, conv.isSeen());
                                                break;
                                            case "image":
                                                holder.setMessage("image", conv.isSeen());
                                                break;
                                            default:
                                                holder.setMessage(" ", conv.isSeen());
                                                break;
                                    }
                                    }

                                    @Override
                                    public void onChildChanged (DataSnapshot dataSnapshot, String s) {
                                        firebaseRecyclerAdapter.notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onChildRemoved (DataSnapshot dataSnapshot){
                                        firebaseRecyclerAdapter.notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onChildMoved (DataSnapshot dataSnapshot, String s){
                                        firebaseRecyclerAdapter.notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onCancelled (DatabaseError databaseError){

                                    }
                            });

                            userDB.child(userId).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    final String name = Objects.requireNonNull(dataSnapshot
                                            .child("name").getValue()).toString();
                                    final String thumb_image = Objects.requireNonNull(dataSnapshot
                                            .child("thumb_image").getValue()).toString();

//                                    if (dataSnapshot.hasChild("online")) {
//                                        String userOnline = dataSnapshot.child("online").getValue().toString();
//                                        holder.setUserOnline(userOnline);
//                                    }

                                    holder.setName(name);
                                    holder.setImage(thumb_image);

                                    holder.view.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent = new Intent(getContext(), ChatActivity.class);
                                            intent.putExtra("user_id", userId);
                                            intent.putExtra("chat_name", name);
                                            startActivity(intent);
                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.d("CHAT_FRAGMENT", databaseError.getMessage());
                                }
                            });
                        }

                        @NonNull
                        @Override
                        public ConvViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_single_layout,
                                    parent, false);

                            TextView userStatus = view.findViewById(R.id.userStatus);
                            TextView userMsg = view.findViewById(R.id.userMsg);

                            userMsg.setVisibility(View.VISIBLE);
                            userStatus.setVisibility(View.GONE);

                            return new ChatFragment.ConvViewHolder(view);
                        }
                    };

                    chatList.setAdapter(firebaseRecyclerAdapter);
                    firebaseRecyclerAdapter.startListening();
                    firebaseRecyclerAdapter.notifyDataSetChanged();
                }

                else {
                    noChat.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private class ConvViewHolder extends RecyclerView.ViewHolder {

        View view;

        ConvViewHolder(View itemView) {
            super(itemView);

            view = itemView;
        }

        public void setMessage(String message, boolean isSeen){
            TextView userMsg = view.findViewById(R.id.userMsg);

                userMsg.setText(message);

            if(!isSeen){
                userMsg.setTypeface(userMsg.getTypeface(), Typeface.BOLD);
            } else {
                userMsg.setTypeface(userMsg.getTypeface(), Typeface.NORMAL);
            }
        }

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
                    Picasso.get().load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.boy).error(R.drawable.boy).into(circleImageView);
                }
            });
        }

//        public void setUserOnline (String ifOnline) {
//            ImageView onlineImage = view.findViewById(R.id.onlineStatus);
//            onlineImage.setVisibility(View.VISIBLE);
//
//            if (ifOnline.equals("true")) {
//                onlineImage.setImageResource(R.drawable.draw_online);
//            }
//            else {
//                onlineImage.setImageResource(R.drawable.draw_offline);
//            }
//        }
    }

    public void convertDate(String d) {

        //You are getting server date as argument, parse your server response and then pass date to this method

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdfAmerica = new SimpleDateFormat("hh:mm:ss");

        String actualTime = sdfAmerica.format(d);
        TimeZone tzInIndia = TimeZone.getTimeZone("IST");
        sdfAmerica.setTimeZone(tzInIndia);

        String convertedTime = sdfAmerica.format(d);

        System.out.println("actual : " + actualTime + "  converted " + convertedTime);
        Log.d(TAG, "convertDate: " + "actual : " + actualTime + "  converted " + convertedTime);
    }

    @Override
    public void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
    }
}
