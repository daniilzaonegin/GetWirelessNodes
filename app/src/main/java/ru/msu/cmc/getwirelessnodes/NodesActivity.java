package ru.msu.cmc.getwirelessnodes;

import android.support.v4.app.Fragment;

/**
 * Created by Данила on 22.02.2017.
 */

public class NodesActivity extends SingleFragmentActivity {
    @Override
    public Fragment createFragment() {
        return new NodesFragment();
    }
}
