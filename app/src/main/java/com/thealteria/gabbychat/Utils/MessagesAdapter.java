package com.thealteria.gabbychat.Utils;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.thealteria.gabbychat.R;
import android.widget.RelativeLayout.LayoutParams;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

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

        TextView messageText, timeText;
        CircleImageView profileImage;
        ImageView messageImage;

        MessageViewHolder(View view) {
            super(view);

            messageText = view.findViewById(R.id.messageTextLayout);
            profileImage = view.findViewById(R.id.messageProfileLayout);
            timeText = view.findViewById(R.id.messageTime);
            messageImage = view.findViewById(R.id.messageImage);
            //layoutparams = (LayoutParams)messageText.getLayoutParams();

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
        mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //String name = dataSnapshot.child("name").getValue().toString();
                final String image = dataSnapshot.child("thumb_image").getValue().toString();

                //viewHolder.displayName.setText(name);
                Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.boy)
                        .error(R.drawable.boy).into(viewHolder.profileImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(image).placeholder(R.drawable.boy)
                                .error(R.drawable.boy).into(viewHolder.profileImage);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (messageTye.equals("text")) {
            viewHolder.messageText.setText(messages.getMessage());
            viewHolder.messageImage.setVisibility(View.INVISIBLE);
        }

        else {
            viewHolder.messageText.setVisibility(View.INVISIBLE);
            Picasso.get().load(messages.getMessage()).networkPolicy(NetworkPolicy.OFFLINE)
                    .into(viewHolder.messageImage, new Callback() {
                @Override
                public void onSuccess() {


                }

                @Override
                public void onError(Exception e) {
                    Picasso.get().load(messages.getMessage()).into(viewHolder.messageImage);
                }
            });
        }

            if (from_user.equals(currentUser)) {
            viewHolder.messageText.setBackgroundResource(R.drawable.message_text_white);
            viewHolder.messageText.setTextColor(Color.BLACK);

//            layoutparams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//            viewHolder.messageText.setLayoutParams(layoutparams);

        }
        else {
            viewHolder.messageText.setBackgroundResource(R.drawable.message_text_back);
            viewHolder.messageText.setTextColor(Color.WHITE);
        }

        viewHolder.messageText.setText(messages.getMessage());
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }
}





