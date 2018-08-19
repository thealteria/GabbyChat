package com.thealteria.gabbychat.Account;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.thealteria.gabbychat.Model.Messages;
import com.thealteria.gabbychat.R;
import com.thealteria.gabbychat.Utils.GetTimeAgo;
import com.thealteria.gabbychat.Utils.MessagesAdapter;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String chatUser, currentUserId;
    private Toolbar mChatToolbar;

    private TextView lastSeen;
    private CircleImageView profileImage;
    private RecyclerView messagesList;

    private DatabaseReference rootRef, typingRef, chatRef, messageRef, userDB ;
    private FirebaseAuth mAuth;

    private ImageButton chatSendBtn;
    private EditText chatMsg;

    private final List<Messages> mMessagesList = new ArrayList<>();
    private MessagesAdapter adapter;

    //private SwipeRefreshLayout refreshLayout;

//    private static final int TOTAL_ITEMS_TO_LOAD = 10;
//    private int currentPage = 1, itemPos = 0;
//    private Query messageQuery;
//
//    private String lastKey = "";
//    private String prevKey = "";
    private static final int GALLERY_PICK = 1;
    private StorageReference imageStorage;
    private ProgressDialog progressDialog;


    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mChatToolbar = findViewById(R.id.chatAppbar);
        setSupportActionBar(mChatToolbar);

        rootRef = FirebaseDatabase.getInstance().getReference();
        userDB = FirebaseDatabase.getInstance().getReference().child("Users");
        userDB.keepSynced(true);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        chatUser = getIntent().getStringExtra("user_id");
        String mchatName = getIntent().getStringExtra("chat_name");

        ActionBar actionBar = getSupportActionBar();

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = null;
        if (inflater != null) {
            actionBarView = inflater.inflate(R.layout.chat_custom_bar, null);
        }

        if (actionBar != null) {
            actionBar.setCustomView(actionBarView);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);

        }

        TextView titleView = findViewById(R.id.chatName);
        lastSeen = findViewById(R.id.chatLastSeen);
        profileImage = findViewById(R.id.chatImage);
        ImageButton chatAddBtn = findViewById(R.id.chat_add);
        chatSendBtn = findViewById(R.id.chat_send);
        chatMsg = findViewById(R.id.chat_message);

        adapter = new MessagesAdapter(mMessagesList);
        imageStorage=  FirebaseStorage.getInstance().getReference();

        messagesList = findViewById(R.id.messagesList);
        //refreshLayout = findViewById(R.id.messageSwipe);

        LinearLayoutManager linearLayout = new LinearLayoutManager(this);
        messagesList.setHasFixedSize(true);
        messagesList.setLayoutManager(linearLayout);

        messagesList.setAdapter(adapter);

        loadMessages();

        titleView.setText(mchatName);

        chatRef = FirebaseDatabase.getInstance().getReference().child("Chat").child(chatUser).child(currentUserId);

        chatMsg.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, int start, int before, int count) {

                if (!TextUtils.isEmpty(s.toString()) && s.toString().trim().length() > 0) {

                    rootRef.child("Chat").child(chatUser)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(currentUserId)) {
                                        chatRef.child("typing").setValue(true);
                                        chatSendBtn.setEnabled(true);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                    }

                else if (s.toString().trim().length() == 0)  {
                    chatRef.child("typing").setValue(false);
                    chatSendBtn.setEnabled(false);
                }
        }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        rootRef.child("Users").child(chatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                final String online = Objects.requireNonNull(dataSnapshot.child("online").getValue()).toString();
                final String image = Objects.requireNonNull(dataSnapshot.child("image").getValue()).toString();

                Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.boy)
                        .error(R.drawable.boy).into(profileImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(image).placeholder(R.drawable.boy)
                                .error(R.drawable.boy).into(profileImage);
                    }
                });

                if (online.equals("true")) {
                    lastSeen.setText("Online");
                }
                else {
                    typingRef = FirebaseDatabase.getInstance().getReference().child("Chat").
                            child(currentUserId).child(chatUser);
                    typingRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String bool = Objects.requireNonNull(dataSnapshot.child("typing").getValue()).toString();

                            if (!bool.equals("false")) {
                                lastSeen.setText("typing..");
                            }

                            else {
                                GetTimeAgo getTimeAgo = new GetTimeAgo();
                                long lastTime = Long.parseLong(online);
                                String lastSeenTime = GetTimeAgo.getTimeAgo(lastTime, getApplicationContext());

                                lastSeen.setText("last seen " + lastSeenTime);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        rootRef.child("Chat").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(chatUser)) {
                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen", false);
                    chatAddMap.put("typing", false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + currentUserId + "/" + chatUser, chatAddMap);
                    chatUserMap.put("Chat/" + chatUser + "/" + currentUserId, chatAddMap);

                    rootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError != null) {
                                Log.d("CHAT LOG", databaseError.getMessage());
                            }

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        chatAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent();
        gallery.setType("image/*");
        gallery.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(gallery, "Select Image"), GALLERY_PICK);
            }
        });

//        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                currentPage++;
//                itemPos = 0;
//                loadMoreMessages();
//
//            }
//        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK && data != null) {
            
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Sending image");
            progressDialog.setMessage("Please wait while sending the image..!!");
            progressDialog.setCanceledOnTouchOutside(false);
            Uri imageUri = data.getData();
            progressDialog.show();
            CropImage.activity(imageUri).start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                Uri imageUri = result.getUri();

                final String currentUserRef = "messages/" + currentUserId + "/" + chatUser;
                final String chatUserRef = "messages/" + chatUser + "/" + currentUserId;

                DatabaseReference userMsgPush = rootRef.child("messages")
                        .child(currentUserId).child(chatUser).push();

                final String pushId = userMsgPush.getKey();

                final StorageReference storageReference = imageStorage.child("message_images").child(pushId + ".jpg");
                storageReference.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()) {

                            String downloadUrl = task.getResult().getDownloadUrl().toString();
                            updateDatabase(downloadUrl, "image", currentUserRef, chatUserRef, pushId);
                            Toast.makeText(getApplicationContext(),
                                    "Image send successfully", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }

                        else {
                            Toast.makeText(getApplicationContext(), "Error in sending image",
                                    Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }
                });
            }

            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.d("SENDING_IMAGE", error.getMessage());
                progressDialog.dismiss();
            }
        }
    }

//        private void loadMoreMessages() {
//
//        messageRef = rootRef.child("messages").child(currentUserId).child(chatUser);
//        messageQuery = messageRef.orderByKey().endAt(lastKey).limitToLast(10);
//
//        messageQuery.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                Messages messages = dataSnapshot.getValue(Messages.class);
//
//                String messageKey = dataSnapshot.getKey();
//
//                if (!prevKey.equals(messageKey)) {
//                    mMessagesList.add(itemPos++, messages);
//                }
//
//                else {
//                    prevKey = lastKey;
//                }
//
//                if(itemPos == 1) {
//
//                    lastKey = messageKey;
//                }
//
//
//                Log.d("TOTAL_KEYS", "lastKey: "+ lastKey + "| Prevkey: " + prevKey + "| MsgKey: " + messages);
//
//                adapter.notifyDataSetChanged();
//                refreshLayout.setRefreshing(false);
//
//                linearLayout.scrollToPositionWithOffset(10, 0);
//            }
//
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//
//
//    }

    private void loadMessages() {

        rootRef.child("messages").child(currentUserId).child(chatUser)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Messages messages = dataSnapshot.getValue(Messages.class);
                        mMessagesList.add(messages);
                        adapter.notifyDataSetChanged();

                        messagesList.scrollToPosition(mMessagesList.size() - 1);

                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


//        messageRef = rootRef.child("messages").child(currentUserId).child(chatUser);
//        messageQuery = messageRef.limitToLast(currentPage * TOTAL_ITEMS_TO_LOAD);
//
//        messageQuery.addChildEventListener(new ChildEventListener() {
//                    @Override
//                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//
//                        Messages messages = dataSnapshot.getValue(Messages.class);
//
//                        itemPos++;
//
//                        if(itemPos == 1) {
//                            String messageKey = dataSnapshot.getKey();
//
//                            lastKey = messageKey;
//                            prevKey = messageKey;
//                        }
//
//                        mMessagesList.add(messages);
//
//                        adapter.notifyDataSetChanged();
//                        messagesList.scrollToPosition(mMessagesList.size() - 1);
//                        refreshLayout.setRefreshing(false);
//
//                    }
//
//                    @Override
//                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//
//                    }
//
//                    @Override
//                    public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//                    }
//
//                    @Override
//                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//
//                    }
//                });

    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        super.onCreateOptionsMenu(menu);
//        getMenuInflater().inflate(R.menu.main_menu, menu);
//        MenuItem item;
//
//        item = menu.findItem(R.id.logout);
//        item.setVisible(false);
//
//        item= menu.findItem(R.id.all_users);
//        item.setVisible(false);
//        item= menu.findItem(R.id.accsettings);
//        item.setVisible(false);
//
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        super.onOptionsItemSelected(item);
//
//        if (item.getItemId() == R.id.call) {
//            Intent intent = new Intent(getApplicationContext(), CallActivity.class);
//            intent.putExtra("recipientId", chatUser);
//            chatUser = getIntent().getStringExtra("user_id");
//            startActivity(intent);
//        }
//        return true;
//    }

    public void chat_send(View view) {
        String message = chatMsg.getText().toString();

        if (message.equals("")) {
            Toast.makeText(getApplicationContext(), "please type", Toast.LENGTH_SHORT).show();
        }
        else  {

            chatSendBtn.setEnabled(true);
            String currentUserRef = "messages/" + currentUserId + "/" + chatUser;
            String chatUserRef = "messages/" + chatUser + "/" + currentUserId;

            DatabaseReference userMsgPush = rootRef.child("messages")
                    .child(currentUserId).child(chatUser).push();

            final String pushId = userMsgPush.getKey();

            updateDatabase(message, "text", currentUserRef, chatUserRef, pushId);
        }

    }

    public void updateDatabase(String message, String type, String currentUserMsg, String chatUserMsg, String mPushId) {
        Map messageMap = new HashMap();
        messageMap.put("message", message);
        messageMap.put("seen", false);
        messageMap.put("type", type);
        messageMap.put("time", ServerValue.TIMESTAMP);
        messageMap.put("from", currentUserId);

        Map messageUserMap = new HashMap();
        messageUserMap.put(currentUserMsg + "/" + mPushId, messageMap);
        messageUserMap.put(chatUserMsg + "/" + mPushId, messageMap);

        chatMsg.getText().clear();

        rootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                if(databaseError != null) {
                    Log.d("CHAT_LOG", databaseError.getMessage());
                }
            }
        });
    }
}
