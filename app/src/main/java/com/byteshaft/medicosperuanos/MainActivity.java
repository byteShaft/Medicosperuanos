package com.byteshaft.medicosperuanos;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.byteshaft.medicosperuanos.accountfragments.AccountManagerActivity;
import com.byteshaft.medicosperuanos.accountfragments.UserBasicInfoStepOne;
import com.byteshaft.medicosperuanos.doctors.Appointments;
import com.byteshaft.medicosperuanos.doctors.Dashboard;
import com.byteshaft.medicosperuanos.doctors.DoctorsList;
import com.byteshaft.medicosperuanos.doctors.MyPatients;
import com.byteshaft.medicosperuanos.doctors.MySchedule;
import com.byteshaft.medicosperuanos.doctors.Services;
import com.byteshaft.medicosperuanos.introscreen.IntroScreen;
import com.byteshaft.medicosperuanos.messages.MainMessages;
import com.byteshaft.medicosperuanos.patients.FavouriteDoctors;
import com.byteshaft.medicosperuanos.patients.MyAppointments;
import com.byteshaft.medicosperuanos.utils.AppGlobals;
import com.byteshaft.medicosperuanos.utils.Helpers;
import com.byteshaft.requests.FormData;
import com.byteshaft.requests.HttpRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Logger;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.byteshaft.medicosperuanos.utils.Helpers.getBitMap;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        HttpRequest.OnReadyStateChangeListener, HttpRequest.OnErrorListener {

    public static MainActivity sInstance;
    private static final int STORAGE_PERMISSION = 0;

    public static MainActivity getInstance() {
        return sInstance;
    }

    private SwitchCompat doctorOnlineSwitch;
    private SwitchCompat patientOnlineSwitch;
    private static CircleImageView profilePicture;
    private boolean isError;

    private String address;
    private int city;
    private String dob;
    private String firstName;
    private String gender;
    private String identityDocument;
    private int insuranceCarrier;
    private String lastName;
    private String location;
    private String phoneNumberPrimary;
    private int state;
    private String consultationTime;
    private String[] speciality;
    private int subscriptionPlan;
    private String collegeId;
    private NavigationView doctorNavigationView;
    private NavigationView patientNavigationView;
    private boolean foreground = false;
    private boolean isLoggingOut = false;

    private static final Logger LOGGER = Logger.getLogger(MainActivity.class.getName());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sInstance = this;
        setContentView(R.layout.activity_main);
        foreground = true;
        if (AccountManagerActivity.getInstance() != null) {
            AccountManagerActivity.getInstance().finish();
        }
        if (IntroScreen.getInstance() != null) {
            IntroScreen.getInstance().finish();
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (AppGlobals.isDoctor()) {
            View headerView;
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
                @Override
                public void onDrawerSlide(View drawerView, float slideOffset) {

                }

                @Override
                public void onDrawerOpened(View drawerView) {
                    Log.i("TAG", "test");
                    updateMessages();

                }

                @Override
                public void onDrawerClosed(View drawerView) {

                }

                @Override
                public void onDrawerStateChanged(int newState) {

                }
            });
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.setDrawerListener(toggle);
            toggle.syncState();
            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {

                }
            });
            navigationView.setNavigationItemSelectedListener(this);
            navigationView.setItemIconTintList(null);
            headerView = getLayoutInflater().inflate(R.layout.nav_header_doctor, navigationView, false);
            navigationView.addHeaderView(headerView);
            navigationView.inflateMenu(R.menu.doctor_menus);
            doctorNavigationView = navigationView;
            TextView docName = (TextView) headerView.findViewById(R.id.doc_nav_name);
            TextView docEmail = (TextView) headerView.findViewById(R.id.doc_nav_email);
            TextView docSpeciality = (TextView) headerView.findViewById(R.id.doc_nav_speciality);
            TextView docExpDate = (TextView) headerView.findViewById(R.id.doc_nav_expiry_date);
            doctorOnlineSwitch = (SwitchCompat) headerView.findViewById(R.id.doc_nav_online_switch);
            profilePicture = (CircleImageView) headerView.findViewById(R.id.nav_imageView);

            doctorOnlineSwitch.setChecked(AppGlobals.isOnline());

            //setting typeface
            docName.setTypeface(AppGlobals.typefaceNormal);
            docEmail.setTypeface(AppGlobals.typefaceNormal);
            docSpeciality.setTypeface(AppGlobals.typefaceNormal);
            docExpDate.setTypeface(AppGlobals.typefaceNormal);

            // setting up information
            docName.setText(AppGlobals.getStringFromSharedPreferences(
                    AppGlobals.KEY_FIRST_NAME) + " " +
                    AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_LAST_NAME));
            docEmail.setText(AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_EMAIL));
            docExpDate.setText(AppGlobals.getSubscription());

            address = AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_ADDRESS);
            city = AppGlobals.getIntegerFromSharedPreferences(AppGlobals.KEY_CITY_SELECTED);
            dob = AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_DATE_OF_BIRTH);
            firstName = AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_FIRST_NAME);
            gender = AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_GENDER);
            identityDocument = AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_DOC_ID);
            insuranceCarrier = AppGlobals.getIntegerFromSharedPreferences(AppGlobals.KEY_INSURANCE_SELECTED);
            lastName = AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_LAST_NAME);
            location = AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_LOCATION);
            phoneNumberPrimary = AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_PHONE_NUMBER_PRIMARY);
            state = AppGlobals.getIntegerFromSharedPreferences(AppGlobals.KEY_STATE_SELECTED);
            consultationTime = AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_CONSULTATION_TIME);
            Set<String> stringSet = AppGlobals.getSpecialityFromSharedPreferences();
            for (String string : stringSet) {
                docSpeciality.setText(string);
                break;
            }
//            speciality = AppGlobals.getIntegerFromSharedPreferences(AppGlobals.KEY_SPECIALIST_SELECTED);
            subscriptionPlan = AppGlobals.getIntegerFromSharedPreferences(AppGlobals.KEY_SUBSCRIPTION_SELECTED);
            collegeId = AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_COLLEGE_ID);

            doctorOnlineSwitch.setTypeface(AppGlobals.typefaceNormal);
            if (AppGlobals.isOnline()) {
                doctorOnlineSwitch.setChecked(true);
                doctorOnlineSwitch.setText(R.string.online);
            } else {
                doctorOnlineSwitch.setChecked(false);
                doctorOnlineSwitch.setText(R.string.offline);
            }
            doctorOnlineSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    switch (compoundButton.getId()) {
                        case R.id.doc_nav_online_switch:
                            if (isError) {
                                isError = false;
                            } else {
                                changeStatus(b, address , String.valueOf(city), dob, firstName, gender, identityDocument,
                                        String.valueOf(insuranceCarrier), lastName, location, phoneNumberPrimary, String.valueOf(state),
                                        consultationTime, String.valueOf(subscriptionPlan), collegeId);
                                doctorOnlineSwitch.setEnabled(false);
                            }
                            break;
                    }
                }
            });
            loadFragment(new Dashboard());

        } else {
            View headerView;
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.setOnDragListener(new View.OnDragListener() {
                @Override
                public boolean onDrag(View view, DragEvent dragEvent) {
                    updateMessages();
                    return true;
                }
            });
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.setDrawerListener(toggle);
            toggle.syncState();

            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {

                }
            });
            navigationView.setNavigationItemSelectedListener(this);
            navigationView.setItemIconTintList(null);
            headerView = getLayoutInflater().inflate(R.layout.nav_header_patient, navigationView, false);
            navigationView.addHeaderView(headerView);
            navigationView.inflateMenu(R.menu.patient_menu);
            patientNavigationView = navigationView;
            TextView patientName = (TextView) headerView.findViewById(R.id.patient_nav_name);
            TextView patientEmail = (TextView) headerView.findViewById(R.id.patient_nav_email);
            TextView patientAge = (TextView) headerView.findViewById(R.id.patient_nav_age);
            patientOnlineSwitch = (SwitchCompat) headerView.findViewById(R.id.patient_nav_online_switch);
            patientOnlineSwitch.setChecked(AppGlobals.isOnline());
            if (AppGlobals.isOnline()) {
                patientOnlineSwitch.setText(getResources().getString(R.string.online));
            } else {
                patientOnlineSwitch.setText(getResources().getString(R.string.offline));
            }
            profilePicture = (CircleImageView) headerView.findViewById(R.id.nav_imageView);
            patientName.setText(AppGlobals.getStringFromSharedPreferences(
                    AppGlobals.KEY_FIRST_NAME) + " " +
                    AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_LAST_NAME));
            String years = Helpers.calculateAge(AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_DATE_OF_BIRTH));
            patientAge.setText(years + " years");
            patientEmail.setText(AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_EMAIL));
            // setting typeface
            patientName.setTypeface(AppGlobals.typefaceNormal);
            patientEmail.setTypeface(AppGlobals.typefaceNormal);
            patientAge.setTypeface(AppGlobals.typefaceNormal);
            patientOnlineSwitch.setTypeface(AppGlobals.typefaceNormal);

            address = AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_ADDRESS);
            city = AppGlobals.getIntegerFromSharedPreferences(AppGlobals.KEY_CITY_SELECTED);
            dob = AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_DATE_OF_BIRTH);
            firstName = AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_FIRST_NAME);
            gender = AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_GENDER);
            identityDocument = AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_DOC_ID);
            insuranceCarrier = AppGlobals.getIntegerFromSharedPreferences(AppGlobals.KEY_INSURANCE_SELECTED);
            lastName = AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_LAST_NAME);
            location = AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_LOCATION);
            phoneNumberPrimary = AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_PHONE_NUMBER_PRIMARY);
            state = AppGlobals.getIntegerFromSharedPreferences(AppGlobals.KEY_STATE_SELECTED);
            consultationTime = AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_CONSULTATION_TIME);
            subscriptionPlan = AppGlobals.getIntegerFromSharedPreferences(AppGlobals.KEY_SUBSCRIPTION_SELECTED);
            collegeId = AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_COLLEGE_ID);

            patientOnlineSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    switch (compoundButton.getId()) {
                        case R.id.patient_nav_online_switch:
                            if (isError) {
                                isError = false;
                            } else {
                                if (b) {
                                    patientOnlineSwitch.setText(R.string.online);
                                } else {
                                    patientOnlineSwitch.setText(R.string.offline);
                                }
                                changeStatus(b,address , String.valueOf(city), dob, firstName, gender, identityDocument,
                                        String.valueOf(insuranceCarrier), lastName, location, phoneNumberPrimary, String.valueOf(state),
                                        consultationTime, String.valueOf(subscriptionPlan), collegeId);
                                patientOnlineSwitch.setEnabled(false);
                            }
                            break;
                    }
                }
            });
            loadFragment(new DoctorsList());
        }

        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle(getResources().getString(R.string.storage_permission_dialog_title));
            alertDialogBuilder.setMessage(getResources().getString(R.string.storage_permission_message))
                    .setCancelable(false).setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            STORAGE_PERMISSION);
                }
            });
            alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

        } else {
            setProfilePicture();
        }
    }

    public static void setProfilePicture() {
        if (AppGlobals.isLogin() && AppGlobals.getStringFromSharedPreferences(AppGlobals.SERVER_PHOTO_URL) != null) {
            String url = String.format("%s" + AppGlobals
                    .getStringFromSharedPreferences(AppGlobals.SERVER_PHOTO_URL), AppGlobals.SERVER_IP);
            Log.i("TAG", "url " + url);
            getBitMap(url, profilePicture);
        }
    }

    public static void changeStatus(boolean status, String address, String city, String dob, String first_name, String gender, String identity_document,
                              String insurance_carrier, String last_name, String location, String phone_number_primary, String state,
                              String consultation_time, String subscription_plan, String collegeId) {
        HttpRequest request = new HttpRequest(AppGlobals.getContext());
        request.setOnReadyStateChangeListener(getInstance());
        request.setOnErrorListener(getInstance());
        request.open("PUT", String.format("%sprofile", AppGlobals.BASE_URL));
        request.setRequestHeader("Authorization", "Token " +
                AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN));
        ArrayList<Integer> ids = new ArrayList<>();
        if (AppGlobals.isDoctor()) {
            String[] specialityIds = AppGlobals.getDoctorSpecialities().replace("[", "").replace("]", "").split(",");
            for (int j = 0; j < specialityIds.length; j++) {
                Log.i("TAG", "loop " + specialityIds[j]);
                ids.add(Integer.valueOf(specialityIds[j].trim()));
            }
        }
        request.send(dataWithChatStatus(status, address, city, dob, first_name, gender, identity_document,
                insurance_carrier, last_name, location, phone_number_primary, state,
                consultation_time, ids, subscription_plan, collegeId));
    }

    private static FormData dataWithChatStatus(boolean status, String address, String city, String dob,String first_name, String gender, String identity_document,
                                        String insurance_carrier, String last_name, String location, String phone_number_primary, String state,
                                        String consultation_time, ArrayList<Integer> speciality, String subscription_plan, String collegeId) {
        FormData formData  = new FormData();
        formData.append(FormData.TYPE_CONTENT_TEXT, "available_to_chat", String.valueOf(status));
        formData.append(FormData.TYPE_CONTENT_TEXT, "address", address);
        formData.append(FormData.TYPE_CONTENT_TEXT, "city", city);
        formData.append(FormData.TYPE_CONTENT_TEXT, "dob", dob);
        formData.append(FormData.TYPE_CONTENT_TEXT, "first_name", first_name);
        formData.append(FormData.TYPE_CONTENT_TEXT, "gender", gender);
        formData.append(FormData.TYPE_CONTENT_TEXT, "identity_document", identity_document);
        formData.append(FormData.TYPE_CONTENT_TEXT, "insurance_carrier", insurance_carrier);
        formData.append(FormData.TYPE_CONTENT_TEXT, "last_name", last_name);
        formData.append(FormData.TYPE_CONTENT_TEXT, "location", location);
        formData.append(FormData.TYPE_CONTENT_TEXT, "phone_number_primary", phone_number_primary);
        formData.append(FormData.TYPE_CONTENT_TEXT, "state", state);
        formData.append(FormData.TYPE_CONTENT_TEXT, "consultation_time", consultation_time);
        if (AppGlobals.isDoctor()) {
            formData.append(FormData.TYPE_CONTENT_TEXT, "speciality", speciality.toString());
        }
        formData.append(FormData.TYPE_CONTENT_TEXT, "subscription_plan", subscription_plan);
        formData.append(FormData.TYPE_CONTENT_TEXT, "college_id", collegeId);
        return formData;

    }

    @Override
    protected void onResume() {
        super.onResume();
        foreground = true;
        updateMessages();
        getMessages();
    }

    @Override
    protected void onPause() {
        super.onPause();
        foreground = false;
    }

    public void updateMessages() {
//        if (foreground) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getMessages();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case STORAGE_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    String url = String.format("%s" + AppGlobals
                                    .getStringFromSharedPreferences(AppGlobals.SERVER_PHOTO_URL),
                            AppGlobals.SERVER_IP);
                    getBitMap(url, profilePicture);
                } else {
                    Helpers.showSnackBar(findViewById(android.R.id.content), R.string.permission_denied);
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_dashboard) {
            loadFragment(new Dashboard());
        } else if (id == R.id.nav_search_doctor) {
            loadFragment(new DoctorsList());
        } else if (id == R.id.nav_appointment) {
            loadFragment(new MyAppointments());
        } else if (id == R.id.nav_favt_doc) {
            loadFragment(new FavouriteDoctors());
        } else if (id == R.id.nav_patients) {
            loadFragment(new MyPatients());
        } else if (id == R.id.nav_doc_appointment) {
            loadFragment(new Appointments());
        } else if (id == R.id.nav_messages) {
            loadFragment(new MainMessages());
        } else if (id == R.id.nav_profile) {
            if (!UserBasicInfoStepOne.foreground) {
                loadFragment(new UserBasicInfoStepOne(false));
            }
        } else if (id == R.id.nav_schedule) {
            loadFragment(new MySchedule());
        } else if (id == R.id.nav_my_services) {
            loadFragment(new Services());
        } else if (id == R.id.nav_exit) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("Confirmation");
            alertDialogBuilder.setMessage("Do you really want to exit?").setCancelable(false).setPositiveButton("Yes",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    });
            alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        } else if (id == R.id.nav_logout) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("Confirmation");
            alertDialogBuilder.setMessage("Do you really want to logout?")
                    .setCancelable(false).setPositiveButton("Yes",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            isLoggingOut = true;
                            Helpers.showProgressDialog(MainActivity.this, "Logging out...");
                            changeStatus(false, address , String.valueOf(city), dob, firstName, gender, identityDocument,
                                    String.valueOf(insuranceCarrier), lastName, location, phoneNumberPrimary, String.valueOf(state),
                                    consultationTime, String.valueOf(subscriptionPlan), collegeId);
//                            AppGlobals.clearSettings();
//                            AppGlobals.firstTimeLaunch(true);
                            dialog.dismiss();
//                            startActivity(new Intent(getApplicationContext(), IntroScreen.class));
                        }
                    });
            alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void loadFragment(Fragment fragment) {
        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.replace(R.id.container, fragment);
        tx.commit();
    }

    @Override
    public void onReadyStateChange(HttpRequest request, int readyState) {
        switch (readyState) {
            case HttpRequest.STATE_DONE:
                Helpers.dismissProgressDialog();
                switch (request.getStatus()) {
                    case HttpURLConnection.HTTP_OK:
                        LOGGER.info(request.getResponseText());
                        try {
                            JSONObject jsonObject1  = new JSONObject(request.getResponseText());
                            if (isLoggingOut) {
                                AppGlobals.clearSettings();
                                AppGlobals.firstTimeLaunch(true);
                                startActivity(new Intent(getApplicationContext(), IntroScreen.class));
                                isLoggingOut = false;
                                return;
                            }
                            if (!AppGlobals.isDoctor()) {
                                if (jsonObject1.getBoolean("available_to_chat")) {
                                    patientOnlineSwitch.setText(R.string.online);
                                } else {
                                    patientOnlineSwitch.setText(R.string.offline);
                                }
                                patientOnlineSwitch.setEnabled(true);
                                AppGlobals.saveChatStatus(jsonObject1.getBoolean("available_to_chat"));
                            } else {
                                if (jsonObject1.getBoolean("available_to_chat")) {
                                    doctorOnlineSwitch.setText(R.string.online);
                                } else {
                                    doctorOnlineSwitch.setText(R.string.offline);
                                }
                                doctorOnlineSwitch.setEnabled(true);
                                AppGlobals.saveChatStatus(jsonObject1.getBoolean("available_to_chat"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case HttpURLConnection.HTTP_UNAUTHORIZED:
                        if (AppGlobals.isDoctor()) {
                            if (isLoggingOut) {
                                AppGlobals.clearSettings();
                                AppGlobals.firstTimeLaunch(true);
                                startActivity(new Intent(getApplicationContext(), IntroScreen.class));
                                isLoggingOut = false;
                            } else {
                                doctorOnlineSwitch.setEnabled(true);
                                Helpers.alertDialog(this, getResources().getString(R.string.account),
                                        getResources().getString(R.string.account_not_activated),
                                        doctorOnlineSwitch);
                            }
                        } else {
                            if (isLoggingOut) {
                                AppGlobals.clearSettings();
                                AppGlobals.firstTimeLaunch(true);
                                startActivity(new Intent(getApplicationContext(), IntroScreen.class));
                                isLoggingOut = false;
                            }
                        }
                        break;
                    case HttpURLConnection.HTTP_BAD_REQUEST:
                        System.out.println(request.getResponseText());
                        break;
                }
                isLoggingOut = false;
        }
    }

    @Override
    public void onError(HttpRequest request, int readyState, short error, Exception exception) {
        isError = true;
        isLoggingOut = false;
        Helpers.dismissProgressDialog();
        Helpers.showSnackBar(findViewById(android.R.id.content), exception.getLocalizedMessage());
        if (!AppGlobals.isDoctor()) {
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    patientOnlineSwitch.setEnabled(true);
                }
            }, 500);
            if (patientOnlineSwitch.isChecked()) {
                patientOnlineSwitch.setChecked(false);
                patientOnlineSwitch.setText(R.string.offline);
            } else {
                patientOnlineSwitch.setChecked(true);
                patientOnlineSwitch.setText(R.string.online);
            }
        } else {
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doctorOnlineSwitch.setEnabled(true);
                }
            }, 500);
            if (doctorOnlineSwitch.isChecked()) {
                doctorOnlineSwitch.setChecked(false);
                doctorOnlineSwitch.setText(R.string.offline);
            } else {
                doctorOnlineSwitch.setChecked(true);
                doctorOnlineSwitch.setText(R.string.online);
            }

        }

    }

    public void getMessages() {
        HttpRequest request = new HttpRequest(AppGlobals.getContext());
        request.setOnReadyStateChangeListener(new HttpRequest.OnReadyStateChangeListener() {
            @Override
            public void onReadyStateChange(HttpRequest httpRequest, int i) {
                switch (i) {
                    case HttpRequest.STATE_DONE:
                        Helpers.dismissProgressDialog();
                        switch (httpRequest.getStatus()) {
                            case HttpURLConnection.HTTP_OK:
                                Log.e("TAG", httpRequest.getResponseText());
                                int count = 0;
                                try {
                                    JSONArray jsonArray = new JSONArray(httpRequest.getResponseText());
                                    for (int k = 0; k < jsonArray.length(); k++) {
                                        JSONObject jsonObject =  jsonArray.getJSONObject(k);
                                        if (jsonObject.getInt("unread_count") != 0) {
                                            count = count+1;
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                NavigationView navigationView;
                                if (AppGlobals.isDoctor())
                                    navigationView = doctorNavigationView;
                                else
                                    navigationView = patientNavigationView;
                                Menu menu = navigationView.getMenu();
                                // find MenuItem you want to change
                                MenuItem navMessages = menu.findItem(R.id.nav_messages);
                                if (count > 0) {
                                    SpannableString s = new SpannableString("Messages         " +
                                            String.valueOf(count));
                                    s.setSpan(new ForegroundColorSpan(Color.RED), 0, s.length(), 0);
                                    s.setSpan(new AbsoluteSizeSpan(14, true), 0, s.length(), 0);
                                    navMessages.setTitle(s);
                                } else {
                                    navMessages.setTitle("Messages");
                                }
                        }
                }
            }
        });
        request.setOnErrorListener(this);
        request.open("GET", String.format("%smessages_metadata", AppGlobals.BASE_URL));
        request.setRequestHeader("Authorization", "Token " +
                AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN));
        request.send();
    }
}
