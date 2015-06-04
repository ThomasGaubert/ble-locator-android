package com.tgaubert.blefinder;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.RemoteException;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;

public class BLEDataTracker implements BeaconConsumer {

    private BeaconManager beaconManager;
    private Collection<Beacon> beacons;
    private boolean isTracking = false;
    private Context context;

    private String TAG = "BLEDataTracker";

    public BLEDataTracker(Context context) {
        this.context = context;

        beaconManager = BeaconManager.getInstanceForApplication(context);
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);
    }

    public void setBeacons(Collection<Beacon> beacons) {
        this.beacons = beacons;
    }

    public Collection<Beacon> getBeacons() {
        return beacons;
    }

    public void setTracking(boolean isTracking) {
        this.isTracking = isTracking;

        try {
            if(isTracking) {
                beaconManager.startMonitoringBeaconsInRegion(new Region("BLEFinder", null, null, null));
                beaconManager.startRangingBeaconsInRegion(new Region("BLEFinder", null, null, null));
            } else {
                beaconManager.stopMonitoringBeaconsInRegion(new Region("BLEFinder", null, null, null));
                beaconManager.stopRangingBeaconsInRegion(new Region("BLEFinder", null, null, null));
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public boolean isTracking() {
        return isTracking;
    }

    @Override
    public void onBeaconServiceConnect() {
        Log.i(TAG, "Connected to service!");
        beaconManager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                Log.i(TAG, "I just saw an beacon for the first time!");
            }

            @Override
            public void didExitRegion(Region region) {
                Log.i(TAG, "I no longer see an beacon");
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                Log.i(TAG, "I have just switched from seeing/not seeing beacons: " + state);
            }
        });

        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    Beacon b = beacons.iterator().next();
                    Log.i(TAG, b.getBluetoothName());
                    Log.i(TAG, "The first beacon I see is about " + b.getDistance() + " meters away.");
                }
            }
        });

        try {
            if(isTracking) {
                beaconManager.startMonitoringBeaconsInRegion(new Region("myMonitoringUniqueId", null, null, null));
                beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Context getApplicationContext() {
        return context;
    }

    @Override
    public void unbindService(ServiceConnection serviceConnection) {
        context.unbindService(serviceConnection);
    }

    @Override
    public boolean bindService(Intent intent, ServiceConnection serviceConnection, int i) {
        context.bindService(intent, serviceConnection, i);
        return true;
    }
}
