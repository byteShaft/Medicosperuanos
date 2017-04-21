package com.byteshaft.medicosperuanos.adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.byteshaft.medicosperuanos.R;
import com.byteshaft.medicosperuanos.gettersetter.AffiliateClinic;
import com.byteshaft.medicosperuanos.utils.AppGlobals;

import java.util.ArrayList;

/**
 * Created by shahid on 28/03/2017.
 */

public class AffiliateClinicAdapter extends BaseAdapter {

    private ViewHolder viewHolder;
    private ArrayList<AffiliateClinic> affiliateClinics;
    private Activity activity;

    public AffiliateClinicAdapter(Activity activity, ArrayList<AffiliateClinic> affiliateClinics) {
        this.activity = activity;
        this.affiliateClinics = affiliateClinics;
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
        AffiliateClinic affiliateClinic = affiliateClinics.get(position);
        viewHolder.spinnerText.setText(affiliateClinic.getName());
        Log.i("TAF", affiliateClinic.getName());
        return convertView;
    }

    @Override
    public int getCount() {
        return affiliateClinics.size();
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
