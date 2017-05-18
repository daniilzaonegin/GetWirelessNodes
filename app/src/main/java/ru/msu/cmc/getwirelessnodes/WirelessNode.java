package ru.msu.cmc.getwirelessnodes;

import android.content.ContentValues;
import android.net.wifi.ScanResult;

import java.io.Serializable;

import ru.msu.cmc.getwirelessnodes.database.NodesDbSchema;

/**
 * Created by Данила on 14.03.2017.
 */

public class WirelessNode implements Serializable {
    //   public static final String BSSID="BSSID";
    //   public static final String SSID="SSID";
    //   public static final String LEVEL="LEVEL";
    //   public static final String FREQUENCY="FREQUENCY";
    //   public static final String CAPABILITIES="CAPABILITIES";

    public String getName() {
        return name;
    }

    public String getID() {
        return ID;
    }

    public int getLevel() {
        return level;
    }

    public int getFrequency() {
        return frequency;
    }

    public String getCapabilities() {
        return capabilities;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getType() {
        return type;
    }



    public WirelessNode(String name, String id, String type, int level, int frequency, String capabilities, double longitude, double latitude)
    {
        ID=id;
        this.type=type;
        this.name=name;
        this.level = level;
        this.frequency = frequency;
        this.capabilities = capabilities;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public WirelessNode (ScanResult scanResult, double longitude, double latitude)
    {
        ID =scanResult.BSSID;
        type="wi-fi network";
        name =scanResult.SSID;
        level = scanResult.level;
        frequency = scanResult.frequency;
        capabilities =scanResult.capabilities;
        this.longitude=longitude;
        this.latitude=latitude;
    }

    public ContentValues getContentValues (long timestamp)
    {
        ContentValues values = new ContentValues();
        values.put(NodesDbSchema.NodesTable.Cols.TIMESTAMP, timestamp);
        values.put(NodesDbSchema.NodesTable.Cols.ID, ID);
        values.put(NodesDbSchema.NodesTable.Cols.NAME, name);
        values.put(NodesDbSchema.NodesTable.Cols.TYPE, getType());
        values.put(NodesDbSchema.NodesTable.Cols.FREQUENCY, frequency);
        values.put(NodesDbSchema.NodesTable.Cols.LEVEL, level);
        values.put(NodesDbSchema.NodesTable.Cols.CAPABILITIES, capabilities);
        values.put(NodesDbSchema.NodesTable.Cols.LONGITUDE, longitude);
        values.put(NodesDbSchema.NodesTable.Cols.LATITUDE, latitude);
        return values;
    }

    private String type;
    private String name;
    private String ID;
    private int level;
    private int frequency;
    private String capabilities;
    private double longitude=-1;
    private double latitude=-1;

}
