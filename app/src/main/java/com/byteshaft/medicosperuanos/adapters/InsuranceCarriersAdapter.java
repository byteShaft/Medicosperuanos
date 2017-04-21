package com.byteshaft.medicosperuanos.adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.byteshaft.medicosperuanos.R;
import com.byteshaft.medicosperuanos.gettersetter.InsuranceCarriers;

import java.util.ArrayList;

/**
 * Created by shahid on 28/03/2017.
 */

public class InsuranceCarriersAdapter extends BaseAdapter {

    private ViewHolder viewHolder;
    private ArrayList<InsuranceCarriers> insuranceCarrierses;
    private Activity activity;

    public InsuranceCarriersAdapter(Activity activity, ArrayList<InsuranceCarriers> insuranceCarrierses) {
        this.activity = activity;
        this.insuranceCarrierses = insuranceCarrierses;
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
        InsuranceCarriers insuranceCarriers = insuranceCarrierses.get(position);
        viewHolder.spinnerText.setText(insuranceCarriers.getName());
        Log.i("TAF", insuranceCarriers.getName());
        return convertView;
    }

    @Override
    public int getCount() {
        return insuranceCarrierses.size();
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