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

import com.byteshaft.medicosperuanos.MainActivity;
import com.byteshaft.medicosperuanos.R;
import com.byteshaft.medicosperuanos.adapters.AffiliateClinicAdapter;
import com.byteshaft.medicosperuanos.adapters.CitiesAdapter;
import com.byteshaft.medicosperuanos.adapters.SpecialitiesAdapter;
import com.byteshaft.medicosperuanos.adapters.StatesAdapter;
import com.byteshaft.medicosperuanos.adapters.SubscriptionTypeAdapter;
import com.byteshaft.medicosperuanos.doctors.Dashboard;
import com.byteshaft.medicosperuanos.doctors.DoctorsList;
import com.byteshaft.medicosperuanos.gettersetter.AffiliateClinic;
import com.byteshaft.medicosperuanos.gettersetter.Cities;
import com.byteshaft.medicosperuanos.gettersetter.Specialities;
import com.byteshaft.medicosperuanos.gettersetter.States;
import com.byteshaft.medicosperuanos.gettersetter.SubscriptionType;
import com.byteshaft.medicosperuanos.uihelpers.MultiSelectionSpinner;
import com.byteshaft.medicosperuanos.utils.AppGlobals;
import com.byteshaft.medicosperuanos.utils.Helpers;
import com.byteshaft.requests.FormData;
import com.byteshaft.requests.HttpRequest;
import com.github.lzyzsd.circleprogress.DonutProgress;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.R.attr.id;
import static com.byteshaft.medicosperuanos.utils.AppGlobals.getDoctorProfileIds;
import static com.byteshaft.medicosperuanos.utils.AppGlobals.isInfoAvailable;

public class DoctorsBasicInfo extends Fragment implements AdapterView.OnItemSelectedListener,
        CompoundButton.OnCheckedChangeListener, View.OnClickListener,
        HttpRequest.OnReadyStateChangeListener, HttpRequest.OnFileUploadProgressListener,
        HttpRequest.OnErrorListener {

    private View mBaseView;
    private Button mSaveButton;

    private Spinner mStateSpinner;
    private Spinner mCitySpinner;
    private MultiSelectionSpinner mSpecialitySpinner;
    private Spinner mAffiliatedClinicsSpinner;
    private Spinner mSubscriptionSpinner;

    private EditText mPhoneOneEditText;
    private EditText mPhoneTwoEditText;
    private EditText mConsultationTimeEditText;
    private EditText mCollegeIdEditText;

    private CheckBox mNotificationCheckBox;
    private CheckBox mNewsCheckBox;
    private CheckBox mTermsConditionCheckBox;
    private String mPhoneOneEditTextString;
    private String mPhoneTwoEditTextString;
    private String mConsultationTimeEditTextString;
    private String mCollegeIdEditTextString;
    private String mStatesSpinnerValueString;
    private String mCitiesSpinnerValueString;
    private ArrayList<Integer> mSpecialitySpinnerArray;
    private String mAffiliatedClinicsSpinnerValueString;
    private String mSubscriptionSpinnerValueString;
    private String mNotificationCheckBoxString = "true";
    private String mNewsCheckBoxString = "true";
    private String mTermsConditionCheckBoxString;

    private HttpRequest mRequest;
    private DonutProgress donutProgress;
    private ProgressBar progressBar;
    private AlertDialog.Builder alertDialogBuilder;
    private AlertDialog alertDialog;
    // Date lists
    private ArrayList<States> statesList;
    private StatesAdapter statesAdapter;
    private ArrayList<Cities> citiesList;
    private CitiesAdapter citiesAdapter;

    private ArrayList<Specialities> specialitiesList;
    private SpecialitiesAdapter specialitiesAdapter;

    private ArrayList<AffiliateClinic> affiliateClinicsList;
    private AffiliateClinicAdapter affiliateClinicAdapter;

    private ArrayList<SubscriptionType> subscriptionTypesList;
    private SubscriptionTypeAdapter subscriptionTypeAdapter;

    private int cityPosition;
    private int statePosition;
    private int subscriptionPosition;
    private int affiliateClinicPosition;
    private int specialistPosition;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBaseView = inflater.inflate(R.layout.fragment_doctor_basic_info, container, false);
        if (AppGlobals.isLogin() && AppGlobals.isInfoAvailable()) {
            ((AppCompatActivity) getActivity()).getSupportActionBar()
                    .setTitle(getResources().getString(R.string.update_profile));
        } else {
            ((AppCompatActivity) getActivity()).getSupportActionBar()
                    .setTitle(getResources().getString(R.string.sign_up));
        }
        setHasOptionsMenu(true);
        getStates();
        getSpecialities();
        getAffiliateClinic();
        getSubscriptionType();
        /// data list work
        statesList = new ArrayList<>();
        citiesList = new ArrayList<>();
        mSpecialitySpinnerArray = new ArrayList<>();
        specialitiesList = new ArrayList<>();
        affiliateClinicsList = new ArrayList<>();
        subscriptionTypesList = new ArrayList<>();

        mSaveButton = (Button) mBaseView.findViewById(R.id.save_button);
        mStateSpinner = (Spinner) mBaseView.findViewById(R.id.states_spinner);
        mCitySpinner = (Spinner) mBaseView.findViewById(R.id.cities_spinner);
        mSpecialitySpinner = (MultiSelectionSpinner) mBaseView.findViewById(R.id.speciality_spinner);
        mAffiliatedClinicsSpinner = (Spinner) mBaseView.findViewById(R.id.clinics_spinner);
        mSubscriptionSpinner = (Spinner) mBaseView.findViewById(R.id.subscriptions_spinner);

        mPhoneOneEditText = (EditText) mBaseView.findViewById(R.id.phone_one_edit_text);
        mPhoneTwoEditText = (EditText) mBaseView.findViewById(R.id.phone_two_edit_text);
        mConsultationTimeEditText = (EditText) mBaseView.findViewById(R.id.consultation_time_edit_text);
        mCollegeIdEditText = (EditText) mBaseView.findViewById(R.id.college_id_edit_text);

        mNotificationCheckBox = (CheckBox) mBaseView.findViewById(R.id.notifications_check_box);
        mNewsCheckBox = (CheckBox) mBaseView.findViewById(R.id.news_check_box);
        mTermsConditionCheckBox = (CheckBox) mBaseView.findViewById(R.id.terms_check_box);

        mNotificationCheckBox.setChecked(AppGlobals.isShowNotification());
        mNewsCheckBox.setChecked(AppGlobals.isShowNews());

        mSaveButton.setTypeface(AppGlobals.typefaceNormal);
        mPhoneOneEditText.setTypeface(AppGlobals.typefaceNormal);
        mPhoneTwoEditText.setTypeface(AppGlobals.typefaceNormal);
        mConsultationTimeEditText.setTypeface(AppGlobals.typefaceNormal);
        mCollegeIdEditText.setTypeface(AppGlobals.typefaceNormal);

        mPhoneOneEditText.setText(AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_PHONE_NUMBER_PRIMARY));
        mPhoneTwoEditText.setText(AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_PHONE_NUMBER_SECONDARY));
        mConsultationTimeEditText.setText(AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_CONSULTATION_TIME));
        mCollegeIdEditText.setText(AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_COLLEGE_ID));
        mStateSpinner.setOnItemSelectedListener(this);
        mCitySpinner.setOnItemSelectedListener(this);
        mAffiliatedClinicsSpinner.setOnItemSelectedListener(this);
        if (!AppGlobals.isInfoAvailable()) {
            mSubscriptionSpinner.setOnItemSelectedListener(this);
            mSubscriptionSpinner.setEnabled(true);
        } else if (AppGlobals.isLogin() && isInfoAvailable()) {
            mSubscriptionSpinner.setEnabled(false);
        }
        mNotificationCheckBox.setOnCheckedChangeListener(this);
        mNewsCheckBox.setOnCheckedChangeListener(this);
        mTermsConditionCheckBox.setOnCheckedChangeListener(this);
        if (AppGlobals.isLogin() && AppGlobals.isInfoAvailable()) {
            mTermsConditionCheckBox.setVisibility(View.GONE);
        }
        mSaveButton.setOnClickListener(this);
        return mBaseView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getFragmentManager().popBackStack();
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
                if (citiesList.size() > 0) {
                    Cities city = citiesList.get(i);
                    mCitiesSpinnerValueString = String.valueOf(city.getCityId());
                    System.out.println(city.getCityId());
                    AppGlobals.saveDoctorProfileIds(AppGlobals.KEY_CITY_SELECTED,
                            city.getCityId());
                }
                break;
            case R.id.clinics_spinner:
                AffiliateClinic affiliateClinic = affiliateClinicsList.get(i);
                mAffiliatedClinicsSpinnerValueString = String.valueOf(affiliateClinic.getId());
                System.out.println(affiliateClinic.getId());
                AppGlobals.saveDoctorProfileIds(AppGlobals.KEY_CLINIC_SELECTED,
                        affiliateClinic.getId());
                break;
            case R.id.subscriptions_spinner:
                SubscriptionType subscriptionType = subscriptionTypesList.get(i);
                mSubscriptionSpinnerValueString = String.valueOf(subscriptionType.getId());
                System.out.println(subscriptionType.getId() + "  " + subscriptionType.getPrice());
                AppGlobals.saveDoctorProfileIds(AppGlobals.KEY_SUBSCRIPTION_SELECTED,
                        subscriptionType.getId());
                break;
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()) {
            case R.id.notifications_check_box:
                if (mNotificationCheckBox.isChecked()) {
                    mNotificationCheckBoxString = "true";
                    System.out.println(mNotificationCheckBoxString);
                }
                break;
            case R.id.news_check_box:
                if (mNewsCheckBox.isChecked()) {
                    mNewsCheckBoxString = "true";
                    System.out.println(mNewsCheckBoxString);
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

    @Override
    public void onClick(View view) {
        mPhoneTwoEditTextString = mPhoneTwoEditText.getText().toString();
        if (validateEditText()) {
            sendingDataToServer();
        }

    }

    private boolean validateEditText() {
        boolean valid = true;
        mPhoneOneEditTextString = mPhoneOneEditText.getText().toString();
        mCollegeIdEditTextString = mCollegeIdEditText.getText().toString();
        mConsultationTimeEditTextString = mConsultationTimeEditText.getText().toString();

        if (mPhoneOneEditTextString.trim().isEmpty()) {
            mPhoneOneEditText.setError("please enter your phone number");
            valid = false;
        } else {
            mPhoneOneEditText.setError(null);
        }
        if (mCollegeIdEditTextString.trim().isEmpty()) {
            mCollegeIdEditText.setError("please provide your collegeID");
            valid = false;
        } else {
            mCollegeIdEditText.setError(null);
        }
        if (mConsultationTimeEditTextString.trim().isEmpty()) {
            mConsultationTimeEditText.setError("please enter your consultation time");
            valid = false;
        } else {
            mConsultationTimeEditText.setError(null);
        }
        if (mSpecialitySpinnerArray.size() < 1) {
            Helpers.showSnackBar(getView(), getResources().getString(R.string.no_speciality_selected));
            valid = false;
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
        Log.i("TAG", "key image url " + AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_IMAGE_URL));
        if (!AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_IMAGE_URL).trim().isEmpty()
                && !UserBasicInfoStepOne.imageUrl.trim().isEmpty()) {
            data.append(FormData.TYPE_CONTENT_FILE, "photo",
                    UserBasicInfoStepOne.imageUrl);
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
            if (UserBasicInfoStepOne.imageUrl.equals("")) {
                data.append(FormData.TYPE_CONTENT_TEXT, "photo",
                        UserBasicInfoStepOne.imageUrl);
            }
        }
        Log.i("TAG", mSpecialitySpinnerArray.toString());
        Log.i("TAG", mAffiliatedClinicsSpinnerValueString);
        data.append(FormData.TYPE_CONTENT_TEXT, "state", mStatesSpinnerValueString);
        data.append(FormData.TYPE_CONTENT_TEXT, "city", mCitiesSpinnerValueString);
        data.append(FormData.TYPE_CONTENT_TEXT, "speciality", mSpecialitySpinnerArray.toString());
        data.append(FormData.TYPE_CONTENT_TEXT, "affiliate_clinic", mAffiliatedClinicsSpinnerValueString);
        data.append(FormData.TYPE_CONTENT_TEXT, "subscription_plan", mSubscriptionSpinnerValueString);
        data.append(FormData.TYPE_CONTENT_TEXT, "phone_number_primary", mPhoneOneEditTextString);
        data.append(FormData.TYPE_CONTENT_TEXT, "phone_number_secondary", mPhoneTwoEditTextString);
        data.append(FormData.TYPE_CONTENT_TEXT, "consultation_time", mConsultationTimeEditTextString);
        data.append(FormData.TYPE_CONTENT_TEXT, "college_id", mCollegeIdEditTextString);
        data.append(FormData.TYPE_CONTENT_TEXT, "show_notification", mNotificationCheckBoxString);
        data.append(FormData.TYPE_CONTENT_TEXT, "show_news", mNewsCheckBoxString);
        mRequest = new HttpRequest(getActivity().getApplicationContext());
        mRequest.setOnReadyStateChangeListener(this);
        mRequest.setOnErrorListener(this);
        mRequest.setOnFileUploadProgressListener(this);
        String method = "POST";
        if (AppGlobals.isLogin() && AppGlobals.isInfoAvailable()) {
            method = "PUT";
        }
        mRequest.open(method, String.format("%sprofile", AppGlobals.BASE_URL));
        mRequest.setRequestHeader("Authorization", "Token " +
                AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN));
        mRequest.send(data);
    }

    private void getAffiliateClinic() {
        HttpRequest affiliateClinicRequest = new HttpRequest(getActivity().getApplicationContext());
        affiliateClinicRequest.setOnReadyStateChangeListener(new HttpRequest.OnReadyStateChangeListener() {
            @Override
            public void onReadyStateChange(HttpRequest request, int readyState) {
                switch (readyState) {
                    case HttpRequest.STATE_DONE:
                        switch (request.getStatus()) {
                            case HttpURLConnection.HTTP_OK:
                                try {
                                    JSONObject spObject = new JSONObject(request.getResponseText());
                                    JSONArray spArray = spObject.getJSONArray("results");
                                    for (int i = 0; i < spArray.length(); i++) {
                                        JSONObject jsonObject = spArray.getJSONObject(i);
                                        AffiliateClinic affiliateClinic = new AffiliateClinic();
                                        affiliateClinic.setId(jsonObject.getInt("id"));
                                        if (getDoctorProfileIds(AppGlobals.KEY_CLINIC_SELECTED)
                                                == jsonObject.getInt("id")) {
                                            affiliateClinicPosition = i;
                                        }
                                        affiliateClinic.setName(jsonObject.getString("name"));
                                        affiliateClinicsList.add(affiliateClinic);
                                    }
                                    affiliateClinicAdapter = new AffiliateClinicAdapter(
                                            getActivity(), affiliateClinicsList);
                                    mAffiliatedClinicsSpinner.setAdapter(affiliateClinicAdapter);
                                    mAffiliatedClinicsSpinner.setSelection(affiliateClinicPosition);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                        }
                }
            }
        });
        affiliateClinicRequest.open("GET", String.format("%sclinics/", AppGlobals.BASE_URL));
        affiliateClinicRequest.send();
    }

    private void getSpecialities() {
        HttpRequest specialitiesRequest = new HttpRequest(getActivity().getApplicationContext());
        specialitiesRequest.setOnReadyStateChangeListener(new HttpRequest.OnReadyStateChangeListener() {
            @Override
            public void onReadyStateChange(HttpRequest request, int readyState) {
                switch (readyState) {
                    case HttpRequest.STATE_DONE:
                        switch (request.getStatus()) {
                            case HttpURLConnection.HTTP_OK:
                                List<String> speciality = new ArrayList<>();
                                try {
                                    JSONObject spObject = new JSONObject(request.getResponseText());
                                    JSONArray spArray = spObject.getJSONArray("results");
                                    List<String> savedSpecialities = new ArrayList<>();
                                    Set<String> specialitiesSaved = AppGlobals.getSpecialityFromSharedPreferences();
                                    Log.i("TAG", "speciality "+ specialitiesSaved.size());
                                    for (String string: specialitiesSaved) {
                                        savedSpecialities.add(string);
                                        Log.i("TAG", "speciality "+ string);
                                    }
                                    if (AppGlobals.isDoctor() && AppGlobals.isInfoAvailable()) {
                                        String[] specialityIds = AppGlobals.getDoctorSpecialities().replace("[", "").replace("]", "").split(",");
                                        for (int j = 0; j < specialityIds.length; j++) {
                                            Log.i("TAG", "loop " + specialityIds[j]);
                                            mSpecialitySpinnerArray.add(Integer.valueOf(specialityIds[j].trim()));
                                        }
                                    }
                                    for (int i = 0; i < spArray.length(); i++) {
                                        JSONObject jsonObject = spArray.getJSONObject(i);
                                        Specialities specialities = new Specialities();
                                        specialities.setSpecialitiesId(jsonObject.getInt("id"));
//                                        String specialityId = jsonObject.getString("id");
                                        specialities.setSpeciality(jsonObject.getString("name"));
                                        speciality.add(jsonObject.getString("name"));
                                        specialitiesList.add(specialities);
//                                        AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_KEY_SPECIALITY_ID, specialityId);
                                    }

//                                    specialitiesAdapter = new SpecialitiesAdapter(getActivity(), specialitiesList);
//                                    mSpecialitySpinner.setAdapter(specialitiesAdapter);
                                    mSpecialitySpinner.setItems(speciality);
                                    mSpecialitySpinner.setSelection(savedSpecialities);
                                    Log.i("TAG", "saved_ " + savedSpecialities);
                                    mSpecialitySpinner.setListener(new MultiSelectionSpinner.OnMultipleItemsSelectedListener() {
                                        @Override
                                        public void selectedIndices(List<Integer> indices) {
                                            Log.i("selectedIndices", indices.toString());
                                        }

                                        @Override
                                        public void selectedStrings(List<String> strings) {
                                            mSpecialitySpinnerArray = new ArrayList<>();
                                            // here
                                            for (String selected : strings) {
                                                for (int i =0; i < specialitiesList.size(); i++) {
                                                    Specialities specialities = specialitiesList.get(i);
                                                    if (selected.equals(specialities.getSpeciality())) {
                                                        Log.i("selectedStrings", "Matched");
                                                        mSpecialitySpinnerArray.add(specialities.getSpecialitiesId());
                                                        System.out.println(specialities.getSpecialitiesId());
                                                        AppGlobals.saveDoctorSpecialities(String.valueOf(mSpecialitySpinnerArray));
                                                    }
                                                }
                                            }

                                        }
                                    });
//                                    mSpecialitySpinner.setSelection(specialistPosition);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                        }
                }
            }
        });

        specialitiesRequest.open("GET", String.format("%sspecialities", AppGlobals.BASE_URL));
        specialitiesRequest.send();
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
                                                getDoctorProfileIds(
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
        getCitiesRequest.setOnReadyStateChangeListener(
                new HttpRequest.OnReadyStateChangeListener() {
                    @Override
                    public void onReadyStateChange(HttpRequest request, int readyState) {
                        switch (readyState) {
                            case HttpRequest.STATE_DONE:
                                switch (request.getStatus()) {
                                    case HttpURLConnection.HTTP_OK:
                                        try {
                                            JSONObject object = new JSONObject(request.getResponseText());
                                            JSONArray jsonArray = object.getJSONArray("results");
                                            citiesList = new ArrayList<>();
                                            for (int i = 0; i < jsonArray.length(); i++) {
                                                System.out.println("Test " + jsonArray.getJSONObject(i));
                                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                                Cities cities = new Cities();
                                                cities.setCityId(jsonObject.getInt("id"));
                                                if (getDoctorProfileIds(AppGlobals.KEY_CITY_SELECTED) ==
                                                        jsonObject.getInt("id")) {
                                                    cityPosition = i;
                                                }
                                                cities.setCityName(jsonObject.getString("name"));
                                                cities.setStateId(jsonObject.getInt("id"));
//                                                cities.setStateName(jsonObject.getString("state_name"));
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

    private void getSubscriptionType() {
        HttpRequest getsubTypeRequest = new HttpRequest(getActivity().getApplicationContext());
        getsubTypeRequest.setOnReadyStateChangeListener(new HttpRequest.OnReadyStateChangeListener() {
            @Override
            public void onReadyStateChange(HttpRequest request, int readyState) {
                switch (readyState) {
                    case HttpRequest.STATE_DONE:
                        switch (request.getStatus()) {
                            case HttpURLConnection.HTTP_OK:
                                try {
                                    JSONObject object = new JSONObject(request.getResponseText());
                                    JSONArray jsonArray = object.getJSONArray("results");
                                    citiesList = new ArrayList<>();
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        System.out.println("Test " + jsonArray.getJSONObject(i));
                                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                                        SubscriptionType subscriptionType = new SubscriptionType();
                                        subscriptionType.setPlanType(jsonObject.getString("plan_type"));
                                        subscriptionType.setDescription(jsonObject.getString("description"));
                                        subscriptionType.setPrice(BigDecimal.valueOf(jsonObject.getDouble("price")).floatValue());
                                        subscriptionType.setId(jsonObject.getInt("id"));
                                        if (getDoctorProfileIds(AppGlobals.KEY_SUBSCRIPTION_SELECTED)
                                                == jsonObject.getInt("id")) {
                                            subscriptionPosition = i;
                                            mSubscriptionSpinnerValueString  = String
                                                    .valueOf(jsonObject.getInt("id"));
                                        }
                                        subscriptionTypesList.add(subscriptionType);
                                    }
                                    subscriptionTypeAdapter = new SubscriptionTypeAdapter(
                                            getActivity(), subscriptionTypesList);
                                    mSubscriptionSpinner.setAdapter(subscriptionTypeAdapter);
                                    mSubscriptionSpinner.setSelection(subscriptionPosition);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                        }
                }
            }
        });
        getsubTypeRequest.open("GET", String.format("%ssubscriptions/", AppGlobals.BASE_URL, id));
        getsubTypeRequest.send();
    }

    @Override
    public void onReadyStateChange(HttpRequest request, int readyState) {
        switch (readyState) {
            case HttpRequest.STATE_DONE:
                Log.i("TAG", "res" + request.getResponseText());
                if (alertDialog != null) {
                    alertDialog.dismiss();
                } else {
                    Helpers.dismissProgressDialog();
                }
                switch (request.getStatus()) {
                    case HttpRequest.ERROR_NETWORK_UNREACHABLE:
                        AppGlobals.alertDialog(getActivity(), getResources().getString(R.string.profile_update_failed),
                                getResources().getString(R.string.check_internet));
                        break;
                    case HttpURLConnection.HTTP_NOT_FOUND:
                        AppGlobals.alertDialog(getActivity(), getResources().getString(R.string.profile_update_failed),
                                getResources().getString(R.string.provide_valid_email));
                        break;
                    case HttpURLConnection.HTTP_UNAUTHORIZED:
                        if (AppGlobals.isLogin() && AppGlobals.isInfoAvailable()) {
                            AppGlobals.alertDialog(getActivity(), getResources().getString(R.string.account_inactive),
                                    getResources().getString(R.string.inactive_message));
                        } else
                            AppGlobals.alertDialog(getActivity(), getResources().getString(R.string.profile_update_failed),
                                    getResources().getString(R.string.check_password));
                        break;

                    case HttpURLConnection.HTTP_BAD_REQUEST:
                        Log.i("TAG", " " + request.getResponseText());
                        break;
                    case HttpURLConnection.HTTP_FORBIDDEN:
                        break;
                    case HttpURLConnection.HTTP_CREATED:
                        Log.i("TAG", "res" + request.getResponseText());
                        AppGlobals.saveDoctorSpecialities(String.valueOf(mSpecialitySpinnerArray));
                        parseServerResponse(request);
                        AppGlobals.gotInfo(true);
                        AccountManagerActivity.getInstance().finish();
                        startActivity(new Intent(getActivity(), MainActivity.class));
                        break;
                    case HttpURLConnection.HTTP_OK:
                        Log.i("TAG", request.getResponseText());
                        AppGlobals.saveDoctorSpecialities(String.valueOf(mSpecialitySpinnerArray));
                        Helpers.showSnackBar(getView(), R.string.profile_updated);
                        parseServerResponse(request);
                        MainActivity.setProfilePicture();
                        new android.os.Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (AppGlobals.isDoctor()) {
                                    MainActivity.getInstance().loadFragment(new Dashboard());
                                } else {
                                    MainActivity.getInstance().loadFragment(new DoctorsList());
                                }
                            }
                        }, 800);
                        break;
                }
        }
    }

    private void parseServerResponse(HttpRequest request) {
        try {
            JSONObject jsonObject = new JSONObject(request.getResponseText());

            String userId = jsonObject.getString("user");
            String profileId = jsonObject.getString("id");
            String firstName = jsonObject.getString(AppGlobals.KEY_FIRST_NAME);
            if (jsonObject.isNull("subscription_expiry_date")) {
                AppGlobals.saveSubscriptionState("Subscription Exp: Doctor inactive");
            }
            String lastName = jsonObject.getString(AppGlobals.KEY_LAST_NAME);

            String gender = jsonObject.getString(AppGlobals.KEY_GENDER);
            String dateOfBirth = jsonObject.getString(AppGlobals.KEY_DATE_OF_BIRTH);
            String phoneNumberPrimary = jsonObject.getString(AppGlobals.KEY_PHONE_NUMBER_PRIMARY);
            String phoneNumberSecondary = jsonObject.getString(AppGlobals.KEY_PHONE_NUMBER_SECONDARY);

            JSONObject affiliateClinicJsonObject = jsonObject.getJSONObject(AppGlobals.KEY_AFFILIATE_CLINIC);
            String affiliateClinic = affiliateClinicJsonObject.getString("name");
            JSONObject subscriptionTypeJsonObject = jsonObject.getJSONObject(AppGlobals.KEY_SUBSCRIPTION_TYPE);
            String subscriptionType = subscriptionTypeJsonObject.getString("plan_type");
            JSONArray specialityJsonArray = jsonObject.getJSONArray("speciality");
            Set<String> specialities = new HashSet<>();
            for (int i = 0; i < specialityJsonArray.length(); i++) {
                JSONObject jsonObject1 = specialityJsonArray.getJSONObject(i);
                specialities.add(jsonObject1.getString("name"));
            }
            String address = jsonObject.getString(AppGlobals.KEY_ADDRESS);
            String location = jsonObject.getString(AppGlobals.KEY_LOCATION);
            boolean chatStatus = jsonObject.getBoolean(AppGlobals.KEY_CHAT_STATUS);

            String state = jsonObject.getString(AppGlobals.KEY_STATE);
            String city = jsonObject.getString(AppGlobals.KEY_CITY);
            String docId = jsonObject.getString(AppGlobals.KEY_DOC_ID);
            String collegeId = jsonObject.getString(AppGlobals.KEY_COLLEGE_ID);
            boolean showNews = jsonObject.getBoolean(AppGlobals.KEY_SHOW_NEWS);

            boolean showNotification = jsonObject.getBoolean(AppGlobals.KEY_SHOW_NOTIFICATION);
            String consultationTime = jsonObject.getString(AppGlobals.KEY_CONSULTATION_TIME);
            String reviewStars = jsonObject.getString(AppGlobals.KEY_REVIEW_STARS);
            String imageUrl = jsonObject.getString(AppGlobals.KEY_IMAGE_URL);

            //saving values
            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_USER_ID, userId);
            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_PROFILE_ID, profileId);
            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_FIRST_NAME, firstName);
            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_LAST_NAME, lastName);

            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_GENDER, gender);
            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_DATE_OF_BIRTH, dateOfBirth);
            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_PHONE_NUMBER_PRIMARY, phoneNumberPrimary);
            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_PHONE_NUMBER_SECONDARY, phoneNumberSecondary);

            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_AFFILIATE_CLINIC, affiliateClinic);
            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_SUBSCRIPTION_TYPE, subscriptionType);
            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_ADDRESS, address);
            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_LOCATION, location);
            AppGlobals.saveSpecialityToSharedPreferences(specialities);

//                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_CHAT_STATUS, chatStatus);
            AppGlobals.saveChatStatus(chatStatus);
            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_STATE, state);
            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_CITY, city);
            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_DOC_ID, docId);
//                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_SHOW_NEWS, showNews);
            AppGlobals.saveNewsState(showNews);
            AppGlobals.saveNotificationState(showNotification);
//                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_SHOW_NOTIFICATION, showNotification);
            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_CONSULTATION_TIME, consultationTime);
            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_REVIEW_STARS, reviewStars);
            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_COLLEGE_ID, collegeId);
            AppGlobals.saveDataToSharedPreferences(AppGlobals.SERVER_PHOTO_URL, imageUrl);
            Log.i("Emergency Contact", " " + AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_EMERGENCY_CONTACT));
        } catch (JSONException e) {
            e.printStackTrace();
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
