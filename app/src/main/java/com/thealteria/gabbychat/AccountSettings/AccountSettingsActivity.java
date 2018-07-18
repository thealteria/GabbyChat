package com.thealteria.gabbychat.AccountSettings;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.thealteria.gabbychat.R;
import com.theartofdev.edmodo.cropper.CropImage;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountSettingsActivity extends AppCompatActivity {

    private DatabaseReference reference;
    private FirebaseUser currentUser;

    private CircleImageView mImage;
    private TextView mName, mStatus;
    private Button changeStatus;

    private ProgressDialog progressDialog;

    private static final int GALLERY_PIC = 1;

    private StorageReference imageStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        mImage = findViewById(R.id.profilepic);
        mName = findViewById(R.id.displayname);
        mStatus = findViewById(R.id.statustext);
        changeStatus = findViewById(R.id.statusbtn);

        imageStorage = FirebaseStorage.getInstance().getReference();



        setCurrentUser();


        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

               //get the image into ImageView
                Picasso.get().load(image).placeholder(R.drawable.boy).error(R.drawable.boy).into(mImage);

                mName.setText(name);
                mStatus.setText(status);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void changeStatus(View view) {

        String getStatus = mStatus.getText().toString();

        new MaterialDialog.Builder(this)
                .title("Lol")
                .content("Content bc")
                .inputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
                .positiveText("Change")
                .negativeText("Cancel")
                .inputRangeRes(2, 20, R.color.red)
                .iconRes(R.drawable.ic_statuschange)
                .input("Status..", getStatus, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {

                        String status = input.toString();

                        reference.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
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
                }).show();

    }


    public void setCurrentUser() {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        String uid = currentUser.getUid();

        reference = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
    }

    public void changeImage(View view) {

        //crop the image

        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(Intent.createChooser(galleryIntent, "Select an image"), GALLERY_PIC);

//        CropImage.activity()
//                .setGuidelines(CropImageView.Guidelines.ON)
//                .start(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        String uid = currentUser.getUid();

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PIC && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                progressDialog = new ProgressDialog(AccountSettingsActivity.this);
                progressDialog.setTitle("Profile Pic");
                progressDialog.setMessage("Please wait while we uploading the image!");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                Uri resultUri = result.getUri();

                StorageReference filepath = imageStorage.child("profile_images").child(uid + ".jpg");
                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()) {

                            String download_url = task.getResult().getDownloadUrl().toString();
                            reference.child("image").setValue(download_url).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful()) {
                                        Toast.makeText(getApplicationContext(), "Profile Pic changed", Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();
                                    }
                                }
                            });
                        }

                        else {
                            Toast.makeText(getApplicationContext(), "Error while changing image", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
