package com.tgaubert.blefinder;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.RemoteException;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class BLEDataTracker implements BeaconConsumer {

    private BeaconManager beaconManager;
    private Collection<Beacon> beacons;
    private BeaconListAdapter listAdapter;
    private boolean isTracking = false;
    private Context context;

    private String TAG = "BLEDataTracker";

    public BLEDataTracker(final Context context) {
        this.context = context;

        BeaconIO.loadBeacons(context);

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

                BeaconIO.saveBeacons(context);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public boolean isTracking() {
        return isTracking;
    }

    public void setListAdapter(BeaconListAdapter listAdapter) {
        this.listAdapter = listAdapter;
    }

    @Override
    public void onBeaconServiceConnect() {
        Log.i(TAG, "Connected to service");
        beaconManager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                Log.i(TAG, "Found a beacon for the first time");
            }

            @Override
            public void didExitRegion(Region region) {
                Log.i(TAG, "No beacons within range");
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                Log.i(TAG, "Switched from seeing/not seeing beacons: " + state);
            }
        });

        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(final Collection<Beacon> beacons, Region region) {
                setBeacons(beacons);
                ((MainActivity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listAdapter.set(new ArrayList<>(beacons));
                    }
                });

                for(Beacon b : beacons) {
                    if(BeaconIO.getSeenBeacons().containsKey(b.getBluetoothAddress())) {
                        Log.i(TAG, "Beacon " + b.getBluetoothAddress() + " has been seen before.");
                    } else {
                        Log.i(TAG, "Just saw a beacon " + b.getBluetoothAddress() + " for the first time.");
                        BeaconIO.getSeenBeacons().put(b.getBluetoothAddress(), new SeenBeacon(b.getBluetoothAddress(), b.getBluetoothName()));
                    }
                }
            }
        });

        try {
            if(isTracking) {
                beaconManager.startMonitoringBeaconsInRegion(new Region("BLEFinder", null, null, null));
                beaconManager.startRangingBeaconsInRegion(new Region("BLEFinder", null, null, null));
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
