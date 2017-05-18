package ru.msu.cmc.getwirelessnodes.Settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.msu.cmc.getwirelessnodes.R;

/**
 * Created by Данила on 04.04.2017.
 */

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //загрузить меню из xml ресурса
        addPreferencesFromResource(R.xml.preferences);
        setSummaries();

    }

    private void setSummaries() {
        Preference dbPathPrf = findPreference(SettingsActivity.DB_PATH);
        Preference refreshPrf = findPreference(SettingsActivity.REFRESH_TIME);
        ListPreference nodesTypePrf = (ListPreference)findPreference(SettingsActivity.NODES_TYPE);

        final SharedPreferences sh = getPreferenceManager().getSharedPreferences() ;

        dbPathPrf.setSummary(sh.getString(SettingsActivity.DB_PATH, ""));

        refreshPrf.setSummary(sh.getString(SettingsActivity.REFRESH_TIME, ""));
        nodesTypePrf.setSummary(nodesTypePrf.getEntry());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onPause() {
        super.onPause();

        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        setSummaries();
    }
}
