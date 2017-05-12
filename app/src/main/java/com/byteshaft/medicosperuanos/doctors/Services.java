package com.byteshaft.medicosperuanos.doctors;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
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
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.byteshaft.medicosperuanos.R;
import com.byteshaft.medicosperuanos.utils.AppGlobals;
import com.byteshaft.medicosperuanos.utils.Helpers;
import com.byteshaft.requests.HttpRequest;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.util.ArrayList;

public class Services extends Fragment implements View.OnClickListener {

    private LinearLayout searchContainer;
    private ListView serviceList;
    private View mBaseView;
    private Toolbar toolbar;
    private HttpRequest mRequestServiceList;
    private Button mSaveButton;
    private ServiceAdapter serviceAdapter;
    private ArrayList<com.byteshaft.medicosperuanos.gettersetter.Services> servicesArrayList;
    private ArrayList<com.byteshaft.medicosperuanos.gettersetter.Services> searchList;
    private int currentAdapterPosition;
    private JSONObject jsonObject;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBaseView = inflater.inflate(R.layout.services_layout, container, false);
        setHasOptionsMenu(true);
        serviceList = (ListView) mBaseView.findViewById(R.id.service_list);
        mSaveButton = (Button) mBaseView.findViewById(R.id.save_button);
        mSaveButton.setOnClickListener(this);
        servicesArrayList = new ArrayList<>();
        searchContainer = new LinearLayout(getActivity());
        getServicesListFromAdmin();
        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        Toolbar.LayoutParams containerParams = new Toolbar.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        containerParams.gravity = Gravity.CENTER_VERTICAL;
        containerParams.setMargins(20, 20, 10, 20);
        searchContainer.setLayoutParams(containerParams);

        // Setup search view
        final EditText toolbarSearchView = new EditText(getActivity());
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
                    serviceAdapter = new ServiceAdapter(searchList);
                    serviceList.setAdapter(serviceAdapter);
                    for (com.byteshaft.medicosperuanos.gettersetter.Services services :
                            servicesArrayList) {
                        if (StringUtils.containsIgnoreCase(services.getServiceName(),
                                s.toString()) || StringUtils.containsIgnoreCase(String.valueOf(
                                services.getServiceId()),
                                s.toString())) {
                            searchList.add(services);
                            serviceAdapter.notifyDataSetChanged();

                        }
                    }
                } else {
                    searchList = new ArrayList<>();
                    serviceAdapter = new ServiceAdapter(servicesArrayList);
                    serviceList.setAdapter(serviceAdapter);
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
        (searchContainer).addView(toolbarSearchView);

        // Setup the clear button
        Resources r = getResources();
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, r.getDisplayMetrics());
        LinearLayout.LayoutParams clearParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        clearParams.gravity = Gravity.CENTER;
        // Add search view to toolbar and hide it
        toolbar.addView(searchContainer);
        return mBaseView;
    }

    private void getServicesListFromAdmin() {
        mRequestServiceList = new HttpRequest(getActivity().getApplicationContext());
        mRequestServiceList.setOnReadyStateChangeListener(new HttpRequest.OnReadyStateChangeListener() {
            @Override
            public void onReadyStateChange(HttpRequest request, int readyState) {
                switch (readyState) {
                    case HttpRequest.STATE_DONE:
                        Helpers.dismissProgressDialog();
                        switch (request.getStatus()) {
                            case HttpURLConnection.HTTP_OK:
                                Log.e("TagList", request.getResponseText());
                                try {
                                    JSONObject serviceObject = new JSONObject(request.getResponseText());
                                    JSONArray array = serviceObject.getJSONArray("results");
                                    for (int i = 0; i < array.length(); i++) {
                                        JSONObject jsonObject = array.getJSONObject(i);
                                        com.byteshaft.medicosperuanos.gettersetter.Services services =
                                                new com.byteshaft.medicosperuanos.gettersetter.Services();
                                        services.setId(jsonObject.getInt("id"));
                                        services.setServiceName(jsonObject.getString("name"));
                                        services.setStatus(false);
                                        servicesArrayList.add(services);
                                    }
                                    serviceAdapter = new ServiceAdapter(servicesArrayList);
                                    serviceList.setAdapter(serviceAdapter);
                                    getDoctorServices();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                        }
                }
            }
        });
        mRequestServiceList.setOnErrorListener(new HttpRequest.OnErrorListener() {
            @Override
            public void onError(HttpRequest request, int readyState, short error, Exception exception) {
                if (exception.getLocalizedMessage().equals("Network is unreachable")) {
                    Helpers.showSnackBar(getView(), exception.getLocalizedMessage());
                }
                switch (readyState) {
                    case HttpRequest.ERROR_CONNECTION_TIMED_OUT:
                        Helpers.showSnackBar(getView(), "connection time out");
                        break;
                }
                Helpers.dismissProgressDialog();
            }
        });
        mRequestServiceList.open("GET", String.format("%sservices", AppGlobals.BASE_URL));
        mRequestServiceList.send();
        Helpers.showProgressDialog(getActivity(), "Getting services list" + "\n" + "please wait..");
    }

    private void setServicePrice(String price) {
        HttpRequest request = new HttpRequest(getActivity());
        Helpers.showProgressDialog(getActivity(), "Setting price..");
        com.byteshaft.medicosperuanos.gettersetter.Services services = servicesArrayList.get(currentAdapterPosition);
        request.setOnReadyStateChangeListener(new HttpRequest.OnReadyStateChangeListener() {
            @Override
            public void onReadyStateChange(HttpRequest request, int readyState) {
                switch (readyState) {
                    case HttpRequest.STATE_DONE:
                        Helpers.dismissProgressDialog();
                        switch (request.getStatus()) {
                            case HttpURLConnection.HTTP_CREATED:
                                getDoctorServices();
                                Helpers.showSnackBar(getView(), "Price successfully set");
                        }
                }
            }
        });
        request.open("POST", String.format("%sdoctor/services/", AppGlobals.BASE_URL));
        request.setRequestHeader("Authorization", "Token " +
                AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN));
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("price", price);
            jsonObject.put("description", services.getServiceName());
            jsonObject.put("service", services.getId());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i("TAG", jsonObject.toString());
        request.send(jsonObject.toString());
    }

    private void removeService(final int itemPosition) {
        final com.byteshaft.medicosperuanos.gettersetter.Services services = servicesArrayList.get(itemPosition);
        jsonObject = new JSONObject();
        try {
            jsonObject.put("is_active", false);
            jsonObject.put("doctor", services.getDoctorId());
            jsonObject.put("description", services.getDescription());
            jsonObject.put("price", services.getPrice());
            jsonObject.put("service", services.getId());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        HttpRequest request = new HttpRequest(getActivity());
        Helpers.showProgressDialog(getActivity(), "Removing Service...");
        request.setOnReadyStateChangeListener(new HttpRequest.OnReadyStateChangeListener() {
            @Override
            public void onReadyStateChange(HttpRequest request, int readyState) {
                switch (readyState) {
                    case HttpRequest.STATE_DONE:
                        Helpers.dismissProgressDialog();
                        switch (request.getStatus()) {
                            case HttpURLConnection.HTTP_OK:
                                services.setStatus(false);
                                serviceAdapter.notifyDataSetChanged();
                                Log.e("Response", request.getResponseText());
                                Helpers.showSnackBar(getView(), "Service removed");
                        }
                }
            }
        });
        request.open("PUT", String.format("%sdoctor/services/%d", AppGlobals.BASE_URL, services.getServiceId()));
        request.setRequestHeader("Authorization", "Token " +
                AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN));
        Log.e("Cha, jitthy jao cha !", jsonObject.toString());
        request.send(jsonObject.toString());
    }

    private void getDoctorServices() {
        HttpRequest request = new HttpRequest(getActivity());
        request.setOnReadyStateChangeListener(new HttpRequest.OnReadyStateChangeListener() {
            @Override
            public void onReadyStateChange(HttpRequest request, int readyState) {
                switch (readyState) {
                    case HttpRequest.STATE_DONE:
                        Helpers.dismissProgressDialog();
                        switch (request.getStatus()) {
                            case HttpURLConnection.HTTP_OK:
                                request.getResponseText();
                                Log.i("DOCTOR services", request.getResponseText());
                                try {
                                    JSONObject object = new JSONObject(request.getResponseText());
                                    JSONArray jsonArray = object.getJSONArray("results");
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                                        int doctorId = jsonObject.getInt("doctor");
                                        int serviceIdMain = jsonObject.getInt("id");
                                        boolean serviceState = jsonObject.getBoolean("is_active");


                                        JSONObject serviceObject = jsonObject.getJSONObject("service");
                                        int serviceId = serviceObject.getInt("id");
                                        for (com.byteshaft.medicosperuanos.gettersetter.Services service :
                                                servicesArrayList) {
                                            if (serviceId == service.getId()) {
                                                service.setServiceId(jsonObject.getInt("id"));
                                                service.setDescription(jsonObject.getString("description"));
                                                service.setPrice(jsonObject.getString("price"));

                                                service.setDoctorId(doctorId);
                                                service.setServiceIdMain(serviceIdMain);
//                                                service.setServiceState(serviceState);
                                                service.setStatus(serviceState);
                                                serviceAdapter.notifyDataSetChanged();
                                            }
                                        }

                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                        }
                }
            }
        });
        request.open("GET", String.format("%sdoctor/services/", AppGlobals.BASE_URL));
        request.setRequestHeader("Authorization", "Token " +
                AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN));
        request.send();
    }

    private void showPriceDialog() {
        final EditText priceEditText = new EditText(getActivity());
        priceEditText.setHint("Enter Price");
        priceEditText.setFocusable(true);
        priceEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle("Add your price to this service")
                .setCancelable(false)
                .setView(priceEditText)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String price = String.valueOf(priceEditText.getText());
                        setServicePrice(price);
                        System.out.println(price);
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }

    private void confirmationDialog(final int itemPosition) {
        new AlertDialog.Builder(getActivity())
                .setCancelable(false)
                .setTitle("Delete Service")
                .setMessage("Do you really want to delete?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        removeService(itemPosition);
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
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
    public void onClick(View view) {
        System.out.println("Click");
    }

    private class ServiceAdapter extends BaseAdapter {

        private ViewHolder viewHolder;
        private ArrayList<com.byteshaft.medicosperuanos.gettersetter.Services> data;

        public ServiceAdapter(ArrayList<com.byteshaft.medicosperuanos.gettersetter.Services> data) {
            this.data = data;
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.delegate_service, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.serviceName = (TextView) convertView.findViewById(R.id.service_name);
                viewHolder.servicePrice = (TextView) convertView.findViewById(R.id.service_price);
                viewHolder.serviceCheckBox = (ImageButton) convertView.findViewById(R.id.service_checkbox);
                viewHolder.removeService = (ImageButton) convertView.findViewById(R.id.remove_service);
                viewHolder.addService = (ImageButton) convertView.findViewById(R.id.add_service);
                viewHolder.servicePrice.setTypeface(AppGlobals.typefaceNormal);
                viewHolder.serviceName.setTypeface(AppGlobals.typefaceNormal);
                AppGlobals.buttonEffect(viewHolder.addService);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            final com.byteshaft.medicosperuanos.gettersetter.Services services = servicesArrayList.get(position);
            viewHolder.removeService.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    confirmationDialog(position);
                }
            });
            viewHolder.addService.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    currentAdapterPosition = position;
                    showPriceDialog();
                }
            });
            viewHolder.serviceName.setText(services.getServiceName());
            AppGlobals.buttonEffect(viewHolder.removeService);
            viewHolder.servicePrice.setText(services.getPrice());
            Log.i("TAG", "state " + services.getStatus());
            if (services.getStatus()) {
                viewHolder.servicePrice.setVisibility(View.VISIBLE);
                viewHolder.serviceCheckBox.setVisibility(View.VISIBLE);
                viewHolder.removeService.setVisibility(View.VISIBLE);
                viewHolder.addService.setVisibility(View.GONE);
            } else {
                viewHolder.servicePrice.setVisibility(View.GONE);
                viewHolder.serviceCheckBox.setVisibility(View.GONE);
                viewHolder.removeService.setVisibility(View.INVISIBLE);
                viewHolder.addService.setVisibility(View.VISIBLE);
            }
            return convertView;
        }

        @Override
        public int getCount() {
            return data.size();
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

    class ViewHolder {
        TextView serviceName;
        TextView servicePrice;
        ImageButton serviceCheckBox;
        ImageButton removeService;
        ImageButton addService;
    }
}
