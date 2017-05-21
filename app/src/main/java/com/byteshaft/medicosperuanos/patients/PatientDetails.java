package com.byteshaft.medicosperuanos.patients;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.byteshaft.medicosperuanos.R;
import com.byteshaft.medicosperuanos.gettersetter.Services;
import com.byteshaft.medicosperuanos.messages.ConversationActivity;
import com.byteshaft.medicosperuanos.utils.AppGlobals;
import com.byteshaft.medicosperuanos.utils.Helpers;
import com.byteshaft.requests.HttpRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class PatientDetails extends AppCompatActivity implements View.OnClickListener {

    private TextView patientName;
    private TextView patientAge;
    private ImageButton callButton;
    private ImageButton chatButton;
    private Button appointmentButton;
    private EditText docId;
    private EditText birthDate;
    private EditText patientAddress;
    private CircleImageView circleImageView;
    private EditText phonePrimary;
    private EditText phoneSecondary;
    private EditText stateEditText;
    private EditText cityEditText;
    private EditText insuranceCarrierEditText;
    private EditText emergencyContact;

    private String patientNameString;
    private String patientAgeString;
    private String docIdString;
    private String patientAddressString;
    private String circleImageViewString;
    private String phonePrimaryString;
    private String phoneSecondaryString;
    private String stateEditTextString;
    private String cityEditTextString;
    private String insuranceCarrierEditTextString;
    private String emergencyContactString;
    private int patientId;
    public static HashMap<Integer, ArrayList<Services>> sDoctorServices;
    private boolean chatStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setContentView(R.layout.activity_patient_details);
        sDoctorServices = new HashMap<>();
        getDoctorServices();
        patientName = (TextView) findViewById(R.id.patient_name_);
        patientAge = (TextView) findViewById(R.id.patient_age_);
        docId = (EditText) findViewById(R.id.doc_id);
        birthDate = (EditText) findViewById(R.id.birth_date);

        patientAddress = (EditText) findViewById(R.id.patient_address);
        circleImageView = (CircleImageView) findViewById(R.id.patient_image);
        phonePrimary = (EditText) findViewById(R.id.Phone_primary);
        phoneSecondary = (EditText) findViewById(R.id.Phone_secondary);

        stateEditText = (EditText) findViewById(R.id.state_Edit_text);
        cityEditText = (EditText) findViewById(R.id.city_Edit_text);
        insuranceCarrierEditText = (EditText) findViewById(R.id.insurance_carrier);
        emergencyContact = (EditText) findViewById(R.id.emergency_contact_);

        callButton = (ImageButton) findViewById(R.id.call_button_);
        chatButton = (ImageButton) findViewById(R.id.chat_button_);
        appointmentButton = (Button) findViewById(R.id.button_appointment);

        patientNameString = getIntent().getStringExtra("name");
        patientId = getIntent().getIntExtra("patient_id", -1);

        docIdString = getIntent().getStringExtra("identity_document");
        patientAddressString = getIntent().getStringExtra("address");
        circleImageViewString = getIntent().getStringExtra("photo");
        chatStatus = getIntent().getBooleanExtra("status", false);
        phonePrimaryString = getIntent().getStringExtra("phone_primary");
        phoneSecondaryString = getIntent().getStringExtra("phone_secondary");
        stateEditTextString = getIntent().getStringExtra("state");
        cityEditTextString = getIntent().getStringExtra("city");

        insuranceCarrierEditTextString = getIntent().getStringExtra("insurance_carrier");
        patientAgeString = getIntent().getStringExtra("dob");
        emergencyContactString = getIntent().getStringExtra("emergency_contact");

        patientName.setText(patientNameString);
        String years = Helpers.calculateAge(patientAgeString);
        patientAge.setText(years + " " + "years");
        Helpers.getBitMap(circleImageViewString, circleImageView);

        emergencyContact.setText(emergencyContactString);
        docId.setText(docIdString);
        birthDate.setText(patientAgeString);
        patientAddress.setText(patientAddressString);

        stateEditText.setText(stateEditTextString);
        cityEditText.setText(cityEditTextString);
        insuranceCarrierEditText.setText(insuranceCarrierEditTextString);
        phonePrimary.setText(phonePrimaryString);
        phoneSecondary.setText(phoneSecondaryString);

        patientName.setTypeface(AppGlobals.typefaceNormal);
        patientAge.setTypeface(AppGlobals.typefaceNormal);
        appointmentButton.setTypeface(AppGlobals.typefaceNormal);
        phonePrimary.setTypeface(AppGlobals.typefaceNormal);
        phoneSecondary.setTypeface(AppGlobals.typefaceNormal);
        emergencyContact.setTypeface(AppGlobals.typefaceNormal);
        insuranceCarrierEditText.setTypeface(AppGlobals.typefaceNormal);
        cityEditText.setTypeface(AppGlobals.typefaceNormal);
        stateEditText.setTypeface(AppGlobals.typefaceNormal);
        patientAddress.setTypeface(AppGlobals.typefaceNormal);
        birthDate.setTypeface(AppGlobals.typefaceNormal);
        docId.setTypeface(AppGlobals.typefaceNormal);

        appointmentButton.setOnClickListener(this);
        callButton.setOnClickListener(this);
        chatButton.setOnClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return false;
        }
    }

    private void getDoctorServices() {
        HttpRequest request = new HttpRequest(getApplicationContext());
        request.setOnReadyStateChangeListener(new HttpRequest.OnReadyStateChangeListener() {
            @Override
            public void onReadyStateChange(HttpRequest request, int readyState) {
                switch (readyState) {
                    case HttpRequest.STATE_DONE:
                        Helpers.dismissProgressDialog();
                        switch (request.getStatus()) {
                            case HttpURLConnection.HTTP_OK:
                                request.getResponseText();
                                try {
                                    JSONObject jsonObject1 = new JSONObject(request.getResponseText());
                                    JSONArray services = jsonObject1.getJSONArray("results");
                                    Log.i("TAG", services.toString());
                                    if (services.length() > 0) {
                                        ArrayList<Services> servicesArrayList = new ArrayList<>();
                                        for (int s = 0; s < services.length(); s++) {
                                            JSONObject singleService = services.getJSONObject(s);
                                            com.byteshaft.medicosperuanos.gettersetter.Services service
                                                    = new com.byteshaft.medicosperuanos.gettersetter.Services();
                                            service.setServiceId(singleService.getInt("id"));
                                            JSONObject internalObject = singleService.getJSONObject("service");
                                            service.setServiceName(internalObject.getString("name"));
                                            service.setServicePrice(singleService.getString("price"));
                                            if (singleService.getBoolean("is_active")) {
                                                servicesArrayList.add(service);
                                            }
                                        }sDoctorServices.put(Integer.valueOf(AppGlobals
                                                .getStringFromSharedPreferences(AppGlobals.KEY_USER_ID))
                                                , servicesArrayList);

                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                        }
                }
            }
        });
        request.open("GET", String.format("%sdoctor/services/", AppGlobals.BASE_URL));
        request.setRequestHeader("Authorization", "Token " +
                AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN));
        request.send();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_appointment:
                Intent appointmentIntent = new Intent(getApplicationContext(),
                        DoctorBookingActivity.class);
                appointmentIntent.putExtra("user", patientId);
                appointmentIntent.putExtra("name", patientNameString);
                appointmentIntent.putExtra("dob", patientAgeString);
                appointmentIntent.putExtra("photo", circleImageViewString);
                appointmentIntent.putExtra("patientID", patientId);
                appointmentIntent.putExtra("from_doctor", true);
                startActivity(appointmentIntent);
                break;
            case R.id.call_button_:
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.CALL_PHONE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE},
                            AppGlobals.CALL_PERMISSION);
                } else {
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phonePrimaryString));
                    startActivity(intent);
                }

                break;
            case R.id.chat_button_:
                Intent intent = new Intent(getApplicationContext(),
                        ConversationActivity.class);
                intent.putExtra("id", patientId);
                intent.putExtra("name",patientNameString);
                intent.putExtra("status", chatStatus);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case AppGlobals.CALL_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phonePrimaryString));
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    startActivity(intent);
                } else {
                    Helpers.showSnackBar(findViewById(android.R.id.content), R.string.permission_denied);
                }
                break;
        }
    }
}
