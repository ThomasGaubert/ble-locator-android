package com.tgaubert.blefinder;

import android.app.Dialog;
import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
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
                            infoDialog.setContentView(R.layout.dialog_beacon_info);
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
                            final Dialog renameDialog = new Dialog(view.getContext());
                            renameDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            renameDialog.setContentView(R.layout.dialog_beacon_rename);
                            WindowManager.LayoutParams renameParams = renameDialog.getWindow().getAttributes();
                            renameParams.width = WindowManager.LayoutParams.MATCH_PARENT;
                            renameDialog.getWindow().setAttributes(renameParams);

                            final EditText name = ((EditText) renameDialog.findViewById(R.id.dialogEditText));
                            name.setHint(selected.getBluetoothName());

                            String currentName = BeaconIO.getSeenBeacon(selected.getBluetoothAddress()).getUserName();
                            if(currentName.contains("BLEFinder\u2063"))
                                name.setText("");
                            else
                                name.setText(currentName);

                            name.setSelection(name.getText().length());

                            renameDialog.findViewById(R.id.dialogCancel).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    renameDialog.dismiss();
                                }
                            });

                            renameDialog.findViewById(R.id.dialogOk).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if(name.getText().toString().equals("")) {
                                        name.setText("BLEFinder\u2063");
                                    }

                                    BeaconIO.getSeenBeacon(selected.getBluetoothAddress()).setUserName(name.getText().toString());
                                    renameDialog.dismiss();
                                }
                            });
                            renameDialog.show();
                            break;
                        case R.id.color:
                            Snackbar.make(view.getRootView().findViewById(R.id.floating_btn), "To be implemented...", Snackbar.LENGTH_LONG).show();
                            break;
                        case R.id.notify:
                            Snackbar.make(view.getRootView().findViewById(R.id.floating_btn), "To be implemented...", Snackbar.LENGTH_LONG).show();
                            break;
                        case R.id.ignore:
                            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                            builder.setTitle("Ignore Beacon");
                            builder.setMessage("Are you sure you want to ignore this beacon?");
                            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    BeaconIO.getSeenBeacons().get(selected.getBluetoothAddress()).setIgnore(true);
                                    Snackbar.make(view.getRootView().findViewById(R.id.floating_btn), "Ignoring " + selected.getBluetoothName() + ".", Snackbar.LENGTH_LONG).show();
                                }
                            });
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
        Beacon b = beacons.get(position);
        if(BeaconIO.getSeenBeacon(b.getBluetoothAddress()).getUserName().contains("BLEFinder\u2063"))
            holder.rowTitle.setText(b.getBluetoothName());
        else
            holder.rowTitle.setText(BeaconIO.getSeenBeacon(b.getBluetoothAddress()).getUserName());
        holder.rowSubtitle.setText(b.getDistance() + " meters away");
    }

    @Override
    public int getItemCount() {
        return beacons.size();
    }
}
