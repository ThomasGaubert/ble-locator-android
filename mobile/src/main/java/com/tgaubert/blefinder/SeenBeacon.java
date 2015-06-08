package com.tgaubert.blefinder;


import org.json.JSONException;
import org.json.JSONObject;

public class SeenBeacon {

    private String uuid;
    private String btName, userName, distance;
    private boolean notify, ignore;
    private int color;

    public SeenBeacon(String uuid, String btName) {
        this.uuid = uuid;
        this.btName = btName;

        userName = "BLEFinder\u2063";
        color = 0;
        notify = false;
        distance = "0";
        ignore = false;
    }

    public SeenBeacon(String uuid, JSONObject json) {
        this.uuid = uuid;

        try {
            btName = json.getString("bt_name");
            userName = json.getString("user_name");
            color = json.getInt("color");

            notify = json.getBoolean("notify");
            distance = json.getString("distance");
            ignore = json.getBoolean("ignore");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getJsonObject() {
        JSONObject beacon = null;
        try {
            beacon = new JSONObject();
            beacon.put("bt_name", btName);
            beacon.put("user_name", userName);
            beacon.put("color", color);
            beacon.put("notify", notify);
            beacon.put("distance", distance);
            beacon.put("ignore", ignore);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return beacon;
    }

    public String getUuid() {
        return uuid;
    }

    public String getBtName() {
        return btName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public boolean isNotify() {
        return notify;
    }

    public void setNotify(boolean notify) {
        this.notify = notify;
    }

    public boolean isIgnore() {
        return ignore;
    }

    public void setIgnore(boolean ignore) {
        this.ignore = ignore;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }
}
