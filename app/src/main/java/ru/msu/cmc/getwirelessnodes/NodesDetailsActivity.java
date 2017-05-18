package ru.msu.cmc.getwirelessnodes;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

/**
 * Created by Данила on 21.02.2017.
 */

public class NodesDetailsActivity extends SingleFragmentActivity {

    public final static String NODE_TO_DISPLAY = "ru.msu.cmc.getwirelessnodes.NodesDetailsActivity.Node_to_display";

    @Override
    public Fragment createFragment() {
        Fragment fragment =null;
        Intent intent = getIntent();
        WirelessNode node = (WirelessNode)intent.getSerializableExtra(NODE_TO_DISPLAY);

        if(getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_PORTRAIT)
            fragment = NodesDetailsFragment.newInstance(node);
        return fragment;
    }
}
