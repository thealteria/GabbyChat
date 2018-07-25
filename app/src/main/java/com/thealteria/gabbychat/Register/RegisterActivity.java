package com.thealteria.gabbychat.Register;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.thealteria.gabbychat.Login.LoginActivity;
import com.thealteria.gabbychat.R;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private EditText name, email, pass, confrmpass;
    private Button signinbtn;
    private Toolbar mtoolbar;
    private CheckBox rshow;
    
    private FirebaseAuth mAuth;
    private DatabaseReference database;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        name = findViewById(R.id.regname);
        email = findViewById(R.id.regemail);
        pass = findViewById(R.id.regpassword);
        confrmpass = findViewById(R.id.regcnfrmpassword);
        signinbtn = findViewById(R.id.regButton);
        rshow = findViewById(R.id.rshowPass);

        mtoolbar = findViewById(R.id.appbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        showPass();
    }

    public void showPass(){
        rshow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    pass.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    confrmpass.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else {
                    pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    confrmpass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
            }
        });
    }

    public void signinBtn(View v) {

        String regname = name.getText().toString();
        String regemail = email.getText().toString();
        String regpass = pass.getText().toString();
        String regcnfrmpass = confrmpass.getText().toString();

        progressDialog.setTitle("Registering User");
        progressDialog.setMessage("Please wait while we create your account!");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        registerUser(regname, regemail, regpass, regcnfrmpass);
    }



    private void registerUser(final String regname, final String regemail, String regpass, String regcnfrmpass) {

        if (regemail.equals("") || regpass.equals("") || regname.equals("") || regcnfrmpass.equals("")) {
            Toast.makeText(getApplicationContext(), "Please Enter your Details", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }

        else if ((regpass.length() < 8) || (regcnfrmpass.length() < 8) ) {
            Toast.makeText(getApplicationContext(), "Please Enter an 8 digits password", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }

        else if (!regpass.equals(regcnfrmpass)) {
            Toast.makeText(getApplicationContext(), "Passwords do not match. Please enter correct password!", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }


            else {
                mAuth.createUserWithEmailAndPassword(regemail, regpass)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()) {

                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                    if (user != null) {
                                        String uid = user.getUid();

                                        database = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                                        String deviceToken = FirebaseInstanceId.getInstance().getToken();

                                        HashMap<String, String> userMap = new HashMap<>();
                                        userMap.put("name", regname);
                                        userMap.put("email", regemail);
                                        userMap.put("status", "Using Gabby chat!");
                                        userMap.put("image", "default");
                                        userMap.put("thumb_image", "default");
                                        userMap.put("device_token", deviceToken);

                                        database.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if(task.isSuccessful()) {

                                                    Log.d(TAG, "createUserWithEmail:success");
                                                    senVerificationEmail();

                                                    FirebaseAuth.getInstance().signOut();
                                                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(intent);
                                                    finish();
                                                    Toast.makeText(getApplicationContext(), "Verification email sent!", Toast.LENGTH_LONG).show();
                                                    progressDialog.dismiss();
                                                }
                                            }
                                        });
                                    }




                                } else {

                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());

                                    progressDialog.hide();
                                    Toast.makeText(RegisterActivity.this, "Cannot Sign in. Use another email and try again..",
                                            Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
            }
    }

    private void senVerificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            user.sendEmailVerification().addOnCompleteListener(this, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                        Toast.makeText(getApplicationContext(), "Verification email sent!", Toast.LENGTH_SHORT).show();

                    else
                        Toast.makeText(getApplicationContext(), "Failed to sent email!", Toast.LENGTH_SHORT).show();
                }
            });


            }
    }
}
