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
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.byteshaft.medicosperuanos.R;
import com.byteshaft.medicosperuanos.messages.ConversationActivity;
import com.byteshaft.medicosperuanos.utils.AppGlobals;
import com.byteshaft.medicosperuanos.utils.Helpers;

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

    private int id;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setContentView(R.layout.activity_patient_details);

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
        docIdString = getIntent().getStringExtra("identity_document");
        patientAddressString = getIntent().getStringExtra("address");
        circleImageViewString = getIntent().getStringExtra("photo");

        phonePrimaryString = getIntent().getStringExtra("phone_primary");
        phoneSecondaryString = getIntent().getStringExtra("phone_secondary");
        stateEditTextString = getIntent().getStringExtra("state");
        cityEditTextString = getIntent().getStringExtra("city");

        insuranceCarrierEditTextString = getIntent().getStringExtra("insurance_carrier");
        patientAgeString = getIntent().getStringExtra("dob");
        emergencyContactString = getIntent().getStringExtra("emergency_contact");

        patientName.setText(patientNameString);
        String years = Helpers.calculateAge(patientAgeString);
        patientAge.setText(years +" " + "years");
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

        docId.setEnabled(false);
        birthDate.setEnabled(false);
        patientAddress.setEnabled(false);
        stateEditText.setEnabled(false);
        cityEditText.setEnabled(false);
        phonePrimary.setEnabled(false);
        phoneSecondary.setEnabled(false);
        emergencyContact.setEnabled(false);
        insuranceCarrierEditText.setEnabled(false);


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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_appointment:
                Intent appointmentIntent = new Intent(this, DoctorBookingActivity.class);
                String id = AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_USER_ID);
                appointmentIntent.putExtra("user", Integer.valueOf(id));
                appointmentIntent.putExtra("name",patientNameString );
                appointmentIntent.putExtra("dob", patientAgeString);
                appointmentIntent.putExtra("photo", circleImageViewString);
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
                startActivity(new Intent(getApplicationContext(),
                        ConversationActivity.class));
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
