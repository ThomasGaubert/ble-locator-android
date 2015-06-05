package com.tgaubert.blefinder;

import android.app.Dialog;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;

public class BeaconListAdapter extends RecyclerView.Adapter<BeaconListAdapter.ViewHolder> {

    private ArrayList<Beacon> beacons;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView rowTitle, rowSubtitle;

        public ViewHolder(View v) {
            super(v);
            rowTitle = (TextView) v.findViewById(R.id.rowTitle);
            rowSubtitle = (TextView) v.findViewById(R.id.rowSubtitle);

            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Beacon selected = beacons.get(getPosition());

            final Dialog dialog = new Dialog(view.getContext());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.beacon_info_dialog);
            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setAttributes(params);

            ((TextView)dialog.findViewById(R.id.dialogTitle)).setText(selected.getBluetoothName());
            ((TextView)dialog.findViewById(R.id.dialogText)).setText(selected.toString());
            dialog.findViewById(R.id.dialogOk).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            dialog.show();
        }
    }

    public void set(ArrayList<Beacon> beacons) {
        this.beacons = beacons;
        notifyDataSetChanged();
    }

    public void add(int position, Beacon item) {
        beacons.add(position, item);
        notifyItemInserted(position);
    }

    public void remove(Beacon item) {
        int position = beacons.indexOf(item);
        beacons.remove(position);
        notifyItemRemoved(position);
    }

    public BeaconListAdapter(ArrayList<Beacon> beacons) {
        this.beacons = beacons;
    }

    @Override
    public BeaconListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.rowTitle.setText(beacons.get(position).getBluetoothName());
        holder.rowSubtitle.setText(beacons.get(position).getDistance() + " meters away");
    }

    @Override
    public int getItemCount() {
        return beacons.size();
    }
}
