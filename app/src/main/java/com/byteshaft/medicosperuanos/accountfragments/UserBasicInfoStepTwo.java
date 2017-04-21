package com.byteshaft.medicosperuanos.accountfragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.byteshaft.medicosperuanos.MainActivity;
import com.byteshaft.medicosperuanos.R;
import com.byteshaft.medicosperuanos.adapters.CitiesAdapter;
import com.byteshaft.medicosperuanos.adapters.InsuranceCarriersAdapter;
import com.byteshaft.medicosperuanos.adapters.StatesAdapter;
import com.byteshaft.medicosperuanos.gettersetter.Cities;
import com.byteshaft.medicosperuanos.gettersetter.InsuranceCarriers;
import com.byteshaft.medicosperuanos.gettersetter.States;
import com.byteshaft.medicosperuanos.utils.AppGlobals;
import com.byteshaft.medicosperuanos.utils.Helpers;
import com.byteshaft.requests.FormData;
import com.byteshaft.requests.HttpRequest;
import com.github.lzyzsd.circleprogress.DonutProgress;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.ArrayList;


public class UserBasicInfoStepTwo extends Fragment implements AdapterView.OnItemSelectedListener,
        View.OnClickListener, CompoundButton.OnCheckedChangeListener, HttpRequest.OnReadyStateChangeListener,
        HttpRequest.OnFileUploadProgressListener, HttpRequest.OnErrorListener {
    private View mBaseView;
    private Spinner mStateSpinner;
    private Spinner mCitySpinner;
    private Spinner mInsuranceCarrierSpinner;
    private TextView mStateSpinnerTextView;
    private EditText mPhoneOneEditText;
    private EditText mPhoneTwoEditText;
    private EditText mEmergencyContactEditText;
    private CheckBox mNotificationCheckBox;
    private CheckBox mNewsCheckBox;
    private CheckBox mTermsConditionCheckBox;
    private String mPhoneOneEditTextString;
    private String mPhoneTwoEditTextString;
    private String mEmergencyContactString;
    private String mStatesSpinnerValueString;
    private String mCitiesSpinnerValueString;
    private String mInsuranceCarrierSpinnerValueString;
    private String mNotificationCheckBoxString = "true";
    private String mNewsCheckBoxString = "true";
    private String mTermsConditionCheckBoxString;
    private Button mSaveButton;
    private HttpRequest mRequest;
    private DonutProgress donutProgress;
    private ProgressBar progressBar;
    private AlertDialog alertDialog;
    private AlertDialog.Builder alertDialogBuilder;


    private ArrayList<States> statesList;
    private StatesAdapter statesAdapter;
    private ArrayList<Cities> citiesList;
    private CitiesAdapter citiesAdapter;

    private ArrayList<InsuranceCarriers> insuranceCarriersList;
    private InsuranceCarriersAdapter insuranceCarriersAdapter;

    private int cityPosition;
    private int statePosition;
    private int insuranceCarrierPosition;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBaseView = inflater.inflate(R.layout.fragment_user_basic_info_step_two, container, false);
        if (AppGlobals.isLogin() && AppGlobals.isInfoAvailable()) {
            ((AppCompatActivity) getActivity()).getSupportActionBar()
                    .setTitle(getResources().getString(R.string.update_profile));
        } else {
            ((AppCompatActivity) getActivity()).getSupportActionBar()
                    .setTitle(getResources().getString(R.string.sign_up));
        }
        setHasOptionsMenu(true);

        getStates();
        getInsuranceCarriers();
        /// data list work
        statesList = new ArrayList<>();
        citiesList = new ArrayList<>();
        insuranceCarriersList = new ArrayList<>();

        mStateSpinner = (Spinner) mBaseView.findViewById(R.id.states_spinner);
        mCitySpinner = (Spinner) mBaseView.findViewById(R.id.cities_spinner);
        mInsuranceCarrierSpinner = (Spinner) mBaseView.findViewById(R.id.insurance_spinner);
        mStateSpinnerTextView = (TextView) mBaseView.findViewById(R.id.states_spinner_text_view);

        mPhoneOneEditText = (EditText) mBaseView.findViewById(R.id.phone_one_edit_text);
        mPhoneTwoEditText = (EditText) mBaseView.findViewById(R.id.phone_two_edit_text);
        mEmergencyContactEditText = (EditText) mBaseView.findViewById(R.id.emergency_contact);

        mPhoneOneEditText.setText(AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_PHONE_NUMBER_PRIMARY));
        mPhoneTwoEditText.setText(AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_PHONE_NUMBER_SECONDARY));
        mEmergencyContactEditText.setText(AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_EMERGENCY_CONTACT));

        mNotificationCheckBox = (CheckBox) mBaseView.findViewById(R.id.notifications_check_box);
        mNewsCheckBox = (CheckBox) mBaseView.findViewById(R.id.news_check_box);
        mTermsConditionCheckBox = (CheckBox) mBaseView.findViewById(R.id.terms_check_box);

        mNotificationCheckBox.setChecked(AppGlobals.isShowNotification());
        mNewsCheckBox.setChecked(AppGlobals.isShowNews());

        mSaveButton = (Button) mBaseView.findViewById(R.id.save_button);

        mPhoneOneEditText.setTypeface(AppGlobals.typefaceNormal);
        mPhoneTwoEditText.setTypeface(AppGlobals.typefaceNormal);
        mEmergencyContactEditText.setTypeface(AppGlobals.typefaceNormal);
        mNotificationCheckBox.setTypeface(AppGlobals.typefaceNormal);
        mNewsCheckBox.setTypeface(AppGlobals.typefaceNormal);
        mTermsConditionCheckBox.setTypeface(AppGlobals.typefaceNormal);

        mStateSpinner.setOnItemSelectedListener(this);
        mCitySpinner.setOnItemSelectedListener(this);
        mInsuranceCarrierSpinner.setOnItemSelectedListener(this);

        mNotificationCheckBox.setOnCheckedChangeListener(this);
        mNewsCheckBox.setOnCheckedChangeListener(this);
        mTermsConditionCheckBox.setOnCheckedChangeListener(this);

        mSaveButton.setOnClickListener(this);

        return mBaseView;
    }

    private void getInsuranceCarriers() {
        HttpRequest getStateRequest = new HttpRequest(getActivity().getApplicationContext());
        getStateRequest.setOnReadyStateChangeListener(new HttpRequest.OnReadyStateChangeListener() {
            @Override
            public void onReadyStateChange(HttpRequest request, int readyState) {
                switch (readyState) {
                    case HttpRequest.STATE_DONE:
                        switch (request.getStatus()) {
                            case HttpURLConnection.HTTP_OK:
                                try {
                                    JSONObject object = new JSONObject(request.getResponseText());
                                    JSONArray jsonArray = object.getJSONArray("results");
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        System.out.println("Test " + jsonArray.getJSONObject(i));
                                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                                        InsuranceCarriers insuranceCarriers = new InsuranceCarriers();
                                        insuranceCarriers.setId(jsonObject.getInt("id"));
                                        if (AppGlobals.getDoctorProfileIds(AppGlobals.KEY_INSURANCE_SELECTED)
                                                == jsonObject.getInt("id")) {
                                            insuranceCarrierPosition = i;
                                        }
                                        insuranceCarriers.setName(jsonObject.getString("name"));
                                        insuranceCarriersList.add(insuranceCarriers);
                                    }
                                    insuranceCarriersAdapter = new InsuranceCarriersAdapter(getActivity(), insuranceCarriersList);
                                    mInsuranceCarrierSpinner.setAdapter(insuranceCarriersAdapter);
                                    mInsuranceCarrierSpinner.setSelection(insuranceCarrierPosition);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                        }
                }
            }
        });
        getStateRequest.open("GET", String.format("%sinsurance-carriers/", AppGlobals.BASE_URL));
        getStateRequest.send();
    }

    private void getStates() {
        HttpRequest getStateRequest = new HttpRequest(getActivity().getApplicationContext());
        getStateRequest.setOnReadyStateChangeListener(new HttpRequest.OnReadyStateChangeListener() {
            @Override
            public void onReadyStateChange(HttpRequest request, int readyState) {
                switch (readyState) {
                    case HttpRequest.STATE_DONE:
                        switch (request.getStatus()) {
                            case HttpURLConnection.HTTP_OK:
                                try {
                                    JSONObject object = new JSONObject(request.getResponseText());
                                    JSONArray jsonArray = object.getJSONArray("results");
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        System.out.println("Test " + jsonArray.getJSONObject(i));
                                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                                        States states = new States();
                                        states.setCode(jsonObject.getString("code"));
                                        states.setId(jsonObject.getInt("id"));
                                        if (jsonObject.getInt("id") ==
                                                AppGlobals.getDoctorProfileIds(
                                                        AppGlobals.KEY_STATE_SELECTED)) {
                                            statePosition = i;
                                        }
                                        states.setName(jsonObject.getString("name"));
                                        statesList.add(states);
                                    }
                                    statesAdapter = new StatesAdapter(getActivity(), statesList);
                                    mStateSpinner.setAdapter(statesAdapter);
                                    mStateSpinner.setSelection(statePosition);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                        }
                }
            }
        });
        getStateRequest.open("GET", String.format("%sstates", AppGlobals.BASE_URL));
        getStateRequest.send();
    }

    private void getCities(int id) {
        HttpRequest getCitiesRequest = new HttpRequest(getActivity().getApplicationContext());
        getCitiesRequest.setOnReadyStateChangeListener(new HttpRequest.OnReadyStateChangeListener() {
            @Override
            public void onReadyStateChange(HttpRequest request, int readyState) {
                switch (readyState) {
                    case HttpRequest.STATE_DONE:
                        switch (request.getStatus()) {
                            case HttpURLConnection.HTTP_OK:
                                System.out.println(request.getResponseText());
                                try {
                                    JSONObject object = new JSONObject(request.getResponseText());
                                    JSONArray jsonArray = object.getJSONArray("results");
                                    citiesList = new ArrayList<>();
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        System.out.println("Test " + jsonArray.getJSONObject(i));
                                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                                        Cities cities = new Cities();
                                        cities.setCityId(jsonObject.getInt("id"));
                                        cities.setCityName(jsonObject.getString("name"));
                                        cities.setStateId(jsonObject.getInt("id"));
                                        if (AppGlobals.getDoctorProfileIds(AppGlobals.KEY_CITY_SELECTED) ==
                                                jsonObject.getInt("id")) {
                                            cityPosition = i;
                                        }
//                                        cities.setStateName(jsonObject.getString("state_name"));
                                        citiesList.add(cities);
                                    }
                                    citiesAdapter = new CitiesAdapter(getActivity(), citiesList);
                                    mCitySpinner.setAdapter(citiesAdapter);
                                    mCitySpinner.setSelection(cityPosition);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                        }
                }
            }
        });
        getCitiesRequest.open("GET", String.format("%sstates/%s/cities", AppGlobals.BASE_URL, id));
        getCitiesRequest.send();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()) {
            case R.id.states_spinner:
                States states = statesList.get(i);
                getCities(states.getId());
                mStatesSpinnerValueString = String.valueOf(states.getId());
                System.out.println(states.getId());
                AppGlobals.saveDoctorProfileIds(AppGlobals.KEY_STATE_SELECTED,
                        states.getId());
                break;
            case R.id.cities_spinner:
                Cities city = citiesList.get(i);
                mCitiesSpinnerValueString = String.valueOf(city.getCityId());
                System.out.println(city.getCityId());
                AppGlobals.saveDoctorProfileIds(AppGlobals.KEY_CITY_SELECTED,
                        city.getCityId());
                break;
            case R.id.insurance_spinner:
                InsuranceCarriers insuranceCarriers = insuranceCarriersList.get(i);
                mInsuranceCarrierSpinnerValueString = String.valueOf(insuranceCarriers.getId());
                AppGlobals.saveDoctorProfileIds(AppGlobals.KEY_INSURANCE_SELECTED,
                        insuranceCarriers.getId());
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onClick(View view) {
        mPhoneTwoEditTextString = mPhoneTwoEditText.getText().toString();
        if (validateEditText()) {
            sendingDataToServer();
        }

    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()) {
            case R.id.notifications_check_box:
                if (mNotificationCheckBox.isChecked()) {
                    mNotificationCheckBoxString = "true";
                    System.out.println(mNotificationCheckBoxString);
                } else {
                    mNotificationCheckBoxString = "false";
                }
                break;
            case R.id.news_check_box:
                if (mNewsCheckBox.isChecked()) {
                    mNewsCheckBoxString = "true";
                    System.out.println(mNewsCheckBoxString);
                } else {
                    mNewsCheckBoxString = "false";
                }
                break;
            case R.id.terms_check_box:
                if (mTermsConditionCheckBox.isChecked()) {
                    mSaveButton.setEnabled(true);
                    mSaveButton.setBackgroundColor(getResources().getColor(R.color.buttonColor));
                    mTermsConditionCheckBoxString = mTermsConditionCheckBox.getText().toString();
                    System.out.println(mTermsConditionCheckBoxString);
                } else {
                    mSaveButton.setEnabled(false);
                    mSaveButton.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                }
                break;
        }

    }

    private boolean validateEditText() {
        boolean valid = true;
        mPhoneOneEditTextString = mPhoneOneEditText.getText().toString();
        mEmergencyContactString = mEmergencyContactEditText.getText().toString();

        if (mPhoneOneEditTextString.trim().isEmpty()) {
            mPhoneOneEditText.setError("please enter your phone number");
            valid = false;
        } else {
            mPhoneOneEditText.setError(null);
        }
        if (mEmergencyContactString.trim().isEmpty()) {
            mEmergencyContactEditText.setError("please provide your Emergency Contact");
            valid = false;
        } else {
            mEmergencyContactEditText.setError(null);
        }

        return valid;
    }

    private void sendingDataToServer() {
        FormData data = new FormData();
        data.append(FormData.TYPE_CONTENT_TEXT, "identity_document", AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_DOC_ID));
        data.append(FormData.TYPE_CONTENT_TEXT, "first_name", AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_FIRST_NAME));
        data.append(FormData.TYPE_CONTENT_TEXT, "last_name", AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_LAST_NAME));
        data.append(FormData.TYPE_CONTENT_TEXT, "dob", AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_DATE_OF_BIRTH));
        data.append(FormData.TYPE_CONTENT_TEXT, "gender", AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_GENDER));
        data.append(FormData.TYPE_CONTENT_TEXT, "location", AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_LOCATION));
        data.append(FormData.TYPE_CONTENT_TEXT, "address", AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_ADDRESS));
        if (!AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_IMAGE_URL).trim().isEmpty()) {
            data.append(FormData.TYPE_CONTENT_FILE, "photo", AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_IMAGE_URL));
            alertDialogBuilder = new AlertDialog.Builder(getActivity());
            alertDialogBuilder.setTitle(getResources().getString(R.string.updating));
            alertDialogBuilder.setCancelable(false);
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.progress_alert_dialog, null);
            alertDialogBuilder.setView(dialogView);
            donutProgress = (DonutProgress) dialogView.findViewById(R.id.upload_progress);

            alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        } else {
            Helpers.showProgressDialog(getActivity(), "Updating your Profile...");
        }
        data.append(FormData.TYPE_CONTENT_TEXT, "state", mStatesSpinnerValueString);
        data.append(FormData.TYPE_CONTENT_TEXT, "city", mCitiesSpinnerValueString);
        data.append(FormData.TYPE_CONTENT_TEXT, "insurance_carrier", mInsuranceCarrierSpinnerValueString);
        data.append(FormData.TYPE_CONTENT_TEXT, "phone_number_primary", mPhoneOneEditTextString);
        data.append(FormData.TYPE_CONTENT_TEXT, "phone_number_secondary", mPhoneTwoEditTextString);
        data.append(FormData.TYPE_CONTENT_TEXT, "emergency_contact", mEmergencyContactString);
        data.append(FormData.TYPE_CONTENT_TEXT, "show_notification", mNotificationCheckBoxString);
        data.append(FormData.TYPE_CONTENT_TEXT, "show_news", mNewsCheckBoxString);

        mRequest = new HttpRequest(getActivity().getApplicationContext());
        mRequest.setOnReadyStateChangeListener(this);
        mRequest.setOnFileUploadProgressListener(this);
        mRequest.setOnErrorListener(this);
        String method = "POST";
        if (AppGlobals.isInfoAvailable()) {
            method = "PATCH";
        }
        mRequest.open(method, String.format("%sprofile", AppGlobals.BASE_URL));
        mRequest.setRequestHeader("Authorization", "Token " +
                AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN));
        mRequest.send(data);
    }

    @Override
    public void onReadyStateChange(HttpRequest request, int readyState) {
        switch (readyState) {
            case HttpRequest.STATE_DONE:
                if (alertDialog != null) {
                    alertDialog.dismiss();
                } else {
                    Helpers.dismissProgressDialog();
                }
                switch (request.getStatus()) {
                    case HttpRequest.ERROR_NETWORK_UNREACHABLE:
                        AppGlobals.alertDialog(getActivity(), "Profile update Failed!", "please check your internet connection");
                        break;
                    case HttpURLConnection.HTTP_NOT_FOUND:
                        AppGlobals.alertDialog(getActivity(), "Profile update Failed!", "provide a valid EmailAddress");
                        break;
                    case HttpURLConnection.HTTP_UNAUTHORIZED:
                        AppGlobals.alertDialog(getActivity(), "Profile update Failed!", "Please enter correct password");
                        break;
                    case HttpURLConnection.HTTP_FORBIDDEN:
                        AppGlobals.alertDialog(getActivity(), "Inactive Account", "Please activate your account");
                        AccountManagerActivity.getInstance().loadFragment(new AccountActivationCode());
                        break;
                    case HttpURLConnection.HTTP_CREATED:
                        System.out.println(request.getResponseText() + "working ");
                        Toast.makeText(getActivity(), "Profile Created Successfully", Toast.LENGTH_SHORT).show();
                        try {
                            JSONObject jsonObject = new JSONObject(request.getResponseText());
                            System.out.println(jsonObject + "working ");

                            String userId = jsonObject.getString(AppGlobals.KEY_USER_ID);
                            String firstName = jsonObject.getString(AppGlobals.KEY_FIRST_NAME);
                            String lastName = jsonObject.getString(AppGlobals.KEY_LAST_NAME);
                            String imageUrl = jsonObject.getString(AppGlobals.KEY_IMAGE_URL);
                            String profileId = jsonObject.getString(AppGlobals.KEY_PROFILE_ID);

                            String gender = jsonObject.getString(AppGlobals.KEY_GENDER);
                            String dateOfBirth = jsonObject.getString(AppGlobals.KEY_DATE_OF_BIRTH);
                            String phoneNumberPrimary = jsonObject.getString(AppGlobals.KEY_PHONE_NUMBER_PRIMARY);
                            String phoneNumberSecondary = jsonObject.getString(AppGlobals.KEY_PHONE_NUMBER_SECONDARY);

                            String insuranceCarrier = jsonObject.getString(AppGlobals.KEY_INSURANCE_CARRIER);
                            String address = jsonObject.getString(AppGlobals.KEY_ADDRESS);
                            String location = jsonObject.getString(AppGlobals.KEY_LOCATION);

                            String chatStatus = jsonObject.getString(AppGlobals.KEY_CHAT_STATUS);
                            String state = jsonObject.getString(AppGlobals.KEY_STATE);
                            String city = jsonObject.getString(AppGlobals.KEY_CITY);
                            String docId = jsonObject.getString(AppGlobals.KEY_DOC_ID);
                            String showNews = jsonObject.getString(AppGlobals.KEY_SHOW_NEWS);

                            String showNotification = jsonObject.getString(AppGlobals.KEY_SHOW_NOTIFICATION);
                            String emergencyContact = jsonObject.getString(AppGlobals.KEY_EMERGENCY_CONTACT);

                            //saving values
                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_USER_ID, userId);
                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_FIRST_NAME, firstName);
                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_LAST_NAME, lastName);

                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_GENDER, gender);
                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_DATE_OF_BIRTH, dateOfBirth);
                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_PHONE_NUMBER_PRIMARY, phoneNumberPrimary);
                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_PHONE_NUMBER_SECONDARY, phoneNumberSecondary);

//                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_AFFILIATE_CLINIC_ID, affiliateClinic);
                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_INSURANCE_CARRIER, insuranceCarrier);
                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_ADDRESS, address);
                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_LOCATION, location);
                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_PROFILE_ID, profileId);
                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_CHAT_STATUS, chatStatus);
                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_STATE, state);
                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_CITY, city);
                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_DOC_ID, docId);
                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_SHOW_NEWS, showNews);
                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_SHOW_NOTIFICATION, showNotification);
                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_EMERGENCY_CONTACT, emergencyContact);
                            Log.i("Emergency Contact", " " + AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_EMERGENCY_CONTACT));
                            AppGlobals.saveDataToSharedPreferences(AppGlobals.SERVER_PHOTO_URL, imageUrl);
                            AppGlobals.gotInfo(true);
                            AccountManagerActivity.getInstance().finish();
                            startActivity(new Intent(getActivity(), MainActivity.class));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case HttpURLConnection.HTTP_OK:
                        Log.i("TAG", "res" + request.getResponseText());
                        Toast.makeText(getActivity(), "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                        try {
                            JSONObject jsonObject = new JSONObject(request.getResponseText());
                            System.out.println(jsonObject + "working ");

                            String userId = jsonObject.getString(AppGlobals.KEY_USER_ID);
                            String firstName = jsonObject.getString(AppGlobals.KEY_FIRST_NAME);
                            String lastName = jsonObject.getString(AppGlobals.KEY_LAST_NAME);
                            String imageUrl = jsonObject.getString(AppGlobals.KEY_IMAGE_URL);
                            String profileId = jsonObject.getString(AppGlobals.KEY_PROFILE_ID);

                            String gender = jsonObject.getString(AppGlobals.KEY_GENDER);
                            String dateOfBirth = jsonObject.getString(AppGlobals.KEY_DATE_OF_BIRTH);
                            String phoneNumberPrimary = jsonObject.getString(AppGlobals.KEY_PHONE_NUMBER_PRIMARY);
                            String phoneNumberSecondary = jsonObject.getString(AppGlobals.KEY_PHONE_NUMBER_SECONDARY);

                            String insuranceCarrier = jsonObject.getString(AppGlobals.KEY_INSURANCE_CARRIER);
                            String address = jsonObject.getString(AppGlobals.KEY_ADDRESS);
                            String location = jsonObject.getString(AppGlobals.KEY_LOCATION);

                            String chatStatus = jsonObject.getString(AppGlobals.KEY_CHAT_STATUS);
                            String state = jsonObject.getString(AppGlobals.KEY_STATE);
                            String city = jsonObject.getString(AppGlobals.KEY_CITY);
                            String docId = jsonObject.getString(AppGlobals.KEY_DOC_ID);
                            String showNews = jsonObject.getString(AppGlobals.KEY_SHOW_NEWS);

                            String showNotification = jsonObject.getString(AppGlobals.KEY_SHOW_NOTIFICATION);
                            String emergencyContact = jsonObject.getString(AppGlobals.KEY_EMERGENCY_CONTACT);

                            //saving values
                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_USER_ID, userId);
                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_FIRST_NAME, firstName);
                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_LAST_NAME, lastName);

                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_GENDER, gender);
                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_DATE_OF_BIRTH, dateOfBirth);
                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_PHONE_NUMBER_PRIMARY, phoneNumberPrimary);
                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_PHONE_NUMBER_SECONDARY, phoneNumberSecondary);

//                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_AFFILIATE_CLINIC_ID, affiliateClinic);
                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_INSURANCE_CARRIER, insuranceCarrier);
                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_ADDRESS, address);
                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_LOCATION, location);
                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_PROFILE_ID, profileId);
                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_CHAT_STATUS, chatStatus);
                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_STATE, state);
                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_CITY, city);
                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_DOC_ID, docId);
                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_SHOW_NEWS, showNews);
                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_SHOW_NOTIFICATION, showNotification);
                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_EMERGENCY_CONTACT, emergencyContact);
                            Log.i("Emergency Contact", " " + AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_EMERGENCY_CONTACT));
                            AppGlobals.saveDataToSharedPreferences(AppGlobals.SERVER_PHOTO_URL, imageUrl);
                            AppGlobals.gotInfo(true);
                            startActivity(new Intent(getActivity(), MainActivity.class));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;

                }
        }

    }

    @Override
    public void onFileUploadProgress(HttpRequest request, File file, long loaded, long total) {
        double progress = (loaded / (double) total) * 100;
        Log.i("current progress", "" + (int) progress);
        donutProgress.setProgress((int) progress);
        if ((int) progress == 100) {
            Log.i("PROGRESS", "condition matched");
            if (alertDialog != null) {
                donutProgress.setProgress(100);
                alertDialog.dismiss();
            }
            alertDialogBuilder = new AlertDialog.Builder(getActivity());
            alertDialogBuilder.setTitle(getResources().getString(R.string.finishing_up));
            alertDialogBuilder.setCancelable(false);
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.finishingup_dialog, null);
            alertDialogBuilder.setView(dialogView);
            progressBar = (ProgressBar) dialogView.findViewById(R.id.progress_bar);
            alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }

    @Override
    public void onError(HttpRequest request, int readyState, short error, Exception exception) {
        if (alertDialog != null) {
            alertDialog.dismiss();
        } else {
            Helpers.dismissProgressDialog();
        }
    }
}