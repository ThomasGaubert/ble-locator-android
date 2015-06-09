package com.tgaubert.blefinder;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;

public class MainActivityFragment extends Fragment {

    private View rootView;
    private FloatingActionButton floatingActionButton;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_main, container, false);

        floatingActionButton = (FloatingActionButton) rootView.findViewById(R.id.floating_btn);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(((MainActivity) getActivity()).getBleDataTracker().isTracking()) {
                    Snackbar.make(v, "Done searching for beacons.", Snackbar.LENGTH_LONG).show();
                    floatingActionButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.bluetooth, null));
                    ((TextView) rootView.findViewById(R.id.msgSubtitle)).setText("Start scanning to find beacons.");
                    ((MainActivity) getActivity()).getBleDataTracker().setTracking(false);
                } else {
                    Snackbar.make(v, "Searching for beacons...", Snackbar.LENGTH_LONG).show();
                    floatingActionButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.bt_scan, null));
                    ((TextView) rootView.findViewById(R.id.msgSubtitle)).setText("No beacons found nearby.");
                    ((MainActivity) getActivity()).getBleDataTracker().setTracking(true);
                }
            }
        });

        EmptyRecyclerView recyclerView = (EmptyRecyclerView) rootView.findViewById(R.id.beaconList);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        BeaconListAdapter adapter = new BeaconListAdapter(new ArrayList<Beacon>());
        recyclerView.setAdapter(adapter);
        recyclerView.setEmptyView(rootView.findViewById(R.id.msgView));

        return rootView;
    }
}
