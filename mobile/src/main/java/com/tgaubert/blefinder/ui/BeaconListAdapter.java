package com.tgaubert.blefinder.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.cocosw.bottomsheet.BottomSheet;
import com.tgaubert.blefinder.MainActivity;
import com.tgaubert.blefinder.R;
import com.tgaubert.blefinder.util.BeaconIO;

import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;

public class BeaconListAdapter extends RecyclerView.Adapter<BeaconListAdapter.ViewHolder> {

    private ArrayList<Beacon> beacons;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public View view;
        public TextView rowTitle, rowSubtitle;
        public ImageView rowColor;

        public ViewHolder(View v) {
            super(v);

            view = v;
            rowTitle = (TextView) v.findViewById(R.id.rowTitle);
            rowSubtitle = (TextView) v.findViewById(R.id.rowSubtitle);
            rowColor = (ImageView) v.findViewById(R.id.colorTag);

            v.setOnClickListener(this);
        }

        @Override
        public void onClick(final View view) {
            final Beacon b = beacons.get(getLayoutPosition());
            String beaconName = b.getBluetoothName();
            if (!BeaconIO.getSeenBeacon(b.getBluetoothAddress()).getUserName().contains("BLEFinder\u2063"))
                beaconName = BeaconIO.getSeenBeacon(b.getBluetoothAddress()).getUserName();

            new BottomSheet.Builder((MainActivity) view.getContext())
                    .title(beaconName)
                    .sheet(R.menu.beacon_sheet)
                    .listener(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case R.id.info:
                                    final Dialog infoDialog = new Dialog(view.getContext());
                                    infoDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                    infoDialog.setContentView(R.layout.dialog_beacon_info);
                                    WindowManager.LayoutParams params = infoDialog.getWindow().getAttributes();
                                    params.width = WindowManager.LayoutParams.MATCH_PARENT;
                                    infoDialog.getWindow().setAttributes(params);

                                    String beaconName = b.getBluetoothName();
                                    if (!BeaconIO.getSeenBeacon(b.getBluetoothAddress()).getUserName().contains("BLEFinder\u2063"))
                                        beaconName = BeaconIO.getSeenBeacon(b.getBluetoothAddress()).getUserName();

                                    ((TextView) infoDialog.findViewById(R.id.dialogTitle)).setText(beaconName);
                                    ((TextView) infoDialog.findViewById(R.id.dialogText)).setText(BeaconIO.getSeenBeacons().get(b.getBluetoothAddress()).getJsonObject().toString());
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
                                    name.setHint(b.getBluetoothName());

                                    name.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                                        @Override
                                        public void onFocusChange(View v, boolean hasFocus) {
                                            if (hasFocus) {
                                                renameDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                                            }
                                        }
                                    });

                                    String currentName = BeaconIO.getSeenBeacon(b.getBluetoothAddress()).getUserName();
                                    if (currentName.contains("BLEFinder\u2063"))
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
                                            if (name.getText().toString().equals("")) {
                                                name.setText("BLEFinder\u2063");
                                            }

                                            BeaconIO.getSeenBeacon(b.getBluetoothAddress()).setUserName(name.getText().toString());
                                            renameDialog.dismiss();
                                        }
                                    });
                                    renameDialog.show();
                                    break;
                                case R.id.color:
                                    HSVColorPickerDialog cpd = new HSVColorPickerDialog(view.getContext(), 0xFF4488CC, new HSVColorPickerDialog.OnColorSelectedListener() {
                                        @Override
                                        public void colorSelected(Integer color) {
                                            if(color == -1)
                                                color = 0;
                                            BeaconIO.getSeenBeacon(b.getBluetoothAddress()).setColor(color);
                                        }
                                    });
                                    cpd.setTitle(R.string.adapter_color_picker_title);
                                    cpd.setNoColorButton(R.string.none);
                                    cpd.show();
                                    break;
                                case R.id.notify:
                                    final Dialog notifyDialog = new Dialog(view.getContext());
                                    notifyDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                    notifyDialog.setContentView(R.layout.dialog_beacon_notify);
                                    WindowManager.LayoutParams notifyParams = notifyDialog.getWindow().getAttributes();
                                    notifyParams.width = WindowManager.LayoutParams.MATCH_PARENT;
                                    notifyDialog.getWindow().setAttributes(notifyParams);

                                    final EditText distEditText = ((EditText) notifyDialog.findViewById(R.id.notifyDistance));
                                    String distance = BeaconIO.getSeenBeacon(b.getBluetoothAddress()).getDistance();
                                    if(Integer.parseInt(distance) != 0)
                                        distEditText.setText(distance);
                                    distEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                                        @Override
                                        public void onFocusChange(View v, boolean hasFocus) {
                                            if (hasFocus) {
                                                notifyDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                                            }
                                        }
                                    });
                                    distEditText.setSelection(distEditText.getText().length());

                                    notifyDialog.findViewById(R.id.dialogCancel).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            notifyDialog.dismiss();
                                        }
                                    });

                                    notifyDialog.findViewById(R.id.dialogOk).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            String distance = distEditText.getText().toString();
                                            if(distance.trim().equals(""))
                                                distance = "0";
                                            BeaconIO.getSeenBeacon(b.getBluetoothAddress()).setDistance(distance);
                                            notifyDialog.dismiss();
                                        }
                                    });

                                    notifyDialog.show();
                                    break;
                                case R.id.ignore:
                                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                                    builder.setTitle(R.string.adapter_ignore_title);
                                    builder.setMessage(R.string.adapter_ignore_text);
                                    builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            BeaconIO.getSeenBeacons().get(b.getBluetoothAddress()).setIgnore(true);
                                            Snackbar.make(view.getRootView().findViewById(R.id.floating_btn),
                                                    view.getContext().getString(R.string.adapter_ignore_verb)
                                                            + b.getBluetoothName() + ".", Snackbar.LENGTH_LONG).show();
                                        }
                                    });
                                    builder.setNegativeButton(R.string.no, null);
                                    builder.show();
                                    break;
                            }
                        }
                    }).show();
        }
    }

    public void set(ArrayList<Beacon> beacons) {
        this.beacons = beacons;
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
        Context context = holder.view.getContext();
        GradientDrawable colorTag;

        Beacon b = beacons.get(position);
        if (BeaconIO.getSeenBeacon(b.getBluetoothAddress()).getUserName().contains("BLEFinder\u2063"))
            holder.rowTitle.setText(b.getBluetoothName());
        else
            holder.rowTitle.setText(BeaconIO.getSeenBeacon(b.getBluetoothAddress()).getUserName());
        holder.rowSubtitle.setText(b.getDistance() + " " + context.getString(R.string.adapter_card_text_units_away));

        if(Build.VERSION.SDK_INT < 21)
            //noinspection deprecation
            colorTag = (GradientDrawable) context.getResources().getDrawable(R.drawable.circle);
        else
            colorTag = (GradientDrawable) context.getResources().getDrawable(R.drawable.circle, context.getTheme());

        if(colorTag != null)
            colorTag.setColor(BeaconIO.getSeenBeacon(b.getBluetoothAddress()).getColor());
        holder.rowColor.setImageDrawable(colorTag);
    }

    @Override
    public int getItemCount() {
        return beacons.size();
    }
}
