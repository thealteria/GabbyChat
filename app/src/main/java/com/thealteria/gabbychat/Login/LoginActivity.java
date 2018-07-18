package com.thealteria.gabbychat.Login;

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
import com.thealteria.gabbychat.MainActivity;
import com.thealteria.gabbychat.R;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private EditText lemail, lpass;
    private Button loginBtn;
    private Toolbar mtoolbar;
    private CheckBox rshow;

    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        lemail = findViewById(R.id.logemail);
        lpass = findViewById(R.id.logpassword);
        loginBtn = findViewById(R.id.loginButton);
        rshow = findViewById(R.id.rshowPass);

        mtoolbar = findViewById(R.id.appbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("Login Account");
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
                    lpass.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else {
                    lpass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
            }
        });
    }

    public void loginBtn(View view) {

        String logemail = lemail.getText().toString();
        String logpass = lpass.getText().toString();

        progressDialog.setTitle("Logging in User");
        progressDialog.setMessage("Please wait while we logging into your account!");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        registerUser(logemail, logpass);
    }

    private void registerUser(String logemail, String logpass) {

        if (logemail.equals("") || logpass.equals("")) {
                Toast.makeText(getApplicationContext(), "Please Enter your Details", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();

        }


        else {

            mAuth.signInWithEmailAndPassword(logemail, logpass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    progressDialog.dismiss();

                    if (task.isSuccessful()) {

                        verifyEmail();

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());

                        progressDialog.hide();
                        Toast.makeText(LoginActivity.this, "Cannot Log in. Check your Email or password..",
                                Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }


    }

    private void verifyEmail() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            if (user.isEmailVerified()) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            else {
                Toast.makeText(getApplicationContext(), "Check your email for verification", Toast.LENGTH_SHORT).show();
                FirebaseAuth.getInstance().signOut();
                finish();
            }
        }
    }
}
