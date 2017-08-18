package com.byteshaft.medicosperuanos.doctors;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import com.byteshaft.medicosperuanos.R;
import com.byteshaft.medicosperuanos.utils.AppGlobals;
import com.byteshaft.medicosperuanos.utils.Helpers;
import com.byteshaft.requests.HttpRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class MySchedule extends Fragment implements HttpRequest.OnReadyStateChangeListener,
        HttpRequest.OnErrorListener, View.OnClickListener {

    private View mBaseView;
    private ListView mListView;
    private ArrayList<JSONObject> scheduleList;
    //    private LinearLayout searchContainer;
    private String currentDate;
    private ArrayList<String> initialTimeSLots;
    private AppCompatButton save;
    private ScheduleAdapter scheduleAdapter;
    private JSONArray jsonArray;
    private HashMap<String, Integer> idForDate;
    private HashMap<Integer, ArrayList<Integer>> toBeDelete;
    private ArrayList<Integer> toBeDeleteSelectedIds;
    private int outerId = -1;
    private boolean foreground = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBaseView = inflater.inflate(R.layout.my_schedule, container, false);
        foreground = true;
        toBeDelete = new HashMap<>();
        mListView = (ListView) mBaseView.findViewById(R.id.schedule_list);
        ((AppCompatActivity) getActivity()).getSupportActionBar()
                .setTitle(getResources().getString(R.string.my_schedule));
        com.byteshaft.medicosperuanos.uihelpers.CalendarView cv = ((com.byteshaft.medicosperuanos.uihelpers.CalendarView)
                mBaseView.findViewById(R.id.calendar_view));
        cv.setCanGoBack(true);
        currentDate = Helpers.getDate();
        cv.update(new Date(), Calendar.getInstance());
        // assign event handler
        cv.setEventHandler(new com.byteshaft.medicosperuanos.uihelpers.CalendarView.EventHandler() {
            @Override
            public void onDayPress(Date date) {
                // show returned day
                Log.i("TAG", "date " + date);
                DateFormat df = SimpleDateFormat.getDateInstance(DateFormat.DEFAULT, Locale.US);
                String resultDate = df.format(date);
                Log.i("TAG", " locale "+ resultDate);
                SimpleDateFormat formatterFrom;
                formatterFrom = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                Date formattedDate = null;
                try {
                    formattedDate = formatterFrom.parse(resultDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
//                Toast.makeText(getActivity(), dateFormat.format(formattedDate), Toast.LENGTH_SHORT).show();
                Log.i("TAG", "current date :" +String.valueOf(currentDate == null));
                Log.i("TAG", "dateFormat :" +String.valueOf(dateFormat == null));
                Log.i("TAG", "formattedDate :" +String.valueOf(formattedDate == null));
                currentDate = dateFormat.format(formattedDate);
                getTimeSlotsForDate(currentDate, TimeUnit.MINUTES.toMillis(Long.parseLong(AppGlobals
                        .getStringFromSharedPreferences(AppGlobals.KEY_CONSULTATION_TIME
                        ))));
//                mListView.setSelectionAfterHeaderView();

            }
        });
        idForDate = new HashMap<>();
        getSchedule(currentDate);
        setHasOptionsMenu(true);
        getTimeSlotsForDate(currentDate, TimeUnit.MINUTES.toMillis(Long.parseLong(AppGlobals
                .getStringFromSharedPreferences(AppGlobals.KEY_CONSULTATION_TIME
                ))));
        save = (AppCompatButton) mBaseView.findViewById(R.id.save_button);
        save.setOnClickListener(this);
        return mBaseView;
    }

    @Override
    public void onResume() {
        super.onResume();
        foreground = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        foreground = false;
    }

    private void getTimeSlotsForDate(String targetDate, long duration) {
        toBeDeleteSelectedIds = new ArrayList<>();
        scheduleList = new ArrayList<>();
        toBeDelete = new HashMap<>();
        String time1 = "08:00:00";
        String time2 = "22:31:00";

        String format = "dd/MM/yyyy hh:mm";
        initialTimeSLots = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat(format);

        Date dateObj1 = null;
        Date dateObj2 = null;
        try {
            dateObj1 = sdf.parse(targetDate + " " + time1);
            dateObj2 = sdf.parse(targetDate + " " + time2);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long dif = dateObj1.getTime();
        while (dif < dateObj2.getTime()) {
            Date slot = new Date(dif);
            DateFormat df = new SimpleDateFormat("HH:mm:ss");
            String date = df.format(slot.getTime());
            initialTimeSLots.add(date);
            dif += duration;
        }
        for (int i = 0; i < initialTimeSLots.size(); i++) {
            StringBuilder time = new StringBuilder();
            if (i + 1 < initialTimeSLots.size()) {
                time.append(initialTimeSLots.get(i));
            }
            time.append(" , ");
            if (i + 1 < initialTimeSLots.size()) {
                time.append(initialTimeSLots.get(i + 1));
            }
            if (!time.toString().trim().isEmpty()) {
                String[] bothTimes = time.toString().split(",");
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("id", -1);
                    jsonObject.put("start_time", bothTimes[0].trim());
                    jsonObject.put("end_time", bothTimes[1].trim());
                    jsonObject.put("taken", 0);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (!bothTimes[0].trim().isEmpty()) {
                    try {
                        jsonObject.put("state" + i, false);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    scheduleList.add(jsonObject);
                }
            }
        }
        getSchedule(currentDate);
        Log.i("TAG", "state "+ currentDate);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.save_button:
                Log.i("TAG", "DELETE size " + toBeDelete.size());
                Log.i("TAG", "Outer id " + outerId);
                if (toBeDelete.size() == 0 && outerId == -1) {
                    Log.i("TAG", "POST");
                    sendSchedule();
                } else if (toBeDeleteSelectedIds.size() > 0 || outerId != -1) {
                    Log.i("TAG", "Update");
                    updateSchedule(outerId);
                    if (outerId != -1) {
                        Log.i("TAG", "delete");
                        deleteSchedule(outerId, toBeDeleteSelectedIds);
                    }
                }
                break;
        }
    }

    private class ScheduleAdapter extends ArrayAdapter<String> {

        private ArrayList<JSONObject> scheduleList;
        private ViewHolder viewHolder;

        public ScheduleAdapter(Context context, ArrayList<JSONObject> scheduleList) {
            super(context, R.layout.delegate_doctor_schedule);
            this.scheduleList = scheduleList;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater()
                        .inflate(R.layout.delegate_doctor_schedule, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.startTime = (TextView) convertView.findViewById(R.id.start_time);
                viewHolder.endTime = (TextView) convertView.findViewById(R.id.end_time);
                viewHolder.state = (CheckBox) convertView.findViewById(R.id.check_box_schedule);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            final JSONObject data = scheduleList.get(position);
            try {
                viewHolder.startTime.setText(data.getString("start_time"));
                viewHolder.endTime.setText(data.getString("end_time"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            viewHolder.state.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    try {
                        if (b) {
                            data.put("state"+position, true);
                            if (toBeDeleteSelectedIds.contains(data.getInt("id"))) {
                                toBeDeleteSelectedIds.remove(data.getInt("id"));
                            }
                        } else {
                            if (data.has("taken") && data.getInt("taken") == 1) {
                                Helpers.showSnackBar(MySchedule.this.getView(),
                                        getResources().getString(R.string.already_taken));
                                notifyDataSetChanged();
                                return;
                            }
                            data.put("state"+position, false);
                            if (data.getInt("id") != -1) {
                                toBeDeleteSelectedIds.add(data.getInt("id"));

                            }

                        }
                        Log.i("TAG", data.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
//                    if (b) {
//                        data.remove(position);
//                        try {
//                            jsonObject.put("state", true);
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                        data.add(position, jsonObject);
//
//                    } else {
//                        try {
//                            if (jsonObject.has("taken") && jsonObject.getInt("taken") == 1) {
//                                Helpers.showSnackBar(MySchedule.this.getView(),
//                                        getResources().getString(R.string.already_taken));
//                                return;
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                        if (jsonObject.has("ids")) {
//                            try {
//                                Integer[] idsArray = (Integer[]) jsonObject.get("ids");
//                                toBeDeleteSelectedIds.add(idsArray[0]);
//                                toBeDelete.put(idsArray[1], toBeDeleteSelectedIds);
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                        data.remove(position);
//                        try {
//                            jsonObject.put("state", false);
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                        data.add(position, jsonObject);
//                    }

                }
            });
//            try {
//
//                if (map != null) {
//                    if (!map.containsKey(data.getString("start_time").trim())) {
//                        viewHolder.state.setChecked(false);
//                    } else {
//                        viewHolder.state.setChecked(true);
//                        alreadySelectedSchedule.add(data);
//                    }
//                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
            try {
                if (data.getBoolean("state"+position)) {
                    viewHolder.state.setChecked(true);
                } else {
                    viewHolder.state.setChecked(false);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return convertView;
        }

        @Override
        public int getCount() {
            return scheduleList.size();
        }
    }

    private class ViewHolder {
        TextView startTime;
        TextView endTime;
        CheckBox state;
    }

    // Internet Connectivity Functions

    private void getSchedule(String date) {
        HttpRequest request = new HttpRequest(getActivity());
        request.setOnReadyStateChangeListener(this);
        request.setOnErrorListener(this);
        request.open("GET", String.format("%sdoctor/schedule?date=%s", AppGlobals.BASE_URL, date));
        request.setRequestHeader("Authorization", "Token " +
                AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN));
        request.send();
    }

    private void deleteSchedule(int idOfSchedule, ArrayList<Integer> ids) {
        HttpRequest request = new HttpRequest(getActivity());
        request.setOnReadyStateChangeListener(new HttpRequest.OnReadyStateChangeListener() {
            @Override
            public void onReadyStateChange(HttpRequest request, int readyState) {
                switch (readyState) {
                    case HttpRequest.STATE_DONE:
                        switch (request.getStatus()) {
                            case HttpURLConnection.HTTP_NO_CONTENT:
                                Helpers.showSnackBar(getView(), getResources().getString(R.string.schedule_updated));
                                break;
                        }
                }
            }
        });
        request.setOnErrorListener(new HttpRequest.OnErrorListener() {

            @Override
            public void onError(HttpRequest httpRequest, int i, short i1, Exception e) {
                Helpers.dismissProgressDialog();
            }
        });
        request.open("POST", String.format("%sdoctor/schedule/%s/delete-slots", AppGlobals.BASE_URL, idOfSchedule));
        request.setRequestHeader("Authorization", "Token " +
                AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN));
        JSONArray jsonArray = new JSONArray();
        for (Integer id : ids) {
            jsonArray.put(id);
        }
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("slots", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        request.send(jsonObject.toString());
    }


    private void updateSchedule(int id) {
        HttpRequest request = new HttpRequest(getActivity());
        request.setOnReadyStateChangeListener(new HttpRequest.OnReadyStateChangeListener() {
            @Override
            public void onReadyStateChange(HttpRequest httpRequest, int i) {
                switch (i) {
                    case HttpRequest.STATE_DONE:
                        switch (httpRequest.getStatus()) {
                            case HttpURLConnection.HTTP_OK:
                                Helpers.showSnackBar(getView(), getResources().getString(R.string.schedule_updated));
                                getSchedule(currentDate);
                        }
                }

            }
        });
        request.setOnErrorListener(this);
        request.open("PUT", String.format("%sdoctor/schedule/%s", AppGlobals.BASE_URL, id));
        request.setRequestHeader("Authorization", "Token " +
                AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN));
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("date", currentDate);
            JSONArray jsonArray = new JSONArray();

            for (int i = 0; i < scheduleList.size() ; i++) {
                JSONObject singleJson = scheduleList.get(i);
                if (singleJson.getBoolean("state"+i) && singleJson.getInt("id") == -1) {
                    JSONObject time = new JSONObject();
                    time.put("start_time", singleJson.get("start_time").toString().trim());
                    time.put("end_time", singleJson.get("end_time").toString().trim());
                    jsonArray.put(time);
                }
            }

//            for (JSONObject singleJson : jsonObjectJSONArray) {
//                if (singleJson.getBoolean("state") && !alreadySelectedSchedule.contains(singleJson)) {
//                    JSONObject time = new JSONObject();
//                    time.put("start_time", singleJson.get("start_time").toString().trim());
//                    time.put("end_time", singleJson.get("end_time").toString().trim());
//                    jsonArray.put(time);
//                }
//            }
            Log.e("DATA", jsonObject.toString());
            if (jsonArray.length() > 0) {
                jsonObject.put("time_slots", jsonArray);
                Log.e("DATA", jsonObject.toString());
                request.send(jsonObject.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendSchedule() {
        HttpRequest request = new HttpRequest(getActivity());
        request.setOnReadyStateChangeListener(this);
        request.setOnErrorListener(this);
        request.open("POST", String.format("%sdoctor/schedule/", AppGlobals.BASE_URL));
        request.setRequestHeader("Authorization", "Token " +
                AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN));
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("date", currentDate);
            JSONArray jsonArray = new JSONArray();

            for (int i = 0; i < scheduleList.size() ; i++) {
                JSONObject singleJson = scheduleList.get(i);
                if (singleJson.getBoolean("state"+i)) {
                    JSONObject time = new JSONObject();
                    time.put("start_time", singleJson.get("start_time").toString().trim());
                    time.put("end_time", singleJson.get("end_time").toString().trim());
                    jsonArray.put(time);
                }
            }
            Log.i("TAG", "jsonArray" + jsonArray.length());
            if (jsonArray.length() > 0) {
                jsonObject.put("time_slots", jsonArray);
                Helpers.showProgressDialog(getActivity(),
                        getResources().getString(R.string.setting_up_schedule));
                request.send(jsonObject.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onReadyStateChange(HttpRequest request, int readyState) {
        switch (readyState) {
            case HttpRequest.STATE_DONE:
                Helpers.dismissProgressDialog();
                switch (request.getStatus()) {
                    case HttpURLConnection.HTTP_OK:
                        outerId = -1;
                        HashMap<String, String[]> alreadyExist = new HashMap<>();
                        ArrayList<String> alreadyExistStartTime = new ArrayList<>();
                        try {
                            JSONObject jsonObject = new JSONObject(request.getResponseText());
                            jsonArray = jsonObject.getJSONArray("results");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject object = jsonArray.getJSONObject(i);
                                outerId = object.getInt("id");
                                JSONArray timeSlots = object.getJSONArray("time_slots");
                                Log.i("TAG", "slots " +timeSlots.toString());
//                                Log.i("TAG", "time slots loop");
                                for (int r = 0; r < timeSlots.length(); r++) {
                                    JSONObject timeSlot = timeSlots.getJSONObject(r);
                                    String startTime = timeSlot.getString("start_time").trim();
                                    if (!alreadyExistStartTime.contains(startTime)) {
                                        alreadyExistStartTime.add(startTime);
                                        alreadyExist.put(startTime,new String[] {startTime, String.valueOf(timeSlot.getInt("id"))});
                                    }
                                }
                            }
                            for (int q = 0; q < scheduleList.size(); q++) {
                                JSONObject alreadyCreated = scheduleList.get(q);
//                                Log.i("TAG", "already created" + alreadyCreated.getString("start_time"));
                                if (alreadyExist.containsKey(alreadyCreated.getString("start_time"))) {
                                    String[] strings = alreadyExist.get(alreadyCreated.getString("start_time"));
                                    alreadyCreated.put("state" + q, true);
                                    alreadyCreated.put("id", strings[1]);
                                    scheduleList.remove(q);
                                    scheduleList.add(q, alreadyCreated);
                                }
                            }
                            if (foreground) {
                                scheduleAdapter = new ScheduleAdapter(getActivity().getApplicationContext(), scheduleList);
                                mListView.setAdapter(scheduleAdapter);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case HttpURLConnection.HTTP_CREATED:
                        Helpers.showSnackBar(getView(), R.string.success);
                        getSchedule(currentDate);
                        break;
                    case HttpURLConnection.HTTP_UNAUTHORIZED:
                        if (Helpers.getAlertDialog() == null) {
                            Helpers.alertDialog(getActivity(), getResources().getString(R.string.account),
                                    getResources().getString(R.string.account_not_activated), null);
                        }
                        break;
                    case HttpURLConnection.HTTP_BAD_REQUEST:
                        Log.i("TAG", "Bad request " + request.getResponseText());
                        break;
                }
        }

    }

    @Override
    public void onError(HttpRequest request, int readyState, short error, Exception exception) {
        Helpers.dismissProgressDialog();

    }
}
