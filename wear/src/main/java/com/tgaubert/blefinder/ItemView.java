package com.tgaubert.blefinder;

import android.content.Context;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.WearableListView;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class ItemView extends LinearLayout implements WearableListView.OnCenterProximityListener {

    CircledImageView image;
    TextView text;

    public ItemView(Context context) {
        super(context, null);
    }

    public ItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        image = (CircledImageView) findViewById(R.id.image);
        text = (TextView) findViewById(R.id.text);
    }

    @Override
    public void onCenterPosition(boolean b) {
        image.animate().alpha(1).setDuration(100);
        text.animate().alpha(1).setDuration(100);
    }

    @Override
    public void onNonCenterPosition(boolean b) {
        image.animate().alpha(0.6f).setDuration(100);
        text.animate().alpha(0.6f).setDuration(100);
    }
}