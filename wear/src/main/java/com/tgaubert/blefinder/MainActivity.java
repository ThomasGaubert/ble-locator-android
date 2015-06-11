package com.tgaubert.blefinder;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.WatchViewStub;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.tgaubert.blefinder.ui.ItemView;
import com.tgaubert.blefinder.ui.ListItem;
import com.tgaubert.blefinder.util.BLEDataTracker;

import org.altbeacon.beacon.BeaconManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements WearableListView.ClickListener {

    private BLEDataTracker bleDataTracker;
    private WearableListView mListView;
    private ListAdapter mAdapter;

    private static ArrayList<ListItem> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bleDataTracker = new BLEDataTracker(getApplicationContext());

        initListView();
    }

    @Override
    public void onResume() {
        super.onResume();
        initListView();
    }

    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        switch (viewHolder.getLayoutPosition()) {
            case 0:
                if(BeaconManager.getInstanceForApplication(getApplicationContext()).getRangedRegions().size() > 0) {
                    bleDataTracker.setTracking(false);
                    Toast.makeText(this, "Stopping scanning...", Toast.LENGTH_SHORT).show();
                } else {
                    bleDataTracker.setTracking(true);
                    Toast.makeText(this, "Scanning...", Toast.LENGTH_SHORT).show();
                }
                finish();
                break;
        }
    }

    @Override
    public void onTopEmptyRegionClick() {

    }

    private class ListAdapter extends WearableListView.Adapter {

        private final Context context;
        private final List<ListItem> items;
        private final LayoutInflater mInflater;

        public ListAdapter(Context context, List<ListItem> items) {
            this.context = context;
            this.items = items;
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public WearableListView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            return new WearableListView.ViewHolder(
                    mInflater.inflate(R.layout.wearablelistview_item, null));
        }

        @Override
        public void onBindViewHolder(WearableListView.ViewHolder viewHolder, final int position) {
            ItemView itemView = (ItemView) viewHolder.itemView;
            final ListItem item = items.get(position);

            TextView textView = (TextView) itemView.findViewById(R.id.text);
            textView.setText(item.title);

            final CircledImageView imageView = (CircledImageView) itemView.findViewById(R.id.image);
            imageView.setImageResource(item.iconRes);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    private void initListView() {
        items = new ArrayList<>();
        if(BeaconManager.getInstanceForApplication(getApplicationContext()).getRangedRegions().size() > 0)
            items.add(new ListItem(R.mipmap.ic_launcher, "Stop Scan"));
        else
            items.add(new ListItem(R.mipmap.ic_launcher, "Start Scan"));

        mAdapter = new ListAdapter(this, items);

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mListView = (WearableListView) stub.findViewById(R.id.main_list_view);
                mListView.setAdapter(mAdapter);
                mListView.setClickListener(MainActivity.this);
            }
        });
    }
}