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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class MySchedule extends Fragment implements HttpRequest.OnReadyStateChangeListener,
        HttpRequest.OnErrorListener, View.OnClickListener {

    private View mBaseView;
    private ListView mListView;
    private HashMap<String, ArrayList<JSONObject>> scheduleList;
    //    private LinearLayout searchContainer;
    private String currentDate;
    private ArrayList<String> initialTimeSLots;
    private AppCompatButton save;
    private ScheduleAdapter scheduleAdapter;
    private JSONArray jsonArray;
    private HashMap<String, Integer> idForDate;
    private HashMap<Integer, ArrayList<Integer>> toBeDelete;
    private ArrayList<Integer> toBeDeleteSelectedIds;
    private HashMap<String, Integer[]> map;
    private ArrayList<JSONObject> alreadySelectedSchedule;
    private int scheduleId = -1;
    private boolean sendUpdate = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBaseView = inflater.inflate(R.layout.my_schedule, container, false);
        toBeDelete = new HashMap<>();
        alreadySelectedSchedule = new ArrayList<>();
        mListView = (ListView) mBaseView.findViewById(R.id.schedule_list);
        ((AppCompatActivity) getActivity()).getSupportActionBar()
                .setTitle(getResources().getString(R.string.my_schedule));
        com.byteshaft.medicosperuanos.uihelpers.CalendarView cv = ((com.byteshaft.medicosperuanos.uihelpers.CalendarView)
                mBaseView.findViewById(R.id.calendar_view));
        currentDate = Helpers.getDate();
        // assign event handler
        cv.setEventHandler(new com.byteshaft.medicosperuanos.uihelpers.CalendarView.EventHandler() {
            @Override
            public void onDayPress(Date date) {
                // show returned day
                DateFormat df = SimpleDateFormat.getDateInstance();
                String resultDate = df.format(date);
                SimpleDateFormat formatterFrom = new SimpleDateFormat("MMM d, yyyy");
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                Date formattedDate = null;
                try {
                    formattedDate = formatterFrom.parse(resultDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
//                Toast.makeText(getActivity(), dateFormat.format(formattedDate), Toast.LENGTH_SHORT).show();
                currentDate = dateFormat.format(formattedDate);
                Log.i("TAG", "current date  " + currentDate);
                getTimeSlotsForDate(currentDate, TimeUnit.MINUTES.toMillis(Long.parseLong(AppGlobals
                        .getStringFromSharedPreferences(AppGlobals.KEY_CONSULTATION_TIME
                        ))));
                mListView.setSelectionAfterHeaderView();

            }
        });
        scheduleList = new HashMap<>();
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

    private void getTimeSlotsForDate(String targetDate, long duration) {
        toBeDeleteSelectedIds = new ArrayList<>();
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
        ArrayList<JSONObject> arrayList = new ArrayList<>();
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
                    jsonObject.put("start_time", bothTimes[0]);
                    jsonObject.put("end_time", bothTimes[1]);
                    jsonObject.put("state", false);
                    jsonObject.put("taken", 0);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (!bothTimes[0].trim().isEmpty()) {
                    arrayList.add(jsonObject);
                }
            }
        }
        getSchedule(currentDate);
        scheduleList.put(currentDate, arrayList);
        if (scheduleAdapter == null) {
            scheduleAdapter = new ScheduleAdapter(getActivity().getApplicationContext(), scheduleList);
            mListView.setAdapter(scheduleAdapter);
        } else {
            sendUpdate = false;
            scheduleAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.save_button:
                Log.i("TAG","size" +  toBeDelete.size());
                Log.i("TAG","schedule " + scheduleId);
                if (toBeDelete.size() == 0 && scheduleId == -1 ) {
                    Log.i("TAG","SCHEDULE");
                    sendSchedule();
                } else if (toBeDelete.size() > 0 || scheduleId != -1 ) {
                    Integer key = -1;
                    ArrayList<Integer> value = new ArrayList<>();
                    for (Map.Entry<Integer, ArrayList<Integer>> entry : toBeDelete.entrySet()) {
                        key = entry.getKey();
                        value = entry.getValue();
                    }
                    if (key != -1) {
                        deleteSchedule(key, value);
                    }
                    updateSchedule(scheduleId);
                }
                break;
        }
    }

    private class ScheduleAdapter extends ArrayAdapter<String> {

        private HashMap<String, ArrayList<JSONObject>> scheduleList;
        private ViewHolder viewHolder;

        public ScheduleAdapter(Context context, HashMap<String, ArrayList<JSONObject>> scheduleList) {
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
                viewHolder.state = (CheckBox) convertView.findViewById(R.id.check_box_appointment);
                convertView.setTag(viewHolder);
                viewHolder.state.setTag(position);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            final ArrayList<JSONObject> data = scheduleList.get(currentDate);
            final JSONObject jsonObject = data.get(position);
            viewHolder.state.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    int pos = (int) viewHolder.state.getTag();
                    View checkBoxView = mListView.getChildAt(pos);
                    if (checkBoxView != null) {
                        CheckBox cbx = (CheckBox) checkBoxView.findViewById(R.id.check_box_appointment);
                        if (b) {
                            data.remove(position);
                            try {
                                jsonObject.put("state", true);
                                Log.i("TAG", "STATE" + String.valueOf(jsonObject.getBoolean("state")));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            data.add(position, jsonObject);
                            Log.i("TAG", String.valueOf(data.get(position)));

                        } else {
                            try {
                                if (jsonObject.has("taken") && jsonObject.getInt("taken") == 1) {
                                    Helpers.showSnackBar(MySchedule.this.getView(),
                                            getResources().getString(R.string.already_taken));
                                    notifyDataSetChanged();
                                    return;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if (jsonObject.has("ids")) {
                                try {
                                    Integer[] idsArray = (Integer[]) jsonObject.get("ids");
                                    toBeDeleteSelectedIds.add(idsArray[0]);
                                    toBeDelete.put(idsArray[1], toBeDeleteSelectedIds);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            data.remove(position);
                            try {
                                jsonObject.put("state", false);
                                Log.i("TAG", "STATE" + String.valueOf(jsonObject.getBoolean("state")));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            data.add(position, jsonObject);
                            Log.i("TAG", String.valueOf(data.get(position)));
                        }
                    }

                }
            });
            try {
                viewHolder.startTime.setText(jsonObject.getString("start_time"));
                viewHolder.endTime.setText(jsonObject.getString("end_time"));
                Log.i("TAG", "STATE" + String.valueOf(jsonObject.getBoolean("state")));
                if (map != null) {
                    if (!map.containsKey(jsonObject.getString("start_time").trim())) {
                        viewHolder.state.setChecked(false);
                    } else {
                        viewHolder.state.setChecked(true);
                        alreadySelectedSchedule.add(jsonObject);
                    }
                }
                if (jsonObject.getBoolean("state")) {
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
            return scheduleList.get(currentDate).size();
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
                        Log.i("TAG", request.getResponseURL());
                        switch (request.getStatus()) {
                            case HttpURLConnection.HTTP_NO_CONTENT:
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
        Log.i("TAG", "DELETE" + jsonObject.toString());
        request.send(jsonObject.toString());
    }


    private void updateSchedule(int id) {
        HttpRequest request = new HttpRequest(getActivity());
        request.setOnReadyStateChangeListener(new HttpRequest.OnReadyStateChangeListener() {
            @Override
            public void onReadyStateChange(HttpRequest httpRequest, int i) {
                switch (i) {
                    case HttpRequest.STATE_DONE:
                        Log.i("URL", httpRequest.getResponseURL());
                        switch (httpRequest.getStatus()) {
                            case HttpURLConnection.HTTP_OK:
                                Helpers.showSnackBar(getView(), getResources().getString(R.string.schedule_updated));
                                getSchedule(currentDate);
                                Log.i("Update", httpRequest.getResponseText());
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
            ArrayList<JSONObject> jsonObjectJSONArray = scheduleList.get(currentDate);
            for (JSONObject singleJson : jsonObjectJSONArray) {
                if (singleJson.getBoolean("state") && !alreadySelectedSchedule.contains(singleJson)) {
//                    singleJson.remove("state");
                    JSONObject time = new JSONObject();
                    time.put("start_time", singleJson.get("start_time").toString().trim());
                    time.put("end_time", singleJson.get("end_time").toString().trim());
                    jsonArray.put(time);
                }
            }
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
            ArrayList<JSONObject> jsonObjectJSONArray = scheduleList.get(currentDate);
            for (JSONObject singleJson : jsonObjectJSONArray) {
                if (singleJson.getBoolean("state") && !alreadySelectedSchedule.contains(singleJson)) {
//                    singleJson.remove("state");
                    JSONObject time = new JSONObject();
                    time.put("start_time", singleJson.get("start_time").toString().trim());
                    time.put("end_time", singleJson.get("end_time").toString().trim());
                    jsonArray.put(time);
                }
            }
            Log.i("DATA", jsonObject.toString());
            if (jsonArray.length() > 0) {
                jsonObject.put("time_slots", jsonArray);
                Log.i("DATA", jsonObject.toString());
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
                        scheduleId = -1;
                        Log.i("TAG", "response  " + request.getResponseText());
                        map = new HashMap<>();
                        try {
                            JSONObject jsonObject = new JSONObject(request.getResponseText());
                            jsonArray = jsonObject.getJSONArray("results");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject object = jsonArray.getJSONObject(i);
//                                Log.i("TAG", "Object " + object);
                                scheduleId = object.getInt("id");
                                JSONArray timeSlots = object.getJSONArray("time_slots");
                                for (int r = 0; r < timeSlots.length(); r++) {
                                    JSONObject timeSlot = timeSlots.getJSONObject(r);
//                                    Log.i("TAG", "time slot" + timeSlot);
                                    String startTime = timeSlot.getString("start_time");
                                    map.put(startTime.trim(), new Integer[]{
                                            timeSlot.getInt("id"), object.getInt("id")});
                                }
                            }
//                            Log.i("Map", map.toString());
                            ArrayList<JSONObject> jsonObjects = scheduleList.get(currentDate);
                            if (jsonObjects.size() > 0) {
                                for (int j = 0; j < jsonObjects.size(); j++) {
                                    if (map.containsKey(jsonObjects.get(j).getString("start_time").trim())) {
                                        JSONObject slot = jsonObjects.get(j);
                                        slot.put("ids", map.get(jsonObjects.get(j)
                                                .getString("start_time").trim()));
                                        jsonObjects.remove(j);
                                        jsonObjects.add(j, slot);
                                    }
                                }
                                scheduleList.put(currentDate, jsonObjects);
                            }
                            scheduleAdapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case HttpURLConnection.HTTP_CREATED:
                        Helpers.showSnackBar(getView(), R.string.success);
                        break;
                    case HttpURLConnection.HTTP_UNAUTHORIZED:
                        if (Helpers.getAlertDialog() == null) {
                            Helpers.alertDialog(getActivity(), getResources().getString(R.string.account),
                                    getResources().getString(R.string.account_not_activated), null);
                        }
                        break;
                    case HttpURLConnection.HTTP_BAD_REQUEST:
                        Log.i("TAG", "request " + request.getResponseText());
                        break;
                }
        }

    }

    @Override
    public void onError(HttpRequest request, int readyState, short error, Exception exception) {
        Helpers.dismissProgressDialog();

    }
}
