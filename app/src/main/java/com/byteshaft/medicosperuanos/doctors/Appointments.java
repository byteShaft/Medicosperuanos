package com.byteshaft.medicosperuanos.doctors;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.byteshaft.medicosperuanos.R;
import com.byteshaft.medicosperuanos.gettersetter.Agenda;
import com.byteshaft.medicosperuanos.gettersetter.Services;
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
import java.util.HashSet;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.byteshaft.medicosperuanos.R.id.state;

public class Appointments extends Fragment implements
        HttpRequest.OnReadyStateChangeListener, HttpRequest.OnErrorListener {

    private View mBaseView;
    private SwipeMenuListView mListView;
    private HttpRequest request;
    private ArrayList<Agenda> agendaArrayList;
    private Adapter arrayAdapter;
    private static Appointments sInstance;

    public static Appointments getInstance() {
        return sInstance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBaseView = inflater.inflate(R.layout.appointments, container, false);
        sInstance = this;
        ((AppCompatActivity) getActivity()).getSupportActionBar()
                .setTitle(getResources().getString(R.string.appointments));
        mListView = (SwipeMenuListView) mBaseView.findViewById(R.id.listView);
        HashSet<Date> events = new HashSet<>();
        events.add(new Date());
        com.byteshaft.medicosperuanos.uihelpers.CalendarView calendarView = (
                (com.byteshaft.medicosperuanos.uihelpers.CalendarView)
                        mBaseView.findViewById(R.id.calendar_view));
        calendarView.updateCalendar(events);
        TextView dateTextview = (TextView) calendarView.findViewById(R.id.calendar_date_display);
        dateTextview.setTextColor(getResources().getColor(R.color.header_background));
        agendaArrayList = new ArrayList<>();
        Helpers.showProgressDialog(getActivity(), "Please wait...");
        getAgendaList(Helpers.getDate());

        // assign event handler
        calendarView.setEventHandler(new com.byteshaft.medicosperuanos.uihelpers.CalendarView.EventHandler() {
            @Override
            public void onDayPress(Date date) {
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
                Helpers.showProgressDialog(getActivity(), "Please wait...");
                agendaArrayList.clear();
                String agendaDate = dateFormat.format(formattedDate);
                getAgendaList(agendaDate);
            }
        });

        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "open" item
                SwipeMenuItem close = new SwipeMenuItem(
                        getContext());
                // set item background
                close.setBackground(new ColorDrawable(getResources().getColor(
                        R.color.reject_background)));
                // set item width
                close.setWidth(dpToPx(50));
                // set item title
                close.setIcon(R.mipmap.cross);
                // add to menu
                menu.addMenuItem(close);

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getActivity().getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(getResources().getColor(
                        R.color.tick_background)));
                // set item width
                deleteItem.setWidth(dpToPx(60));
                // set a icon
                deleteItem.setIcon(R.mipmap.tick);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };

        // set creator
        mListView.setMenuCreator(creator);
        mListView.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Agenda agenda = agendaArrayList.get(i);
                Intent intent = new Intent(getActivity(), DoctorsAppointment.class);
                intent.putExtra("id", agenda.getAgendaId());
                intent.putExtra("reason", agenda.getReason());
                intent.putExtra("first_name", agenda.getFirstName());
                intent.putExtra("last_name", agenda.getLastName());
                intent.putExtra("age", agenda.getDateOfBirth());
                intent.putExtra("date", agenda.getDate());
                intent.putExtra("services", agenda.getPatientServices());
                intent.putExtra("position", i);
                startActivity(intent);
            }
        });
        return mBaseView;
    }

    private void getAgendaList(String date) {
        request = new HttpRequest(getActivity());
        request.setOnReadyStateChangeListener(this);
        request.setOnErrorListener(this);
        request.open("GET", String.format("%sdoctor/appointments/?date=%s", AppGlobals.BASE_URL, date));
        request.setRequestHeader("Authorization", "Token " +
                AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN));
        request.send();
    }

    public void updateAppointmentStatus(final String state, int id, final int position) {
        HttpRequest request = new HttpRequest(getActivity());
        request.setOnErrorListener(new HttpRequest.OnErrorListener() {
            @Override
            public void onError(HttpRequest request, int readyState, short error, Exception exception) {
                Helpers.dismissProgressDialog();
                Helpers.alertDialog(getActivity(), "", exception.getMessage(), null);
            }
        });
        Helpers.showProgressDialog(getActivity(), "Please wait..");
        request.setOnReadyStateChangeListener(new HttpRequest.OnReadyStateChangeListener() {
            @Override
            public void onReadyStateChange(HttpRequest request, int readyState) {
                switch (readyState) {
                    case HttpRequest.STATE_DONE:
                        Helpers.dismissProgressDialog();
                        switch (request.getStatus()) {
                            case HttpURLConnection.HTTP_OK:
                                Agenda agenda = agendaArrayList.get(position);
                                agenda.setAgendaState(state);
                                arrayAdapter.notifyDataSetChanged();
                                break;
                            case HttpURLConnection.HTTP_GATEWAY_TIMEOUT:
                                Helpers.alertDialog(getActivity(), "Warning", "check your internet connection", null);
                        }
                }
            }
        });
        request.open("PATCH", String.format("%sdoctor/appointments/%d", AppGlobals.BASE_URL, id));
        request.setRequestHeader("Authorization", "Token " +
                AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN));
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("state", state);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        request.send(jsonObject.toString());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                Agenda agenda = agendaArrayList.get(position);
                switch (index) {
                    // close
                    case 0:
                        updateAppointmentStatus(AppGlobals.REJECTED, agenda.getAgendaId(), position);
                        return true;
                    // tick
                    case 1:
                        updateAppointmentStatus(AppGlobals.ACCEPTED, agenda.getAgendaId(), position);
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    @Override
    public void onReadyStateChange(HttpRequest request, int readyState) {
        switch (readyState) {
            case HttpRequest.STATE_DONE:
                Helpers.dismissProgressDialog();
                switch (request.getStatus()) {
                    case HttpURLConnection.HTTP_OK:
                        Log.i("agenda List ", request.getResponseText());
                        try {
                            JSONObject jsonObject = new JSONObject(request.getResponseText());
                            JSONArray jsonArray = jsonObject.getJSONArray("results");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject agendaObject = jsonArray.getJSONObject(i);
                                JSONObject patientDetailsObject = agendaObject.getJSONObject("patient");

                                /// getting patient details

                                Agenda agenda = new Agenda();
                                agenda.setFirstName(patientDetailsObject.getString("first_name"));
                                agenda.setLastName(patientDetailsObject.getString("last_name"));
                                agenda.setDateOfBirth(patientDetailsObject.getString("dob"));
                                agenda.setPhotoUrl(patientDetailsObject.getString("photo").replace(
                                        "http://localhost", AppGlobals.SERVER_IP));
                                agenda.setAvailAbleForChat(
                                        patientDetailsObject.getBoolean("available_to_chat"));

                                agenda.setCreatedAt(agendaObject.getString("created_at"));
                                agenda.setDate(agendaObject.getString("date"));
                                agenda.setAgendaState(agendaObject.getString("state"));
                                agenda.setReason(agendaObject.getString("reason"));
                                JSONObject doctorJsonObject = agendaObject.getJSONObject("doctor");
                                agenda.setDoctorId(doctorJsonObject.getInt("id"));

                                agenda.setAgendaId(agendaObject.getInt("id"));
                                agenda.setStartTIme(agendaObject.getString("start_time"));
                                JSONArray services = agendaObject.getJSONArray("services");
                                ArrayList<Services> servicesArrayList = new ArrayList<>();
                                for (int k = 0; k < services.length(); k++) {
                                    JSONObject serviceObject = services.getJSONObject(k);
                                    Services service = new Services();
                                    service.setId(serviceObject.getInt("id"));
                                    service.setPrice(String.valueOf(serviceObject.getInt("price")));
                                    service.setDescription(serviceObject.getString("description"));
                                    JSONObject serviceMainObject = serviceObject.getJSONObject("service");
                                    service.setServiceName(serviceMainObject.getString("name"));
                                    servicesArrayList.add(service);
                                }
                                agenda.setPatientServices(servicesArrayList);
                                agendaArrayList.add(agenda);
                                if (arrayAdapter == null) {
                                    arrayAdapter = new Adapter(getActivity(), agendaArrayList);
                                    mListView.setAdapter(arrayAdapter);
                                } else {
                                    arrayAdapter.notifyDataSetChanged();
                                }

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

    private class Adapter extends ArrayAdapter {

        private ViewHolder viewHolder;
        private ArrayList<Agenda> data;

        public Adapter(Context context, ArrayList<Agenda> data) {
            super(context, R.layout.delegate_appointments);
            this.data = data;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(
                        R.layout.delegate_appointments, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.nameAge = (TextView) convertView.findViewById(R.id.name_age);
                viewHolder.appointmentTime = (TextView) convertView.findViewById(
                        R.id.appointment_time);
                viewHolder.appointmentState = convertView.findViewById(state);
                viewHolder.reason = (TextView) convertView.findViewById(R.id.service);
                viewHolder.patientImage = (CircleImageView) convertView.findViewById(
                        R.id.patient_appointment_image_view);
                viewHolder.chatStatus = (ImageView) convertView.findViewById(
                        R.id.available_for_chat_status);
                convertView.setTag(viewHolder);

                viewHolder.nameAge.setTypeface(AppGlobals.typefaceNormal);
                viewHolder.appointmentTime.setTypeface(AppGlobals.typefaceNormal);
                viewHolder.reason.setTypeface(AppGlobals.typefaceNormal);

            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            // setting values
            Agenda agenda = agendaArrayList.get(position);
            Helpers.getBitMap(String.format(AppGlobals.SERVER_IP + "%s", agenda.getPhotoUrl()), viewHolder.patientImage);

            if (agenda.isAvailAbleForChat()) {
                viewHolder.chatStatus.setImageDrawable(
                        getResources().getDrawable(R.mipmap.ic_online_indicator));
            } else {
                viewHolder.chatStatus.setImageDrawable(
                        getResources().getDrawable(R.mipmap.ic_offline_indicator));
            }
            String age = Helpers.calculateAge(agenda.getDateOfBirth());
            String name = agenda.getFirstName() + " " + agenda.getLastName();
            viewHolder.nameAge.setText(name + " (" + age + "a)");
            viewHolder.reason.setText(agenda.getReason());
            SimpleDateFormat formatter_from = new SimpleDateFormat("HH:mm:ss");
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
            try {
                Date date = formatter_from.parse(agenda.getStartTIme());
                viewHolder.appointmentTime.setText(dateFormat.format(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            String state = agenda.getAgendaState();
            if (state.contains(AppGlobals.PENDING)) {
                viewHolder.appointmentState.setBackgroundColor(
                        getResources().getColor(R.color.pending_background_color));
            } else if (state.contains(AppGlobals.ACCEPTED)) {
                viewHolder.appointmentState.setBackgroundColor(
                        getResources().getColor(R.color.attended_background_color));
            } else if (state.contains(AppGlobals.REJECTED)) {
                viewHolder.appointmentState.setBackgroundColor(
                        getResources().getColor(R.color.reject_background));
            } else if (state.contains(AppGlobals.ATTENDED)) {
                viewHolder.appointmentState.setBackgroundColor(
                        getResources().getColor(R.color.attended_background));
            }
            return convertView;
        }
    }

    class ViewHolder {
        TextView appointmentTime;
        View appointmentState;
        TextView nameAge;
        TextView reason;
        ImageView chatStatus;
        CircleImageView patientImage;
    }
}
