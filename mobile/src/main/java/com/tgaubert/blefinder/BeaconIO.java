package com.tgaubert.blefinder;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;

public class BeaconIO {

    public static ArrayList<SeenBeacon> loadBeacons(Context context) {
        String path = context.getFilesDir().getAbsolutePath();
        File file = new File(path + "/beacons.json");

        FileInputStream stream = null;
        ArrayList<SeenBeacon> beacons = new ArrayList<>();
        JSONObject seenBeacons;

        if (file.exists()) {
            try {
                stream = new FileInputStream(file);
                String data = null;
                FileChannel fc = stream.getChannel();
                MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
                data = Charset.defaultCharset().decode(bb).toString();
                seenBeacons = new JSONObject(data);

                Iterator<?> keys = seenBeacons.keys();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    if (seenBeacons.get(key) instanceof JSONObject) {
                        beacons.add(new SeenBeacon(key, (JSONObject) seenBeacons.get(key)));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    stream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return beacons;
    }

    public static void saveBeacons(ArrayList<SeenBeacon> beacons, Context context) {
        String path = context.getFilesDir().getAbsolutePath();
        File file = new File(path + "/beacons.json");

        JSONObject seenBeacons = new JSONObject();

        try {
            for(SeenBeacon b : beacons) {
                seenBeacons.put(b.getUuid(), b.getJsonObject());
            }

            try {
                file.createNewFile();
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write(seenBeacons.toString());
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
