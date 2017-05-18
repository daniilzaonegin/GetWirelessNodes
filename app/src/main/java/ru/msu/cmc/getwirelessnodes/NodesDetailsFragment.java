package ru.msu.cmc.getwirelessnodes;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by Данила on 21.02.2017.
 */

public class NodesDetailsFragment extends Fragment {
    private WirelessNode mDisplayedNode;

    public String getDisplayedNetId() {
        if (mDisplayedNode !=null) {
            return mDisplayedNode.getID();
        }
        else
        {
            return null;
        }
    }

    private static final String NODE_KEY ="ru.msu.cmc.getwirelessnodes.NodesDetailsFragment.node_key";

    public static NodesDetailsFragment newInstance (WirelessNode node)
    {
        NodesDetailsFragment fragment = new NodesDetailsFragment();

        Bundle args = new Bundle();

        args.putSerializable(NODE_KEY, node);
        fragment.setArguments(args);

        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments()!=null) {
            mDisplayedNode = (WirelessNode) getArguments().getSerializable(NODE_KEY);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_details, container, false);

        if(mDisplayedNode!=null) {
            ((TextView) v.findViewById(R.id.BSSID_TextView)).setText(mDisplayedNode.getID());
            ((TextView) v.findViewById(R.id.SSID_TextView)).setText(mDisplayedNode.getName());
            ((TextView) v.findViewById(R.id.type_TextView)).setText(mDisplayedNode.getType());
            ((TextView) v.findViewById(R.id.level_TextView)).setText(Integer.toString(mDisplayedNode.getLevel()));
            ((TextView) v.findViewById(R.id.frequency_TextView)).setText(Integer.toString(mDisplayedNode.getFrequency()));
            ((TextView) v.findViewById(R.id.capabilities_TextView)).setText(mDisplayedNode.getCapabilities());
        }
        else
        {
            ((TextView) v.findViewById(R.id.BSSID_TextView)).setText("");
            ((TextView) v.findViewById(R.id.SSID_TextView)).setText("");
            ((TextView) v.findViewById(R.id.type_TextView)).setText("");
            ((TextView) v.findViewById(R.id.level_TextView)).setText("");
            ((TextView) v.findViewById(R.id.frequency_TextView)).setText("");
            ((TextView) v.findViewById(R.id.capabilities_TextView)).setText("");
        }
        return v;
    }
}
