package com.thealteria.gabbychat.Welcome;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.thealteria.gabbychat.Login.LoginActivity;
import com.thealteria.gabbychat.R;
import com.thealteria.gabbychat.Register.RegisterActivity;

public class StartActivity extends AppCompatActivity {

    private Button regButton, logButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        regButton = findViewById(R.id.regButton);
        logButton = findViewById(R.id.loginButton);

        regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent regIntent = new Intent(StartActivity.this, RegisterActivity.class);
                startActivity(regIntent);
            }
        });

        logButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent regIntent = new Intent(StartActivity.this, LoginActivity.class);
                startActivity(regIntent);
            }
        });


    }
}
