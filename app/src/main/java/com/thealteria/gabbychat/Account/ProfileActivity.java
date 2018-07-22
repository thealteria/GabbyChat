package com.thealteria.gabbychat.Account;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.thealteria.gabbychat.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private TextView mName, mStatus, mFriendsCount;
    private ImageView mProfilePic;
    private Button mSendRequest;

    private DatabaseReference reference;
    private ProgressDialog progressDialog;
    private DatabaseReference friendRequestDB;

    private FirebaseUser currentUser;
    private String current_state;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        uid = getIntent().getStringExtra("user_id");

        reference = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        friendRequestDB = FirebaseDatabase.getInstance().getReference().child("Friend_Request");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        mProfilePic = findViewById(R.id.profilePic);
        mName = findViewById(R.id.displayName);
        mStatus = findViewById(R.id.displayStatus);
        mFriendsCount = findViewById(R.id.friends);
        mSendRequest = findViewById(R.id.sendRequest);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading Profile");
        progressDialog.setMessage("Please wait while loading the profile..!!");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        current_state = "not_friends";

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                Picasso.get().load(image).placeholder(R.drawable.boy).error(R.drawable.boy).into(mProfilePic);

                mName.setText(name);
                mStatus.setText(status);

                progressDialog.dismiss();


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //Friend Request Feature

    public void sendRequest(View view) {
        if (current_state.equals("not_friends")) {

            // our uid and friend's uid ||user key 1 && user key 2

            friendRequestDB.child(currentUser.getUid()).child(uid).child("request_type").setValue("sent")
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {

                                //friend's uid and our uid || user key 2 && user key 1
                                friendRequestDB.child(uid).child(currentUser.getUid()).child("request_type")
                                        .setValue("received")
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                Toast.makeText(getApplicationContext(), "Friend Request Sent!",
                                                        Toast.LENGTH_LONG).show();

                                            }
                                        });

                            }

                            else {
                                Toast.makeText(getApplicationContext(), "Failed to send Friend Request !!",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });

        }
    }
}
