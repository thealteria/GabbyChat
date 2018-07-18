package com.thealteria.gabbychat;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountSettingsActivity extends AppCompatActivity {

    private DatabaseReference reference;
    private FirebaseUser currentUser;

    private CircleImageView mImage;
    private TextView mName, mStatus;
    private Button changeStatus;
    private String task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        mImage = findViewById(R.id.profilepic);
        mName = findViewById(R.id.displayname);
        mStatus = findViewById(R.id.statustext);
        changeStatus = findViewById(R.id.statusbtn);

        setCurrentUser();

        changeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(AccountSettingsActivity.this);

                final EditText taskEditText = new EditText(AccountSettingsActivity.this);

                dialog.setTitle("Status");
                dialog.setMessage("Change your status..");
                dialog.setView(taskEditText);
                dialog.setPositiveButton("Change", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        task = taskEditText.getText().toString();

                        reference.child("status").setValue(task).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), "Status changed!", Toast.LENGTH_LONG).show();
                                }

                                else {
                                    Toast.makeText(getApplicationContext(), "Error occurs while saving the status!", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                        }
                });
                dialog.setNegativeButton("Cancel", null);
                AlertDialog b = dialog.create();
                b.show();
            }
        });


        setCurrentUser();
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                mName.setText(name);
                mStatus.setText(status);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void setCurrentUser() {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        String uid = currentUser.getUid();

        reference = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
    }
}
