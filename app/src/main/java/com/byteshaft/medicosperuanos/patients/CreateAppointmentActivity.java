package com.byteshaft.medicosperuanos.patients;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
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
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.byteshaft.medicosperuanos.MainActivity;
import com.byteshaft.medicosperuanos.R;
import com.byteshaft.medicosperuanos.doctors.Appointments;
import com.byteshaft.medicosperuanos.doctors.DoctorDetailsActivity;
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
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreateAppointmentActivity extends AppCompatActivity implements View.OnClickListener,
        HttpRequest.OnReadyStateChangeListener, HttpRequest.OnErrorListener {

    private Button mSaveButton;
    private TextView serviceListSpinner;
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
    private String scheduleDate;
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
    private ArrayList<Services> arrayList;
    private HashMap<Integer, Integer> selectedServicesArrayList;
    private ListView listView;
    private int amount = 0;
    private static CreateAppointmentActivity sInstance;
    private String date;


    public static CreateAppointmentActivity getInstance() {
        return sInstance;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setContentView(R.layout.activity_create_appoint);
        sInstance = this;
        id = getIntent().getIntExtra("user", -1);
        startTime = getIntent().getStringExtra("start_time");
        scheduleDate = getIntent().getStringExtra("schedule_date");
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
        HashMap<Integer, ArrayList<Services>> hashMap = (HashMap<Integer, ArrayList<Services>>) getIntent().getSerializableExtra("services_array");
        if (AppGlobals.isDoctor()) {
            arrayList = hashMap.get(Integer.valueOf(AppGlobals
                    .getStringFromSharedPreferences(AppGlobals.KEY_USER_ID)));
        } else {
            arrayList = hashMap.get(id);
        }

        dateText = (TextView) findViewById(R.id.date_text);
        timeText = (TextView) findViewById(R.id.time_text);
        System.out.println("date is : " + scheduleDate);
        dateText.setText(scheduleDate);
        timeText.setText(slotTime);

        serviceListSpinner = (TextView) findViewById(R.id.service_spinner);
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
        priceTotalEditText = (EditText) findViewById(R.id.tv_total);


        mNameTextView.setTypeface(AppGlobals.typefaceNormal);
        mSpecialityTextView.setTypeface(AppGlobals.typefaceNormal);
        mDoctorStartTime.setTypeface(AppGlobals.typefaceNormal);
        priceTotalEditText.setTypeface(AppGlobals.robotoBlackItalic);

        callButton.setOnClickListener(this);
        chatButton.setOnClickListener(this);
        mSaveButton.setOnClickListener(this);
        favouriteButton.setOnClickListener(this);
        serviceListSpinner.setOnClickListener(this);
        selectedServicesArrayList = new HashMap<>();

        Log.i("TAG", "boolean for button " + AppGlobals.isDoctorFavourite);
        if (AppGlobals.isDoctorFavourite) {
            favouriteButton.setBackgroundResource(R.mipmap.ic_heart_fill);
        } else {
            favouriteButton.setBackgroundResource(R.mipmap.ic_empty_heart);
        }

        timeText.setText(startTime);

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
    protected void onResume() {
        super.onResume();
        System.out.println("On resume Called !");
        priceTotalEditText.setText(String.valueOf(amount));
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
                Intent intent = new Intent(getApplicationContext(),
                        ConversationActivity.class);
                intent.putExtra("id", id);
                intent.putExtra("name", drName);
                intent.putExtra("status", availableForChat);
                startActivity(intent);
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
                System.out.println(appointmentReason + "working");
                if (selectedServicesArrayList.size() < 1) {
                    Helpers.showSnackBar(findViewById(android.R.id.content), getResources().getString(R.string.select_service));
                    return;
                }
                if (appointmentReason != null && !appointmentReason.trim().isEmpty()) {
                    patientsAppointment(appointmentReason);
                } else {
                    mAppointmentEditText.setError(getResources().getString(R.string.please_enter_appointment_reason));
//                    Helpers.showSnackBar(findViewById(android.R.id.content),
//                            getResources().getString(R.string.please_enter_appointment_reason));
                }
                break;
            case R.id.service_spinner:
                if (arrayList != null && arrayList.size() > 0) {
                    DoctorServicesDialog dialog = new DoctorServicesDialog(CreateAppointmentActivity.this);
                    dialog.setTitle("Select Services");
                    dialog.show();
                } else {
                    Helpers.showSnackBar(findViewById(android.R.id.content), R.string.no_services_message);
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

    private void patientsAppointment(String appointmentReason) {
        Helpers.showProgressDialog(this, "Creating Appointment");
        HttpRequest request = new HttpRequest(this);
        request.setOnReadyStateChangeListener(this);
        request.setOnErrorListener(this);
        String url;
        if (AppGlobals.isDoctor()) {
            url = String.format("%sdoctor/patients/%s/appointments/%s",
                    AppGlobals.BASE_URL, id, appointmentId);
        } else {
            url = String.format("%sdoctors/%s/schedule/%s/get-appointment",
                    AppGlobals.BASE_URL, id, appointmentId);
        }
        request.open("POST", url);
        Log.i("TAG", "id " + appointmentId);
        request.setRequestHeader("Authorization", "Token " +
                AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN));
        request.send(getAppointmentData(appointmentReason));
        Log.i("TAG", getAppointmentData(appointmentReason));
    }

    private String getAppointmentData(String appointmentReason) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("reason", appointmentReason);
            JSONArray jsonArray = new JSONArray();
            for (Map.Entry<Integer, Integer> entry : selectedServicesArrayList.entrySet()) {
                jsonArray.put(entry.getValue());
            }
            Log.i("TAG", jsonArray.toString());
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
                Log.i("TAG", "response " + request.getResponseURL());
                Helpers.dismissProgressDialog();
                switch (request.getStatus()) {
                    case HttpURLConnection.HTTP_OK:
                        Log.i("TAG", "response " + request.getResponseText());
                        if (AppGlobals.isDoctor()) {
                            if (DoctorBookingActivity.getInstance() != null) {
                                DoctorBookingActivity.getInstance().finish();
                            }
                        }
                        break;
                    case HttpURLConnection.HTTP_CREATED:
                        Log.i("TAG", "response " + request.getResponseText());
                        Helpers.showSnackBar(findViewById(android.R.id.content), getResources().getString(R.string.appointment_created));
                        new android.os.Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (DoctorBookingActivity.getInstance() != null) {
                                    DoctorBookingActivity.getInstance().finish();
                                    if (DoctorDetailsActivity.getInstance() != null) {
                                        DoctorDetailsActivity.getInstance().finish();
                                    }
                                    if (PatientDetails.getInstance() != null) {
                                        PatientDetails.getInstance().finish();
                                    }
                                    finish();
                                    new android.os.Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            MainActivity.getInstance().loadFragment(new Appointments());
                                        }
                                    }, 600);
                                } else {
                                    finish();
                                    new android.os.Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            MainActivity.getInstance().loadFragment(new MyAppointments());
                                        }
                                    }, 600);                                }
                            }
                        }, 500);
                        break;
                    case HttpURLConnection.HTTP_FORBIDDEN:
                        Log.i("TAG", "response " + request.getResponseText());
                        break;
                }
        }

    }

    @Override
    public void onError(HttpRequest request, int readyState, short error, Exception exception) {
        Helpers.dismissProgressDialog();

    }

    private class DoctorServicesDialog extends Dialog implements View.OnClickListener {
        private Button positiveButton;
        private Button negativeButton;

        public DoctorServicesDialog(Activity activity) {
            super(activity);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.dialog_doctor_services);
            serviceAdapter = new ServiceAdapter(arrayList);
            listView = (ListView) findViewById(R.id.doctor_service_list);
            positiveButton = (Button) findViewById(R.id.positive_button);
            negativeButton = (Button) findViewById(R.id.negative_button);
            positiveButton.setOnClickListener(this);
            negativeButton.setOnClickListener(this);
            listView.setAdapter(serviceAdapter);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.positive_button:
                    dismiss();
                    amount = 0;
                    for (Map.Entry<Integer, Integer> entry : selectedServicesArrayList.entrySet()) {
                        for (Services services : arrayList) {
                            if (entry.getValue() == services.getServiceId()) {
                                amount = amount + Integer.valueOf(services.getServicePrice());
                            }
                        }
                    }
                    priceTotalEditText.setText(String.valueOf(amount));
                    break;
                case R.id.negative_button:
                    dismiss();
                    break;
            }
        }
    }


    public class ServiceAdapter extends BaseAdapter {

        private ArrayList<Services> arrayList;
        private ServiceViewHolder viewHolder;

        public ServiceAdapter(ArrayList<Services> arrayList) {
            super();
            this.arrayList = arrayList;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.service_delegate, parent, false);
                viewHolder = new ServiceViewHolder();
                viewHolder.serviceName = (TextView) convertView.findViewById(R.id.service_name);
                viewHolder.servicePrice = (TextView) convertView.findViewById(R.id.doc_service_price);
                viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.checkbox_doc_service);
                convertView.setTag(viewHolder);
                viewHolder.checkBox.setTag(position);
            } else {
                viewHolder = (ServiceViewHolder) convertView.getTag();
            }
            final Services services = arrayList.get(position);
            viewHolder.serviceName.setText(services.getServiceName());
            viewHolder.servicePrice.setText(services.getServicePrice());
            final View finalConvertView = convertView;
            viewHolder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    int pos = (int) viewHolder.checkBox.getTag();
                    View checkBoxView = listView.getChildAt(pos);

                    if (checkBoxView != null) {
                        if (b) {
                            if (selectedServicesArrayList.size() < 5) {
                                selectedServicesArrayList.put(position, services.getServiceId());
                            } else {
                                Toast.makeText(getApplicationContext(), "Max limit is 4", Toast.LENGTH_SHORT).show();
                                compoundButton.setChecked(false);

                            }
                        } else {
                            selectedServicesArrayList.remove(position);
                        }
                    }
                }
            });
            if (selectedServicesArrayList.containsKey(position)) {
                viewHolder.checkBox.setChecked(true);
            } else {
                viewHolder.checkBox.setChecked(false);
            }
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

    public class ServiceViewHolder {
        TextView serviceName;
        TextView servicePrice;
        CheckBox checkBox;
    }

}
