package com.thealteria.gabbychat.Account;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.thealteria.gabbychat.R;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "MyActivity";

    private TextView mName, mStatus, mFriendsCount;
    private ImageView mProfilePic;
    private Button mSendRequest, mDeclineBtn;

    private FirebaseAuth mAuth;

    private ProgressDialog progressDialog;
    private DatabaseReference reference, friendRequestDB, friendDatabase, notificationsDatabase, mRootRef, userRef;

    private FirebaseUser currentUser;
    private String current_state;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        uid = getIntent().getStringExtra("user_id");

        mRootRef = FirebaseDatabase.getInstance().getReference();

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        friendRequestDB = FirebaseDatabase.getInstance().getReference().child("Friend_Request");
        friendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        //notificationsDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");
        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

        mProfilePic = findViewById(R.id.profilePic);
        mName = findViewById(R.id.displayName);
        mStatus = findViewById(R.id.displayStatus);
        mFriendsCount = findViewById(R.id.friends);
        mSendRequest = findViewById(R.id.sendRequest);
        mDeclineBtn = findViewById(R.id.declineRequest);


        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading Profile");
        progressDialog.setMessage("Please wait while loading the profile..!!");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        current_state = "not_friends";

        disableDeclineBtn();


        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                Picasso.get().load(image).placeholder(R.drawable.boy).error(R.drawable.boy).into(mProfilePic);

                mName.setText(name);
                mStatus.setText(status);

                // ------------ RECEIVED REQ ---------------

                friendRequestDB.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(uid)) {

                            String req_type = dataSnapshot.child(uid).child("request_type").getValue().toString();

                            if (req_type.equals("received")) {

                                current_state = "req_received";
                                mSendRequest.setText("Accept Friend Request");

                                mDeclineBtn.setVisibility(View.VISIBLE);
                                mDeclineBtn.setEnabled(true);
                            }

                            else if (req_type.equals("sent")) {

                                current_state = "req_sent";
                                mSendRequest.setText("Cancel Friend Request");

                                disableDeclineBtn();
                            }

                            progressDialog.dismiss();

                        }

                        else {

                            friendDatabase.child(currentUser.getUid())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            if (dataSnapshot.hasChild(uid)) {

                                                current_state = "friends";
                                                mSendRequest.setText("Unfriend this person");

                                                disableDeclineBtn();
                                            }

                                            progressDialog.dismiss();
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            progressDialog.dismiss();

                                        }
                                    });
                        }
                        }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // ------------SENDING REQ STATE---------------

    // NOT FRIENDS

    public void sendRequest(View view) {

        mSendRequest.setEnabled(false);

        if (current_state.equals("not_friends")) {

            DatabaseReference newNotiRef = mRootRef.child("notifications").child(uid).push();
            String newNotiId = newNotiRef.getKey();

            HashMap<String, String> notificationData = new HashMap<>();
            notificationData.put("from", currentUser.getUid());
            notificationData.put("type", "request");

            Map requestMap = new HashMap();
            requestMap.put("Friend_Request/" + currentUser.getUid() + "/" + uid + "/request_type", "sent");
            requestMap.put("Friend_Request/" + uid + "/" + currentUser.getUid() + "/request_type", "received");
            requestMap.put("notifications/" + uid + "/" + newNotiId, notificationData);

            mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                    if (databaseError == null) {

                        requestState(mSendRequest, "req_sent", "Cancel Friend Request"
                                , "Friend Request Sent!");
                        }

                    else {
                        Toast.makeText(getApplicationContext(), " Error occured while sending request!"
                                , Toast.LENGTH_LONG).show();
                    }
                }
            });

            }

        // ------------ CANCEL REQ STATE---------------
        if (current_state.equals("req_sent")) {

            reqState("Friend_Request", mSendRequest, "not_friends",
                    "Send Friend Request", "Request Canceled!");
            disableDeclineBtn();
        }

        // ------------ACCEPT REQUEST---------------

        if (current_state.equals("req_received")) {
            final String currentDate = DateFormat.getDateInstance().format(new Date());
            final String currentTime = DateFormat.getTimeInstance().format(new Date());

            Map friendsMap = new HashMap();
            friendsMap.put("Friends/" + currentUser.getUid() + "/" + uid + "/date", currentDate);
            friendsMap.put("Friends/" + currentUser.getUid() + "/" + uid + "/time", currentTime);
            friendsMap.put("Friends/" + uid + "/" + currentUser.getUid() + "/date", currentDate);
            friendsMap.put("Friends/" + uid + "/" + currentUser.getUid() + "/time", currentTime);

            friendsMap.put("Friend_Request/" + currentUser.getUid() + "/" + uid, null);
            friendsMap.put("Friend_Request/" + uid + "/" + currentUser.getUid(), null);

            mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                    if (databaseError == null) {

                        requestState(mSendRequest, "friends", "Unfriend this person",
                                "Friend Request Accepted!");
                        disableDeclineBtn();
                    }

                    else {
                        String error = databaseError.getMessage();
                        Log.d(TAG, error);

                    }

                }
            });
        }

        // ------------ UNFRIEND REQ STATE---------------

        if (current_state.equals("friends")) {

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(ProfileActivity.this);
            alertDialog.setTitle("Unfriend");
            alertDialog.setMessage("Are you sure you want to unfriend this person?");
            alertDialog.setIcon(R.drawable.ic_out);

            alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int which) {

                    reqState("Friends", mSendRequest,
                            "not_friends", "Send Friend Request", "Unfriended succesfully!");
                    }
            });

            alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    mSendRequest.setEnabled(true);
                    dialog.cancel();
                }
            });

            alertDialog.show();

        }

    }

    // ----------- DECLINE FRIEND REQ----------

    public void declineRequest(View view) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ProfileActivity.this);
        alertDialog.setTitle("Decline Request");
        alertDialog.setMessage("Are you sure you want to decline the Request?");

        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {


                reqState("Friend_Request", mSendRequest, "not_friends",
                        "Send Friend Request", "Request declined succesfully!");
                }
        });

        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }

    // ------------REQ STATE--------------

    public void reqState(String userData, final Button button,
                         final String currentState, final String buttonText, final String toast) {

        Map declineMap = new HashMap();

        declineMap.put(userData + "/" + currentUser.getUid() + "/" + uid, null);
        declineMap.put(userData + "/" + uid + "/" + currentUser.getUid(), null);

        mRootRef.updateChildren(declineMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                if(databaseError == null) {

                    current_state = currentState;
                    button.setText(buttonText);

                    Toast.makeText(getApplicationContext(), toast,
                            Toast.LENGTH_LONG).show();
                    }

                else {
                    String error = databaseError.getMessage();
                    Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_LONG).show();
                }

                mSendRequest.setEnabled(true);
            }
        });

    }

    private void requestState(Button button, String currentState, String buttonText, String toast) {

        mSendRequest.setEnabled(true);
        current_state = currentState;
        button.setText(buttonText);

        disableDeclineBtn();

        Toast.makeText(getApplicationContext(), toast,
                Toast.LENGTH_LONG).show();
    }

    private void disableDeclineBtn() {

        mDeclineBtn.setVisibility(View.INVISIBLE);
        mDeclineBtn.setEnabled(false);
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//
//        userRef.child("online").setValue(true);
//    }
}
