package com.byteshaft.medicosperuanos.doctors;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
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
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.byteshaft.medicosperuanos.R;
import com.byteshaft.medicosperuanos.adapters.ViewHolder;
import com.byteshaft.medicosperuanos.gettersetter.DiagnosticMedication;
import com.byteshaft.medicosperuanos.gettersetter.Services;
import com.byteshaft.medicosperuanos.gettersetter.Targets;
import com.byteshaft.medicosperuanos.utils.AppGlobals;
import com.byteshaft.medicosperuanos.utils.Helpers;
import com.byteshaft.requests.FormData;
import com.byteshaft.requests.HttpRequest;
import com.darsh.multipleimageselect.activities.AlbumSelectActivity;
import com.darsh.multipleimageselect.helpers.Constants;
import com.github.lzyzsd.circleprogress.DonutProgress;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
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


public class DoctorsAppointment extends AppCompatActivity implements View.OnClickListener,
        DatePickerDialog.OnDateSetListener, HttpRequest.OnReadyStateChangeListener,
        HttpRequest.OnErrorListener, AdapterView.OnItemSelectedListener,
        HttpRequest.OnFileUploadProgressListener, CompoundButton.OnCheckedChangeListener {

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
    private int position = -1;
    private int updateId = -1;
    private ArrayList<Services> arrayList;
    private ArrayList<String> imagesArrayList;
    private static String method = "POST";
    private String photo1 = "";
    private String photo2 = "";
    private String photo3 = "";
    private String photo4 = "";
    private ArrayList<Integer> providedServicesIds;
    private int totalImagesCounter = 0;
    public static ArrayList<String> removedImages;
    private MenuItem showImages;
    private DonutProgress donutProgress;
    private AlertDialog alertDialog;
    private AlertDialog.Builder alertDialogBuilder;
    private ProgressBar progressBar;

    public static HashMap<String, String> photosHashMap;

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
        position = getIntent().getIntExtra("position", -1);
        arrayList = (ArrayList<Services>) getIntent().getSerializableExtra("services");

        providedServicesIds = new ArrayList<>();
        removedImages = new ArrayList<>();
        diagnosticsList = new ArrayList<>();
        medicationList = new ArrayList<>();
        targetsArrayList = new ArrayList<>();
        selectedDiagnosticsList = new ArrayList<>();
        selectedMedicationList = new ArrayList<>();
        selectedDiagnostic = new HashMap<>();
        selectedMedication = new HashMap<>();
        imagesArray = new ArrayList<>();
        photosHashMap = new HashMap<>();

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
        quantityTextView = (TextView) findViewById(R.id.qty_text_view);
        saveButton = (AppCompatButton) findViewById(R.id.save_button);
        saveButton.setOnClickListener(this);
        providedServicesIds.add(arrayList.get(0).getId());
        showCheckbox(arrayList);

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

    private void showCheckbox(ArrayList<Services> arrayList) {
        int counter = 0;
        for (Services services : arrayList) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(services.getServiceName());
            checkBox.setTextSize(16);
            checkBox.setTag(services.getId());
            checkBox.setTypeface(AppGlobals.typefaceNormal);
            checkBox.setId(services.getId());
            if (counter == 0) {
                checkBox.setChecked(true);
            }
            checkBox.setOnCheckedChangeListener(this);
            checkBoxLayout.addView(checkBox);
            counter++;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (b) {
                providedServicesIds.add(compoundButton.getId());
        } else {
            if (providedServicesIds.contains(compoundButton.getId())) {
                int index = providedServicesIds.indexOf(compoundButton.getId());
                providedServicesIds.remove(index);
            }

        }


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
        if (menu != null) {
            showImages = menu.findItem(R.id.view_images);
            showImages.setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.view_images:
                startActivity(new Intent(this, SelectedImages.class));
                break;
            case R.id.take_screenshot:
                takeScreenshot();
                break;
            case R.id.attach_icon:
                Intent intent = new Intent(this, AlbumSelectActivity.class);
                int limit = 4;
                if (method.equals("PUT")) {
                    limit = 4 - DoctorsAppointment.photosHashMap.size();
                }
                intent.putExtra(Constants.INTENT_EXTRA_LIMIT, limit);
                startActivityForResult(intent, Constants.REQUEST_CODE);
//                ImageToPdf();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            //The array list has the image paths of the selected images
            imagesArrayList = new ArrayList<>();
            ArrayList<com.darsh.multipleimageselect.models.Image> images =
                    data.getParcelableArrayListExtra(Constants.INTENT_EXTRA_IMAGES);
            for (com.darsh.multipleimageselect.models.Image image : images) {
                imagesArrayList.add(image.path);
            }

        }
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
            screenShotShareAndImageToPdfDialog();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void imageToPdf() {
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

    private void screenShotShareAndImageToPdfDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Share&Export!");
        alertDialogBuilder.setMessage("You can Share screen capture and Export to Pdf!")
                .setCancelable(true).setPositiveButton("Share Image",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        Uri uri = Uri.parse("file://" + mPath);
                        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                        shareIntent.setType("image/jpeg");
                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(Intent.createChooser(shareIntent, "Share image File"));
                    }
                });
        alertDialogBuilder.setNegativeButton("Convert to Pdf", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                imageToPdf();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.return_date_edit_text:
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
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
        mReturnDateEditText.setText(i2 + "/" + (i1 + 1) + "/" + i);
        System.out.println(i2 + "/" + (i1 + 1) + "/" + i);

    }

    private void getAppointmentDetails() {
        HttpRequest appointmentDetails = new HttpRequest(this);
        appointmentDetails.setOnReadyStateChangeListener(new HttpRequest.OnReadyStateChangeListener() {
            @Override
            public void onReadyStateChange(HttpRequest request, int readyState) {
                switch (readyState) {
                    case HttpRequest.STATE_DONE:
                        switch (request.getStatus()) {
                            case HttpURLConnection.HTTP_OK:
                                if (!request.getResponseText().trim().isEmpty()) {
                                    try {
                                        JSONObject jsonObject = new JSONObject(request.getResponseText());
                                        updateId = jsonObject.getInt("id");
                                        JSONObject destinationObject = jsonObject.getJSONObject("destination");
                                        selectedTargetId = destinationObject.getInt("id");
                                        JSONArray jsonArray = jsonObject.getJSONArray("diagnostics");
                                        JSONArray providedServicesArray = jsonObject.getJSONArray("services_provided");
                                        for (int i = 0; i < providedServicesArray.length(); i++) {
                                            JSONObject serviceObject = providedServicesArray.getJSONObject(i);
                                            if (!providedServicesIds.contains(serviceObject.getInt("id"))) {
                                                providedServicesIds.add(serviceObject.getInt("id"));
                                            }
                                        }
                                        int count = checkBoxLayout.getChildCount();
                                        View view;
                                        for(int i=0; i<count; i++) {
                                            view = ((LinearLayout ) checkBoxLayout).getChildAt(i);
                                            if (view instanceof CheckBox)
                                            if (providedServicesIds.contains(view.getId())) {
                                                CheckBox checkbox = (CheckBox) view;
                                                checkbox.setChecked(true);
                                            }

                                            //do something with your child element
                                        }

                                        for (int i = 0; i < jsonArray.length(); i++) {
                                            JSONObject diagnosticObject = jsonArray.getJSONObject(i);
                                            DiagnosticMedication diagnosticMedication =
                                                    new DiagnosticMedication();
                                            diagnosticMedication.setId(diagnosticObject.getInt("id"));
                                            diagnosticMedication.setDiagnosticMedication(diagnosticObject
                                                    .getString("name"));
                                            selectedDiagnosticsList.add(diagnosticMedication);
                                        }
                                        JSONObject appointmentObject = jsonObject.getJSONObject("appointment");
                                        JSONArray services = appointmentObject.getJSONArray("services");
                                        arrayList = new ArrayList<>();
                                        for (int k = 0; k < services.length(); k++) {
                                            JSONObject serviceObject = services.getJSONObject(k);
                                            Services service = new Services();
                                            service.setId(serviceObject.getInt("id"));
                                            service.setPrice(String.valueOf(serviceObject.getInt("price")));
                                            service.setDescription(serviceObject.getString("description"));
                                            JSONObject serviceMainObject = serviceObject.getJSONObject("service");
                                            service.setServiceName(serviceMainObject.getString("name"));
                                            arrayList.add(service);
                                        }
                                        String exploration = jsonObject.getString("exploration");
                                        String dateOfReturn = jsonObject.getString("date_of_return");
                                        String conclusion = jsonObject.getString("conclusion");
                                        if (!jsonObject.isNull("photo1")) {
                                            photo1 = jsonObject.getString("photo1");
                                        }
                                        if (!jsonObject.isNull("photo2")) {
                                            photo2 = jsonObject.getString("photo2");
                                        }
                                        if (!jsonObject.isNull("photo2")) {
                                            photo3 = jsonObject.getString("photo3");
                                        }
                                        if (!jsonObject.isNull("photo4")) {
                                            photo4 = jsonObject.getString("photo4");
                                        }
                                        if (photo1 != null && !photo1.trim().isEmpty()) {
                                            totalImagesCounter = totalImagesCounter+1;
                                            photosHashMap.put("photo1", photo1.replace("http://localhost", AppGlobals.SERVER_IP));
                                        }
                                        if (photo2 != null &&  !photo2.trim().isEmpty())  {
                                            totalImagesCounter = totalImagesCounter+1;
                                            photosHashMap.put("photo2", photo2.replace("http://localhost", AppGlobals.SERVER_IP));
                                        }
                                        if (photo3 != null && !photo3.trim().isEmpty()) {
                                            totalImagesCounter = totalImagesCounter+1;
                                            photosHashMap.put("photo3", photo3.replace("http://localhost", AppGlobals.SERVER_IP));
                                        }
                                        if (photo4 != null && !photo4.trim().isEmpty()) {
                                            totalImagesCounter = totalImagesCounter+1;
                                            photosHashMap.put("photo4", photo4.replace("http://localhost", AppGlobals.SERVER_IP));
                                        }
                                        Log.i("TAG", "photos " + photosHashMap);
                                        if (totalImagesCounter > 0) {
                                            showImages.setVisible(true);
                                        } else {
                                            showImages.setVisible(false);
                                        }
                                        mExplanationEditText.setText(exploration);
                                        mConclusionsEditText.setText(conclusion);
                                        mReturnDateEditText.setText(dateOfReturn);
                                        JSONArray treatmentsArray = jsonObject.getJSONArray("treatments");
                                        for (int i = 0; i < treatmentsArray.length(); i++) {
                                            JSONObject treatment = treatmentsArray.getJSONObject(i);
                                            DiagnosticMedication diagnosticMedication = new DiagnosticMedication();
                                            diagnosticMedication.setQuantity(treatment.getInt("quantity"));
                                            JSONObject treatmentDetail = treatment.getJSONObject("treatment");
                                            diagnosticMedication.setId(treatmentDetail.getInt("id"));
                                            diagnosticMedication.setDiagnosticMedication(treatmentDetail.getString("name"));
                                            selectedMedicationList.add(diagnosticMedication);
                                        }
                                        for (int k = 0; k < diagnosticsList.size(); k++) {
                                            DiagnosticMedication diagnosticMedication =
                                                    diagnosticsList.get(k);
                                            for (DiagnosticMedication diagnosticMedication1 : selectedDiagnosticsList) {
                                                if (diagnosticMedication.getId() == diagnosticMedication1.getId()) {
                                                    selectedDiagnostic.put(k, diagnosticMedication.getId());
                                                }
                                            }

                                        }
                                        for (int o = 0; o < medicationList.size(); o++) {
                                            DiagnosticMedication diagnosticMedication =
                                                    medicationList.get(o);
                                            for (DiagnosticMedication diagnosticMedication1 : selectedMedicationList) {
                                                if (diagnosticMedication.getId() == diagnosticMedication1.getId()) {
                                                    selectedMedication.put(o, diagnosticMedication.getId());
                                                }
                                            }
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    method = "PUT";
                                    saveButton.setText("Update");
                                }
                                break;
                            case HttpURLConnection.HTTP_NOT_FOUND:
                                System.out.println("rana" + request.getResponseText());
                        }
                }
            }
        });
        appointmentDetails.setOnErrorListener(new HttpRequest.OnErrorListener() {
            @Override
            public void onError(HttpRequest request, int readyState, short error, Exception exception) {
                exception.printStackTrace();
            }
        });
        String url = String.format("%sdoctor/appointments/%s/attention", AppGlobals.BASE_URL, id);
        appointmentDetails.open("GET", url);
        appointmentDetails.setRequestHeader("Authorization", "Token " +
                AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN));
        appointmentDetails.send();
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
                                    targetsAdapter = new TargetsAdapter(targetsArrayList);
                                    mDestinationSpinner.setAdapter(targetsAdapter);
                                    for (int i = 0; i < targetsArrayList.size(); i++) {
                                        Targets targets = targetsArrayList.get(i);
                                        if (targets.getId() == selectedTargetId) {
                                            mDestinationSpinner.setSelection(i);
                                        }
                                    }
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

    private void sendAttentionData(int appointmentId, String conclusion, String date, String dateOfReturn,
                                   String destination, String exploration, String time) {
        request = new HttpRequest(this);
        request.setOnReadyStateChangeListener(this);
        request.setOnErrorListener(this);
        request.setOnFileUploadProgressListener(this);
        request.open(method, String.format("%sdoctor/appointments/%s/attention", AppGlobals.BASE_URL, appointmentId));
        request.setRequestHeader("Authorization", "Token " +
                AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN));
        try {
//            getAttentionsData(conclusion, date, dateOfReturn, destination, exploration, time);
            request.send(getAttentionsData(conclusion, date, dateOfReturn, destination, exploration, time));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (imagesArrayList != null && imagesArrayList.size() < 1) {
            alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle(getResources().getString(R.string.updating_attention));
            alertDialogBuilder.setCancelable(false);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.progress_alert_dialog, null);
            alertDialogBuilder.setView(dialogView);
            donutProgress = (DonutProgress) dialogView.findViewById(R.id.upload_progress);
            alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }


    @Override
    public void onError(HttpRequest request, int readyState, short error, Exception exception) {
        switch (readyState) {
            case HttpRequest.ERROR_CONNECTION_TIMED_OUT:
                Helpers.showSnackBar(findViewById(android.R.id.content), "connection time out");
                break;
            case HttpRequest.ERROR_NETWORK_UNREACHABLE:
                Helpers.showSnackBar(findViewById(android.R.id.content),
                        getResources().getString(R.string.network_unreachable));
                break;
        }

    }

    @Override
    public void onFileUploadProgress(HttpRequest request, File file, long loaded, long total) {
        double progress = (loaded / (double) total) * 100;
        if ((int) progress == 100) {
            if (alertDialog != null) {
                donutProgress.setProgress(100);
                alertDialog.dismiss();
            }
            alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle(getResources().getString(R.string.finishing_up));
            alertDialogBuilder.setCancelable(false);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.finishingup_dialog, null);
            alertDialogBuilder.setView(dialogView);
            progressBar = (ProgressBar) dialogView.findViewById(R.id.progress_bar);
            alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }

    }

    @Override
    public void onReadyStateChange(HttpRequest request, int readyState) {
        switch (readyState) {
            case HttpRequest.STATE_DONE:
                Log.i("TAG", request.getResponseURL());
                switch (request.getStatus()) {
                    case HttpURLConnection.HTTP_OK:
                        alertDialog.dismiss();
                        Log.i("TAG", request.getResponseText());
                        finish();
                        break;
                    case HttpURLConnection.HTTP_BAD_REQUEST:
                        Log.i("TAG", request.getResponseText());
                        break;
                    case HttpURLConnection.HTTP_CREATED:
                        Appointments.getInstance().updateAppointmentStatus(AppGlobals.ATTENDED, id, position);
                        finish();
                        break;
                }
        }

    }

    private FormData getAttentionsData(String conclusion, String date, String dateOfReturn,
                                       String destination, String exploration, String time) throws JSONException {
        FormData data = new FormData();
        data.append(FormData.TYPE_CONTENT_TEXT, "conclusion", conclusion);
        data.append(FormData.TYPE_CONTENT_TEXT, "date", date);
        data.append(FormData.TYPE_CONTENT_TEXT, "date_of_return", dateOfReturn);
        data.append(FormData.TYPE_CONTENT_TEXT, "destination", destination);
        JSONArray jsonArray = new JSONArray();
        for (DiagnosticMedication diagnosticMedication : selectedDiagnosticsList) {
            jsonArray.put(diagnosticMedication.getId());
        }
        data.append(FormData.TYPE_CONTENT_JSON, "diagnostics", jsonArray.toString());
        JSONArray treatmentArray = new JSONArray();
        for (DiagnosticMedication diagnosticMedication : selectedMedicationList) {
            JSONObject treatmentObject = new JSONObject();
            treatmentObject.put("treatment", diagnosticMedication.getId());
            treatmentObject.put("quantity", diagnosticMedication.getQuantity());
            treatmentArray.put(treatmentObject);
        }
        data.append(FormData.TYPE_CONTENT_JSON, "treatments", treatmentArray.toString());
        data.append(FormData.TYPE_CONTENT_JSON, "services_provided", providedServicesIds.toString());
        data.append(FormData.TYPE_CONTENT_TEXT, "exploration", exploration);
        data.append(FormData.TYPE_CONTENT_TEXT, "time", time);
        if (method.equals("PUT")) {
            if (totalImagesCounter == 0) {
                int imagesCounter = 1;
                if (imagesArrayList != null) {
                    for (String path : imagesArrayList) {
                        data.append(FormData.TYPE_CONTENT_FILE, "photo" + imagesCounter, path);
                        imagesCounter++;
                    }
                }

            } else if (totalImagesCounter == 4) {
                if (imagesArrayList != null) {
                    for (int i = 1; i < imagesArrayList.size(); i++) {
                        String name = "photo" + removedImages.get(i);
                        data.append(FormData.TYPE_CONTENT_FILE, name, imagesArrayList.get(i));
                    }
                }
            } else if (totalImagesCounter < 4) {
                if (imagesArrayList != null) {
                    Log.i("NAME", "images Array size" + imagesArrayList.size());
                    Log.i("NAME", "Photo Array size" + photosHashMap.size());
                    int counter = photosHashMap.size();
                    for (int i = 0; i < (imagesArrayList.size()); i++) {
                        String name;
                        if (i < removedImages.size() && removedImages.size() > 0) {
                            name = "photo" + removedImages.get(i) + 1;
                            Log.i("IF", "name" + name);
                        } else {
                            counter = counter+1;
                            name = "photo" + (counter);
                            Log.i("else", "name" + name);
                        }
                        Log.i("NAME", "name" + name);
                        Log.i("NAME", "file" + imagesArrayList.get(i));
                        data.append(FormData.TYPE_CONTENT_FILE, name, imagesArrayList.get(i));
                    }
                }

            }

        } else {
            int imagesCounter = 1;
            if (imagesArrayList != null) {
                for (String path : imagesArrayList) {
                    data.append(FormData.TYPE_CONTENT_FILE, "photo" + imagesCounter, path);
                }
            }
        }
        return data;
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
        Button okTextView = (Button) dialog.findViewById(R.id.ok_textV_view);
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
                if (value) {
                    if (!charSequence.toString().isEmpty()) {
                        searchListForDiagnostics = new ArrayList<>();
                        diagnosticAdapter = new DiagnosticAdapter(getApplicationContext(),
                                searchListForDiagnostics);
                        medicationDiagnosticListView.setAdapter(diagnosticAdapter);
                        for (DiagnosticMedication diagnosticMedication : diagnosticsList) {
                            if (StringUtils.containsIgnoreCase(diagnosticMedication.getDiagnosticMedication(),
                                    charSequence.toString()) || StringUtils.containsIgnoreCase(String.valueOf(diagnosticMedication.getId()),
                                    charSequence.toString())) {
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
                                    charSequence.toString())) {
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
                                    System.out.println(treatmentsArray + "treatment");
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
                                getAppointmentDetails();

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

        public DiagnosticAdapter(Context context, ArrayList<DiagnosticMedication> diagnosticMedications) {
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
                viewHolder.diagnosticListTextView.setTypeface(AppGlobals.robotoBold);
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
                viewHolder.diagnosticListTextView.setTypeface(AppGlobals.robotoBold);
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
                    diagnostic.setQuantity(diagnostic.getQuantity() + 1);
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

    private class TargetsAdapter extends BaseAdapter {

        private ViewHolder viewHolder;
        private ArrayList<Targets> targetsArrayList;

        public TargetsAdapter(ArrayList<Targets> targetsArrayList) {
            this.targetsArrayList = targetsArrayList;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.delegate_spinner, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.spinnerText = (TextView) convertView.findViewById(R.id.spinner_text);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            Targets targets = targetsArrayList.get(position);
            viewHolder.spinnerText.setText(targets.getName());
            return convertView;
        }

        @Override
        public int getCount() {
            return targetsArrayList.size();
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

}