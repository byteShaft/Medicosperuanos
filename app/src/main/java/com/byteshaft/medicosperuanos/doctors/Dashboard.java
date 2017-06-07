package com.byteshaft.medicosperuanos.doctors;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.byteshaft.medicosperuanos.R;
import com.byteshaft.medicosperuanos.utils.AppGlobals;
import com.byteshaft.medicosperuanos.utils.Helpers;
import com.byteshaft.requests.HttpRequest;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.byteshaft.medicosperuanos.utils.Helpers.getBitMap;

public class Dashboard extends Fragment {

    private View mBaseView;
    private TextView doctorName;
    private TextView doctorEmail;
    private TextView doctorSp;
    private CircleImageView doctorImage;
    private BarChart mChart;
    private ListView list;
    private ArrayList<String> dashBoardItems = new ArrayList<>();
    private JSONObject dashBoardValues;
    private boolean foreground = false;
    private DashboardAdapter dashboardAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean swipeRefresh = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBaseView = inflater.inflate(R.layout.dashboard_fragment, container, false);
        swipeRefreshLayout = (SwipeRefreshLayout) mBaseView.findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefresh = true;
                getDashBoardDetails();
            }
        });
        ((AppCompatActivity) getActivity()).getSupportActionBar()
                .setTitle(getResources().getString(R.string.dashboard));
        foreground = true;
        dashBoardItems.add(AppGlobals.INCOME_TODAY);
        dashBoardItems.add(AppGlobals.APPOINTMENT_TODAY);
        dashBoardItems.add(AppGlobals.MESSAGES);
        dashBoardItems.add(AppGlobals.APPOINTMENT_FOR_CONFIRMATION);
        dashBoardItems.add(AppGlobals.APPOINTMENT_CANCELLED);
        doctorName = (TextView) mBaseView.findViewById(R.id.doctor_name_dashboard);
        doctorEmail = (TextView) mBaseView.findViewById(R.id.doctor_email);
        doctorSp = (TextView) mBaseView.findViewById(R.id.doctor_sp);
        doctorImage = (CircleImageView) mBaseView.findViewById(R.id.doctor_image);
        list = (ListView) mBaseView.findViewById(R.id.dashboard_list_main_view);
        mChart = (BarChart) mBaseView.findViewById(R.id.chart);
        mChart.setPinchZoom(false);
        mChart.animateXY(2000, 2000);

        ArrayList<BarEntry> list = new ArrayList<>();
        list.add(new BarEntry(1, 2));
        list.add(new BarEntry(3, 4));
        list.add(new BarEntry(4, 12));
        list.add(new BarEntry(10, 8));

        ArrayList<BarEntry> rejectedList = new ArrayList<>();
        list.add(new BarEntry(10, 12));
        list.add(new BarEntry(13, 14));
        list.add(new BarEntry(41, 12));
        list.add(new BarEntry(11, 18));

        BarDataSet income = new BarDataSet(list, "income");
        BarDataSet rejected = new BarDataSet(rejectedList, "rejected");
        BarDataSet accepted = new BarDataSet(list, "accepted");

        income.setColor(R.color.buttonColor);
        rejected.setColor(R.color.colorAccent);
        accepted.setColor(R.color.common_google_signin_btn_text_dark);

        ArrayList<String> date = new ArrayList<>();
        date.add("January");
        date.add("January");
        date.add("Feb");
        date.add("January");
        date.add("January");
        date.add("January");

        BarData data = new BarData(income, rejected, accepted);
        mChart.setData(data);

        doctorName.setTypeface(AppGlobals.typefaceNormal);
        doctorEmail.setTypeface(AppGlobals.typefaceNormal);
        doctorSp.setTypeface(AppGlobals.typefaceNormal);

        doctorName.setText(AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_FIRST_NAME)
                + " " + AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_LAST_NAME));
        doctorEmail.setText(AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_EMAIL));
        doctorSp.setText(AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_DOC_SPECIALITY));
        if (AppGlobals.isLogin() && AppGlobals.getStringFromSharedPreferences(AppGlobals.SERVER_PHOTO_URL) != null) {
            String url = String.format("%s" + AppGlobals
                    .getStringFromSharedPreferences(AppGlobals.SERVER_PHOTO_URL), AppGlobals.SERVER_IP);
            getBitMap(url, doctorImage);
        }
        return mBaseView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getDashBoardDetails();
        foreground = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        foreground = false;
    }

    private void getDashBoardDetails() {
        HttpRequest dashBoardRequest = new HttpRequest(getActivity().getApplicationContext());
        dashBoardRequest.setOnReadyStateChangeListener(new HttpRequest.OnReadyStateChangeListener() {
            @Override
            public void onReadyStateChange(HttpRequest request, int readyState) {
                switch (readyState) {
                    case HttpRequest.STATE_DONE:
                        swipeRefreshLayout.setRefreshing(false);
                        swipeRefresh = false;
                        switch (request.getStatus()) {
                            case HttpURLConnection.HTTP_OK:
                                try {
                                    dashBoardValues = new JSONObject(request.getResponseText());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                if (foreground) {
                                    dashboardAdapter = new DashboardAdapter(AppGlobals.getContext(),
                                            dashBoardItems);
                                    list.setAdapter(dashboardAdapter);
                                }
                        }
                }
            }
        });
        dashBoardRequest.setOnErrorListener(new HttpRequest.OnErrorListener() {
            @Override
            public void onError(HttpRequest request, int readyState, short error, Exception exception) {
                swipeRefreshLayout.setRefreshing(false);
                swipeRefresh = false;
                if (exception.getLocalizedMessage().equals("Network is unreachable")) {
                    Helpers.showSnackBar(getView(), exception.getLocalizedMessage());
                }
                switch (readyState) {
                    case HttpRequest.ERROR_CONNECTION_TIMED_OUT:
                        Helpers.showSnackBar(getView(), "connection time out");
                        break;
                }
            }
        });
        dashBoardRequest.open("GET", String.format("%sdoctor/statistics", AppGlobals.BASE_URL));
        dashBoardRequest.setRequestHeader("Authorization", "Token " +
                AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN));
        dashBoardRequest.send();
    }

    private class DashboardAdapter extends ArrayAdapter<String> {

        private ViewHolder viewHolder;
        private ArrayList<String> arrayList;

        DashboardAdapter(Context context, ArrayList<String> arrayList) {
            super(context, R.layout.delegate_dashboard);
            this.arrayList = arrayList;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.delegate_dashboard, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.nextButton = (ImageButton) convertView.findViewById(R.id.button_next);
                viewHolder.tvAchievementTitle = (TextView) convertView.findViewById(R.id.achievement_title);
                viewHolder.tvAchievement = (TextView) convertView.findViewById(R.id.achievement);
                viewHolder.tvAchievement.setTypeface(AppGlobals.robotoBoldItalic);
                viewHolder.tvAchievementTitle.setTypeface(AppGlobals.robotoBoldItalic);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            String text = arrayList.get(position);
            try {
            if (text.equals(AppGlobals.INCOME_TODAY)) {
                viewHolder.tvAchievementTitle.setText(text);
                viewHolder.tvAchievement.setText(String.valueOf(dashBoardValues
                        .getInt("income_amount")));
                viewHolder.tvAchievement.setBackgroundColor(
                        getResources().getColor(R.color.attended_background_color));
            } else if (text.equals(AppGlobals.APPOINTMENT_TODAY)) {
                viewHolder.tvAchievementTitle.setText(text);
                viewHolder.tvAchievement.setText(String.valueOf(dashBoardValues
                        .getInt("appointments_count")));
                viewHolder.tvAchievement.setBackgroundColor(
                        getResources().getColor(R.color.attended_background_color));
            } else if (text.equals(AppGlobals.MESSAGES)) {
                viewHolder.tvAchievementTitle.setText(text);
                viewHolder.tvAchievement.setText(String.valueOf(dashBoardValues
                        .getInt("incoming_message_count")));
                viewHolder.tvAchievement.setBackgroundColor(
                        getResources().getColor(R.color.attended_background));
            } else if (text.equals(AppGlobals.APPOINTMENT_FOR_CONFIRMATION)) {
                Log.i("TAG", "TEXT" + text);
                viewHolder.tvAchievementTitle.setText(text);
                viewHolder.tvAchievement.setText(String.valueOf(dashBoardValues
                        .getInt("appointments_to_be_confirmed")));
                viewHolder.tvAchievement.setBackgroundColor(
                        getResources().getColor(R.color.pending_background_color));
            } else if (text.equals(AppGlobals.APPOINTMENT_CANCELLED)) {
                viewHolder.tvAchievementTitle.setText(text);
                viewHolder.tvAchievement.setText(String.valueOf(dashBoardValues
                        .getInt("appointments_cancelled")));
                viewHolder.tvAchievement.setBackgroundColor(
                        getResources().getColor(R.color.reject_background));
            }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return convertView;
        }

        @Override
        public int getCount() {
            return arrayList.size();
        }
    }

    private class ViewHolder {
        TextView tvAchievement;
        TextView tvAchievementTitle;
        ImageButton nextButton;
    }
}
