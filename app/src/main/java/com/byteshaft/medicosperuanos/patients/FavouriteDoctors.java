package com.byteshaft.medicosperuanos.patients;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.byteshaft.medicosperuanos.R;
import com.byteshaft.medicosperuanos.gettersetter.DoctorLocations;
import com.byteshaft.medicosperuanos.gettersetter.FavoriteDoctorsList;
import com.byteshaft.medicosperuanos.gettersetter.Services;
import com.byteshaft.medicosperuanos.gettersetter.TimeSlots;
import com.byteshaft.medicosperuanos.utils.AppGlobals;
import com.byteshaft.medicosperuanos.utils.FilterDialog;
import com.byteshaft.medicosperuanos.utils.Helpers;
import com.byteshaft.requests.HttpRequest;
import com.google.android.gms.maps.model.LatLng;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.byteshaft.medicosperuanos.utils.Helpers.calculationByDistance;


public class FavouriteDoctors extends Fragment implements HttpRequest.OnReadyStateChangeListener,
        HttpRequest.OnErrorListener, View.OnClickListener {

    private View mBaseView;
    private ListView mListView;
    private ArrayList<FavoriteDoctorsList> favoriteDoctorsList;
    private ArrayList<FavoriteDoctorsList> searchList;
    private LinearLayout searchContainer;
    private CustomAdapter customAdapter;
    private Toolbar toolbar;
    private HttpRequest request;
    private HashMap<Integer, ArrayList<TimeSlots>> slotsList;
    private ImageButton backwardCalender;
    private ImageButton farwardCalender;
    private TextView currentDay;
    private Calendar currentDate = Calendar.getInstance();
    private HashMap<Integer, ArrayList<Services>> sFavtDoctorServices;
    private int mainLayoutPosition = -1;
    private static FavouriteDoctors sInstance;
    private static final int LOCATION_PERMISSION = 1;
    public ArrayList<DoctorLocations> locationsArrayList;

    public static FavouriteDoctors getsInstance() {
        return sInstance;
    }

    @SuppressLint("UseSparseArrays")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBaseView = inflater.inflate(R.layout.favourite_doctors, container, false);
        mListView = (ListView) mBaseView.findViewById(R.id.favt_doctors_list);
        farwardCalender = (ImageButton) mBaseView.findViewById(R.id.forward_calendar);
        backwardCalender = (ImageButton) mBaseView.findViewById(R.id.go_back_calendar);
        currentDay = (TextView) mBaseView.findViewById(R.id.current_date);
        sInstance = this;
        locationsArrayList = new ArrayList<>();
        updateDate();
        farwardCalender.setOnClickListener(this);
        backwardCalender.setOnClickListener(this);
        searchContainer = new LinearLayout(getActivity());
        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        Toolbar.LayoutParams containerParams = new Toolbar.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        containerParams.gravity = Gravity.CENTER_VERTICAL;
        containerParams.setMargins(20, 20, 10, 20);
        searchContainer.setLayoutParams(containerParams);
        // Setup search view
        EditText toolbarSearchView = new EditText(getActivity());
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
                    customAdapter = new CustomAdapter(getActivity().getApplicationContext(),
                            R.layout.doctors_search_delagete, searchList);
                    mListView.setAdapter(customAdapter);
                    for (FavoriteDoctorsList doctorDetails : favoriteDoctorsList) {
                        if (StringUtils.containsIgnoreCase(doctorDetails.getDoctorsName(),
                                s.toString()) || StringUtils.containsIgnoreCase(doctorDetails.getLastName(),
                                s.toString()) ||
                                StringUtils.containsIgnoreCase(doctorDetails.getSpeciality(),
                                        s.toString())) {
                            searchList.add(doctorDetails);
                            customAdapter.notifyDataSetChanged();

                        }
                    }
                } else {
                    searchList = new ArrayList<>();
                    customAdapter = new CustomAdapter(getActivity().getApplicationContext(),
                            R.layout.doctors_search_delagete, favoriteDoctorsList);
                    mListView.setAdapter(customAdapter);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
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
//        toolbar.addView(searchContainer);
        favoriteDoctorsList = new ArrayList<>();
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
                Log.i("TAG", "click");
                mainLayoutPosition = i;

            }
        });
        return mBaseView;
    }

    @Override
    public void onResume() {
        super.onResume();
        toolbar.addView(searchContainer);
        geFavoriteDoctorsList();

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
                FilterDialog filterDialog = new FilterDialog(getActivity(), true);
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
                            Intent intent = new Intent(getActivity().getApplicationContext(), DoctorsRoute.class);
                            intent.putExtra("location_array", locationsArrayList);
                            startActivity(intent);
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

    public void geFavoriteDoctorsList() {
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        Helpers.showProgressDialog(getActivity(), getResources().getString(R.string.getting_favourite_doctors));
        request = new HttpRequest(getActivity());
        request.setOnReadyStateChangeListener(this);
        request.setOnErrorListener(this);
        request.open("GET", String.format("%spatient/doctors/?date=%s", AppGlobals.BASE_URL, df.format(currentDate.getTime())));
        request.setRequestHeader("Authorization", "Token " +
                AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN));
        request.send();
    }

    public void getFavDoctorList(String startDate, String endDate, int radius, int affiliateClinicId, int specialityID) {
        Helpers.showProgressDialog(getActivity(), FavouriteDoctors.getsInstance().getString(R.string.getting_doctor_list));
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
    public void onError(HttpRequest request, int readyState, short error, Exception exception) {
        Helpers.dismissProgressDialog();
    }

    @Override
    public void onReadyStateChange(HttpRequest request, int readyState) {
        switch (readyState) {
            case HttpRequest.STATE_DONE:
                Helpers.dismissProgressDialog();
                switch (request.getStatus()) {
                    case HttpURLConnection.HTTP_OK:
                        System.out.println(request.getResponseText());
                        if (request.getResponseText().trim().isEmpty()) {
                            return;
                        }
                        slotsList = new HashMap<>();
                        sFavtDoctorServices = new HashMap<>();
                        favoriteDoctorsList = new ArrayList<>();
                        customAdapter = new CustomAdapter(getActivity().getApplicationContext(),
                                R.layout.favt_doc_delegate, favoriteDoctorsList);
                        mListView.setAdapter(customAdapter);
                        try {
                            JSONArray jsonArray = new JSONArray(request.getResponseText());
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                com.byteshaft.medicosperuanos.gettersetter.FavoriteDoctorsList myFavoriteDoctorsList
                                        = new com.byteshaft.medicosperuanos.gettersetter.FavoriteDoctorsList();
                                StringBuilder stringBuilder = new StringBuilder();
                                if (jsonObject.getString("gender").equals("M")) {
                                    stringBuilder.append("Dr.");
                                } else {
                                    stringBuilder.append("Dra.");
                                }
                                myFavoriteDoctorsList.setFirstName(jsonObject.getString("first_name"));
                                myFavoriteDoctorsList.setLastName(jsonObject.getString("last_name"));
                                stringBuilder.append(jsonObject.getString("first_name"));
                                stringBuilder.append(" ");
                                stringBuilder.append(jsonObject.getString("last_name"));
                                myFavoriteDoctorsList.setDoctorsName(stringBuilder.toString());
                                myFavoriteDoctorsList.setDoctorsLocation(jsonObject.getString("location"));
                                myFavoriteDoctorsList.setId(jsonObject.getInt("id"));
                                myFavoriteDoctorsList.setBlocked(jsonObject.getBoolean("am_i_blocked"));
                                myFavoriteDoctorsList.setFavorite(jsonObject.getBoolean("is_favorite"));
                                myFavoriteDoctorsList.setLocation(jsonObject.getString("location"));
                                myFavoriteDoctorsList.setAvailableToChat(jsonObject.getBoolean("available_to_chat"));
                                JSONObject specialityJsonObject = jsonObject.getJSONObject("speciality");
                                myFavoriteDoctorsList.setSpeciality(specialityJsonObject.getString("name"));
                                myFavoriteDoctorsList.setDoctorImage(jsonObject.getString("photo").replace("http://localhost", AppGlobals.SERVER_IP));
                                myFavoriteDoctorsList.setStars(jsonObject.getInt("review_stars"));
                                JSONArray services = jsonObject.getJSONArray("services");
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
                                    sFavtDoctorServices.put(jsonObject.getInt("id"), servicesArrayList);
                                }
                                DoctorLocations doctorLocations = new DoctorLocations();
                                doctorLocations.setId(jsonObject.getInt("id"));
                                doctorLocations.setLocation(jsonObject.getString("location"));
                                StringBuilder string = new StringBuilder();
                                if (jsonObject.getString("gender").equals("M")) {
                                    string.append("Dr.");
                                } else {
                                    string.append("Dra.");
                                }
                                doctorLocations.setName(string.toString()+" "
                                        +jsonObject.getString("first_name"));
                                doctorLocations.setAvailableToChat(jsonObject.getBoolean("available_to_chat"));
                                locationsArrayList.add(doctorLocations);
                                JSONArray dateJSONArray = jsonObject.getJSONArray("schedule");
                                for (int j = 0; j < dateJSONArray.length(); j++) {
                                    JSONObject dateJObject = dateJSONArray.getJSONObject(j);
                                    myFavoriteDoctorsList.setSchduleDate(dateJObject.getString("date"));
                                    myFavoriteDoctorsList.setTimeId(dateJObject.getInt("id"));
                                    JSONArray timeJSONArray = dateJObject.getJSONArray("time_slots");
                                    ArrayList<TimeSlots> arrayList = new ArrayList<>();
                                    for (int k = 0; k < timeJSONArray.length(); k++) {
                                        JSONObject timeJsonObject = timeJSONArray.getJSONObject(k);
                                        TimeSlots timeSlots = new TimeSlots();
                                        timeSlots.setEndTime(timeJsonObject.getString("end_time"));
                                        timeSlots.setStartTime(timeJsonObject.getString("start_time"));
                                        timeSlots.setTaken(timeJsonObject.getBoolean("taken"));
                                        timeSlots.setSlotId(timeJsonObject.getInt("id"));
                                        arrayList.add(timeSlots);
                                    }
                                    favoriteDoctorsList.add(myFavoriteDoctorsList);
                                    slotsList.put(jsonObject.getInt("id"), arrayList);
                                    customAdapter.notifyDataSetChanged();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                }
        }
    }

    private void updateDate() {
        SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy");
        currentDay.setText(df.format(currentDate.getTime()));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.go_back_calendar:
                Calendar calendar = (Calendar) currentDate.clone();
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
                Date dateOne = null;
                Date dateTwo = null;
                Date dateThree = null;
                Calendar c = Calendar.getInstance();
                c.add(Calendar.DAY_OF_YEAR, 1);
                Calendar cal = Calendar.getInstance();
                try {
                    dateOne = sdf.parse(sdf.format(calendar.getTime()));
                    dateTwo = sdf.parse(sdf.format(c.getTime()));
                    dateThree = sdf.parse(sdf.format(cal.getTime()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Log.i("TAG", "one " + dateOne);
                Log.i("TAG", "two " + dateTwo);
                Log.i("TAG", "boolean " + String.valueOf(dateOne.compareTo(dateTwo) < 0));
                if (dateOne.compareTo(dateTwo) < 0) {
                    Helpers.showSnackBar(getView(), R.string.cannot_go_back_from_current_date);
                } else {
                    currentDate.add(Calendar.DAY_OF_YEAR, -1);
                    updateDate();
                    geFavoriteDoctorsList();
                }
                break;
            case R.id.forward_calendar:
                currentDate.add(Calendar.DAY_OF_YEAR, 1);
                updateDate();
                geFavoriteDoctorsList();
                break;
        }

    }

    private class CustomAdapter extends ArrayAdapter<FavoriteDoctorsList> {

        private ArrayList<FavoriteDoctorsList> favoriteDoctorsList;
        private ViewHolder viewHolder;

        public CustomAdapter(Context context, int resource, ArrayList<FavoriteDoctorsList> favoriteDoctorsList) {
            super(context, resource);
            this.favoriteDoctorsList = favoriteDoctorsList;
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater()
                        .inflate(R.layout.favt_doc_delegate, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.circleImageView = (CircleImageView) convertView.findViewById(R.id.user_image);
                viewHolder.name = (TextView) convertView.findViewById(R.id.dr_name);
                viewHolder.specialist = (TextView) convertView.findViewById(R.id.specialist);
                viewHolder.distance = (TextView) convertView.findViewById(R.id.distance);
                viewHolder.review = (RatingBar) convertView.findViewById(R.id.ratingBar);
                viewHolder.timingList = (RecyclerView) convertView.findViewById(R.id.timing_list);

                viewHolder.name.setTypeface(AppGlobals.typefaceNormal);
                viewHolder.specialist.setTypeface(AppGlobals.typefaceNormal);
                viewHolder.distance.setTypeface(AppGlobals.typefaceNormal);
                LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
                viewHolder.timingList.setLayoutManager(layoutManager);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            FavoriteDoctorsList favorite = favoriteDoctorsList.get(position);
            viewHolder.name.setText(favorite.getDoctorsName());
            String[] startLocation = favorite.getDoctorsLocation().split(",");
            String[] endLocation = AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_LOCATION).split(",");
            viewHolder.distance.setText(" " + String.valueOf(calculationByDistance(new LatLng(Double.parseDouble(startLocation[0]),
                    Double.parseDouble(startLocation[1])), new LatLng(Double.parseDouble(endLocation[0]),
                    Double.parseDouble(endLocation[1])))) + " " + "km");
            viewHolder.specialist.setText(favorite.getSpeciality());
            viewHolder.review.setRating(favorite.getStars());
            Helpers.getBitMap(favorite.getDoctorImage(), viewHolder.circleImageView);
            TimingAdapter timingAdapter = new TimingAdapter(slotsList.get(favorite.getId()));
            viewHolder.timingList.setAdapter(timingAdapter);
            viewHolder.timingList.canScrollVertically(LinearLayoutManager.VERTICAL);
            viewHolder.timingList.setHasFixedSize(true);
            viewHolder.timingList.addOnItemTouchListener(new TimingAdapter(slotsList.get(favorite.getId()), new TimingAdapter.OnItemClickListener() {
                @Override
                public void onItem(TimeSlots time) {
                    FavoriteDoctorsList doctorDetails = favoriteDoctorsList.get(position);
                    Intent intent = new Intent(getActivity(), CreateAppointmentActivity.class);
                    intent.putExtra("start_time", doctorDetails.getStartTime());
                    intent.putExtra("name", doctorDetails.getDoctorsName());
                    intent.putExtra("specialist", doctorDetails.getSpeciality());
                    intent.putExtra("stars", doctorDetails.getStars());
                    AppGlobals.isDoctorFavourite = doctorDetails.isFavorite();
                    intent.putExtra("block", doctorDetails.isBlocked());
                    intent.putExtra("number", doctorDetails.getPrimaryPhoneNumber());
                    intent.putExtra("available_to_chat", doctorDetails.isAvailableToChat());
                    intent.putExtra("user", doctorDetails.getId());
                    intent.putExtra("photo", doctorDetails.getDoctorImage());
                    intent.putExtra("location", doctorDetails.getLocation());
                    intent.putExtra("appointment_id", time.getSlotId());
                    intent.putExtra("start_time", time.getStartTime());
                    intent.putExtra("services_array", sFavtDoctorServices);
                    startActivity(intent);
                }
            }));
            return convertView;
        }

        @Override
        public int getCount() {
            return favoriteDoctorsList.size();
        }
    }

    private static class ViewHolder {
        CircleImageView circleImageView;
        TextView name;
        TextView specialist;
        TextView distance;
        RatingBar review;
        RecyclerView timingList;

    }

    static class TimingAdapter extends RecyclerView.Adapter<TimingAdapter.Holder> implements
            RecyclerView.OnItemTouchListener {

        private ArrayList<TimeSlots> timingList;
        private Holder holder;
        private OnItemClickListener mListener;
        private GestureDetector mGestureDetector;

        public TimingAdapter(ArrayList<TimeSlots> timingList, OnItemClickListener listener) {
            this.timingList = timingList;
            mListener = listener;
            mGestureDetector = new GestureDetector(AppGlobals.getContext(),
                    new GestureDetector.SimpleOnGestureListener() {
                        @Override
                        public boolean onSingleTapUp(MotionEvent e) {
                            return true;
                        }
                    });
            Log.i("TAG", "check array " + String.valueOf(this.timingList == null));

        }

        public TimingAdapter(ArrayList<TimeSlots> timingList) {
            this.timingList = timingList;
        }

        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.delegate_timing_list,
                    parent, false);
            holder = new Holder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(final Holder holder, final int position) {
            holder.setIsRecyclable(false);
            final TimeSlots timeSlots = timingList.get(position);
            holder.timeButton.setText(timeSlots.getStartTime());
            if (!timeSlots.isTaken()) {
                holder.timeButton.setBackground(AppGlobals.getContext().getResources().getDrawable(R.drawable.normal_time_slot));
            } else {
                holder.timeButton.setBackground(AppGlobals.getContext().getResources().getDrawable(R.drawable.pressed_time_slot));
            }
//            holder.timeButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    if (!timeSlots.isTaken()) {
//                        new android.os.Handler().postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                Log.i("TAG", "Inner click");
////                                FavoriteDoctorsList doctorDetails = favoriteDoctorsList.get(mainLayoutPosition);
////                                Intent intent = new Intent(getActivity(), CreateAppointmentActivity.class);
////                                intent.putExtra("start_time", doctorDetails.getStartTime());
////                                intent.putExtra("name", doctorDetails.getDoctorsName());
////                                intent.putExtra("specialist", doctorDetails.getSpeciality());
////                                intent.putExtra("stars", doctorDetails.getStars());
////                                AppGlobals.isDoctorFavourite =  doctorDetails.isFavorite();
////                                intent.putExtra("block", doctorDetails.isBlocked());
////                                intent.putExtra("number", doctorDetails.getPrimaryPhoneNumber());
////                                intent.putExtra("available_to_chat", doctorDetails.isAvailableToChat());
////                                intent.putExtra("user", doctorDetails.getId());
////                                intent.putExtra("photo", doctorDetails.getDoctorImage());
////                                intent.putExtra("location", doctorDetails.getLocation());
////                                TimeSlots time = timingList.get(position);
////                                intent.putExtra("appointment_id", time.getSlotId());
////                                intent.putExtra("start_time", time.getStartTime());
////                                startActivity(intent);
//
//                            }
//                        }, 500);
//
//                    } else {
//                        Helpers.showSnackBar(getView(), R.string.time_slot_booked);
//                    }
//                }
//            });
        }

        @Override
        public int getItemCount() {
            return timingList.size();
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View childView = rv.findChildViewUnder(e.getX(), e.getY());
            if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
                Log.i("TAG", "check listener " + String.valueOf(mListener == null));
                Log.i("TAG", "check array " + String.valueOf(timingList == null));
                Log.i("TAG", "check item " + String.valueOf(rv.getChildPosition(childView)));
                mListener.onItem(timingList.get(rv.getChildPosition(childView)));
                return true;
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }

        static class Holder extends RecyclerView.ViewHolder {
            TextView timeButton;

            public Holder(View itemView) {
                super(itemView);
                timeButton = (TextView) itemView.findViewById(R.id.time);
            }
        }

        public interface OnItemClickListener {
            void onItem(TimeSlots item);
        }
    }


}
