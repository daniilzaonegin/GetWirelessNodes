package ru.msu.cmc.getwirelessnodes.Settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;


/**
 * Created by Данила on 04.04.2017.
 */

public class SettingsActivity extends AppCompatActivity {

    public static final String SAVE_TO_DB_KEY="save_to_db";
    public static final String DB_PATH="db_path";
    public static final String REFRESH_TIME="refresh_time";
    public static final String NODES_TYPE="nodes_type";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
