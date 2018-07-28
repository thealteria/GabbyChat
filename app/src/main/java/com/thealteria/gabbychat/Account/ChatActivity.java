package com.thealteria.gabbychat.Account;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.storage.StorageManager;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.thealteria.gabbychat.R;
import com.thealteria.gabbychat.Utils.GetTimeAgo;
import com.thealteria.gabbychat.Utils.Messages;
import com.thealteria.gabbychat.Utils.MessagesAdapter;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class ChatActivity extends AppCompatActivity {

    private String chatUser, chatName, chatThumbImage, currentUserId;
    private Toolbar mChatToolbar;

    private TextView titleView, lastSeen;
    private CircleImageView profileImage;
    private RecyclerView messagesList;

    private DatabaseReference rootRef, messageRef;
    private FirebaseAuth mAuth;

    private ImageButton chatAddBtn, chatSendBtn;
    private EditText chatMsg;

    private final List<Messages> mMessagesList = new ArrayList<>();
    private LinearLayoutManager linearLayout;
    private MessagesAdapter adapter;

    private SwipeRefreshLayout refreshLayout;

    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int currentPage = 1, itemPos = 0;
    private Query messageQuery;

    private String lastKey = "";
    private String prevKey = "";
    private static final int GALLERY_PICK = 1;
    private StorageReference imageStorage;
    private DatabaseReference userDB;

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
        currentUserId = mAuth.getCurrentUser().getUid();

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

        titleView = findViewById(R.id.chatName);
        lastSeen = findViewById(R.id.chatLastSeen);
        profileImage = findViewById(R.id.chatImage);
        chatAddBtn = findViewById(R.id.chat_add);
        chatSendBtn = findViewById(R.id.chat_send);
        chatMsg = findViewById(R.id.chat_message);

        adapter = new MessagesAdapter(mMessagesList);
        imageStorage=  FirebaseStorage.getInstance().getReference();

        messagesList = findViewById(R.id.messagesList);
        refreshLayout = findViewById(R.id.messageSwipe);

        linearLayout = new LinearLayoutManager(this);
        messagesList.setHasFixedSize(true);
        messagesList.setLayoutManager(linearLayout);

        messagesList.setAdapter(adapter);

        loadMessages();

        titleView.setText(mchatName);

        rootRef.child("Users").child(chatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String online = dataSnapshot.child("online").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                if (online.equals("true")) {
                    lastSeen.setText("Online");
                }

                else {
                    GetTimeAgo getTimeAgo = new GetTimeAgo();
                    long lastTime = Long.parseLong(online);
                    String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, getApplicationContext());

                    lastSeen.setText("last seen " + lastSeenTime);
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
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + currentUserId + "/" + chatUser, chatAddMap);
                    chatUserMap.put("Chat/" + chatUser + "/" + currentUserId, chatAddMap);

                    rootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError != null) {
                                Log.d("CHAT LOG", databaseError.getMessage().toString());
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

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                currentPage++;
                itemPos = 0;
                loadMoreMessages();

            }
        });
    }


    private void loadMoreMessages() {

        messageRef = rootRef.child("messages").child(currentUserId).child(chatUser);
        messageQuery = messageRef.orderByKey().endAt(lastKey).limitToLast(10);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages messages = dataSnapshot.getValue(Messages.class);

                String messageKey = dataSnapshot.getKey();

                if (!prevKey.equals(messageKey)) {
                    mMessagesList.add(itemPos++, messages);
                }

                else {
                    prevKey = lastKey;
                }

                if(itemPos == 1) {

                    lastKey = messageKey;
                }


                Log.d("TOTAL_KEYS", "lastKey: "+ lastKey + "| Prevkey: " + prevKey + "| MsgKey: " + messages);

                adapter.notifyDataSetChanged();
                refreshLayout.setRefreshing(false);

                linearLayout.scrollToPositionWithOffset(10, 0);
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


    }

    private void loadMessages() {

        messageRef = rootRef.child("messages").child(currentUserId).child(chatUser);
        messageQuery = messageRef.limitToLast(currentPage * TOTAL_ITEMS_TO_LOAD);

        messageQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        Messages messages = dataSnapshot.getValue(Messages.class);

                        itemPos++;

                        if(itemPos == 1) {
                            String messageKey = dataSnapshot.getKey();

                            lastKey = messageKey;
                            prevKey = messageKey;
                        }

                        mMessagesList.add(messages);

                        adapter.notifyDataSetChanged();
                        messagesList.scrollToPosition(mMessagesList.size() - 1);
                        refreshLayout.setRefreshing(false);

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

    }

//    public void chat_add(View view) {
//        Intent gallery = new Intent();
//        gallery.setType("image/*");
//        gallery.setAction(Intent.ACTION_GET_CONTENT);
//
//        startActivityForResult(Intent.createChooser(gallery, "Select Image"), GALLERY_PICK);
//
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri).start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                Uri imageUri = result.getUri();

                final File thumb_filepath = new File(imageUri.getPath());
                final byte[] thumb_byte;


                Bitmap thumb_bitmap = null;
                try {
                    thumb_bitmap = new Compressor(this)
                            .setQuality(75)
                            .compressToBitmap(thumb_filepath);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                thumb_byte = baos.toByteArray();

                final String currentUserRef = "messages/" + currentUserId + "/" + chatUser;
                final String chatUserRef = "messages/" + chatUser + "/" + currentUserId;

                DatabaseReference userMsgPush = rootRef.child("messages")
                        .child(currentUserId).child(chatUser).push();

                final String pushId = userMsgPush.getKey();

                final StorageReference storageReference = imageStorage.child("message_images").child(pushId + ".jpg");
                final StorageReference thumb_filePath = imageStorage.child("message_images")
                        .child("thumb_image").child(pushId + ".jpg");

                storageReference.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {

                            final String downloadUrl = task.getResult().getDownloadUrl().toString();
                            UploadTask uploadTask = thumb_filePath.putBytes(thumb_byte);

                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    final String thumb_download_url = task.getResult().getDownloadUrl().toString();

                                    updateDatabase(downloadUrl, "image", currentUserRef, chatUserRef, pushId);
                                    updateDatabase(thumb_download_url, "thumb_pic", currentUserRef, chatUserRef, pushId);

                                }
                            });

                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Error in sending image",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public void chat_send(View view) {
        String message = chatMsg.getText().toString();
        if(!TextUtils.isEmpty(message)) {

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
                    Log.d("CHAT_LOG", databaseError.getMessage().toString());
                }
            }
        });
    }
}
