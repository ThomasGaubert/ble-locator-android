package com.tgaubert.blefinder;

import android.app.Application;

public class BLEFinderApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // LeakCanary.install(this);
    }
}