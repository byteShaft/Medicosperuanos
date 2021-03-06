package com.byteshaft.medicosperuanos.adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.byteshaft.medicosperuanos.R;
import com.byteshaft.medicosperuanos.gettersetter.Treatments;

import java.util.ArrayList;


public class TreatmentsAdapter extends BaseAdapter {

    private ViewHolder viewHolder;
    private ArrayList<Treatments> treatmentsArrayList;
    private Activity activity;

    public TreatmentsAdapter(Activity activity , ArrayList<Treatments> treatmentsArrayList) {
        this.activity = activity;
        this.treatmentsArrayList = treatmentsArrayList;
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
        Treatments treatments = treatmentsArrayList.get(position);
        viewHolder.spinnerText.setText(treatments.getName());
        Log.i("TAF", treatments.getName());
        return convertView;
    }

    @Override
    public int getCount() {
        return treatmentsArrayList.size();
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
