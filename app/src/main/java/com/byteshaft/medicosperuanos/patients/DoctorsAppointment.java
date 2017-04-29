package com.byteshaft.medicosperuanos.patients;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
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

import com.byteshaft.medicosperuanos.R;
import com.byteshaft.medicosperuanos.adapters.TargetsAdapter;
import com.byteshaft.medicosperuanos.gettersetter.DiagnosticMedication;
import com.byteshaft.medicosperuanos.gettersetter.Services;
import com.byteshaft.medicosperuanos.gettersetter.Targets;
import com.byteshaft.medicosperuanos.utils.AppGlobals;
import com.byteshaft.medicosperuanos.utils.Helpers;
import com.byteshaft.requests.HttpRequest;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfWriter;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by husnain on 2/23/17.
 */

public class DoctorsAppointment extends AppCompatActivity implements View.OnClickListener,
        DatePickerDialog.OnDateSetListener, HttpRequest.OnReadyStateChangeListener,
        HttpRequest.OnErrorListener, AdapterView.OnItemSelectedListener {

    private Spinner mDiagnosticsSpinner;
    private Spinner mMedicationSpinner;
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
    private ArrayList<DiagnosticMedication> searchListForDiagnostics;
    private ArrayList<DiagnosticMedication> searchListForMedications;
    private DiagnosticSpinnerAdapter diagnosticSpinnerAdapter;
    private MedicationSpinnerAdapter medicationSpinnerAdapter;

    private TextView quantityTextView;
    private TextView saveButton;
    private int selectedTargetId = -1;

    private static final int REQUEST_CODE = 123;
    private ArrayList<String> imagesArray;

    private String mPath;

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
        Log.i("TAG", " id " + id);
        ArrayList<Services> arrayList = (ArrayList<Services>) getIntent().getSerializableExtra("services");

        diagnosticsList = new ArrayList<>();
        medicationList = new ArrayList<>();
        targetsArrayList = new ArrayList<>();
        selectedDiagnosticsList = new ArrayList<>();
        selectedMedicationList = new ArrayList<>();
        selectedDiagnostic = new HashMap<>();
        selectedMedication = new HashMap<>();
        imagesArray = new ArrayList<>();

        mPatientsName = (TextView) view.findViewById(R.id.action_bar_title);
        mPatientsAge = (TextView) view.findViewById(R.id.action_bar_age);
        mAppointmentReason = (EditText) findViewById(R.id.appointment_reason_editText);
        mDiagnosticsSpinner = (Spinner) findViewById(R.id.diagnostics_spinner);
        mMedicationSpinner = (Spinner) findViewById(R.id.medication_spinner);
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
        quantityTextView = (TextView) findViewById(R.id.qty_textview);
        saveButton = (AppCompatButton) findViewById(R.id.save_button);
        saveButton.setOnClickListener(this);
        int counter = 0;
        for (Services services : arrayList) {
            Log.i("TAG", "services");
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
        mDestinationSpinner.setOnItemSelectedListener(this);

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
//        Fresco.initialize(getApplicationContext());
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
        diagnosticSpinnerAdapter = new DiagnosticSpinnerAdapter(selectedDiagnosticsList);
        mDiagnosticsSpinner.setAdapter(diagnosticSpinnerAdapter);
        medicationSpinnerAdapter = new MedicationSpinnerAdapter(selectedMedicationList);
        mMedicationSpinner.setAdapter(medicationSpinnerAdapter);

        mMedicationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                DiagnosticMedication diagnosticMedication = selectedMedicationList.get(i);
                quantityTextView.setText(String.valueOf(diagnosticMedication.getQuantity()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mDestinationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Targets targets = targetsArrayList.get(i);
                selectedTargetId = targets.getId();

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
// get selected images from selector
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
//                imagesArray = data.getStringArrayListExtra(SelectorSettings.SELECTOR_RESULTS);
                assert imagesArray != null;

                // show results in textview
                StringBuffer sb = new StringBuffer();
                sb.append(String.format("Totally %d images selected:", imagesArray.size())).append("\n");
                for (String result : imagesArray) {
                    sb.append(result).append("\n");
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
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
                takeScreenshot();
                break;
            case R.id.attach_icon:
                ImageToPdf();
//                Intent intent = new Intent(DoctorsAppointment.this, ImagesSelectorActivity.class);
//                intent.putExtra(SelectorSettings.SELECTOR_MAX_IMAGE_NUMBER, 5);
//                intent.putExtra(SelectorSettings.SELECTOR_MIN_IMAGE_SIZE, 100000);
//                intent.putExtra(SelectorSettings.SELECTOR_SHOW_CAMERA, false);
//                intent.putStringArrayListExtra(SelectorSettings.SELECTOR_INITIAL_SELECTED_LIST, imagesArray);
//                startActivityForResult(intent, REQUEST_CODE);
                break;

        }
        return super.onOptionsItemSelected(item);
    }


    private void takeScreenshot() {
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

        try {
            mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg";

            View v1 = getWindow().getDecorView().getRootView();
            v1.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);

            File imageFile = new File(mPath);

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();
            openScreenshot(imageFile);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void openScreenshot(File imageFile) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(imageFile);
        intent.setDataAndType(uri, "image/*");
        startActivity(intent);
    }

    private void ImageToPdf() {
        Document document = new Document();
        String path = android.os.Environment.getExternalStorageDirectory().toString();
        try {
            PdfWriter.getInstance(document, new FileOutputStream(path + "/medicosperuanos.pdf"));
        } catch (DocumentException | FileNotFoundException e) {
            e.printStackTrace();
        }
        document.open();
        Image image = null;
        try {
            image = Image.getInstance(mPath);
        } catch (BadElementException | IOException e) {
            e.printStackTrace();
        }
        try {
            float width = document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin();
            float height = document.getPageSize().getHeight() - document.topMargin() - document.bottomMargin();
            image.scaleToFit(width, height);
            document.add(new Paragraph(path + "medicosperuanos screen capture"));
            document.add(image);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        document.close();
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
            case R.id.medication_spinner:
                break;
            case R.id.diagnostics_spinner:
                break;
            case R.id.save_button:
                sendAttentionData(id, mConclusionsEditText.getText().toString(), mDateEditText.getText().toString(),
                        mReturnDateEditText.getText().toString(),
                        String.valueOf(selectedTargetId), mExplanationEditText.getText().toString(),
                        mTimeEditText.getText().toString());
                break;
        }
    }

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
            mReturnDateEditText.setText(i2 + "/" + i1 + "/" + i);
            isSetForReturn = false;

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
                                break;


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
        switch (readyState) {
            case HttpRequest.STATE_DONE:
                switch (request.getStatus()) {
                    case HttpURLConnection.HTTP_OK:
                        Log.i("TAG", request.getResponseText());
                        break;
                    case HttpURLConnection.HTTP_BAD_REQUEST:
                        Log.i("TAG", request.getResponseText());
                        break;
                }
        }

    }

    private void sendAttentionData(int appointmentId, String conclusion, String date, String dateOfReturn,
                              String destination, String exploration, String time) {
        request = new HttpRequest(this);
        request.setOnReadyStateChangeListener(this);
        request.setOnErrorListener(this);
        request.open("POST", String.format("%sdoctor/appointments/%s/attention ", AppGlobals.BASE_URL, appointmentId));
        request.setRequestHeader("Authorization", "Token " +
                AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN));
        Log.i("TAG", "work " + getAttentionsData(conclusion, date, dateOfReturn, destination, exploration, time));
        request.send(getAttentionsData(conclusion, date, dateOfReturn, destination, exploration, time));
    }


    private String getAttentionsData(String conclusion, String date, String dateOfReturn,
                                     String destination, String exploration, String time) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("conclusion", conclusion);
            jsonObject.put("date", date);
            jsonObject.put("date_of_return", dateOfReturn);
            jsonObject.put("destination", destination);
            JSONArray jsonArray = new JSONArray();
            for (DiagnosticMedication diagnosticMedication : selectedDiagnosticsList) {
                JSONObject diagnosticObject = new JSONObject();
//                diagnosticObject.put("diagnostics", diagnosticMedication.getId());
                jsonArray.put(diagnosticMedication.getId());
            }
            jsonObject.put("diagnostics", jsonArray);
            JSONArray treatmentArray = new JSONArray();
            for (DiagnosticMedication diagnosticMedication : selectedMedicationList) {
                JSONObject treatmentObject = new JSONObject();
                treatmentObject.put("treatment", diagnosticMedication.getId());
                treatmentObject.put("quantity", diagnosticMedication.getQuantity());
                treatmentArray.put(treatmentObject);
            }
            jsonObject.put("treatments", treatmentArray);
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
        } else {
            medicationAdapter = new MedicationAdapter(getApplicationContext(), medicationList);
            medicationDiagnosticListView.setAdapter(medicationAdapter);
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
                        searchListForDiagnostics = new ArrayList<>();
                        diagnosticAdapter = new DiagnosticAdapter(getApplicationContext(),
                                searchListForDiagnostics);
                        medicationDiagnosticListView.setAdapter(diagnosticAdapter);
                        for (DiagnosticMedication diagnosticMedication : diagnosticsList) {
                            if (StringUtils.containsIgnoreCase(diagnosticMedication.getDiagnosticMedication(),
                                    charSequence.toString()) || StringUtils.containsIgnoreCase(String.valueOf(diagnosticMedication.getId()),
                                    charSequence.toString()) ) {
                                searchListForDiagnostics.add(diagnosticMedication);
                                diagnosticAdapter.notifyDataSetChanged();

                            }
                        }
                    } else {
                        searchListForDiagnostics = new ArrayList<>();
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
                medicationSpinnerAdapter.notifyDataSetChanged();
                diagnosticSpinnerAdapter.notifyDataSetChanged();
                mDiagnosticsSpinner.setSelection(0);
                mMedicationSpinner.setSelection(0);
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
               viewHolder.minus = (ImageButton) convertView.findViewById(R.id.minus);
               viewHolder.quantity = (TextView) convertView.findViewById(R.id.quantity);
               viewHolder.add = (ImageButton) convertView.findViewById(R.id.plus);
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
           viewHolder.quantity.setText(String.valueOf(diagnostic.getQuantity()));
           viewHolder.add.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   Log.i("TAG", "click");
                   diagnostic.setQuantity(diagnostic.getQuantity()+1);
                   notifyDataSetChanged();
               }
           });
           viewHolder.minus.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   if (diagnostic.getQuantity() > 0) {
                       diagnostic.setQuantity(diagnostic.getQuantity() - 1);
                       notifyDataSetChanged();
                   }

               }
           });
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
           ImageButton minus;
           ImageButton add;
           TextView quantity;

       }
   }

   private class DiagnosticSpinnerAdapter extends BaseAdapter {

       private ViewHolder viewHolder;
       private ArrayList<DiagnosticMedication> diagnosticMedicationArrayList;

       public DiagnosticSpinnerAdapter(ArrayList<DiagnosticMedication> diagnosticMedicationArrayList) {
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
               view = getLayoutInflater().inflate(R.layout.delegate_view_diagonistics, viewGroup, false);
               viewHolder = new ViewHolder();
               viewHolder.id = (TextView) view.findViewById(R.id.id_text_view);
               viewHolder.textView = (TextView) view.findViewById(R.id.text);
               view.setTag(viewHolder);
           } else {
               viewHolder = (ViewHolder) view.getTag();
           }
           DiagnosticMedication diagnosticMedication = diagnosticMedicationArrayList.get(i);
           viewHolder.id.setText(String.valueOf(diagnosticMedication.getId()));
           viewHolder.textView.setText(diagnosticMedication.getDiagnosticMedication());
           return view;
       }

       private class ViewHolder {
           TextView id;
           TextView textView;

       }
   }

    private class MedicationSpinnerAdapter extends BaseAdapter {

        private ViewHolder viewHolder;
        private ArrayList<DiagnosticMedication> diagnosticMedicationArrayList;

        public MedicationSpinnerAdapter(ArrayList<DiagnosticMedication> diagnosticMedicationArrayList) {
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
                view = getLayoutInflater().inflate(R.layout.delegate_view_medication, viewGroup, false);
                viewHolder = new ViewHolder();
                viewHolder.id = (TextView) view.findViewById(R.id.id_text_view);
                viewHolder.textView = (TextView) view.findViewById(R.id.text);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            DiagnosticMedication diagnosticMedication = diagnosticMedicationArrayList.get(i);
            viewHolder.id.setText(String.valueOf(diagnosticMedication.getId()));
            viewHolder.textView.setText(diagnosticMedication.getDiagnosticMedication());
            return view;
        }

        private class ViewHolder {
            TextView id;
            TextView textView;

        }
    }
}
