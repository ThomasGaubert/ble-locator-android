package com.tgaubert.blefinder;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.cocosw.bottomsheet.BottomSheet;

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
        public void onClick(final View view) {
            final Beacon selected = beacons.get(getPosition());

            new BottomSheet.Builder((MainActivity) view.getContext())
                    .title(selected.getBluetoothName())
                    .sheet(R.menu.beacon_sheet)
                    .listener(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch(which) {
                        case R.id.info:
                            final Dialog infoDialog = new Dialog(view.getContext());
                            infoDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            infoDialog.setContentView(R.layout.beacon_info_dialog);
                            WindowManager.LayoutParams params = infoDialog.getWindow().getAttributes();
                            params.width = WindowManager.LayoutParams.MATCH_PARENT;
                            infoDialog.getWindow().setAttributes(params);

                            ((TextView)infoDialog.findViewById(R.id.dialogTitle)).setText(selected.getBluetoothName());
                            ((TextView)infoDialog.findViewById(R.id.dialogText)).setText(BeaconIO.getSeenBeacons().get(selected.getBluetoothAddress()).getJsonObject().toString());
                            infoDialog.findViewById(R.id.dialogOk).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    infoDialog.dismiss();
                                }
                            });
                            infoDialog.show();
                            break;
                        case R.id.rename:
                            Toast.makeText(view.getContext(), "To be implemented...", Toast.LENGTH_SHORT).show();
                            break;
                        case R.id.color:
                            Toast.makeText(view.getContext(), "To be implemented...", Toast.LENGTH_SHORT).show();
                            break;
                        case R.id.notify:
                            Toast.makeText(view.getContext(), "To be implemented...", Toast.LENGTH_SHORT).show();
                            break;
                        case R.id.ignore:
                            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                            builder.setTitle("Ignore Beacon");
                            builder.setMessage("Are you sure you want to permanently ignore this beacon?");
                            builder.setPositiveButton("Yes", null);
                            builder.setNegativeButton("No", null);
                            builder.show();
                            break;
                    }
                }
            }).show();
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
