package com.thealteria.gabbychat.Utils;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

    public MessagesAdapter(List<Messages> mMessageList) {

        this.mMessageList = mMessageList;

    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_single_layout, parent, false);

        return new MessageViewHolder(v);

    }

    class MessageViewHolder extends RecyclerView.ViewHolder {

        private ImageView messageImageRight, messageImageLeft;
        private TextView messageTextLeft, timeText, messageTextRight;
        private LinearLayout leftLayout, rightLayout;

        private String messageTye;
        //CircleImageView profileImage;

        MessageViewHolder(View view) {
            super(view);

            messageTextLeft = view.findViewById(R.id.messageTextLeft);
            messageTextRight = view.findViewById(R.id.messageTextRight);
           //profileImage = view.findViewById(R.id.messageProfileLayout);
            //timeText = view.findViewById(R.id.messageTime);
            messageImageRight = view.findViewById(R.id.messageImageRight);
            messageImageLeft = view.findViewById(R.id.messageImageLeft);
            leftLayout = view.findViewById(R.id.leftLayout);
            rightLayout = view.findViewById(R.id.rightLayout);

        }
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder viewHolder, int i) {

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


        if (from_user.equals(currentUser)) {
            viewHolder.messageTextLeft.setVisibility(View.GONE);
            viewHolder.messageImageLeft.setVisibility(View.GONE);
            viewHolder.leftLayout.setVisibility(View.GONE);
            viewHolder.rightLayout.setVisibility(View.VISIBLE);

            if(messageTye.equals("text")) {
                viewHolder.messageTextRight.setText(messages.getMessage());
                viewHolder.messageTextRight.setVisibility(View.VISIBLE);
                viewHolder.messageImageRight.setVisibility(View.GONE);
            }

            else if (messageTye.equals("image")) {
                viewHolder.messageTextRight.setVisibility(View.GONE);
                viewHolder.messageImageRight.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).networkPolicy(NetworkPolicy.OFFLINE)
                        .resize(400, 400)
                        .centerCrop()
                        .into(viewHolder.messageImageRight, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {
                                Picasso.get().load(messages.getMessage()).resize(400, 400)
                                        .centerCrop().into(viewHolder.messageImageRight);
                            }
                        });
            }
        }
        else {
            viewHolder.messageTextRight.setVisibility(View.GONE);
            viewHolder.messageImageRight.setVisibility(View.GONE);
            viewHolder.rightLayout.setVisibility(View.GONE);
            viewHolder.leftLayout.setVisibility(View.VISIBLE);

            if (messageTye.equals("text")) {
                viewHolder.messageTextLeft.setText(messages.getMessage());
                //viewHolder.messageImageLeft.setVisibility(View.GONE);
            }

            else if (messageTye.equals("image")) {
                viewHolder.messageTextLeft.setVisibility(View.GONE);
                viewHolder.messageImageLeft.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).networkPolicy(NetworkPolicy.OFFLINE)
                        .resize(400, 400)
                        .centerCrop()
                        .into(viewHolder.messageImageLeft, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {
                                Picasso.get().load(messages.getMessage()).resize(400, 400)
                                        .centerCrop().into(viewHolder.messageImageLeft);
                            }
                        });
            }
        }

    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    /* public void setMessage(TextView goneText, TextView showText, final ImageView goneImage, final ImageView showImage) {

        goneText.setVisibility(View.GONE);
        goneImage.setVisibility(View.GONE);

        if(messageTye.equals("text")) {
            showText.setText(messages.getMessage());
            showImage.setVisibility(View.GONE);
        }

        else if (messageTye.equals("image")) {
            showText.setVisibility(View.INVISIBLE);
            Picasso.get().load(messages.getMessage()).networkPolicy(NetworkPolicy.OFFLINE)
                    .resize(400, 400)
                    .centerCrop()
                    .into(showImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(messages.getMessage()).resize(400, 400)
                                    .centerCrop().into(showImage);
                        }
                    });
        }
    } */
}




