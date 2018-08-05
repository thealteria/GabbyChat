package com.thealteria.gabbychat.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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
import com.thealteria.gabbychat.Account.ChatActivity;
import com.thealteria.gabbychat.Account.ProfileActivity;
import com.thealteria.gabbychat.Model.Friends;
import com.thealteria.gabbychat.Model.Request;
import com.thealteria.gabbychat.Model.Users;
import com.thealteria.gabbychat.R;
import com.thealteria.gabbychat.UsersActivity;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestFragment extends Fragment {

    private RecyclerView requestList;
    private TextView noRequests, status;

    private DatabaseReference usersDatabase, mRootRef, requestDatabase, requestType;

    private String currentUserId, uid;
    private LinearLayout requestLayout;
    private FirebaseRecyclerAdapter adapter;


    public RequestFragment() {
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
        View view = inflater.inflate(R.layout.fragment_request, container, false);

        requestList = view.findViewById(R.id.request_list);
        noRequests = view.findViewById(R.id.noRequests);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        currentUserId = mAuth.getCurrentUser().getUid();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        requestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_Request");
        usersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        requestList.setHasFixedSize(true);
        LinearLayoutManager linearLayout = new LinearLayoutManager(getContext());
        requestList.setLayoutManager(linearLayout);

        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(requestList.getContext(),
                linearLayout.getOrientation());
        requestList.addItemDecoration(mDividerItemDecoration);

        requestDatabase.keepSynced(true);
        usersDatabase.keepSynced(true);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Request> options =
                            new FirebaseRecyclerOptions.Builder<Request>()
                                    .setQuery(requestDatabase.child(currentUserId),
                                            Request.class).build();

                    adapter = new FirebaseRecyclerAdapter<Request, RequestViewHolder>(options) {
                        @Override
                        protected void onBindViewHolder(@NonNull final RequestViewHolder holder,
                                                        int position, @NonNull Request model) {

                            uid = getRef(position).getKey();
                            requestType = getRef(position).child("request_type").getRef();

                            requestType.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if(dataSnapshot.exists()) {
                                        String requestType = dataSnapshot.getValue().toString();
                                        if (requestType.equals("received")) {

                                            if (uid != null) {

                                                usersDatabase.child(uid).addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        final String name = dataSnapshot.child("name").getValue().toString();
                                                        final String thumb_image = dataSnapshot.child("thumb_image").
                                                                getValue().toString();

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
                                                                            intent.putExtra("user_id", uid);
                                                                            startActivity(intent);
                                                                        }

                                                                        if (which == 1) {
                                                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                                                            chatIntent.putExtra("user_id", uid);
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

                                                    }
                                                });
                                            }
                                        }

                                        else if(requestType.equals("sent")) {

                                            if (uid != null) {

                                                usersDatabase.child(uid).addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        final String name = dataSnapshot.child("name").getValue().toString();
                                                        final String thumb_image = dataSnapshot.child("thumb_image").
                                                                getValue().toString();

                                                        holder.setName(name);
                                                        holder.setImage(thumb_image);
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });
                                            }
                                        }

                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }

                        @NonNull
                        @Override
                        public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                            Button acceptRequest, declineRequest;

                            View view;
                            view = LayoutInflater.from(parent.getContext()).
                                    inflate(R.layout.users_single_layout, parent, false);

                            requestLayout = view.findViewById(R.id.requestLayout);
                            requestLayout.setVisibility(View.VISIBLE);
                            status = view.findViewById(R.id.userStatus);
                            status.setVisibility(View.GONE);

                            acceptRequest = view.findViewById(R.id.accept);
                            declineRequest = view.findViewById(R.id.decline);

                            acceptRequest.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                                    alertDialog.setTitle("Decline Request");
                                    alertDialog.setMessage("Are you sure you want to decline the Request?");

                                    alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,int which) {

                                            final String currentDate = DateFormat.getDateInstance().format(new Date());
                                            final String currentTime = DateFormat.getTimeInstance().format(new Date());

                                            Map friendsMap = new HashMap();
                                            friendsMap.put("Friends/" + currentUserId + "/" + uid + "/date", currentDate);
                                            friendsMap.put("Friends/" + currentUserId + "/" + uid + "/time", currentTime);
                                            friendsMap.put("Friends/" + uid + "/" + currentUserId + "/date", currentDate);
                                            friendsMap.put("Friends/" + uid + "/" + currentUserId + "/time", currentTime);

                                            friendsMap.put("Friend_Request/" + currentUserId + "/" + uid, null);
                                            friendsMap.put("Friend_Request/" + uid + "/" + currentUserId, null);

                                            mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                                                @Override
                                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                                    if (databaseError == null) {

                                                        Toast.makeText(getContext(), "Friend Request Accepted!",
                                                                Toast.LENGTH_LONG).show();
                                                    }

                                                    else {
                                                        String error = databaseError.getMessage();
                                                        Log.d("REQFRAGMENT_ACCEPT_REQ", error);

                                                    }

                                                }
                                            });
                                        }
                                    });

                                    alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    });

                                    alertDialog.show();
                                }
                            });

                            declineRequest.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                                    alertDialog.setTitle("Decline Request");
                                    alertDialog.setMessage("Are you sure you want to decline the Request?");

                                    alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,int which) {

                                            Map declineMap = new HashMap();

                                            declineMap.put("Friend_Request/" + "/" + currentUserId + "/" + uid, null);
                                            declineMap.put("Friend_Request/" + "/" + uid + "/" + currentUserId, null);

                                            mRootRef.updateChildren(declineMap, new DatabaseReference.CompletionListener() {
                                                @Override
                                                public void onComplete(DatabaseError databaseError,
                                                                       DatabaseReference databaseReference) {

                                                    if(databaseError == null) {

                                                        Toast.makeText(getContext(), "Request declined succesfully!",
                                                                Toast.LENGTH_LONG).show();
                                                    }

                                                    else {
                                                        String error = databaseError.getMessage();
                                                        Log.d("REQFRAGMENT_DECLINE_REQ", error);
                                                    }
                                                }
                                            });
                                        }
                                    });

                                    alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    });

                                    alertDialog.show();
                                }
                            });



                            return new RequestFragment.RequestViewHolder(view);
                        }

                        @Override
                        public void onDataChanged() {
                            super.onDataChanged();

                            if(adapter.getItemCount() == 0){

                                noRequests.setVisibility(View.VISIBLE);
                            }
                            else
                            {
                                noRequests.setVisibility(View.GONE);
                            }
                        }
                    };

        requestList.setAdapter(adapter);
        adapter.startListening();
    }

    private class RequestViewHolder extends RecyclerView.ViewHolder {
        View view;

        RequestViewHolder(View itemView) {
            super(itemView);

            view = itemView;
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
                    Picasso.get().load(thumb_image)
                            .placeholder(R.drawable.boy).error(R.drawable.boy).into(circleImageView);
                }
            });
        }
    }
}
