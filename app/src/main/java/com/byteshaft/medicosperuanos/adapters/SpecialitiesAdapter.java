package com.byteshaft.medicosperuanos.adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.byteshaft.medicosperuanos.R;
import com.byteshaft.medicosperuanos.gettersetter.Specialities;
import com.byteshaft.medicosperuanos.utils.AppGlobals;

import java.util.ArrayList;

/**
 * Created by shahid on 25/04/2017.
 */

public class SpecialitiesAdapter extends BaseAdapter {

    private ViewHolder viewHolder;
    private ArrayList<Specialities> specialities;
    private Activity activity;

    public SpecialitiesAdapter(Activity activity, ArrayList<Specialities> specialities) {
        this.activity = activity;
        this.specialities = specialities;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = activity.getLayoutInflater().inflate(R.layout.delegate_spinner, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.spinnerText = (TextView) convertView.findViewById(R.id.spinner_text);
            viewHolder.spinnerText.setTypeface(AppGlobals.typefaceNormal);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Specialities speciality = specialities.get(position);
        viewHolder.spinnerText.setText(speciality.getSpeciality());
        return convertView;
    }

    @Override
    public int getCount() {
        return specialities.size();
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
