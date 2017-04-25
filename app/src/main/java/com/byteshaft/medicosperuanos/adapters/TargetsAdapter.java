package com.byteshaft.medicosperuanos.adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.byteshaft.medicosperuanos.R;
import com.byteshaft.medicosperuanos.gettersetter.Targets;

import java.util.ArrayList;


public class TargetsAdapter extends BaseAdapter {

    private ViewHolder viewHolder;
    private ArrayList<Targets> targetsArrayList;
    private Activity activity;

    public TargetsAdapter(Activity activity , ArrayList<Targets> targetsArrayList) {
        this.activity = activity;
        this.targetsArrayList = targetsArrayList;
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
        Targets targets = targetsArrayList.get(position);
        viewHolder.spinnerText.setText(targets.getName());
        Log.i("TAF", targets.getName());
        return convertView;
    }

    @Override
    public int getCount() {
        return targetsArrayList.size();
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
