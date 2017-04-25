package com.byteshaft.medicosperuanos.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.byteshaft.medicosperuanos.R;
import com.byteshaft.medicosperuanos.gettersetter.Cities;
import com.byteshaft.medicosperuanos.gettersetter.DiagnosticMedication;

import java.util.ArrayList;

public class DiagnosticMedicationAdapter extends ArrayAdapter {

    private ViewHolder viewHolder;
    private ArrayList<DiagnosticMedication> diagnosticMedications;
    private Activity activity;

    public DiagnosticMedicationAdapter(Context context, Activity activity , ArrayList<DiagnosticMedication> diagnosticMedications) {
        super(context, R.layout.delegate_diagnostic_medication);
        this.activity = activity;
        this.diagnosticMedications = diagnosticMedications;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = activity.getLayoutInflater().inflate(R.layout.delegate_diagnostic_medication, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.idTextView = (TextView) convertView.findViewById(R.id.id_text_view);
            viewHolder.diagnosticListTextView = (TextView) convertView.findViewById(R.id.diagnostic_list_text_view);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        DiagnosticMedication diagnostic = diagnosticMedications.get(position);
        viewHolder.diagnosticListTextView.setText(diagnostic.getDiagnosticMedication());
        viewHolder.idTextView.setText(String.valueOf(diagnostic.getId()));
        return convertView;
    }

    @Override
    public int getCount() {
        return diagnosticMedications.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }
    public class ViewHolder {
        TextView idTextView;
        TextView diagnosticListTextView;
    }
}
