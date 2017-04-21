package com.byteshaft.medicosperuanos.patients;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.TextView;

import com.byteshaft.medicosperuanos.R;
import com.byteshaft.medicosperuanos.utils.AppGlobals;

public class PatientsRecentHistory extends AppCompatActivity {

    private TextView patientName;
    private TextView patientEmail;
    private TextView patientAge;
    private ImageView patientImage;
    private RecyclerView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patients_recent_history);
        setContentView(R.layout.dashboard_fragment);
        patientName = (TextView) findViewById(R.id.patient_name);
        patientEmail = (TextView) findViewById(R.id.patient_email);
        patientAge = (TextView) findViewById(R.id.patient_age);
        patientImage = (ImageView) findViewById(R.id.patient_image);

        // typeface

        patientName.setTypeface(AppGlobals.typefaceNormal);
        patientEmail.setTypeface(AppGlobals.typefaceNormal);
        patientAge.setTypeface(AppGlobals.typefaceNormal);

        list = (RecyclerView) findViewById(R.id.patient_history_list);
    }
}
