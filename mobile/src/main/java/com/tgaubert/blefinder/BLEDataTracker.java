package com.tgaubert.blefinder;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
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
            if (isTracking) {
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
                final ArrayList<Beacon> visibleBeacons = new ArrayList<>();
                ((MainActivity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (Beacon b : beacons) {
                            SeenBeacon seenBeacon = BeaconIO.getSeenBeacon(b.getBluetoothAddress());
                            if (seenBeacon != null && !seenBeacon.isIgnore())
                                visibleBeacons.add(b);
                        }

                        listAdapter.set(visibleBeacons);
                    }
                });

                int notificationId = 1;
                int beaconCount = 0;
                NotificationManager notifyMgr = (NotificationManager) context.getSystemService(Service.NOTIFICATION_SERVICE);
                NotificationCompat.Builder builder =
                        new NotificationCompat.Builder(getApplicationContext())
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setColor(context.getResources().getColor(R.color.primary))
                                .setGroup("BLE_FINDER_ALERT")
                                .setContentTitle("Beacon Alert");
                NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

                for (Beacon b : beacons) {
                    if (BeaconIO.getSeenBeacons().containsKey(b.getBluetoothAddress())) {
                        Log.i(TAG, "Beacon " + b.getBluetoothAddress() + " has been seen before.");
                        SeenBeacon seenBeacon = BeaconIO.getSeenBeacon(b.getBluetoothAddress());
                        //int notificationId = Integer.parseInt(b.getBluetoothAddress().replace(":", "").substring(0, 5), 16);

                        if(!seenBeacon.isIgnore() && Double.parseDouble(seenBeacon.getDistance()) >= b.getDistance()) {
                            beaconCount++;
                            String savedName = seenBeacon.getUserName();
                            if(savedName.contains("BLEFinder\u2063"))
                                savedName = b.getBluetoothName();

                            if(beaconCount == 1) {
                                builder.setContentText(savedName + " is within " + seenBeacon.getDistance() + "m.");
                            } else {
                                inboxStyle.setBigContentTitle(beaconCount + " beacons nearby");
                                if(beaconCount < 6) {
                                    if(beaconCount == 2) {
                                        inboxStyle.addLine(builder.mContentText.toString().replace(" is within ", "   ").replace(".", ""));
                                    }
                                    inboxStyle.addLine(savedName + "   " + seenBeacon.getDistance() + "m");
                                } else {
                                    inboxStyle.setSummaryText("+" + (beaconCount - 5) + " more");
                                }
                                builder.setContentText(beaconCount + " beacons nearby");
                                builder.setStyle(inboxStyle);
                            }
                        }
                    } else {
                        Log.i(TAG, "Just saw a beacon " + b.getBluetoothAddress() + " for the first time.");
                        BeaconIO.getSeenBeacons().put(b.getBluetoothAddress(), new SeenBeacon(b.getBluetoothAddress(), b.getBluetoothName()));
                    }
                }

                if(beaconCount == 0) {
                    notifyMgr.cancel(1);
                } else {
                    notifyMgr.notify(notificationId, builder.build());
                }
            }
        });

        try {
            if (isTracking) {
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
