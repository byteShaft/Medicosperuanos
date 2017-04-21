package com.byteshaft.medicosperuanos.messages;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.byteshaft.medicosperuanos.R;
import com.byteshaft.medicosperuanos.utils.AppGlobals;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by s9iper1 on 3/21/17.
 */

public class MainMessages extends Fragment {

    private View mBaseView;
    private ListView mMessagesList;
    private ArrayList<String[]> mainMessages;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBaseView = inflater.inflate(R.layout.activity_main_messages, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar()
                .setTitle(getResources().getString(R.string.messages));
        mMessagesList = (ListView) mBaseView.findViewById(R.id.main_messages);
        mainMessages = new ArrayList<>();
        mainMessages.add(new String[]{AppGlobals.getStringFromSharedPreferences(AppGlobals.SERVER_PHOTO_URL),
        "Dr Bilal", "Dermatologist", "2-3-2017", "12:00"});
        mainMessages.add(new String[]{AppGlobals.getStringFromSharedPreferences(AppGlobals.SERVER_PHOTO_URL),
                "Dr shahid", "ENT", "3-3-2017", "14:00"});
        mainMessages.add(new String[]{AppGlobals.getStringFromSharedPreferences(AppGlobals.SERVER_PHOTO_URL),
                "Dr Omer", "Chest Specilist", "4-3-2017", "16:00"});
        mainMessages.add(new String[]{AppGlobals.getStringFromSharedPreferences(AppGlobals.SERVER_PHOTO_URL),
                "Dr Imran", "ENT", "5-3-2017", "09:00"});
        mainMessages.add(new String[]{AppGlobals.getStringFromSharedPreferences(AppGlobals.SERVER_PHOTO_URL),
                "Dr Zeshan", "Cest Specilist", "4-3-2017", "15:00"});
        mainMessages.add(new String[]{AppGlobals.getStringFromSharedPreferences(AppGlobals.SERVER_PHOTO_URL),
                "Dr Arham", "Dermatologist", "7-3-2017", "16:00"});
        mMessagesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                startActivity(new Intent(getActivity().getApplicationContext(), ConversationActivity.class));
            }
        });
        mMessagesList.setAdapter(new Adapter(getActivity().getApplicationContext(), mainMessages));
        return mBaseView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private class Adapter extends ArrayAdapter<String> {

        private ArrayList<String[]> messagesList;
        private ViewHolder viewHolder;

        public Adapter(Context context,  ArrayList<String[]> messagesList) {
            super(context, R.layout.delegate_main_messages);
            this.messagesList = messagesList;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.delegate_main_messages,
                        parent, false);
                viewHolder = new ViewHolder();
                viewHolder.profilePicture = (CircleImageView) convertView.findViewById(R.id.profile_picture);
                viewHolder.drName = (TextView) convertView.findViewById(R.id.dr_name);
                viewHolder.specialist = (TextView) convertView.findViewById(R.id.specialist);
                viewHolder.date = (TextView) convertView.findViewById(R.id.date);
                viewHolder.time = (TextView) convertView.findViewById(R.id.time);
                viewHolder.navigateButton = (ImageButton) convertView.findViewById(R.id.navigate_button);

                viewHolder.drName.setTypeface(AppGlobals.typefaceNormal);
                viewHolder.date.setTypeface(AppGlobals.typefaceNormal);
                viewHolder.time.setTypeface(AppGlobals.typefaceNormal);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
//            String url = String.format("%s"+messagesList.get(position)[0], AppGlobals.SERVER_IP);
//            Helpers.getBitMap(url, viewHolder.profilePicture);
            viewHolder.profilePicture.setImageResource(R.mipmap.image_placeholder);
            viewHolder.drName.setText(messagesList.get(position)[1]);
            viewHolder.specialist.setText(messagesList.get(position)[2]);
            viewHolder.date.setText(messagesList.get(position)[3]);
            viewHolder.time.setText(messagesList.get(position)[4]);
            return convertView;
        }

        @Override
        public int getCount() {
            return messagesList.size();
        }
    }

    private class ViewHolder {
        CircleImageView profilePicture;
        TextView drName;
        TextView specialist;
        TextView date;
        TextView time;
        ImageButton navigateButton;
    }
}
