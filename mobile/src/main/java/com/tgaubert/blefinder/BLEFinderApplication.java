package com.tgaubert.blefinder;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

public class BLEFinderApplication extends Application {

    private BeaconListAdapter adapter;

    @Override
    public void onCreate() {
        super.onCreate();
        LeakCanary.install(this);
    }

    public void setAdapter(BeaconListAdapter adapter) {
        this.adapter = adapter;
    }

    public BeaconListAdapter getAdapter() {
        return adapter;
    }
}
