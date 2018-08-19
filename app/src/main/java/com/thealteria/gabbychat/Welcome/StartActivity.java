package com.thealteria.gabbychat.Welcome;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dd.processbutton.iml.ActionProcessButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.thealteria.gabbychat.MainActivity;
import com.thealteria.gabbychat.R;

import java.util.HashMap;
import java.util.Objects;

public class StartActivity extends AppCompatActivity
{
    private final String TAG = "CA/WelcomeActivity";
    private FirebaseAuth mAuth;
    private DatabaseReference userDatabase, database;
    private ActionProcessButton loginButton;
    private TextInputEditText loginEmail, loginPassword, registerName, registerEmail, registerPassword, registerConfrmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        loginEmail = findViewById(R.id.loginEmail);
        loginPassword = findViewById(R.id.loginPassword);

        registerName = findViewById(R.id.registerName);
        registerEmail = findViewById(R.id.registerEmail);
        registerPassword = findViewById(R.id.registerPassword);
        registerConfrmPassword = findViewById(R.id.registerConfirmPass);

        mAuth = FirebaseAuth.getInstance();
        userDatabase = FirebaseDatabase.getInstance().getReference().child("Users");


        //------------------------------------------login user----------------------------------------------
        loginButton = findViewById(R.id.loginButton);
        loginButton.setProgress(0);
        loginButton.setMode(ActionProcessButton.Mode.ENDLESS);

        loginButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                loginButton.setClickable(false);

                loginEmail.clearFocus();
                loginPassword.clearFocus();

                String lEmail = loginEmail.getText().toString();
                String lPass = loginPassword.getText().toString();

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(loginPassword.getWindowToken(), 0);
                }

                if(lEmail.length() == 0 || lPass.length() == 0)
                {
                    Toast.makeText(getApplicationContext(), "Please enter your details..", Toast.LENGTH_SHORT).show();

                    loginButton.setProgress(-1);

                    setButton(loginButton);
                    loginButton.setClickable(true);
                }
                else
                {
                    loginButton.setProgress(1);

                    mAuth.signInWithEmailAndPassword(lEmail, lPass)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if(task.isSuccessful())
                            {
                                String deviceToken = FirebaseInstanceId.getInstance().getToken();
                                String currentUser = Objects.requireNonNull(FirebaseAuth.getInstance()
                                        .getCurrentUser()).getUid();

                                userDatabase.child(currentUser).child("token").setValue(deviceToken)
                                        .addOnCompleteListener(new OnCompleteListener<Void>()
                                {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task)
                                    {
                                        if(task.isSuccessful())
                                        {
                                            loginButton.setProgress(100);
                                            verifyEmail();
                                        }
                                        else
                                        {
                                            Log.d(TAG, "uploadToken failed " + Objects.requireNonNull(task
                                                    .getException()).getMessage());
                                        }
                                    }
                                });
                            }
                            else
                            {
                                Toast.makeText(StartActivity.this,
                                        "Cannot Log in. Check your Email or password..",
                                        Toast.LENGTH_SHORT).show();

                                Log.d(TAG, "signIn failed: " + Objects.requireNonNull(task.getException())
                                        .getMessage());

                                loginButton.setProgress(-1);
                                setButton(loginButton);
                                loginButton.setClickable(true);
                            }
                        }
                    });
                }
            }
        });


        loginPassword.setOnKeyListener(new View.OnKeyListener()
        {
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER))
                {
                    loginButton.performClick();
                    return true;
                }
                return false;
            }
        });

        //--------------------------- Registering User-----------------------------------------------------

        final ActionProcessButton registerButton = findViewById(R.id.registerButton);
        registerButton.setProgress(0);
        registerButton.setMode(ActionProcessButton.Mode.ENDLESS);
        registerButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                registerButton.setClickable(false);

                loginButton.setClickable(false);

                registerName.clearFocus();
                registerEmail.clearFocus();
                registerPassword.clearFocus();

                final String regEmail = registerEmail.getText().toString();
                String regPass = registerPassword.getText().toString();
                String regCnfrmPass = registerConfrmPassword.getText().toString();
                final String regName = registerName.getText().toString();

                // Hiding soft keyboard

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(registerButton.getWindowToken(), 0);
                }

                if(regName.length() == 0 || regEmail.length() == 0 || regPass.length() == 0 || regCnfrmPass.length() == 0)
                {
                    Toast.makeText(getApplicationContext(), "Please enter your details..", Toast.LENGTH_SHORT).show();

                    registerButton.setProgress(-1);

                    setButton(registerButton);
                    registerButton.setClickable(true);
                }

                else if (!regPass.equals(regCnfrmPass)) {
                    Toast.makeText(getApplicationContext(),
                            "Passwords do not match. Please enter correct password!", Toast.LENGTH_SHORT).show();

                    registerButton.setProgress(-1);
                    setButton(registerButton);
                    registerButton.setClickable(true);

                }

                else
                {
                    registerButton.setProgress(1);

                    mAuth.createUserWithEmailAndPassword(regEmail, regPass).addOnCompleteListener(new OnCompleteListener<AuthResult>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if(task.isSuccessful())
                            {
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                                if(user != null)
                                {
                                    String uid = user.getUid();

                                    database = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
                                    String deviceToken = FirebaseInstanceId.getInstance().getToken();

                                    HashMap<String, String> userMap = new HashMap<>();
                                    userMap.put("name", regName);
                                    userMap.put("email", regEmail);
                                    userMap.put("status", "Using Gabby chat!");
                                    userMap.put("image", "default");
                                    userMap.put("thumb_image", "default");
                                    userMap.put("device_token", deviceToken);


                                    database.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                registerButton.setProgress(100);

                                                senVerificationEmail();
                                                FirebaseAuth.getInstance().signOut();

                                                loginButton.setClickable(true);
                                            }
                                            else
                                            {
                                                Log.d(TAG, "registerData failed: " + Objects.requireNonNull(task
                                                        .getException()).getMessage());
                                            }
                                        }
                                    });
                                }
                            }
                            else
                            {
                                Toast.makeText(getApplicationContext(),
                                        "Cannot Sign in. Use another email and try again..", Toast.LENGTH_LONG).show();

                                Log.d(TAG, "createUser failed: " + Objects.requireNonNull(task.getException())
                                        .getMessage());

                                registerButton.setProgress(-1);
                                setButton(registerButton);
                                registerButton.setClickable(true);

                                loginButton.setClickable(true);
                            }
                        }
                    });
                }
            }
        });

        registerPassword.setOnKeyListener(new View.OnKeyListener()
        {
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER))
                {
                    registerButton.performClick();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onBackPressed()
    {
        SlidingUpPanelLayout slidingUpPanelLayout = findViewById(R.id.welcome_sliding);

        if(slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED)
        {
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }
        else
        {
            super.onBackPressed();
        }
    }

    private void verifyEmail() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            if (user.isEmailVerified()) {
                Intent intent = new Intent(StartActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            else {

                Toast.makeText(getApplicationContext(),
                        "You can't login without verifying your email. Please Check your email for verification",
                        Toast.LENGTH_SHORT).show();

                FirebaseAuth.getInstance().signOut();
                loginButton.setProgress(-1);
                setButton(loginButton);
                loginButton.setClickable(true);
            }
        }
    }

    public void lForgetPass(View view) {

        new MaterialDialog.Builder(StartActivity.this)
                .title("Reset Password")
                .inputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
                .positiveText("Change")
                .negativeText("Cancel")
                .inputRangeRes(5, 30, R.color.red)
                .input("Enter your Email..", null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {

                        String email = input.toString();

                        mAuth.sendPasswordResetEmail(email)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful())
                                            Toast.makeText(getApplicationContext(),
                                                    "Check email to reset your password!", Toast.LENGTH_SHORT).show();
                                        else
                                            Toast.makeText(getApplicationContext(),
                                                    "Fail to send reset password email! Try another email.",
                                                    Toast.LENGTH_SHORT).show();
                                    }
                                });


                    }
                }).show();
    }

    private void senVerificationEmail() {

        if (mAuth.getCurrentUser() != null) {
            mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(this, new OnCompleteListener<Void>() {
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

    private void setButton(final ActionProcessButton button) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                button.setProgress(0);
            }
        }, 2000);
    }
}
