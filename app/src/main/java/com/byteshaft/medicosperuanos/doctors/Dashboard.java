package com.byteshaft.medicosperuanos.doctors;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.byteshaft.medicosperuanos.R;
import com.byteshaft.medicosperuanos.utils.AppGlobals;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

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
    private RecyclerView list;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBaseView = inflater.inflate(R.layout.dashboard_fragment, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar()
                .setTitle(getResources().getString(R.string.dashboard));

        doctorName = (TextView) mBaseView.findViewById(R.id.doctor_name_dashboard);
        doctorEmail = (TextView) mBaseView.findViewById(R.id.doctor_email);
        doctorSp = (TextView) mBaseView.findViewById(R.id.doctor_sp);
        doctorImage = (CircleImageView) mBaseView.findViewById(R.id.doctor_image);
        list = (RecyclerView) mBaseView.findViewById(R.id.dashboard_list);
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

    class DashboardAdapter extends ArrayAdapter<String> {

        private ViewHolder viewHolder;

        DashboardAdapter(Context context, int resource, ArrayList<String> arrayList) {
            super(context, resource);
        }

//        @Override
//        public View getView(final int position, View convertView, ViewGroup parent) {
//            if (convertView == null) {
//                convertView = getLayoutInflater().inflate(R.layout.delegate_dashboard, parent, false);
//                viewHolder = new ViewHolder();
//                viewHolder.nextButton = (ImageButton) convertView.findViewById(R.id.button_next);
//                viewHolder.tvAchievementTitle = (TextView) convertView.findViewById(R.id.achievement_title);
//                viewHolder.tvAchievement = (TextView) convertView.findViewById(R.id.achievement);
//                convertView.setTag(viewHolder);
//            } else {
//                viewHolder = (ViewHolder) convertView.getTag();
//            }
//            return convertView;
//        }
    }

    private class ViewHolder {
        TextView tvAchievement;
        TextView tvAchievementTitle;
        ImageButton nextButton;
    }
}
