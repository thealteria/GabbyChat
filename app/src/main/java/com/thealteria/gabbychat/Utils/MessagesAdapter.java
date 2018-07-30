package com.thealteria.gabbychat.Utils;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.thealteria.gabbychat.Model.Messages;
import com.thealteria.gabbychat.R;

import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {


    private List<Messages> mMessageList;
    private DatabaseReference mUserDatabase, userDB;
    private FirebaseAuth mAuth;
    private LayoutParams layoutparams;


    public MessagesAdapter(List<Messages> mMessageList) {

        this.mMessageList = mMessageList;

    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_single_layout, parent, false);

        return new MessageViewHolder(v);

    }

    class MessageViewHolder extends RecyclerView.ViewHolder {

        private final RelativeLayout messageRelLayout;
        TextView messageText, timeText;
        //CircleImageView profileImage;
        ImageView messageImage;

        MessageViewHolder(View view) {
            super(view);

            messageText = view.findViewById(R.id.messageTextLayout);
            messageRelLayout = view.findViewById(R.id.messageRelLayout);
            //profileImage = view.findViewById(R.id.messageProfileLayout);
            //timeText = view.findViewById(R.id.messageTime);
            messageImage = view.findViewById(R.id.messageImage);
            layoutparams = (LayoutParams)messageRelLayout.getLayoutParams();

        }
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder viewHolder, int i) {

        mAuth = FirebaseAuth.getInstance();

        String currentUser = mAuth.getCurrentUser().getUid();

        final Messages messages = mMessageList.get(i);
        String from_user = messages.getFrom();
        String messageTye = messages.getType();

        userDB = FirebaseDatabase.getInstance().getReference().child("Users");
        userDB.keepSynced(true);

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);
//        mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                //String name = dataSnapshot.child("name").getValue().toString();
//                final String image = dataSnapshot.child("thumb_image").getValue().toString();
//
//                //viewHolder.displayName.setText(name);
//                Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.boy)
//                        .error(R.drawable.boy).into(viewHolder.profileImage, new Callback() {
//                    @Override
//                    public void onSuccess() {
//
//                    }
//
//                    @Override
//                    public void onError(Exception e) {
//                        Picasso.get().load(image).placeholder(R.drawable.boy)
//                                .error(R.drawable.boy).into(viewHolder.profileImage);
//                    }
//                });
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });

        if (messageTye.equals("text")) {
            viewHolder.messageImage.setVisibility(View.INVISIBLE);
            viewHolder.messageText.setText(messages.getMessage());
        }

        else if (messageTye.equals("image")) {
            viewHolder.messageText.setVisibility(View.INVISIBLE);
            Picasso.get().load(messages.getMessage()).networkPolicy(NetworkPolicy.OFFLINE)
                    .resize(500, 500)
                    .centerCrop()
                    .into(viewHolder.messageImage, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(Exception e) {
                    Picasso.get().load(messages.getMessage()).resize(500, 500)
                            .centerCrop().into(viewHolder.messageImage);
                }
            });
        }

        if (from_user.equals(currentUser)) {
            
            //layoutparams.setMargins(0, 5, 10, 0);
            viewHolder.messageText.setBackgroundResource(R.drawable.message_text_white);
            viewHolder.messageText.setTextColor(Color.BLACK);
            viewHolder.messageRelLayout.setGravity(Gravity.END);

        }
        else {
            viewHolder.messageText.setBackgroundResource(R.drawable.message_text_back);
            viewHolder.messageText.setTextColor(Color.WHITE);
            viewHolder.messageRelLayout.setGravity(Gravity.START);

        }
        viewHolder.messageText.setText(messages.getMessage());
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }
}





