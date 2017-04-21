package com.byteshaft.medicosperuanos.adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.byteshaft.medicosperuanos.R;
import com.byteshaft.medicosperuanos.gettersetter.SubscriptionType;

import java.util.ArrayList;

/**
 * Created by shahid on 28/03/2017.
 */

public class SubscriptionTypeAdapter extends BaseAdapter {

    private ViewHolder viewHolder;
    private ArrayList<SubscriptionType> subscriptionTypes;
    private Activity activity;

    public SubscriptionTypeAdapter(Activity activity, ArrayList<SubscriptionType> subscriptionTypes) {
        this.activity = activity;
        this.subscriptionTypes = subscriptionTypes;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = activity.getLayoutInflater().inflate(R.layout.delegate_spinner, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.spinnerText = (TextView) convertView.findViewById(R.id.spinner_text);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        SubscriptionType subscriptionType = subscriptionTypes.get(position);
        viewHolder.spinnerText.setText(subscriptionType.getPlanType());
        Log.i("TAF", subscriptionType.getPlanType());
        return convertView;
    }

    @Override
    public int getCount() {
        return subscriptionTypes.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }
}
