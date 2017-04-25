package com.byteshaft.medicosperuanos.adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.byteshaft.medicosperuanos.R;
import com.byteshaft.medicosperuanos.gettersetter.Diagnostics;

import java.util.ArrayList;


public class DiagnosticsAdapter extends BaseAdapter {

    private ViewHolder viewHolder;
    private ArrayList<Diagnostics> diagnosticses;
    private Activity activity;

    public DiagnosticsAdapter(Activity activity , ArrayList<Diagnostics> diagnosticses) {
        this.activity = activity;
        this.diagnosticses = diagnosticses;
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
        Diagnostics diagnosticsList = diagnosticses.get(position);
        viewHolder.spinnerText.setText(diagnosticsList.getName());
        Log.i("TAF", diagnosticsList.getName());
        return convertView;
    }

    @Override
    public int getCount() {
        return diagnosticses.size();
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
