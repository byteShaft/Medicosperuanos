package com.byteshaft.medicosperuanos.patients;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.byteshaft.medicosperuanos.R;
import com.byteshaft.medicosperuanos.adapters.TargetsAdapter;
import com.byteshaft.medicosperuanos.doctors.Appointments;
import com.byteshaft.medicosperuanos.gettersetter.DiagnosticMedication;
import com.byteshaft.medicosperuanos.gettersetter.Services;
import com.byteshaft.medicosperuanos.gettersetter.Targets;
import com.byteshaft.medicosperuanos.utils.AppGlobals;
import com.byteshaft.medicosperuanos.utils.Helpers;
import com.byteshaft.requests.HttpRequest;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by husnain on 2/23/17.
 */

public class DoctorsAppointment extends AppCompatActivity implements View.OnClickListener,
        DatePickerDialog.OnDateSetListener, HttpRequest.OnReadyStateChangeListener,
        HttpRequest.OnErrorListener, AdapterView.OnItemSelectedListener {

    private Spinner mDiagnosticsTextView;
    private Spinner mMedicationTextView;
    private Spinner mDestinationSpinner;

    private EditText mDateEditText;
    private EditText mTimeEditText;
    private EditText mReturnDateEditText;
    private EditText mExplanationEditText;
    private EditText mConclusionsEditText;

    private Button mPlusButtonDiagnostics;
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

    private ArrayList<DiagnosticMedication> diagnosticsList;
    private ArrayList<DiagnosticMedication> medicationList;

    private ArrayList<DiagnosticMedication> selectedDiagnosticsList;
    private ArrayList<DiagnosticMedication> selectedMedicationList;
    private HashMap<Integer, Integer> selectedDiagnostic;
    private HashMap<Integer, Integer> selectedMedication;
    private ArrayList<Targets> targetsArrayList;
    private TargetsAdapter targetsAdapter;

    private HttpRequest request;

    private ListView medicationDiagnosticListView;
    private DiagnosticAdapter diagnosticAdapter;
    private MedicationAdapter medicationAdapter;
    private LinearLayout checkBoxLayout;
    private int id = -1;
    private ArrayList<DiagnosticMedication> searchListForDiagonistics;
    private ArrayList<DiagnosticMedication> searchListForMedications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(R.string.my_patient_details);
        setContentView(R.layout.activity_doctors_appointment);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.custom_action_bar);
        getDiagnostic();
        getMedications();
        view = (View) findViewById(R.id.layout_for_name);
        mFname = getIntent().getStringExtra("first_name");
        mLname = getIntent().getStringExtra("last_name");
        mAge = getIntent().getStringExtra("age");
        mReason = getIntent().getStringExtra("reason");
        mDate = getIntent().getStringExtra("date");
        id = getIntent().getIntExtra("id", -1);

        diagnosticsList = new ArrayList<>();
        medicationList = new ArrayList<>();
        targetsArrayList = new ArrayList<>();
        selectedDiagnosticsList = new ArrayList<>();
        selectedMedicationList = new ArrayList<>();
        selectedDiagnostic = new HashMap<>();
        selectedMedication = new HashMap<>();

        mPatientsName = (TextView) view.findViewById(R.id.action_bar_title);
        mPatientsAge = (TextView) view.findViewById(R.id.action_bar_age);
        mAppointmentReason = (EditText) findViewById(R.id.appointment_reason_editText);
        mDiagnosticsTextView = (Spinner) findViewById(R.id.diagnostics_TextView);
        mMedicationTextView = (Spinner) findViewById(R.id.medication_TextView);
        mDestinationSpinner = (Spinner) findViewById(R.id.destination_spinner);

        mDateEditText = (EditText) findViewById(R.id.date_edit_text);
        mTimeEditText = (EditText) findViewById(R.id.time_edit_text);
        mReturnDateEditText = (EditText) findViewById(R.id.return_date_edit_text);
        mExplanationEditText = (EditText) findViewById(R.id.explanation_edit_text);
        mConclusionsEditText = (EditText) findViewById(R.id.conclusions_edit_text);

        backPress = (ImageButton) findViewById(R.id.back_press);
        mPlusButtonDiagnostics = (Button) findViewById(R.id.plus_button_diagnostics);
        mPlusButtonMedication = (Button) findViewById(R.id.plus_button_medication);
        checkBoxLayout = (LinearLayout) findViewById(R.id.checkbox_layout);
        int counter = 0;
        for (Services services : Appointments.getInstance().patientServices.get(id)) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(services.getServiceName());
            checkBox.setTextSize(16);
            checkBox.setTypeface(AppGlobals.typefaceNormal);
            checkBox.setId(services.getId());
            if (counter == 0) {
                checkBox.setChecked(true);
            }
            checkBoxLayout.addView(checkBox);
            counter++;
        }

        mDateEditText.setTypeface(AppGlobals.typefaceNormal);
        mTimeEditText.setTypeface(AppGlobals.typefaceNormal);
        mReturnDateEditText.setTypeface(AppGlobals.typefaceNormal);
        mExplanationEditText.setTypeface(AppGlobals.typefaceNormal);
        mConclusionsEditText.setTypeface(AppGlobals.typefaceNormal);

        backPress.setOnClickListener(this);
        mReturnDateEditText.setOnClickListener(this);
//        mDiagnosticsTextView.setOnClickListener(this);
        mDestinationSpinner.setOnItemSelectedListener(this);
//        mMedicationTextView.setOnClickListener(this);

        mPlusButtonDiagnostics.setOnClickListener(this);
        mPlusButtonMedication.setOnClickListener(this);

        mPatientsName.setText(mFname + " " + mLname);
        String years = Helpers.calculateAge(mAge);
        mPatientsAge.setText(years + " " + "years");
        mAppointmentReason.setText(mReason);
        mAppointmentReason.setEnabled(false);
        mDateEditText.setText(mDate);
        mTimeEditText.setText(Helpers.getTime24HourFormat());
        mTimeEditText.setEnabled(false);
        mDateEditText.setEnabled(false);
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

    @Override
    protected void onResume() {
        super.onResume();
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
                medicationDiagnosticsDialog(true);
                break;
            case R.id.plus_button_medication:
                medicationDiagnosticsDialog(false);
                break;
            case R.id.medication_TextView:
                break;
            case R.id.diagnostics_TextView:
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
                System.out.println(targets.getId());
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void medicationDiagnosticsDialog(final boolean value) {
        final Dialog dialog = new Dialog(DoctorsAppointment.this);
        dialog.setContentView(R.layout.medication_diagnostics_search_list);
        int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.95);
        int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.70);
        dialog.getWindow().setLayout(width, height);
        dialog.setCancelable(true);
        EditText searchEditText = (EditText) dialog.findViewById(R.id.search_edit_text);
        ImageButton arrowUpButton = (ImageButton) dialog.findViewById(R.id.arrow_up);
        ImageButton arrowDownButton = (ImageButton) dialog.findViewById(R.id.arrow_down);
        TextView okTextView = (TextView) dialog.findViewById(R.id.ok_textV_view);
        medicationDiagnosticListView = (ListView) dialog.
                findViewById(R.id.medication_diagnostic_search_list_view);
        if (value) {
            diagnosticAdapter = new DiagnosticAdapter(getApplicationContext(), diagnosticsList);
            medicationDiagnosticListView.setAdapter(diagnosticAdapter);
            medicationDiagnosticListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    DiagnosticMedication diagnosticMedication = medicationList.get(position);
//                if (mMedicationArrayList.contains(diagnosticMedication))
//                mMedicationArrayList.add(diagnosticMedication);
                }
            });
        } else {
            medicationAdapter = new MedicationAdapter(getApplicationContext(), medicationList);
            medicationDiagnosticListView.setAdapter(medicationAdapter);
            medicationDiagnosticListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (value) {
                        DiagnosticMedication diagnosticMedication = diagnosticsList.get(position);
//                if (mMedicationArrayList.contains(diagnosticMedication))
//                mMedicationArrayList.add(diagnosticMedication);
                    } else {

                    }

                }
            });
        }
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.i("TAG", charSequence.toString());
                if (value) {
                    if (!charSequence.toString().isEmpty()) {
                        searchListForDiagonistics = new ArrayList<>();
                        diagnosticAdapter = new DiagnosticAdapter(getApplicationContext(),
                                searchListForDiagonistics);
                        medicationDiagnosticListView.setAdapter(diagnosticAdapter);
                        for (DiagnosticMedication diagnosticMedication : diagnosticsList) {
                            if (StringUtils.containsIgnoreCase(diagnosticMedication.getDiagnosticMedication(),
                                    charSequence.toString()) || StringUtils.containsIgnoreCase(String.valueOf(diagnosticMedication.getId()),
                                    charSequence.toString()) ) {
                                searchListForDiagonistics.add(diagnosticMedication);
                                diagnosticAdapter.notifyDataSetChanged();

                            }
                        }
                    } else {
                        searchListForDiagonistics = new ArrayList<>();
                        diagnosticAdapter = new DiagnosticAdapter(getApplicationContext(),
                                diagnosticsList);
                        medicationDiagnosticListView.setAdapter(diagnosticAdapter);
                    }
                } else {
                    if (!charSequence.toString().isEmpty()) {
                        searchListForMedications = new ArrayList<>();
                        medicationAdapter = new MedicationAdapter(getApplicationContext(),
                                searchListForMedications);
                        medicationDiagnosticListView.setAdapter(medicationAdapter);
                        for (DiagnosticMedication diagnosticMedication : medicationList) {
                            if (StringUtils.containsIgnoreCase(diagnosticMedication.getDiagnosticMedication(),
                                    charSequence.toString()) || StringUtils.containsIgnoreCase(String.valueOf(diagnosticMedication.getId()),
                                    charSequence.toString()) ) {
                                searchListForMedications.add(diagnosticMedication);
                                medicationAdapter.notifyDataSetChanged();

                            }
                        }
                    } else {
                        searchListForMedications = new ArrayList<>();
                        medicationAdapter = new MedicationAdapter(getApplicationContext(),
                                medicationList);
                        medicationDiagnosticListView.setAdapter(medicationAdapter);
                    }
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
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
        okTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
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
                                        diagnosticsList.add(diagnosticMedication);
                                    }
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

    private void getMedications() {
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
                                        medicationList.add(diagnosticMedication);
                                    }
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

   private class DiagnosticAdapter extends ArrayAdapter {

        private ViewHolder viewHolder;
        private ArrayList<DiagnosticMedication> diagnosticMedications;

        public DiagnosticAdapter(Context context , ArrayList<DiagnosticMedication> diagnosticMedications) {
            super(context, R.layout.delegate_diagnostic);
            this.diagnosticMedications = diagnosticMedications;
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.delegate_diagnostic, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.idTextView = (TextView) convertView.findViewById(R.id.id_text_view);
                viewHolder.diagnosticListTextView = (TextView) convertView.findViewById(R.id.diagnostic_list_text_view);
                viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.check_box);
                viewHolder.idTextView.setTypeface(AppGlobals.typefaceNormal);
                viewHolder.diagnosticListTextView.setTypeface(AppGlobals.typefaceNormal);
                convertView.setTag(viewHolder);
                viewHolder.checkBox.setTag(position);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            final DiagnosticMedication diagnostic = diagnosticMedications.get(position);
            viewHolder.diagnosticListTextView.setText(diagnostic.getDiagnosticMedication());
            viewHolder.idTextView.setText(String.valueOf(diagnostic.getId()));
            viewHolder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    int pos = (int) viewHolder.checkBox.getTag();
                    Log.i("TAG", "position "+ position);
                    View checkBoxView = medicationDiagnosticListView.getChildAt(pos);
                    if (checkBoxView != null) {
                        if (b) {
                            selectedDiagnostic.put(position, diagnostic.getId());
                            selectedDiagnosticsList.add(diagnostic);
                        } else {
                            selectedDiagnostic.remove(position);
                            selectedDiagnosticsList.remove(diagnostic);
                        }
                    }
                }
            });
            Log.i("TAG", selectedDiagnostic.toString());
            if (selectedDiagnostic.containsKey(position)) {
                viewHolder.checkBox.setChecked(true);
            } else {
                viewHolder.checkBox.setChecked(false);
            }
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
            CheckBox checkBox;
        }
    }

   private class MedicationAdapter extends ArrayAdapter {

       private ViewHolder viewHolder;
       private ArrayList<DiagnosticMedication> diagnosticMedications;

       public MedicationAdapter(Context context,
                                ArrayList<DiagnosticMedication> diagnosticMedications) {
           super(context, R.layout.delegate_diagnostic);
           this.diagnosticMedications = diagnosticMedications;
       }

       @NonNull
       @Override
       public View getView(final int position, View convertView, ViewGroup parent) {
           if (convertView == null) {
               convertView = getLayoutInflater().inflate(R.layout.delegate_medication, parent, false);
               viewHolder = new ViewHolder();
               viewHolder.idTextView = (TextView) convertView.findViewById(R.id.id_text_view);
               viewHolder.diagnosticListTextView = (TextView) convertView.findViewById(R.id.diagnostic_list_text_view);
               viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.check_box);
               viewHolder.idTextView.setTypeface(AppGlobals.typefaceNormal);
               viewHolder.diagnosticListTextView.setTypeface(AppGlobals.typefaceNormal);
               convertView.setTag(viewHolder);
               viewHolder.checkBox.setTag(position);
           } else {
               viewHolder = (ViewHolder) convertView.getTag();
           }
           final DiagnosticMedication diagnostic = diagnosticMedications.get(position);
           viewHolder.diagnosticListTextView.setText(diagnostic.getDiagnosticMedication());
           viewHolder.idTextView.setText(String.valueOf(diagnostic.getId()));
           if (selectedMedication.containsKey(position)) {
               viewHolder.checkBox.setChecked(true);
           } else {
               viewHolder.checkBox.setChecked(false);
           }
           viewHolder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
               @Override
               public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                   int pos = (int) viewHolder.checkBox.getTag();
                   View checkBoxView = medicationDiagnosticListView.getChildAt(pos);
                   if (checkBoxView != null) {
                       if (b) {
                           selectedMedication.put(position, diagnostic.getId());
                           selectedMedicationList.add(diagnostic);
                       } else {
                           selectedMedication.remove(position);
                           selectedMedicationList.remove(diagnostic);
                       }
                   }
               }
           });
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
           CheckBox checkBox;
       }
   }

   private class DiagonisticSpinnerAdapter extends BaseAdapter {

       private ViewHolder viewHolder;
       private ArrayList<DiagnosticMedication> diagnosticMedicationArrayList;

       public DiagonisticSpinnerAdapter(ArrayList<DiagnosticMedication> diagnosticMedicationArrayList) {
           this.diagnosticMedicationArrayList = diagnosticMedicationArrayList;
       }

       @Override
       public int getCount() {
           return diagnosticMedicationArrayList.size();
       }

       @Override
       public Object getItem(int i) {
           return null;
       }

       @Override
       public long getItemId(int i) {
           return 0;
       }

       @Override
       public View getView(int i, View view, ViewGroup viewGroup) {
           if (view == null) {
               view = getLayoutInflater().inflate(R.layout.delegate_view, viewGroup, false);
               viewHolder = new ViewHolder();
               viewHolder.id = (TextView) view.findViewById(R.id.id_text_view);
               viewHolder.textView = (TextView) view.findViewById(R.id.text);
               view.setTag(viewHolder);
           } else {
               viewHolder = (ViewHolder) view.getTag();
               DiagnosticMedication diagnosticMedication = diagnosticMedicationArrayList.get(i);
               viewHolder.id.setText(String.valueOf(diagnosticMedication.getId()));
               viewHolder.textView.setText(diagnosticMedication.getDiagnosticMedication());
           }

           return view;
       }

       private class ViewHolder {
           TextView id;
           TextView textView;

       }
   }
}
