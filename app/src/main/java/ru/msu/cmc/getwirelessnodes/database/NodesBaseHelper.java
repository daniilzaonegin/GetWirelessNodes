package ru.msu.cmc.getwirelessnodes.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.preference.PreferenceManager;

import java.io.File;

import ru.msu.cmc.getwirelessnodes.Settings.SettingsActivity;

/**
 * Created by Данила on 21.03.2017.
 */

public class NodesBaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "nodesBase.db";

    private static NodesBaseHelper sInstance ;

    public static NodesBaseHelper getInstance(Context context)
    {
        if (sInstance == null)
            sInstance = new NodesBaseHelper(context);

        return sInstance;
    }

    private NodesBaseHelper(Context context){
        super(context, PreferenceManager.getDefaultSharedPreferences(context).getString(SettingsActivity.DB_PATH,"/sdcard")
                + File.separator + DATABASE_NAME, null, VERSION);

        //super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL("create table "+NodesDbSchema.NodesTable.NAME + "("+
            "_id integer primary key autoincrement," +
            NodesDbSchema.NodesTable.Cols.TIMESTAMP +" integer not null," +
            NodesDbSchema.NodesTable.Cols.ID +" TEXT not null," +
            NodesDbSchema.NodesTable.Cols.NAME +" TEXT," +
            NodesDbSchema.NodesTable.Cols.TYPE +" TEXT," +
            NodesDbSchema.NodesTable.Cols.FREQUENCY +" integer," +
            NodesDbSchema.NodesTable.Cols.LEVEL +" integer," +
            NodesDbSchema.NodesTable.Cols.CAPABILITIES +" TEXT," +
            NodesDbSchema.NodesTable.Cols.LONGITUDE + " REAL," +
            NodesDbSchema.NodesTable.Cols.LATITUDE + " REAL"  +  ")"
            );
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
