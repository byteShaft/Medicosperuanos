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

public class PatientDetails extends AppCompatActivity implements View.OnClickListener {

    private TextView patientName;
    private TextView patientAge;
    private ImageButton callButton;
    private ImageButton chatButton;
    private Button appointmentButton;
    private EditText docId;
    private EditText birthDate;
    private EditText patientAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setContentView(R.layout.activity_patient_details);
        patientName = (TextView) findViewById(R.id.patient_name_);
        patientAge = (TextView) findViewById(R.id.patient_age_);
        callButton = (ImageButton) findViewById(R.id.call_button_);
        chatButton = (ImageButton) findViewById(R.id.chat_button_);
        appointmentButton = (Button) findViewById(R.id.button_appointment);

        patientName.setTypeface(AppGlobals.typefaceNormal);
        patientAge.setTypeface(AppGlobals.typefaceNormal);
        appointmentButton.setTypeface(AppGlobals.typefaceNormal);
        appointmentButton.setOnClickListener(this);
        docId = (EditText) findViewById(R.id.doc_id);
        birthDate = (EditText) findViewById(R.id.birth_date);
        patientAddress = (EditText) findViewById(R.id.patient_address);
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
                startActivity(new Intent(getApplicationContext(), DoctorBookingActivity.class));
                break;
            case R.id.call_button_:
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.CALL_PHONE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE},
                            AppGlobals.CALL_PERMISSION);
                } else {
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + "03120676767"));
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
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + "03120676767"));
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
