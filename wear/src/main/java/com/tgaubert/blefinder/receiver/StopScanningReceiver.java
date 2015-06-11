package com.tgaubert.blefinder.receiver;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.service.BeaconService;

public class StopScanningReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ((NotificationManager) context.getSystemService(Service.NOTIFICATION_SERVICE)).cancel(1);
        context.stopService(new Intent(context, BeaconService.class));
        BeaconManager beaconManager = BeaconManager.getInstanceForApplication(context.getApplicationContext());
        if (beaconManager.getRangedRegions().size() > 0) {
            try {
                beaconManager.stopRangingBeaconsInRegion(new Region("BLEFinder", null, null, null));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}
