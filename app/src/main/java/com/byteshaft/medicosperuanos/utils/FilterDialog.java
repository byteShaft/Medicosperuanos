package com.byteshaft.medicosperuanos.utils;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.byteshaft.medicosperuanos.R;
import com.byteshaft.medicosperuanos.adapters.AffiliateClinicAdapter;
import com.byteshaft.medicosperuanos.adapters.SpecialitiesAdapter;
import com.byteshaft.medicosperuanos.doctors.DoctorsList;
import com.byteshaft.medicosperuanos.gettersetter.AffiliateClinic;
import com.byteshaft.medicosperuanos.gettersetter.Specialities;
import com.byteshaft.requests.HttpRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Calendar;

public class FilterDialog extends Dialog implements View.OnClickListener,
        DatePickerDialog.OnDateSetListener, AdapterView.OnItemSelectedListener {

    private SeekBar seekBar;
    private Activity activity;
    private TextView seekBarText;
    private ImageButton closeDialog;
    private EditText startDate;
    private EditText endDate;
    private Button clearFilter;
    private Button applyFilter;

    private DatePickerDialog datePickerDialog;

    private ArrayList<Specialities> specialitiesList;
    private SpecialitiesAdapter specialitiesAdapter;

    private Spinner mAffiliatedClinicsSpinner;
    private Spinner mSpecialitySpinner;

    private int affiliateClinicPosition;
    private int specialistPosition;

    private ArrayList<AffiliateClinic> affiliateClinicsList;
    private AffiliateClinicAdapter affiliateClinicAdapter;
    private boolean isStartDate;
    private AffiliateClinic mAffiliateClinic;
    private Specialities mSpecialities;

    public FilterDialog(Activity activity) {
        super(activity);
        this.activity = activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_dialog);
        seekBar = (SeekBar) findViewById(R.id.filter_seek_bar);
        seekBarText = (TextView) findViewById(R.id.seek_bar_text);
        closeDialog = (ImageButton) findViewById(R.id.close_dialog);
        startDate = (EditText) findViewById(R.id.start_date);
        endDate = (EditText) findViewById(R.id.end_date);
        applyFilter = (Button) findViewById(R.id.button_apply_filters);
        clearFilter = (Button) findViewById(R.id.button_clear_filters);
        specialitiesList = new ArrayList<>();
        affiliateClinicsList = new ArrayList<>();
        mAffiliateClinic = new AffiliateClinic();
        mSpecialities = new Specialities();
        mAffiliatedClinicsSpinner = (Spinner) findViewById(R.id.clinics_spinner_filter);
        mSpecialitySpinner = (Spinner) findViewById(R.id.speciality_spinner_filter);
        mAffiliatedClinicsSpinner.setOnItemSelectedListener(this);
        mSpecialitySpinner.setOnItemSelectedListener(this);
        getAffiliateClinic();
        getSpecialities();

        applyFilter.setOnClickListener(this);
        clearFilter.setOnClickListener(this);
        startDate.setOnClickListener(this);
        endDate.setOnClickListener(this);
        closeDialog.setOnClickListener(this);
        startDate.setText(Helpers.getDate());
        endDate.setText(Helpers.getDate());

        seekBarText.setText(String.valueOf(seekBar.getProgress()));
        final Calendar calendar = Calendar.getInstance();
        datePickerDialog = new DatePickerDialog(activity,
                this,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                seekBarText.setText(String.valueOf(seekBar.getProgress()));
                System.out.println(seekBar.getProgress());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.close_dialog:
                dismiss();
                break;
            case R.id.start_date:
                isStartDate = true;
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
                datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis() + (1000 * 60 * 60 * 24 * 7));
                datePickerDialog.show();
                System.out.println("start date");
                break;
            case R.id.end_date:
                isStartDate = false;
                datePickerDialog.show();
                System.out.println("end date");
                break;
            case R.id.button_clear_filters:
                System.out.println("Clear filters..");
                seekBar.setProgress(20);
                startDate.setText(Helpers.getDate());
                endDate.setText(Helpers.getDate());
                break;
            case R.id.button_apply_filters:
                Log.e(" Pppppppppp", mAffiliateClinic.getId() + "  " + mSpecialities.getSpecialitiesId());
                String sDate = startDate.getText().toString();
                String eDate = endDate.getText().toString();
                DoctorsList.getInstance().getDoctorList(
                        sDate,
                        eDate,
                        seekBar.getProgress(),
                        mAffiliateClinic.getId(),
                        mSpecialities.getSpecialitiesId());
                dismiss();
                break;
        }

    }

    private void getAffiliateClinic() {
        HttpRequest affiliateClinicRequest = new HttpRequest(AppGlobals.getContext());
        affiliateClinicRequest.setOnReadyStateChangeListener(new HttpRequest.OnReadyStateChangeListener() {
            @Override
            public void onReadyStateChange(HttpRequest request, int readyState) {
                switch (readyState) {
                    case HttpRequest.STATE_DONE:
                        switch (request.getStatus()) {
                            case HttpURLConnection.HTTP_OK:
                                try {
                                    JSONObject spObject = new JSONObject(request.getResponseText());
                                    JSONArray spArray = spObject.getJSONArray("results");
                                    for (int i = 0; i < spArray.length(); i++) {
                                        JSONObject jsonObject = spArray.getJSONObject(i);
                                        AffiliateClinic affiliateClinic = new AffiliateClinic();
                                        affiliateClinic.setId(jsonObject.getInt("id"));
                                        affiliateClinic.setName(jsonObject.getString("name"));
                                        affiliateClinicsList.add(affiliateClinic);
                                    }
                                    affiliateClinicAdapter = new AffiliateClinicAdapter(
                                            activity, affiliateClinicsList);
                                    mAffiliatedClinicsSpinner.setAdapter(affiliateClinicAdapter);
                                    mAffiliatedClinicsSpinner.setSelection(affiliateClinicPosition);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                        }
                }
            }
        });

        affiliateClinicRequest.open("GET", String.format("%sclinics/", AppGlobals.BASE_URL));
        affiliateClinicRequest.send();
    }

    private void getSpecialities() {
        HttpRequest specialitiesRequest = new HttpRequest(AppGlobals.getContext());
        specialitiesRequest.setOnReadyStateChangeListener(new HttpRequest.OnReadyStateChangeListener() {
            @Override
            public void onReadyStateChange(HttpRequest request, int readyState) {
                switch (readyState) {
                    case HttpRequest.STATE_DONE:
                        switch (request.getStatus()) {
                            case HttpURLConnection.HTTP_OK:
                                try {
                                    JSONObject spObject = new JSONObject(request.getResponseText());
                                    JSONArray spArray = spObject.getJSONArray("results");
                                    for (int i = 0; i < spArray.length(); i++) {
                                        JSONObject jsonObject = spArray.getJSONObject(i);
                                        Specialities specialities = new Specialities();
                                        specialities.setSpecialitiesId(jsonObject.getInt("id"));
                                        specialities.setSpeciality(jsonObject.getString("name"));
                                        specialitiesList.add(specialities);
                                    }
                                    specialitiesAdapter = new SpecialitiesAdapter(
                                            activity, specialitiesList);
                                    mSpecialitySpinner.setAdapter(specialitiesAdapter);
                                    mSpecialitySpinner.setSelection(specialistPosition);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                        }
                }
            }
        });

        specialitiesRequest.open("GET", String.format("%sspecialities", AppGlobals.BASE_URL));
        specialitiesRequest.send();
    }

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        if (isStartDate) {
            startDate.setText(i2 + "/" + (i1 + 1) + "/" + i);
        } else {
            endDate.setText(i2 + "/" + (i1 + 1) + "/" + i);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//        System.out.println("clinic onItemSelected");
        switch (adapterView.getId()) {
            case R.id.clinics_spinner_filter:
                System.out.println("clinic onItemSelected");
                mAffiliateClinic = affiliateClinicsList.get(i);
                break;
            case R.id.speciality_spinner_filter:
                System.out.println("speciality onItemSelected");
                mSpecialities = specialitiesList.get(i);
                break;
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
