package com.tgaubert.blefinder.util;

import android.content.Context;

import com.tgaubert.blefinder.model.SeenBeacon;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class BeaconIO {

    private static HashMap<String, SeenBeacon> seenBeacons = new HashMap<>();

    public static void loadBeacons(final Context context) {
        new Thread(new Runnable() {
            public void run() {
                String path = context.getFilesDir().getAbsolutePath();
                File file = new File(path + "/beacons.json");

                FileInputStream stream = null;
                JSONObject beacons;

                if (file.exists()) {
                    try {
                        stream = new FileInputStream(file);
                        String data;
                        FileChannel fc = stream.getChannel();
                        MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
                        data = Charset.defaultCharset().decode(bb).toString();
                        beacons = new JSONObject(data);

                        Iterator<?> keys = beacons.keys();
                        while (keys.hasNext()) {
                            String key = (String) keys.next();
                            if (beacons.get(key) instanceof JSONObject) {
                                seenBeacons.put(key, new SeenBeacon(key, (JSONObject) beacons.get(key)));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            assert stream != null;
                            stream.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    try {
                        //noinspection ResultOfMethodCallIgnored
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public static void saveBeacons(final Context context) {
        new Thread(new Runnable() {
            public void run() {
                String path = context.getFilesDir().getAbsolutePath();
                File file = new File(path + "/beacons.json");

                JSONObject beacons = new JSONObject();

                try {
                    for (Map.Entry<String, SeenBeacon> entry : seenBeacons.entrySet()) {
                        beacons.put(entry.getKey(), entry.getValue().getJsonObject());
                    }

                    try {
                        //noinspection ResultOfMethodCallIgnored
                        file.createNewFile();
                        FileWriter fileWriter = new FileWriter(file);
                        fileWriter.write(beacons.toString());
                        fileWriter.flush();
                        fileWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static HashMap<String, SeenBeacon> getSeenBeacons() {
        return seenBeacons;
    }

    public static SeenBeacon putSeenBeacon(String key, SeenBeacon value) {
        return seenBeacons.put(key, value);
    }
}
