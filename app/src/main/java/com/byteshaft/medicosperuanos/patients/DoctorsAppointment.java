package com.byteshaft.medicosperuanos.patients;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ActionBarOverlayLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.byteshaft.medicosperuanos.R;
import com.byteshaft.medicosperuanos.adapters.DiagnosticMedicationAdapter;
import com.byteshaft.medicosperuanos.adapters.DiagnosticsAdapter;
import com.byteshaft.medicosperuanos.adapters.TargetsAdapter;
import com.byteshaft.medicosperuanos.adapters.TreatmentsAdapter;
import com.byteshaft.medicosperuanos.gettersetter.DiagnosticMedication;
import com.byteshaft.medicosperuanos.gettersetter.Diagnostics;
import com.byteshaft.medicosperuanos.gettersetter.Targets;
import com.byteshaft.medicosperuanos.gettersetter.Treatments;
import com.byteshaft.medicosperuanos.utils.AppGlobals;
import com.byteshaft.medicosperuanos.utils.Helpers;
import com.byteshaft.requests.HttpRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by husnain on 2/23/17.
 */

public class DoctorsAppointment extends AppCompatActivity implements View.OnClickListener,
        DatePickerDialog.OnDateSetListener, HttpRequest.OnReadyStateChangeListener,
        HttpRequest.OnErrorListener, AdapterView.OnItemSelectedListener {

    private TextView mDiagnosticsTextView;
    private TextView mMedicationTextView;
    private Spinner mDestinationSpinner;

    private EditText mDateEditText;
    private EditText mTimeEditText;
    private EditText mReturnDateEditText;
    private EditText mExplanationEditText;
    private EditText mConclusionsEditText;

    private Button mPlusButtonDiagnostics;
    private Button mMinusButtonDiagnostics;
    private Button mMinusButtonMedication;
    private Button mPlusButtonMedication;

    private DatePickerDialog datePickerDialog;
    private TimePickerDialog mTimePickerDialog;

    private boolean isSetForReturn = false;

    private View view;
    private ImageButton backPress;
    private TextView mPatientsName;
    private TextView mPatientsAge;
    private EditText mAppointmentReason;

    private String mFname;
    private String mLname;
    private String mAge;
    private String mReason;
    private String mDate;

    private String mDiagnosticsSpinnerValue;
    private String mMedicationSpinnerValue;
    private String mDestinationSpinnerValue;

    private ArrayList<DiagnosticMedication> diagnosticsMedicationList;
    private DiagnosticsAdapter diagnosticsAdapter;

    private ArrayList<Treatments> treatmentsArrayList;
    private TreatmentsAdapter treatmentsAdapter;

    private ArrayList<Targets> targetsArrayList;
    private TargetsAdapter targetsAdapter;

    private HttpRequest request;
    private ArrayList<Integer> diagonisticsArrayList;

    private ListView mdeiacationDiagnosticListView;
    private DiagnosticMedicationAdapter diagnosticMedicationAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(R.string.my_patient_details);
        setContentView(R.layout.activity_doctors_appointment);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.custom_action_bar);
        view = (View) findViewById(R.id.layout_for_name);
        diagonisticsArrayList = new ArrayList<>();
        mPatientsName = (TextView) view.findViewById(R.id.action_bar_title);
        mPatientsAge = (TextView) view.findViewById(R.id.action_bar_age);
        mAppointmentReason = (EditText) findViewById(R.id.appointment_reason_editText);
        mDiagnosticsTextView = (TextView) findViewById(R.id.diagnostics_TextView);
        mMedicationTextView = (TextView) findViewById(R.id.medication_TextView);
        mDestinationSpinner = (Spinner) findViewById(R.id.destination_spinner);

        mDateEditText = (EditText) findViewById(R.id.date_edit_text);
        mTimeEditText = (EditText) findViewById(R.id.time_edit_text);
        mReturnDateEditText = (EditText) findViewById(R.id.return_date_edit_text);
        mExplanationEditText = (EditText) findViewById(R.id.explanation_edit_text);
        mConclusionsEditText = (EditText) findViewById(R.id.conclusions_edit_text);

        backPress = (ImageButton) findViewById(R.id.back_press);
        mPlusButtonDiagnostics = (Button) findViewById(R.id.plus_button_diagnostics);
        mMinusButtonDiagnostics = (Button) findViewById(R.id.minus_button_diagnostics);
        mPlusButtonMedication = (Button) findViewById(R.id.plus_button_medication);
        mMinusButtonMedication = (Button) findViewById(R.id.minus_button_medication);

        mDateEditText.setTypeface(AppGlobals.typefaceNormal);
        mTimeEditText.setTypeface(AppGlobals.typefaceNormal);
        mReturnDateEditText.setTypeface(AppGlobals.typefaceNormal);
        mExplanationEditText.setTypeface(AppGlobals.typefaceNormal);
        mConclusionsEditText.setTypeface(AppGlobals.typefaceNormal);

        backPress.setOnClickListener(this);
        mReturnDateEditText.setOnClickListener(this);
        mDiagnosticsTextView.setOnClickListener(this);
        mDestinationSpinner.setOnItemSelectedListener(this);
        mMedicationTextView.setOnClickListener(this);

        mPlusButtonDiagnostics.setOnClickListener(this);
        mMinusButtonDiagnostics.setOnClickListener(this);
        mPlusButtonMedication.setOnClickListener(this);
        mMinusButtonMedication.setOnClickListener(this);


        mFname = getIntent().getStringExtra("first_name");
        mLname = getIntent().getStringExtra("last_name");
        mAge = getIntent().getStringExtra("age");
        mReason = getIntent().getStringExtra("reason");
        mDate = getIntent().getStringExtra("date");

        mPatientsName.setText(mFname + " " + mLname);
        String years = Helpers.calculateAge(mAge);
        mPatientsAge.setText(years + " " + "years");
        mAppointmentReason.setText(mReason);
        mAppointmentReason.setEnabled(false);
        mDateEditText.setText(mDate);
        mTimeEditText.setText(Helpers.getTime24HourFormat());
        mTimeEditText.setEnabled(false);
        mDateEditText.setEnabled(false);

        diagnosticsMedicationList = new ArrayList<>();
        treatmentsArrayList = new ArrayList<>();
        targetsArrayList = new ArrayList<>();
//        getDiagnostic();
//        getTreatments();
        getTargets();


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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.appointment, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.share:
                Toast.makeText(this, "working", Toast.LENGTH_SHORT).show();
                break;
            case R.id.attach_icon:
                Toast.makeText(this, "working", Toast.LENGTH_SHORT).show();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.return_date_edit_text:
                datePickerDialog.show();
                break;
            case R.id.back_press:
                onBackPressed();
                break;
            case R.id.plus_button_diagnostics:
                break;
            case R.id.minus_button_diagnostics:
                break;
            case R.id.plus_button_medication:
                break;
            case R.id.minus_button_medication:
                break;
            case R.id.medication_TextView:
                medicationDiagnosticsDialog();
                diagnosticsMedicationList.clear();
                getTreatments();
                break;
            case R.id.diagnostics_TextView:
                medicationDiagnosticsDialog();
                diagnosticsMedicationList.clear();
                getDiagnostic();
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


    private void getTargets() {
        HttpRequest diagnosticsRequest = new HttpRequest(this);
        diagnosticsRequest.setOnReadyStateChangeListener(new HttpRequest.OnReadyStateChangeListener() {
            @Override
            public void onReadyStateChange(HttpRequest request, int readyState) {
                switch (readyState) {
                    case HttpRequest.STATE_DONE:
                        switch (request.getStatus()) {
                            case HttpURLConnection.HTTP_OK:
                                try {
                                    JSONObject targetsObject = new JSONObject(request.getResponseText());
                                    JSONArray targetsArray = targetsObject.getJSONArray("results");
                                    System.out.println(targetsArray + "working");
                                    for (int i = 0; i < targetsArray.length(); i++) {
                                        JSONObject jsonObject = targetsArray.getJSONObject(i);
                                        Targets targets = new Targets();
                                        targets.setId(jsonObject.getInt("id"));
                                        targets.setName(jsonObject.getString("name"));
                                        targetsArrayList.add(targets);
                                    }
                                    System.out.println(targetsArray.length() + "length");
                                    targetsAdapter = new TargetsAdapter(DoctorsAppointment.this, targetsArrayList);
                                    mDestinationSpinner.setAdapter(targetsAdapter);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                        }
                }
            }
        });

        diagnosticsRequest.open("GET", String.format("%stargets/", AppGlobals.BASE_URL));
        diagnosticsRequest.send();
    }

    @Override
    public void onError(HttpRequest request, int readyState, short error, Exception exception) {

    }

    @Override
    public void onReadyStateChange(HttpRequest request, int readyState) {

    }

    private void registerUser(int appointmentId, String conclusion, String date, String dateOfReturn,
                              String destination, String diagnostics, String exploration, String time) {
        request = new HttpRequest(this);
        request.setOnReadyStateChangeListener(this);
        request.setOnErrorListener(this);
        request.open("POST", String.format("%sdoctor/appointments/%s/attention ", AppGlobals.BASE_URL, appointmentId));
        request.send(getAttentionsData(conclusion, date, dateOfReturn, destination, diagnostics, exploration, time));
    }


    private String getAttentionsData(String conclusion, String date, String dateOfReturn,
                                     String destination, String diagnostics, String exploration, String time) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("conclusion", conclusion);
            jsonObject.put("date", date);
            jsonObject.put("date_of_return", dateOfReturn);
            jsonObject.put("destination", destination);
            jsonObject.put("diagnostics", diagnostics);
            jsonObject.put("exploration", exploration);
            jsonObject.put("time", time);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();

    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        switch (adapterView.getId()) {
            case R.id.destination_spinner:
                Targets targets = targetsArrayList.get(position);
                mDestinationSpinnerValue = String.valueOf(targets.getId());
                System.out.println(targets.getId());
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void medicationDiagnosticsDialog() {
        final Dialog dialog = new Dialog(DoctorsAppointment.this);
        dialog.setContentView(R.layout.medication_diagnostics_search_list);
        int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.95);
        int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.70);
        dialog.getWindow().setLayout(width, height);
        dialog.setCancelable(true);
        ImageButton arrowUpButton = (ImageButton) dialog.findViewById(R.id.arrow_up);
        ImageButton arrowDownButton = (ImageButton) dialog.findViewById(R.id.arrow_down);
        mdeiacationDiagnosticListView = (ListView) dialog.findViewById(R.id.medication_diagnostic_search_list_view);
        arrowUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        arrowDownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        dialog.show();

    }


    private void getDiagnostic() {
        HttpRequest diagnosticsRequest = new HttpRequest(this);
        diagnosticsRequest.setOnReadyStateChangeListener(new HttpRequest.OnReadyStateChangeListener() {
            @Override
            public void onReadyStateChange(HttpRequest request, int readyState) {
                switch (readyState) {
                    case HttpRequest.STATE_DONE:
                        switch (request.getStatus()) {
                            case HttpURLConnection.HTTP_OK:
                                try {
                                    JSONObject diagnosticsObject = new JSONObject(request.getResponseText());
                                    JSONArray diagnosticsArray = diagnosticsObject.getJSONArray("results");
                                    System.out.println("new List view" + " " + diagnosticsArray);
                                    for (int i = 0; i < diagnosticsArray.length(); i++) {
                                        JSONObject jsonObject = diagnosticsArray.getJSONObject(i);
                                        DiagnosticMedication diagnosticMedication = new DiagnosticMedication();
                                        diagnosticMedication.setId(jsonObject.getInt("id"));
                                        diagnosticMedication.setDiagnosticMedication(jsonObject.getString("name"));
                                        diagnosticsMedicationList.add(diagnosticMedication);
                                    }
                                    System.out.println(diagnosticsArray.length() + "length");
                                    diagnosticMedicationAdapter = new DiagnosticMedicationAdapter(getApplicationContext(), DoctorsAppointment.this, diagnosticsMedicationList);
                                    mdeiacationDiagnosticListView.setAdapter(diagnosticMedicationAdapter);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                        }
                }
            }
        });

        diagnosticsRequest.open("GET", String.format("%sdiagnostics/", AppGlobals.BASE_URL));
        diagnosticsRequest.send();
    }

    private void getTreatments() {
        HttpRequest diagnosticsRequest = new HttpRequest(this);
        diagnosticsRequest.setOnReadyStateChangeListener(new HttpRequest.OnReadyStateChangeListener() {
            @Override
            public void onReadyStateChange(HttpRequest request, int readyState) {
                switch (readyState) {
                    case HttpRequest.STATE_DONE:
                        switch (request.getStatus()) {
                            case HttpURLConnection.HTTP_OK:
                                try {
                                    JSONObject treatmentsObject = new JSONObject(request.getResponseText());
                                    JSONArray treatmentsArray = treatmentsObject.getJSONArray("results");
                                    System.out.println(treatmentsArray + "working");
                                    for (int i = 0; i < treatmentsArray.length(); i++) {
                                        JSONObject jsonObject = treatmentsArray.getJSONObject(i);
                                        DiagnosticMedication diagnosticMedication = new DiagnosticMedication();
                                        diagnosticMedication.setId(jsonObject.getInt("id"));
                                        diagnosticMedication.setDiagnosticMedication(jsonObject.getString("name"));
                                        diagnosticsMedicationList.add(diagnosticMedication);
                                    }
                                    System.out.println(treatmentsArray.length() + "length");
                                    diagnosticMedicationAdapter = new DiagnosticMedicationAdapter(getApplicationContext(), DoctorsAppointment.this, diagnosticsMedicationList);
                                    mdeiacationDiagnosticListView.setAdapter(diagnosticMedicationAdapter);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                        }
                }
            }
        });

        diagnosticsRequest.open("GET", String.format("%streatments/", AppGlobals.BASE_URL));
        diagnosticsRequest.send();
    }
}
