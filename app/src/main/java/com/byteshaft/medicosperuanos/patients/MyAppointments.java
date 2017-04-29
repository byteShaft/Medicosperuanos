package com.byteshaft.medicosperuanos.patients;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.byteshaft.medicosperuanos.R;
import com.byteshaft.medicosperuanos.gettersetter.PatientAppointment;
import com.byteshaft.medicosperuanos.utils.AppGlobals;
import com.byteshaft.medicosperuanos.utils.Helpers;
import com.byteshaft.requests.HttpRequest;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.byteshaft.medicosperuanos.utils.Helpers.getBitMap;


public class MyAppointments extends Fragment implements HttpRequest.OnReadyStateChangeListener,
        HttpRequest.OnErrorListener {

    private View mBaseView;
    private ListView appointmentList;
    private ArrayList<PatientAppointment> appointments;
    private LinearLayout searchContainer;
    private EditText toolbarSearchView;
    private ArrayList<PatientAppointment> searchList;

    private TextView patientName;
    private TextView patientEmail;
    private TextView patientAge;
    private CircleImageView profilePicture;
    private Toolbar toolbar;
    private HttpRequest request;
    private Adapter patientAppointmentAdapter;
    private float mUserRating;
    private String mUserReview;
    private int doctorsId;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBaseView = inflater.inflate(R.layout.patient_my_appointment, container, false);
        setHasOptionsMenu(true);
        searchContainer = new LinearLayout(getActivity());
        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        Toolbar.LayoutParams containerParams = new Toolbar.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        containerParams.gravity = Gravity.CENTER_VERTICAL;
        containerParams.setMargins(20, 20, 10, 20);
        searchContainer.setLayoutParams(containerParams);

        // Setup search view
        toolbarSearchView = new EditText(getActivity());
        toolbarSearchView.setBackgroundColor(getResources().getColor(R.color.search_background));
        // Set width / height / gravity
        int[] textSizeAttr = new int[]{android.R.attr.actionBarSize};
        int indexOfAttrTextSize = 0;
        TypedArray a = getActivity().obtainStyledAttributes(new TypedValue().data, textSizeAttr);
        int actionBarHeight = a.getDimensionPixelSize(indexOfAttrTextSize, -1);
        a.recycle();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, actionBarHeight);
        params.gravity = Gravity.CENTER_VERTICAL;
        params.setMargins(5, 5, 5, 5);
        params.weight = 1;
        toolbarSearchView.setLayoutParams(params);
        // Setup display
        toolbarSearchView.setPadding(2, 0, 0, 0);
        toolbarSearchView.setTextColor(Color.WHITE);
        toolbarSearchView.setGravity(Gravity.CENTER_VERTICAL);
        toolbarSearchView.setSingleLine(true);
        toolbarSearchView.setImeActionLabel("Search", EditorInfo.IME_ACTION_UNSPECIFIED);
        try {
            Field f = TextView.class.getDeclaredField("mCursorDrawableRes");
            f.setAccessible(true);
            f.set(toolbarSearchView, R.drawable.cursor_color);
        } catch (Exception ignored) {

        }
        // Search text changed listener
        toolbarSearchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.i("TAG", s.toString());
                if (!s.toString().isEmpty()) {
                    searchList = new ArrayList<>();
                    patientAppointmentAdapter = new Adapter(getActivity().getApplicationContext(), searchList);
                    appointmentList.setAdapter(patientAppointmentAdapter);
                    for (PatientAppointment patientAppointment : appointments) {
                        if (StringUtils.containsIgnoreCase(patientAppointment.getDrFirstName(),
                                s.toString()) ||
                                StringUtils.containsIgnoreCase(patientAppointment.getDrLastName(),
                                s.toString()) ||
                                StringUtils.containsIgnoreCase(patientAppointment.getDrSpeciality(),
                                        s.toString()) ) {
                            searchList.add(patientAppointment);
                            patientAppointmentAdapter.notifyDataSetChanged();

                        }
                    }
                } else {
                    searchList = new ArrayList<>();
                    patientAppointmentAdapter = new Adapter(getActivity().getApplicationContext(),
                            appointments);
                    appointmentList.setAdapter(patientAppointmentAdapter);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        toolbarSearchView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                toolbarSearchView.setFocusable(true);
                toolbarSearchView.setFocusableInTouchMode(true);
                return false;
            }
        });
        toolbarSearchView.setFocusableInTouchMode(false);
        toolbarSearchView.setFocusable(false);
        toolbarSearchView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b) {

                } else {

                }
            }
        });
        (searchContainer).addView(toolbarSearchView);

        // Setup the clear button
        Resources r = getResources();
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, r.getDisplayMetrics());
        LinearLayout.LayoutParams clearParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        clearParams.gravity = Gravity.CENTER;
        // Add search view to toolbar and hide it
        toolbar.addView(searchContainer);
        appointments = new ArrayList<>();
        appointmentList = (ListView) mBaseView.findViewById(R.id.patient_appointment);
        patientAppointmentAdapter = new Adapter(getContext(), appointments);
        appointmentList.setAdapter(patientAppointmentAdapter);
        patientName = (TextView) mBaseView.findViewById(R.id.patient_name_dashboard);
        patientEmail = (TextView) mBaseView.findViewById(R.id.patient_email);
        patientAge = (TextView) mBaseView.findViewById(R.id.patient_age);
        profilePicture = (CircleImageView) mBaseView.findViewById(R.id.patient_image);

        //setting typeface
        patientName.setTypeface(AppGlobals.typefaceNormal);
        patientEmail.setTypeface(AppGlobals.typefaceNormal);
        patientAge.setTypeface(AppGlobals.typefaceNormal);

        // setting up information
        patientName.setText(AppGlobals.getStringFromSharedPreferences(
                AppGlobals.KEY_FIRST_NAME) + " " +
                AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_LAST_NAME));
        patientEmail.setText(AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_EMAIL));
        if (AppGlobals.isLogin() && AppGlobals.getStringFromSharedPreferences(AppGlobals.SERVER_PHOTO_URL) != null) {
            String url = String.format("%s" + AppGlobals
                    .getStringFromSharedPreferences(AppGlobals.SERVER_PHOTO_URL), AppGlobals.SERVER_IP);
            getBitMap(url, profilePicture);
        }
        String years = Helpers.calculateAge(AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_DATE_OF_BIRTH));
        patientAge.setText(years + " years");
        appointmentList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PatientAppointment patientAppointment = appointments.get(position);
                if (patientAppointment.getState().equals("accepted")) {
                    doctorsId = patientAppointment.getDoctorsId();
                    System.out.println(doctorsId + "doctors id");
                    reviewRatingBarDialog();
                }

            }
        });

        return mBaseView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getPatientAppointments();
    }

    @Override
    public void onPause() {
        super.onPause();
        toolbar.removeView(searchContainer);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.search_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void getPatientAppointments() {
        Helpers.showProgressDialog(getActivity(),
                getResources().getString(R.string.fetching_my_appointments));
        request = new HttpRequest(getActivity());
        request.setOnReadyStateChangeListener(this);
        request.setOnErrorListener(this);
        request.open("GET", String.format("%spatient/appointments/", AppGlobals.BASE_URL));
        request.setRequestHeader("Authorization", "Token " +
                AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN));
        request.send();
    }

    @Override
    public void onReadyStateChange(HttpRequest request, int readyState) {
        switch (readyState) {
            case HttpRequest.STATE_DONE:
                Helpers.dismissProgressDialog();
                switch (request.getStatus()) {
                    case HttpURLConnection.HTTP_OK:
                        Log.i("TAG", "patient appointments " + request.getResponseText());
                        try {
                            JSONObject jsonObject = new JSONObject(request.getResponseText());
                            JSONArray jsonArray = jsonObject.getJSONArray("results");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject appointmentObject = jsonArray.getJSONObject(i);
                                PatientAppointment appointment = new PatientAppointment();
                                appointment.setDate(appointmentObject.getString("created_at"));
                                JSONObject doctorObject = appointmentObject.getJSONObject("doctor");
                                appointment.setDrFirstName(doctorObject.getString("first_name"));
                                appointment.setDrLastName(doctorObject.getString("last_name"));
                                appointment.setDoctorsId(doctorObject.getInt("id"));
                                appointment.setGender(doctorObject.getString("gender"));
                                JSONObject specialityJsonObject = doctorObject.getJSONObject("speciality");
                                appointment.setDrSpeciality(specialityJsonObject.getString("name"));
                                JSONArray serviceArray = appointmentObject.getJSONArray("services");
                                for (int j = 0; j < serviceArray.length(); j++) {
                                    JSONObject service = serviceArray.getJSONObject(j);
                                    JSONObject serviceDetail = service.getJSONObject("service");
                                    appointment.setServiceName(serviceDetail.getString("name"));
                                }
                                appointment.setAppointmentTime(appointmentObject.getString("start_time"));
                                appointment.setState(appointmentObject.getString("state"));
                                appointments.add(appointment);
                                patientAppointmentAdapter.notifyDataSetChanged();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                }
        }
    }

    @Override
    public void onError(HttpRequest request, int readyState, short error, Exception exception) {
        Helpers.dismissProgressDialog();
    }

    private class Adapter extends ArrayAdapter<PatientAppointment> {

        private ArrayList<PatientAppointment> appointmentsList;
        private ViewHolder viewHolder;

        public Adapter(Context context, ArrayList<PatientAppointment> appointmentsList) {
            super(context, R.layout.delegate_p_appointment_history);
            this.appointmentsList = appointmentsList;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.delegate_p_appointment_history,
                        parent, false);
                viewHolder = new ViewHolder();
                viewHolder.appointmentDate = (TextView) convertView.findViewById(R.id.appointment_date);
                viewHolder.appointmentTime = (TextView) convertView.findViewById(R.id.appointment_time);
                viewHolder.doctorName = (TextView) convertView.findViewById(R.id.doctor_name);
                viewHolder.serviceDescription = (TextView) convertView.findViewById(R.id.service_description);
                viewHolder.appointmentStatus = (TextView) convertView.findViewById(R.id.appointment_status);

                // setting typeface
                viewHolder.appointmentDate.setTypeface(AppGlobals.typefaceNormal);
                viewHolder.appointmentTime.setTypeface(AppGlobals.typefaceNormal);
                viewHolder.doctorName.setTypeface(AppGlobals.robotoBold);
                viewHolder.serviceDescription.setTypeface(AppGlobals.typefaceNormal);
                viewHolder.appointmentStatus.setTypeface(AppGlobals.typefaceNormal);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            PatientAppointment patientAppointment = appointmentsList.get(position);
            SimpleDateFormat formatterFrom = new SimpleDateFormat("dd/MM/yyyy hh:mm");
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            Date formattedDate = null;
            try {
                formattedDate = formatterFrom.parse(patientAppointment.getDate());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            viewHolder.appointmentDate.setText(dateFormat.format(formattedDate));
            viewHolder.appointmentTime.setText(patientAppointment.getAppointmentTime());
            StringBuilder stringBuilder = new StringBuilder();
            if (patientAppointment.getGender().equals("M")) {
                stringBuilder.append("Dr.");
            } else {
                stringBuilder.append("Dra.");
            }
            stringBuilder.append(patientAppointment.getDrFirstName());
            stringBuilder.append(" ");
            stringBuilder.append(patientAppointment.getDrLastName());
            viewHolder.doctorName.setText(stringBuilder.toString() + " " + patientAppointment.getDrSpeciality());

            TextPaint paint = viewHolder.doctorName.getPaint();
            Rect rect = new Rect();
            String text = String.valueOf(viewHolder.doctorName.getText());
            paint.getTextBounds(text, 0, text.length(), rect);
            if (rect.height() > viewHolder.doctorName.getHeight() || rect.width() >
                    viewHolder.doctorName.getWidth()) {
                Log.i("My Appointments", "Your text is too large");
                String specialist;
                if (patientAppointment.getDrSpeciality().length() > 7) {
                    specialist = patientAppointment.getDrSpeciality().substring(0, 7
                    ).trim() + "…";
                } else {
                    specialist = patientAppointment.getDrSpeciality();
                }
                viewHolder.doctorName.setText(patientAppointment.getDrFirstName() + " - " + specialist);

            }

            viewHolder.serviceDescription.setText(patientAppointment.getServiceName());
            Log.i("TAG", patientAppointment.getState());
            switch (patientAppointment.getState()) {
                case "accepted":
                    viewHolder.appointmentStatus.setText("A");
                    viewHolder.appointmentStatus.setBackgroundColor(getResources()
                            .getColor(R.color.attended_background_color));
                    break;
                case "rejected":
                    viewHolder.appointmentStatus.setText("C");
                    viewHolder.appointmentStatus.setBackgroundColor(getResources()
                            .getColor(R.color.cancel_background_color));
                    break;
                case "pending":
                    viewHolder.appointmentStatus.setText("P");
                    viewHolder.appointmentStatus.setBackgroundColor(getResources()
                            .getColor(R.color.pending_background_color));
                    break;

            }
            return convertView;
        }

        @Override
        public int getCount() {
            return appointmentsList.size();
        }
    }

    private class ViewHolder {
        TextView appointmentDate;
        TextView appointmentTime;
        TextView doctorName;
        TextView serviceDescription;
        TextView appointmentStatus;
    }

    public void reviewRatingBarDialog() {
        final Dialog dialog = new Dialog(getActivity(), R.style.FullHeightDialog);
        dialog.setContentView(R.layout.review_rating_bar_dialoge);
        dialog.setCancelable(false);
        Button submitBtn = (Button) dialog.findViewById(R.id.submitRateBtn);
        Button cancelBtn = (Button) dialog.findViewById(R.id.cancelRateBtn);
        RatingBar ratingBar = (RatingBar) dialog.findViewById(R.id.dialog_ratingbar);
        final EditText reviewEditText = (EditText) dialog.findViewById(R.id.reviewED);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                mUserRating = rating;

            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUserReview = reviewEditText.getText().toString();
                userReview(doctorsId, mUserRating, mUserReview);
                dialog.dismiss();

            }
        });
        dialog.show();
    }

    private void userReview(int doctorsId, float stars, String review) {
        request = new HttpRequest(getActivity());
        request.setOnReadyStateChangeListener(new HttpRequest.OnReadyStateChangeListener() {
            @Override
            public void onReadyStateChange(HttpRequest request, int readyState) {
                switch (readyState) {
                    case HttpRequest.STATE_DONE:
                        Helpers.dismissProgressDialog();
                        switch (request.getStatus()) {
                            case HttpURLConnection.HTTP_CREATED:
                                AppGlobals.alertDialog(getActivity(), getString(R.string.review_submitted), getString(R.string.review_submitted_successfully));
                                break;
                            case HttpURLConnection.HTTP_BAD_REQUEST:
                                AppGlobals.alertDialog(getActivity(), getString(R.string.review_failed), getString(R.string.review_submitted_failed));
                                break;

                        }
                }

            }
        });
        request.setOnErrorListener(new HttpRequest.OnErrorListener() {
            @Override
            public void onError(HttpRequest request, int readyState, short error, Exception exception) {

            }
        });
        request.open("POST", String.format("%sdoctors/%s/review", AppGlobals.BASE_URL, doctorsId));
        request.setRequestHeader("Authorization", "Token " +
                AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN));
        request.send(getUserReviewData(stars, review));
        Helpers.showProgressDialog(getActivity(), "Submitting your review...");
    }


    private String getUserReviewData(float stars, String review) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("stars", stars);
            jsonObject.put("message", review);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();

    }
}
