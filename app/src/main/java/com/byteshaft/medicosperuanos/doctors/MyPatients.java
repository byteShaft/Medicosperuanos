package com.byteshaft.medicosperuanos.doctors;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.TextView;

import com.byteshaft.medicosperuanos.R;
import com.byteshaft.medicosperuanos.messages.ConversationActivity;
import com.byteshaft.medicosperuanos.patients.PatientDetails;
import com.byteshaft.medicosperuanos.utils.AppGlobals;
import com.byteshaft.medicosperuanos.utils.Helpers;
import com.byteshaft.requests.HttpRequest;
import com.google.android.gms.maps.model.LatLng;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.byteshaft.medicosperuanos.utils.Helpers.calculationByDistance;

/**
 * Created by s9iper1 on 3/9/17.
 */

public class MyPatients extends Fragment {

    private View mBaseView;
    private ListView mListView;
    private ArrayList<com.byteshaft.medicosperuanos.gettersetter.MyPatients> myPatientsList;
    private LinearLayout searchContainer;
    private CustomAdapter customAdapter;
    private HttpRequest request;
    private Toolbar toolbar;
    private ArrayList<com.byteshaft.medicosperuanos.gettersetter.MyPatients> searchList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBaseView = inflater.inflate(R.layout.my_patients, container, false);
        mListView = (ListView) mBaseView.findViewById(R.id.patients_list);
        ((AppCompatActivity) getActivity()).getSupportActionBar()
                .setTitle(getResources().getString(R.string.my_patient));
        getPatientsDetails();
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
                            R.layout.delegate_my_patients, searchList);
                    mListView.setAdapter(customAdapter);
                    for (com.byteshaft.medicosperuanos.gettersetter.MyPatients
                            myPatient : myPatientsList) {
                        if (StringUtils.containsIgnoreCase(myPatient.getPatientsName(),
                                s.toString()) ||
                                StringUtils.containsIgnoreCase(myPatient.getPatientLastName(),
                                s.toString())) {
                            searchList.add(myPatient);
                            customAdapter.notifyDataSetChanged();

                        }
                    }
                } else {
                    searchList = new ArrayList<>();
                    customAdapter = new CustomAdapter(getActivity().getApplicationContext(),
                            R.layout.doctors_search_delagete, myPatientsList);
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
        myPatientsList = new ArrayList<>();
        customAdapter = new CustomAdapter(getActivity().getApplicationContext(),
                R.layout.doctors_search_delagete, myPatientsList);
        mListView.setAdapter(customAdapter);
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
                startActivity(new Intent(getActivity().getApplicationContext(), PatientDetails.class));
            }
        });
        return mBaseView;
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

    private class CustomAdapter extends ArrayAdapter<ArrayList<com.byteshaft.medicosperuanos.gettersetter.MyPatients>> {

        private ArrayList<com.byteshaft.medicosperuanos.gettersetter.MyPatients> myPatientsList;
        private ViewHolder viewHolder;

        public CustomAdapter(Context context, int resource, ArrayList<com.byteshaft.medicosperuanos.gettersetter.MyPatients> myPatientsList) {
            super(context, resource);
            this.myPatientsList = myPatientsList;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater()
                        .inflate(R.layout.delegate_my_patients, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.circleImageView = (CircleImageView) convertView.findViewById(R.id.profile_image_view_search);
                viewHolder.name = (TextView) convertView.findViewById(R.id.name);
//                viewHolder.patientAge = (TextView) convertView.findViewById(R.id.patient_age);
                viewHolder.distance = (TextView) convertView.findViewById(R.id.distance);
                viewHolder.chat = (ImageButton) convertView.findViewById(R.id.chat);
                viewHolder.call = (ImageButton) convertView.findViewById(R.id.call);
                viewHolder.openDetailButton = (ImageButton) convertView.findViewById(R.id.open_details);
                viewHolder.status = (ImageView) convertView.findViewById(R.id.status);

                viewHolder.name.setTypeface(AppGlobals.typefaceNormal);
//                viewHolder.patientAge.setTypeface(AppGlobals.typefaceNormal);
                viewHolder.distance.setTypeface(AppGlobals.typefaceNormal);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            final com.byteshaft.medicosperuanos.gettersetter.MyPatients myPatients = myPatientsList.get(position);
            String years = Helpers.calculateAge(myPatients.getPatientAge());
            viewHolder.name.setText(myPatients.getPatientsName() + " " +
                    "-" + " " + "(" +  years +"a)");
            String[] startLocation = myPatients.getPatientLocation().split(",");
            String[] endLocation = AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_LOCATION).split(",");
            viewHolder.distance.setText(" " + String.valueOf(calculationByDistance(new LatLng(Double.parseDouble(startLocation[0]),
                    Double.parseDouble(startLocation[1])), new LatLng(Double.parseDouble(endLocation[0]),
                    Double.parseDouble(endLocation[1])))) + " " + "km");
            Helpers.getBitMap(myPatients.getPatientImage(), viewHolder.circleImageView);
            if (!myPatients.getChatStatus()) {
                viewHolder.status.setImageResource(R.mipmap.ic_offline_indicator);
            } else {
                viewHolder.status.setImageResource(R.mipmap.ic_online_indicator);
            }
            viewHolder.chat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(getActivity().getApplicationContext(),
                            ConversationActivity.class));
                }
            });
            viewHolder.call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.CALL_PHONE)
                            != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.CALL_PHONE},
                                AppGlobals.CALL_PERMISSION);
                    } else {
                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + myPatients.getPatientPhoneNumber()));
                        startActivity(intent);
                    }
                }
            });
            return convertView;
        }


        @Override
        public int getCount() {
            return myPatientsList.size();
        }
    }

    private void getPatientsDetails() {
        request = new HttpRequest(getActivity());
        request.setOnReadyStateChangeListener(new HttpRequest.OnReadyStateChangeListener() {
            @Override
            public void onReadyStateChange(HttpRequest request, int readyState) {
                switch (readyState) {
                    case HttpRequest.STATE_DONE:
                        System.out.println(request.getResponseURL());
                        Helpers.dismissProgressDialog();
                        switch (request.getStatus()) {
                            case HttpURLConnection.HTTP_OK:
                                System.out.println(request.getResponseText());
                                try {
                                    JSONObject object = new JSONObject(request.getResponseText());
                                    JSONArray jsonArray = object.getJSONArray("results");
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                                        com.byteshaft.medicosperuanos.gettersetter.MyPatients myPatients =
                                                new com.byteshaft.medicosperuanos.gettersetter.MyPatients();
                                        myPatients.setPatientId(jsonObject.getInt("id"));
                                        myPatients.setPatientFirstName(jsonObject.getString("first_name"));
                                        myPatients.setPatientLastName(jsonObject.getString("last_name"));
                                        myPatients.setPatientsName(jsonObject.getString("first_name") + " " +
                                                jsonObject.getString("last_name"));
                                        myPatients.setPatientAge(jsonObject.getString("dob"));
                                        myPatients.setPatientPhoneNumber(jsonObject.getString("phone_number_primary"));
                                        myPatients.setPatientLocation(jsonObject.getString("location"));
                                        myPatients.setPatientImage(jsonObject.getString("photo").replace("http://localhost", AppGlobals.SERVER_IP));
                                        myPatients.setChatStatus(jsonObject.getBoolean("available_to_chat"));
                                        myPatientsList.add(myPatients);
                                        customAdapter.notifyDataSetChanged();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case HttpURLConnection.HTTP_UNAUTHORIZED:
                            Helpers.dismissProgressDialog();
                                try {
                                    Helpers.showSnackBar(getView(), new JSONObject(request.getResponseText()).getString("detail"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                break;
                        }
                }
            }
        });
        request.setOnErrorListener(new HttpRequest.OnErrorListener() {
            @Override
            public void onError(HttpRequest request, int readyState, short error, Exception exception) {
                Helpers.dismissProgressDialog();
                Helpers.showSnackBar(getView(), getResources().getString(R.string.check_internet));
            }
        });
        request.open("GET", String.format("%sdoctor/patients/", AppGlobals.BASE_URL));
        request.setRequestHeader("Authorization", "Token " +
                AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN));
        request.send();
        Helpers.showProgressDialog(getActivity(), "Getting patients ...");
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case AppGlobals.CALL_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
//        TextView patientAge;
        TextView distance;
        ImageButton chat;
        ImageButton call;
        ImageButton openDetailButton;
        ImageView status;


    }
}

