package com.tgaubert.blefinder;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class BLEDataTracker implements BeaconConsumer {

    private BeaconManager beaconManager;
    private ArrayList<Beacon> validBeacons;
    private boolean isTracking = false;
    private Context context;

    private String TAG = "BLEDataTracker";
    private NotificationManager notifyMgr;

    private NotificationCompat.Builder builder;
    private NotificationCompat.InboxStyle inboxStyle;
    private final int NOTIFICATION_ID = 1;
    private int beaconCount = 0;

    public BLEDataTracker(final Context context) {
        this.context = context;
        validBeacons = new ArrayList<>();

        int bitmapWidth = 100;
        int bitmapHeight = 100;
        Bitmap bg = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
        bg.eraseColor(context.getResources().getColor(R.color.primary));

        Intent stopIntent = new Intent(context, StopScanningReceiver.class);
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(context, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notifyMgr = (NotificationManager) context.getSystemService(Service.NOTIFICATION_SERVICE);
        builder = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setColor(context.getResources().getColor(R.color.primary))
                .setGroup("BLE_FINDER_ALERT")
                .setContentTitle("Scanning...")
                .extend(new NotificationCompat.WearableExtender().setBackground(bg))
                .addAction(new NotificationCompat.Action(R.drawable.stop, "Stop Scanning", stopPendingIntent));
        inboxStyle = new NotificationCompat.InboxStyle();
        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        builder.setContentIntent(PendingIntent.getActivity(context, 0, notificationIntent, 0));

        BeaconIO.loadBeacons(context);

        beaconManager = BeaconManager.getInstanceForApplication(context);
        try {
            // Attempt to register iBeacon layout, will throw UnsupportedOperationException if
            // resuming from background.
            beaconManager.getBeaconParsers().add(new BeaconParser().
                    setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        } catch(UnsupportedOperationException e) { e.printStackTrace(); }
        beaconManager.bind(this);
    }

    public ArrayList<Beacon> getValidBeacons() {
        return validBeacons;
    }

    public void setTracking(boolean isTracking) {
        this.isTracking = isTracking;

        try {
            if (isTracking) {
                beaconManager.startRangingBeaconsInRegion(new Region("BLEFinder", null, null, null));
            } else {
                beaconManager.stopRangingBeaconsInRegion(new Region("BLEFinder", null, null, null));

                BeaconIO.saveBeacons(context);
                notifyMgr.cancel(1);
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
        Log.i(TAG, "Connected to service");

        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(final Collection<Beacon> beacons, Region region) {
                HashMap<String, SeenBeacon> seenBeacons = BeaconIO.getSeenBeacons();
                validBeacons.clear();

                for (Beacon b : beacons) {
                    if (seenBeacons.containsKey(b.getBluetoothAddress())) {
                        if (seenBeacons.get(b.getBluetoothAddress()).isIgnore()) {
                            Log.i(TAG, "Beacon " + b.getBluetoothAddress() + " has been seen before, but will be ignored.");
                            continue;
                        }

                        Log.i(TAG, "Beacon " + b.getBluetoothAddress() + " has been seen before.");
                        validBeacons.add(b);

                        SeenBeacon seenBeacon = seenBeacons.get(b.getBluetoothAddress());

                        if(!seenBeacon.isIgnore()) {
                            beaconCount++;
                            /*
                            String savedName = seenBeacon.getUserName();
                            if(savedName.contains("BLEFinder\u2063"))
                                savedName = b.getBluetoothName();
                            */
                        }
                    } else {
                        Log.i(TAG, "Just saw beacon " + b.getBluetoothAddress() + " for the first time.");
                        BeaconIO.getSeenBeacons().put(b.getBluetoothAddress(), new SeenBeacon(b.getBluetoothAddress(), b.getBluetoothName()));
                        beaconCount++;
                    }

                    if(beaconCount == 0) {
                        builder.setContentText("No nearby beacons");
                    } else {
                        builder.setContentText(beaconCount + " beacons nearby");
                    }
                }

                notifyMgr.notify(NOTIFICATION_ID, builder.build());

                beaconCount = 0;
            }
        });

        try {
            if (isTracking) {
                beaconManager.startRangingBeaconsInRegion(new Region("BLEFinder", null, null, null));
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void updateState() {
        isTracking = beaconManager.getRangedRegions().size() != 0;
    }

    public void unbind() {
        beaconManager.unbind(this);
    }

    @Override
    public Context getApplicationContext() {
        return context;
    }

    @Override
    public void unbindService(ServiceConnection serviceConnection) {
        context.unbindService(serviceConnection);
        notifyMgr.cancel(1);
    }

    @Override
    public boolean bindService(Intent intent, ServiceConnection serviceConnection, int i) {
        context.bindService(intent, serviceConnection, i);
        return true;
    }
}
