package com.tgaubert.blefinder;

import android.app.Application;

import org.altbeacon.beacon.BeaconManager;

public class BLEFinderApplication extends Application {

    private BeaconManager beaconManager;

    public void onCreate() {
        super.onCreate();
        beaconManager = BeaconManager.getInstanceForApplication(this);
    }

    public BeaconManager getBeaconManager() {
        return beaconManager;
    }
}
