package com.thealteria.gabbychat.Utils;

import android.app.Application;
import android.content.Intent;

import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

public class GabbyChat extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //----------- FIREBASE OFFLINE---------

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);           //only loads Strings quickly

        //load image quickly
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttp3Downloader(this, Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(true);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);
    }
}
