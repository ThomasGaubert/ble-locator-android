package com.tgaubert.blefinder;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

public class BLEFinderApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        LeakCanary.install(this);
    }
}
