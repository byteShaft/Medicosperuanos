package com.byteshaft.medicosperuanos.utils;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

public class FilterDialog extends Dialog implements View.OnClickListener, DatePickerDialog.OnDateSetListener {

    private SeekBar seekBar;
    private Context mContext;
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

    private String mSpecialitySpinnerValueString;
    private int specialistPosition;
    private int affiliateClinicPosition;

    private ArrayList<AffiliateClinic> affiliateClinicsList;
    private AffiliateClinicAdapter affiliateClinicAdapter;
    private boolean isStartDate;

    public FilterDialog(Context context) {
        super(context);
        mContext = context;
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
        mAffiliatedClinicsSpinner = (Spinner) findViewById(R.id.clinics_spinner_filter);
        mSpecialitySpinner = (Spinner) findViewById(R.id.speciality_spinner_filter);
        getAffiliateClinic();
        getSpecialities();

        applyFilter.setOnClickListener(this);
        clearFilter.setOnClickListener(this);
        startDate.setOnClickListener(this);
        endDate.setOnClickListener(this);
        closeDialog.setOnClickListener(this);

        seekBarText.setText(String.valueOf(seekBar.getProgress()));
        final Calendar calendar = Calendar.getInstance();
        datePickerDialog = new DatePickerDialog(DoctorsList.getInstance().getActivity(),
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
                startDate.setText("Pick Date");
                endDate.setText("Pick Date");
                break;
            case R.id.button_apply_filters:
                startDate.getText().toString();
                endDate.getText().toString();
                Log.e("Result : ", startDate.getText().toString() + endDate.getText().toString());
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
                                            DoctorsList.getInstance().getActivity(), affiliateClinicsList);
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
                                    specialitiesAdapter = new SpecialitiesAdapter(DoctorsList.getInstance().getActivity(), specialitiesList);
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
}
