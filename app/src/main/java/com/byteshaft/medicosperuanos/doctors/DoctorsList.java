package com.byteshaft.medicosperuanos.doctors;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.byteshaft.medicosperuanos.R;
import com.byteshaft.medicosperuanos.gettersetter.DoctorDetails;
import com.byteshaft.medicosperuanos.gettersetter.DoctorLocations;
import com.byteshaft.medicosperuanos.gettersetter.Services;
import com.byteshaft.medicosperuanos.messages.ConversationActivity;
import com.byteshaft.medicosperuanos.patients.DoctorsRoute;
import com.byteshaft.medicosperuanos.utils.AppGlobals;
import com.byteshaft.medicosperuanos.utils.FilterDialog;
import com.byteshaft.medicosperuanos.utils.Helpers;
import com.byteshaft.requests.HttpRequest;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.view.View.GONE;
import static com.byteshaft.medicosperuanos.utils.Helpers.calculationByDistance;
import static com.byteshaft.medicosperuanos.utils.Helpers.getFormattedTime;


public class DoctorsList extends Fragment implements HttpRequest.OnReadyStateChangeListener,
        HttpRequest.OnErrorListener {

    private View mBaseView;
    private ListView mListView;
    private ArrayList<String> addedDates;
    private LinearLayout searchContainer;
    private CustomAdapter customAdapter;
    private HashMap<String, Integer> showingPosition;
    private EditText toolbarSearchView;
    private HttpRequest request;
    private ArrayList<DoctorDetails> doctors;
    private TextView noDoctor;
    private Toolbar toolbar;
    private static final int LOCATION_PERMISSION = 1;
    public ArrayList<DoctorLocations> locationsArrayList;

    public static HashMap<Integer, ArrayList<Services>> sDoctorServices;
    private static DoctorsList sInstnace;

    public static DoctorsList getInstance() {
        return sInstnace;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        doctors = new ArrayList<>();
        sDoctorServices = new HashMap<>();
        locationsArrayList = new ArrayList<>();
        getDoctorList();
        sInstnace = this;
        mBaseView = inflater.inflate(R.layout.search_doctor, container, false);
        mListView = (ListView) mBaseView.findViewById(R.id.doctors_list);
        noDoctor = (TextView) mBaseView.findViewById(R.id.no_doctor);
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
                    customAdapter.notifyDataSetChanged();
                } else {
                    customAdapter.notifyDataSetChanged();
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

        setHasOptionsMenu(true);
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {


            }
        });
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                DoctorDetails doctorDetails = doctors.get(i);
                Intent intent = new Intent(getActivity(), DoctorDetailsActivity.class);
                intent.putExtra("start_time", doctorDetails.getStartTime());
                StringBuilder stringBuilder = new StringBuilder();
                if (doctorDetails.getGender().equals("M")) {
                    stringBuilder.append("Dr.");
                } else {
                    stringBuilder.append("Dra.");
                }
                stringBuilder.append(doctorDetails.getFirstName());
                stringBuilder.append(" ");
                stringBuilder.append(doctorDetails.getLastName());
                intent.putExtra("name", stringBuilder.toString());
                intent.putExtra("specialist", doctorDetails.getSpeciality());
                intent.putExtra("stars", doctorDetails.getReviewStars());
                AppGlobals.isDoctorFavourite =  doctorDetails.isFavouriteDoctor();
                intent.putExtra("block", doctorDetails.isBlocked());
                intent.putExtra("number", doctorDetails.getPrimaryPhoneNumber());
                intent.putExtra("available_to_chat", doctorDetails.isAvailableToChat());
                intent.putExtra("user", doctorDetails.getUserId());
                intent.putExtra("photo", doctorDetails.getPhotoUrl());
                intent.putExtra("location", doctorDetails.getLocation());
                startActivity(intent);
            }
        });
        return mBaseView;
    }

    private void getDoctorList() {
        Helpers.showProgressDialog(getActivity(), getResources().getString(R.string.getting_doctor_list));
        request = new HttpRequest(getActivity());
        request.setOnReadyStateChangeListener(this);
        request.setOnErrorListener(this);
        request.open("GET", String.format("%sdoctors/?start_date=%s&end_date=%s",
                AppGlobals.BASE_URL, Helpers.getDate(), Helpers.getDateNextSevenDays()));
        request.setRequestHeader("Authorization", "Token " +
                AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN));
        request.send();
    }


    @Override
    public void onPause() {
        super.onPause();
        toolbar.removeView(searchContainer);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.doctors_list_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                return true;
            case R.id.action_filter:
                FilterDialog filterDialog = new FilterDialog(getActivity());
                filterDialog.show();
                return true;
            case R.id.action_location:
                if (ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                    alertDialogBuilder.setTitle(getResources().getString(R.string.permission_dialog_title));
                    alertDialogBuilder.setMessage(getResources().getString(R.string.location_permission_for_route))
                            .setCancelable(false).setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                    LOCATION_PERMISSION);
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
                    if (Helpers.locationEnabled()) {
                        if (locationsArrayList.size() > 0) {
                            startActivity(new Intent(getActivity().getApplicationContext(), DoctorsRoute.class));
                        } else {
                            Helpers.showSnackBar(getView(), R.string.no_doctor_available);
                        }
                    } else {
                        Helpers.dialogForLocationEnableManually(getActivity());
                    }
                }
                return true;
            default:
                return false;
        }
    }

    public void getDoctorList(String startDate, String endDate, int radius, int affiliateClinicId, int specialityID) {
        Helpers.showProgressDialog(DoctorsList.getInstance().getActivity(), DoctorsList.getInstance().getString(R.string.getting_doctor_list));
        HttpRequest request = new HttpRequest(AppGlobals.getContext());
        request.setOnReadyStateChangeListener(this);
        request.setOnErrorListener(this);
        request.open("GET", String.format("%sdoctors/?start_date=%s&end_date=%s&radius=%s&speciality=%s&affiliate_clininc=%s",
                AppGlobals.BASE_URL, startDate, endDate, radius, affiliateClinicId, specialityID));
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
                        Log.i("TAG", "response " + request.getResponseText());
                        addedDates = new ArrayList<>();
                        showingPosition = new HashMap<>();
                        customAdapter = new CustomAdapter(getActivity().getApplicationContext(),
                                R.layout.doctors_search_delagete, doctors);
                        mListView.setAdapter(customAdapter);
                        try {
                            JSONObject jsonObject = new JSONObject(request.getResponseText());
                            JSONArray jsonArray = jsonObject.getJSONArray("results");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject mainJsonObject = jsonArray.getJSONObject(i);
                                String date = mainJsonObject.getString("date");
                                JSONArray doctorList = mainJsonObject.getJSONArray("doctors");
                                for (int j = 0; j < doctorList.length(); j++) {
                                    JSONObject doctorDetail = doctorList.getJSONObject(j);
                                    DoctorDetails doctorDetails = new DoctorDetails();
                                    doctorDetails.setDate(date);
                                    JSONObject speciality = doctorDetail.getJSONObject("speciality");
                                    doctorDetails.setSpeciality(speciality.getString("name"));
                                    doctorDetails.setFirstName(doctorDetail.getString("first_name"));
                                    doctorDetails.setLastName(doctorDetail.getString("last_name"));
                                    doctorDetails.setPhotoUrl(doctorDetail.getString("photo")
                                            .replace("http://localhost", AppGlobals.SERVER_IP));
                                    doctorDetails.setGender(doctorDetail.getString("gender"));
                                    doctorDetails.setLocation(doctorDetail.getString("location"));
                                    doctorDetails.setFavouriteDoctor(doctorDetail.getBoolean("is_favorite"));
                                    if (!doctorDetail.isNull("start_time")) {
                                        doctorDetails.setStartTime(getFormattedTime(doctorDetail.getString("start_time")));
                                    }
                                    doctorDetails.setBlocked(doctorDetail.getBoolean("am_i_blocked"));
                                    doctorDetails.setPrimaryPhoneNumber(doctorDetail.getString("phone_number_primary"));
                                    if (doctorDetail.has("phone_number_secondary") && !doctorDetail.isNull("phone_number_secondary")) {
                                        doctorDetails.setPhoneNumberSecondary(doctorDetail.getString("phone_number_secondary"));
                                    }
                                    doctorDetails.setReviewStars(doctorDetail.getInt("review_stars"));
                                    doctorDetails.setUserId(doctorDetail.getInt("id"));
                                    doctorDetails.setAvailableToChat(doctorDetail.getBoolean("available_to_chat"));
                                    doctors.add(doctorDetails);
                                    customAdapter.notifyDataSetChanged();
                                    JSONArray services = doctorDetail.getJSONArray("services");
                                    if (services.length() > 0) {
                                        ArrayList<com.byteshaft.medicosperuanos.gettersetter.Services> servicesArrayList = new ArrayList<>();
                                        for (int s = 0; s < services.length(); s++) {
                                            JSONObject singleService = services.getJSONObject(s);
                                            com.byteshaft.medicosperuanos.gettersetter.Services service
                                                    = new com.byteshaft.medicosperuanos.gettersetter.Services();
                                            service.setServiceId(singleService.getInt("id"));
                                            JSONObject internalObject = singleService.getJSONObject("service");
                                            service.setServiceName(internalObject.getString("name"));
                                            service.setServicePrice(singleService.getString("price"));
                                            servicesArrayList.add(service);
                                        }
                                        sDoctorServices.put(doctorDetail.getInt("id"), servicesArrayList);
                                        DoctorLocations doctorLocations = new DoctorLocations();
                                        doctorLocations.setId(doctorDetail.getInt("id"));
                                        doctorLocations.setLocation(doctorDetail.getString("location"));
                                        StringBuilder stringBuilder = new StringBuilder();
                                        if (doctorDetail.getString("gender").equals("M")) {
                                            stringBuilder.append("Dr.");
                                        } else {
                                            stringBuilder.append("Dra.");
                                        }
                                        doctorLocations.setName(stringBuilder.toString()+" "
                                                +doctorDetail.getString("first_name"));
                                        doctorLocations.setAvailableToChat(doctorDetail.getBoolean("available_to_chat"));
                                        locationsArrayList.add(doctorLocations);
                                    }
                                }
                                if (doctors.size() < 1) {
                                    mListView.setVisibility(GONE);
                                    noDoctor.setVisibility(View.VISIBLE);
                                    Helpers.showSnackBar(getView(), R.string.no_doctor_available_snack_bar);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                }
        }
        Log.i("TAG", sDoctorServices.toString());
    }

    @Override
    public void onError(HttpRequest request, int readyState, short error, Exception exception) {
//        if (exception.getLocalizedMessage())
        if (exception.getLocalizedMessage().equals("Network is unreachable")) {
            Helpers.showSnackBar(getView(), exception.getLocalizedMessage());
        }
        Helpers.dismissProgressDialog();
    }

    private class CustomAdapter extends ArrayAdapter<ArrayList<DoctorDetails>> {

        private ArrayList<DoctorDetails> doctorsList;
        private ViewHolder viewHolder;

        public CustomAdapter(Context context, int resource, ArrayList<DoctorDetails>
                doctorsList) {
            super(context, resource);
            this.doctorsList = doctorsList;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater()
                        .inflate(R.layout.doctors_search_delagete, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.circleImageView = (CircleImageView) convertView.findViewById(R.id.profile_image_view_search);
                viewHolder.name = (TextView) convertView.findViewById(R.id.name);
                viewHolder.specialist = (TextView) convertView.findViewById(R.id.specialist);
                viewHolder.distance = (TextView) convertView.findViewById(R.id.distance);
                viewHolder.review = (RatingBar) convertView.findViewById(R.id.rating_bar);
                viewHolder.chat = (ImageButton) convertView.findViewById(R.id.chat);
                viewHolder.call = (ImageButton) convertView.findViewById(R.id.call);
                viewHolder.availableTime = (TextView) convertView.findViewById(R.id.available_time);
                viewHolder.openDetailButton = (ImageButton) convertView.findViewById(R.id.open_details);
                viewHolder.dateLayout = (LinearLayout) convertView.findViewById(R.id.date_layout);
                viewHolder.date = (TextView) convertView.findViewById(R.id.date);
                viewHolder.status = (ImageView) convertView.findViewById(R.id.status);

                // setting typeface
                viewHolder.name.setTypeface(AppGlobals.typefaceNormal);
                viewHolder.specialist.setTypeface(AppGlobals.typefaceNormal);
                viewHolder.distance.setTypeface(AppGlobals.typefaceNormal);
                viewHolder.availableTime.setTypeface(AppGlobals.typefaceNormal);
                viewHolder.date.setTypeface(AppGlobals.typefaceNormal);
                
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            final DoctorDetails singleDoctor = doctorsList.get(position);
            Helpers.getBitMap(singleDoctor.getPhotoUrl(), viewHolder.circleImageView);

            if (addedDates.contains(singleDoctor.getDate()) &&
                    showingPosition.get(singleDoctor.getDate()) != position) {
                viewHolder.dateLayout.setVisibility(GONE);
            } else {
                viewHolder.dateLayout.setVisibility(View.VISIBLE);
                viewHolder.date.setText(singleDoctor.getDate().replaceAll("-", " "));
                addedDates.add(singleDoctor.getDate());
                showingPosition.put(singleDoctor.getDate(), position);
            }
            if (!singleDoctor.isAvailableToChat()) {
                viewHolder.status.setImageResource(R.mipmap.ic_offline_indicator);
            } else {
                viewHolder.status.setImageResource(R.mipmap.ic_online_indicator);
            }
            StringBuilder stringBuilder = new StringBuilder();
            if (singleDoctor.getGender().equals("M")) {
                stringBuilder.append("Dr.");
            } else {
                stringBuilder.append("Dra.");
            }
            stringBuilder.append(singleDoctor.getFirstName());
            stringBuilder.append(" ");
            stringBuilder.append(singleDoctor.getLastName());
            viewHolder.name.setText(stringBuilder.toString());
            viewHolder.specialist.setText(singleDoctor.getSpeciality());
            String[] startLocation = AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_LOCATION).split(",");
            String[] endLocation = singleDoctor.getLocation().split(",");
            viewHolder.distance.setText(" " + calculationByDistance(new LatLng(Double.parseDouble(startLocation[0]),
                    Double.parseDouble(startLocation[1])), new LatLng(Double.parseDouble(endLocation[0]),
                    Double.parseDouble(endLocation[1]))) + " km");
            viewHolder.review.setRating(singleDoctor.getReviewStars());
            viewHolder.availableTime.setText(singleDoctor.getStartTime());
            viewHolder.call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.CALL_PHONE)
                            != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.CALL_PHONE},
                                AppGlobals.CALL_PERMISSION);
                    } else {
                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + singleDoctor.getPrimaryPhoneNumber()));
                        startActivity(intent);
                    }
                }
            });
            viewHolder.chat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(getActivity().getApplicationContext(),
                            ConversationActivity.class));
                }
            });
            if (singleDoctor.isBlocked()) {
                viewHolder.chat.setEnabled(false);
            } else {
                viewHolder.chat.setEnabled(true);
            }
            return convertView;
        }


        @Override
        public int getCount() {
            return doctorsList.size();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case AppGlobals.CALL_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    Helpers.showSnackBar(getView(), R.string.permission_granted);

                } else {
                    Helpers.showSnackBar(getView(), R.string.permission_denied);
                }
                break;
        }
    }

    class ViewHolder {
        CircleImageView circleImageView;
        TextView name;
        TextView specialist;
        TextView distance;
        RatingBar review;
        ImageButton chat;
        ImageButton call;
        TextView availableTime;
        ImageButton openDetailButton;
        LinearLayout dateLayout;
        TextView date;
        ImageView status;
    }
}
