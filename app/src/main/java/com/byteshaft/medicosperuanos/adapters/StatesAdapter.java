package com.byteshaft.medicosperuanos.adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.byteshaft.medicosperuanos.R;
import com.byteshaft.medicosperuanos.gettersetter.States;

import java.util.ArrayList;

/**
 * Created by shahid on 28/03/2017.
 */

public class StatesAdapter extends BaseAdapter {

    private ViewHolder viewHolder;
    private ArrayList<States> states;
    private Activity activity;

    public StatesAdapter(Activity activity, ArrayList<States> states) {
        this.activity = activity;
        this.states = states;
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
        States singleState = states.get(position);
        viewHolder.spinnerText.setText(singleState.getName());
        Log.i("TAF", singleState.getName());
        return convertView;
    }

    @Override
    public int getCount() {
        return states.size();
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