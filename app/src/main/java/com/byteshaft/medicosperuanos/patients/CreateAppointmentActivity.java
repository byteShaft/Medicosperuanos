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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.byteshaft.medicosperuanos.R;
import com.byteshaft.medicosperuanos.doctors.DoctorDetailsActivity;
import com.byteshaft.medicosperuanos.doctors.DoctorsList;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class CreateAppointmentActivity extends AppCompatActivity implements View.OnClickListener,
        HttpRequest.OnReadyStateChangeListener, HttpRequest.OnErrorListener {

    private Button mSaveButton;
    private Spinner serviceListSpinner;
    private ImageButton callButton;
    private ImageButton chatButton;
    private EditText mAppointmentEditText;
    private String mPhoneNumber;
    private CircleImageView mDoctorImage;
    private TextView mNameTextView;
    private TextView mSpecialityTextView;
    private TextView mDoctorStartTime;
    private RatingBar mDoctorRating;
    private ImageView status;

    private int id;

    private HttpRequest request;
    private boolean blocked;
    private ImageButton favouriteButton;
    private TextView dateText;
    private TextView timeText;
    private boolean isBlocked;
    private String startTime;
    private String phonenumber;
    private String drName;
    private String drSpecialist;
    private float drStars;
    private String drPhoto;
    private boolean availableForChat;
    private int appointmentId;
    private ServiceAdapter serviceAdapter;
    private EditText priceTotalEditText;
    private String slotTime;
    private String appointmentDate;
    private int selectedServiceId;
    private String reason;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setContentView(R.layout.activity_create_appoint);
        id = getIntent().getIntExtra("user", -1);
        startTime = getIntent().getStringExtra("start_time");
        isBlocked = getIntent().getBooleanExtra("block", false);
        drName = getIntent().getStringExtra("name");
        drSpecialist = getIntent().getStringExtra("specialist");
        drStars = getIntent().getFloatExtra("stars", 0);
        appointmentId = getIntent().getIntExtra("appointment_id", -1);
        Log.i("TAG", "appointmentId " + appointmentId);
        phonenumber = getIntent().getStringExtra("number");
        drPhoto = getIntent().getStringExtra("photo");
        availableForChat = getIntent().getBooleanExtra("available_to_chat", false);
        slotTime = getIntent().getStringExtra("time_slot");
        appointmentDate = getIntent().getStringExtra("appointment_date");
        dateText = (TextView) findViewById(R.id.date_text);
        timeText = (TextView) findViewById(R.id.time_text);
        dateText.setText(appointmentDate);
        timeText.setText(slotTime);
        callButton = (ImageButton) findViewById(R.id.btn_call);
        chatButton = (ImageButton) findViewById(R.id.btn_chat);
        mDoctorImage = (CircleImageView) findViewById(R.id.doctor_image);
        mNameTextView = (TextView) findViewById(R.id.doctor_name);
        mSpecialityTextView = (TextView) findViewById(R.id.doctor_speciality);
        mDoctorStartTime = (TextView) findViewById(R.id.starts_time);
        mDoctorRating = (RatingBar) findViewById(R.id.user_ratings);
        status = (ImageView) findViewById(R.id.status);
        mAppointmentEditText = (EditText) findViewById(R.id.appointment_reason_editText);
        mSaveButton = (Button) findViewById(R.id.button_save);
        favouriteButton = (ImageButton) findViewById(R.id.btn_fav);
        mNameTextView.setTypeface(AppGlobals.typefaceNormal);
        mSpecialityTextView.setTypeface(AppGlobals.typefaceNormal);
        mDoctorStartTime.setTypeface(AppGlobals.typefaceNormal);
        priceTotalEditText = (EditText) findViewById(R.id.tv_total);
        priceTotalEditText.setTypeface(AppGlobals.robotoBlackItalic);

        callButton.setOnClickListener(this);
        chatButton.setOnClickListener(this);
        mSaveButton.setOnClickListener(this);
        favouriteButton.setOnClickListener(this);
        Log.i("TAG", "boolean for button " + AppGlobals.isDoctorFavourite);
        if (AppGlobals.isDoctorFavourite) {
            favouriteButton.setBackgroundResource(R.mipmap.ic_heart_fill);
        } else {
            favouriteButton.setBackgroundResource(R.mipmap.ic_empty_heart);
        }

        dateText.setText(Helpers.getDate());
        timeText.setText(Helpers.getTime());
        final ArrayList<Services> arrayList = DoctorsList.sDoctorServices.get(id);
        if (arrayList != null && arrayList.size() > 0) {
            serviceListSpinner = (Spinner) findViewById(R.id.service_spinner);
            serviceAdapter = new ServiceAdapter(arrayList);
            serviceListSpinner.setAdapter(serviceAdapter);
            serviceListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    Services service = arrayList.get(i);
                    selectedServiceId = service.getServiceId();
                    priceTotalEditText.setText(service.getServicePrice());
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
        } else {
            Helpers.alertDialog(this, getResources().getString(R.string.no_services),
                    getResources().getString(R.string.no_services_message), null);
        }
        if (!availableForChat) {
            status.setImageResource(R.mipmap.ic_offline_indicator);
        } else {
            status.setImageResource(R.mipmap.ic_online_indicator);
        }
        blocked = getIntent().getBooleanExtra("block", false);
        mDoctorStartTime.setText(startTime);
        mNameTextView.setText(drName);
        mSpecialityTextView.setText(drSpecialist);
        mDoctorRating.setRating(drStars);
        if (blocked) {
            chatButton.setEnabled(false);
        }
        Helpers.getBitMap(drPhoto, mDoctorImage);

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
            case R.id.btn_call:
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.CALL_PHONE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE},
                            AppGlobals.CALL_PERMISSION);
                } else {
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + mPhoneNumber));
                    startActivity(intent);
                }
                break;
            case R.id.btn_chat:
                startActivity(new Intent(getApplicationContext(),
                        ConversationActivity.class));
                break;
            case R.id.btn_fav:
                favouriteButton.setEnabled(false);
                if (!AppGlobals.isDoctorFavourite) {
                    Helpers.favouriteDoctorTask(id, new HttpRequest.OnReadyStateChangeListener() {
                        @Override
                        public void onReadyStateChange(HttpRequest request, int readyState) {
                            switch (readyState) {
                                case HttpRequest.STATE_DONE:
                                    switch (request.getStatus()) {
                                        case HttpURLConnection.HTTP_OK:
                                            favouriteButton.setEnabled(true);
                                            AppGlobals.isDoctorFavourite = true;
                                            favouriteButton.setBackgroundResource(R.mipmap.ic_heart_fill);
                                    }
                            }
                        }
                    }, new HttpRequest.OnErrorListener() {
                        @Override
                        public void onError(HttpRequest request, int readyState, short error, Exception exception) {
                            favouriteButton.setEnabled(true);
                        }
                    });
                } else {
                    Helpers.unFavouriteDoctorTask(id, new HttpRequest.OnReadyStateChangeListener() {
                                @Override
                                public void onReadyStateChange(HttpRequest request, int readyState) {
                                    switch (readyState) {
                                        case HttpRequest.STATE_DONE:
                                            switch (request.getStatus()) {
                                                case HttpURLConnection.HTTP_NO_CONTENT:
                                                    favouriteButton.setEnabled(true);
                                                    AppGlobals.isDoctorFavourite = false;
                                                    favouriteButton.setBackgroundResource(R.mipmap.ic_empty_heart);

                                            }
                                    }

                                }
                            }, new HttpRequest.OnErrorListener() {
                                @Override
                                public void onError(HttpRequest request, int readyState, short error, Exception exception) {
                                    favouriteButton.setEnabled(true);
                                }
                            });
                }
                break;

            case R.id.button_save:
                String appointmentReason = mAppointmentEditText.getText().toString();
                System.out.println(appointmentReason  + "working");
                if (appointmentReason != null && !appointmentReason.trim().isEmpty()) {
                    patientsAppointment(appointmentReason, selectedServiceId);
                } else {
                    Helpers.showSnackBar(findViewById(android.R.id.content),
                            getResources().getString(R.string.please_enter_appointment_reason));
                }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case AppGlobals.CALL_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + mPhoneNumber));
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

    private void patientsAppointment(String appointmentReason, int serviceId) {
        Helpers.showProgressDialog(this, "Getting appointment");
        request = new HttpRequest(this);
        request.setOnReadyStateChangeListener(this);
        request.setOnErrorListener(this);
        request.open("POST", String.format("%sdoctors/%s/schedule/%s/get-appointment",
                AppGlobals.BASE_URL,id,  appointmentId));
        Log.i("TAG", "id " + appointmentId);
        request.setRequestHeader("Authorization", "Token " +
                AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN));
        request.send(getAppointmentData(appointmentReason, serviceId));
        Log.i("TAG", getAppointmentData(appointmentReason, serviceId));
    }

    private String getAppointmentData(String appointmentReason, int serviceId) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("reason", appointmentReason);
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(serviceId);
            jsonObject.put("services", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();

    }

    @Override
    public void onReadyStateChange(HttpRequest request, int readyState) {
        switch (readyState) {
            case HttpRequest.STATE_DONE:
                Helpers.dismissProgressDialog();
                switch (request.getStatus()) {
                    case HttpURLConnection.HTTP_OK:
                        Log.i("TAG", "response " + request.getResponseText());
                        break;
                    case HttpRequest.STATE_DONE:
                            case HttpURLConnection.HTTP_CREATED:
                                Helpers.showSnackBar(findViewById(android.R.id.content), getResources().getString(R.string.appointment_created));
                                new android.os.Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        DoctorBookingActivity.getInstance().finish();
                                        DoctorDetailsActivity.getInstance().finish();
                                        finish();
                                    }
                                }, 500);
                                break;
                }

        }

    }

    @Override
    public void onError(HttpRequest request, int readyState, short error, Exception exception) {
        Helpers.dismissProgressDialog();

    }

    private class ServiceAdapter extends BaseAdapter {

        private ArrayList<Services> arrayList;
        private ViewHolder viewHolder;

        public ServiceAdapter(ArrayList<Services> arrayList) {
            super();
            this.arrayList = arrayList;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.service_delegate, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.serviceName = (TextView) convertView.findViewById(R.id.service_name);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            Services services = arrayList.get(position);
            Log.i("TAG", "service id " + services.getServiceId());
            viewHolder.serviceName.setText(services.getServiceName());
            return convertView;
        }

        @Override
        public int getCount() {
            return arrayList.size();
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
    private class ViewHolder {
        TextView serviceName;
    }
}
