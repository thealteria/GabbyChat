package com.thealteria.gabbychat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.thealteria.gabbychat.Account.AccountSettingsActivity;
import com.thealteria.gabbychat.Utils.SectionsPageAdapter;
import com.thealteria.gabbychat.Welcome.StartActivity;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Toolbar mtoolbar;
    private DatabaseReference userRef, currentUserId;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mtoolbar = findViewById(R.id.appbar);
        setSupportActionBar(mtoolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Gabby Chat");

        SectionsPageAdapter adapter = new SectionsPageAdapter(getSupportFragmentManager());

        ViewPager viewPager = findViewById(R.id.container);
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.mainTabs);
        tabLayout.setupWithViewPager(viewPager);

        Objects.requireNonNull(tabLayout.getTabAt(0)).setText("Requests");
        Objects.requireNonNull(tabLayout.getTabAt(1)).setText("Chats");
        Objects.requireNonNull(tabLayout.getTabAt(2)).setText("Friends");


        currentUserId = FirebaseDatabase.getInstance().getReference().child("Users");

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            gotoStart();
        }

        else {
            currentUserId.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild(mAuth.getCurrentUser().getUid()))
                    {
                        userRef.child("online").setValue("true");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d("MainActivity", databaseError.getMessage());
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(currentUser != null) {
            userRef.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(currentUser != null) {
            userRef.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }

    private void gotoStart() {
        Intent intent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);

        MenuItem item = menu.findItem(R.id.call);
        item.setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.logout) {

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
            alertDialog.setTitle("Log Out");
            alertDialog.setMessage("Are you sure you want to log out");
            alertDialog.setIcon(R.drawable.ic_out);

            alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int which) {

                    FirebaseAuth.getInstance().signOut();
                    gotoStart();

                    Toast.makeText(getApplicationContext(), "Succesfully logged out!", Toast.LENGTH_SHORT).show();
                }
            });

            alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });


            alertDialog.show();

        }

        if (item.getItemId() == R.id.accsettings) {
            Intent intent = new Intent(MainActivity.this, AccountSettingsActivity.class);
            startActivity(intent);
        }

        if (item.getItemId() == R.id.all_users) {
            Intent intent = new Intent(MainActivity.this, UsersActivity.class);
            startActivity(intent);
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
