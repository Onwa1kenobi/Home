package com.voidloop.home.app;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.voidloop.home.R;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by ameh on 14/06/2017.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Firebase app initialization
        FirebaseApp.initializeApp(this);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Lato-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

    }
}
