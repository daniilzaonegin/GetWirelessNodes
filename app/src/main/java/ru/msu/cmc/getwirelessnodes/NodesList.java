package ru.msu.cmc.getwirelessnodes;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.ScanResult;
import android.util.Log;
import android.widget.ArrayAdapter;

import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import ru.msu.cmc.getwirelessnodes.database.NodesBaseHelper;
import ru.msu.cmc.getwirelessnodes.database.NodesDbSchema;

/**
 * Created by Данила on 20.03.2017.
 */

public class NodesList {
    private static final String TAG ="NodesList";
    private static NodesList sNodesList;
    private static CopyOnWriteArrayList<WirelessNode> sList;
    private static NodesBaseHelper sNodesDbHelper;
    private static SQLiteDatabase sNodesDb;

    public static NodesList getInstance(){
        if(sNodesList==null){
            sNodesList= new NodesList();
        }
        return sNodesList;
    }

    private NodesList()
    {
        sList=new CopyOnWriteArrayList<>();
    }

    public List<WirelessNode> getNodes(){
        return sList;
    }

    public WirelessNode getNode(String id)
    {
        for(WirelessNode node : sList)
        {
            if(node.getID() == id)
            {
                return node;
            }
        }
        return null;
    }

    public WirelessNode getNodeByIndex(int index)
    {
        return sList.get(index);
    }

    public void push(WirelessNode node, ArrayAdapter<WirelessNode> adapter)
    {
        Log.d(TAG, "insertWifiScanResults");
        sList.add(node);
        adapter.notifyDataSetChanged();
    }


    public void insertWifiScanResults(List<ScanResult> scList, double longitude, double latitude, ArrayAdapter<WirelessNode> adapter)
    {
        Log.d(TAG, "insertWifiScanResults");
        clearWifiInfo();
        for (ScanResult sc : scList){
            sList.add(new WirelessNode(sc,longitude, latitude));
            adapter.notifyDataSetChanged();
        }
    }

    public void postAllDataToDb(Context context)
    {
        Log.i(TAG, "writing data to nodes db");
        if(sNodesDbHelper ==null)
            sNodesDbHelper =NodesBaseHelper.getInstance(context);
        if (sNodesDb == null)
            sNodesDb = sNodesDbHelper.getWritableDatabase();
        try {

            for (WirelessNode node : sList) {
                ContentValues values = node.getContentValues((new Date()).getTime());

                if (sNodesDb != null)
                    sNodesDb.insert(NodesDbSchema.NodesTable.NAME, null, values);

            }
        } catch (Exception e) {
            Log.i(TAG, "exception occured during inserting data to db!");
            Log.i(TAG, e.getStackTrace().toString());
        }
    }

    public void postBtDataToDb(Context context)
    {
        Log.i(TAG, "writing bluetooth nodes data to nodes db");
        if(sNodesDbHelper ==null)
            sNodesDbHelper =NodesBaseHelper.getInstance(context);
        if (sNodesDb == null)
            sNodesDb = sNodesDbHelper.getWritableDatabase();
        try {
            //нужно создать новый массив, снимок текущего, иначе можем выгрузить лишнее в базу, так как sList все время пополняется/изменяется
            ArrayList<WirelessNode> btList = new ArrayList<>(sList);
            for (WirelessNode node : btList) {
                if(node.getType().substring(0,9).equals("bluetooth")) {
                    ContentValues values = node.getContentValues((new Date()).getTime());

                    if (sNodesDb != null)
                        sNodesDb.insert(NodesDbSchema.NodesTable.NAME, null, values);
                }

            }
        } catch (Exception e) {
            Log.i(TAG, "exception occured during inserting data to db!");
            Log.i(TAG, e.getStackTrace().toString());
        }
    }
    public void postWifiDataToDb(Context context)
    {
        Log.i(TAG, "writing wi-fi nodes data to nodes db");
        if(sNodesDbHelper ==null)
            sNodesDbHelper =NodesBaseHelper.getInstance(context);
        if (sNodesDb == null)
            sNodesDb = sNodesDbHelper.getWritableDatabase();
        try {

            //нужно создать новый массив, снимок текущего, иначе можем выгрузить лишнее в базу, так как sList все время пополняется/изменяется
            ArrayList<WirelessNode> wifiList = new ArrayList<>(sList);
            for (WirelessNode node : wifiList) {
                if(node.getType().substring(0,5).equals("wi-fi")) {
                    ContentValues values = node.getContentValues((new Date()).getTime());

                    if (sNodesDb != null)
                        sNodesDb.insert(NodesDbSchema.NodesTable.NAME, null, values);
                }

            }
        } catch (Exception e) {
            Log.i(TAG, "exception occured during inserting data to db!");
            Log.i(TAG, e.getStackTrace().toString());
        }
    }
    public void clear()
    {
        sList.clear();
    }
    public void clearBtInfo()
    {
        for(WirelessNode node : sList)
        {
            if(node.getType().substring(0,9).equals("bluetooth"))
            {
                sList.remove(node);
            }
        }
    }

    public void clearWifiInfo()
    {
        for(WirelessNode node : sList)
        {
            if(node.getType().substring(0,5).equals("wi-fi"))
            {
                sList.remove(node);
            }
        }

    }

}
