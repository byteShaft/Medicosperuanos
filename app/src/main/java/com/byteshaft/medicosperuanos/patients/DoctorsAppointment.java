package com.byteshaft.medicosperuanos.patients;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TimePicker;

import com.byteshaft.medicosperuanos.R;
import com.byteshaft.medicosperuanos.utils.AppGlobals;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by husnain on 2/23/17.
 */

public class DoctorsAppointment extends AppCompatActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener {

    private Spinner mAppointmentReasonSpinner;
    private Spinner mDiagnosticsSpinner;
    private Spinner mMedicationSpinner;
    private Spinner mDestinationSpinner;

    private EditText mDateEditText;
    private EditText mTimeEditText;
    private EditText mReturnDateEditText;
    private EditText mExplanationEditText;
    private EditText mConclusionsEditText;

    private DatePickerDialog datePickerDialog;
    private TimePickerDialog mTimePickerDialog;

    private boolean isSetForReturn = false;
    private ImageButton backPress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(R.string.my_patient_details);
        setContentView(R.layout.activity_doctors_appointment);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.custom_action_bar);
        mAppointmentReasonSpinner = (Spinner) findViewById(R.id.appointment_reason);
        mDiagnosticsSpinner = (Spinner) findViewById(R.id.diagnostics_spinner);
        mMedicationSpinner = (Spinner) findViewById(R.id.medication_spinner);
        mDestinationSpinner = (Spinner) findViewById(R.id.destination_spinner);

        mDateEditText = (EditText) findViewById(R.id.date_edit_text);
        mTimeEditText = (EditText) findViewById(R.id.time_edit_text);
        mReturnDateEditText = (EditText) findViewById(R.id.return_date_edit_text);
        mExplanationEditText = (EditText) findViewById(R.id.explanation_edit_text);
        mConclusionsEditText = (EditText) findViewById(R.id.conclusions_edit_text);
        backPress = (ImageButton) findViewById(R.id.back_press);
        backPress.setOnClickListener(this);

        mDateEditText.setTypeface(AppGlobals.typefaceNormal);
        mTimeEditText.setTypeface(AppGlobals.typefaceNormal);
        mReturnDateEditText.setTypeface(AppGlobals.typefaceNormal);
        mExplanationEditText.setTypeface(AppGlobals.typefaceNormal);
        mConclusionsEditText.setTypeface(AppGlobals.typefaceNormal);

        mDateEditText.setOnClickListener(this);
        mTimeEditText.setOnClickListener(this);
        mReturnDateEditText.setOnClickListener(this);

        List<String> AppointmentReasonList = new ArrayList<String>();
        AppointmentReasonList.add("Headache");
        AppointmentReasonList.add("Acute illness or injury");
        AppointmentReasonList.add("Diabetes");
        AppointmentReasonList.add("Cancer");
        AppointmentReasonList.add("Fever/cough");
        ArrayAdapter<String> AppointmentReasonAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, AppointmentReasonList);
        AppointmentReasonAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mAppointmentReasonSpinner.setAdapter(AppointmentReasonAdapter);

        List<String> DiagnosticList = new ArrayList<String>();
        DiagnosticList.add("center1");
        DiagnosticList.add("center2");
        DiagnosticList.add("cantt");
        DiagnosticList.add("city");
        DiagnosticList.add("MMA road");
        ArrayAdapter<String> DiagnosticListAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, DiagnosticList);
        DiagnosticListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mDiagnosticsSpinner.setAdapter(DiagnosticListAdapter);


        List<String> MedicationList = new ArrayList<String>();
        MedicationList.add("Abilify");
        MedicationList.add("Aspirin");
        MedicationList.add("Bisoprolol");
        MedicationList.add("Zantac");
        MedicationList.add("Flagyl");
        ArrayAdapter<String> MedicationListAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, MedicationList);
        MedicationListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mMedicationSpinner.setAdapter(MedicationListAdapter);

        List<String> DestinationList = new ArrayList<String>();
        DestinationList.add("Appointment");
        DestinationList.add("Waiting");
        DestinationList.add("Booking for time");
        ArrayAdapter<String> DestinationSpinnerListAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, DestinationList);
        DestinationSpinnerListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mDestinationSpinner.setAdapter(DestinationSpinnerListAdapter);


        final Calendar calendar = Calendar.getInstance();
        datePickerDialog = new DatePickerDialog(DoctorsAppointment.this,
                this,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        mTimePickerDialog = new TimePickerDialog(DoctorsAppointment.this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        mTimeEditText.setText(convertDate(hourOfDay) + ":" + convertDate(minute));

                    }
                }, calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE), false);
    }


    public String convertDate(int input) {
        if (input >= 10) {
            return String.valueOf(input);
        } else {
            return "0" + String.valueOf(input);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.date_edit_text:
                datePickerDialog.show();
                break;

            case R.id.time_edit_text:
                mTimePickerDialog.show();
                break;
            case R.id.return_date_edit_text:
                datePickerDialog.show();
                break;
            case R.id.back_press:
                onBackPressed();
                break;
        }
    }


    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        if (!isSetForReturn) {
            mDateEditText.setText(i2 + "/" + i1 + "/" + i);
            isSetForReturn = true;
        } else {
            mReturnDateEditText.setText(i2 + "/" + i1 + "/" + i);
            isSetForReturn = false;
        }


    }
}
