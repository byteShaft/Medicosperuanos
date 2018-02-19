package com.byteshaft.medicosperuanos.messages;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;


public class MainMessages extends Fragment implements HttpRequest.OnReadyStateChangeListener,
        HttpRequest.OnErrorListener {

    private View mBaseView;
    private ListView mMessagesListView;
    private String nextUrl;
    private String previousUrl;
    private ArrayList<ChatModel> chatWithList;
    private Adapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean swipeRefresh = false;
    private static MainMessages instance;
    public static boolean foreground = false;

    public static MainMessages getInstance() {
        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBaseView = inflater.inflate(R.layout.activity_main_messages, container, false);
        foreground = true;
        instance = this;
        swipeRefreshLayout = (SwipeRefreshLayout) mBaseView.findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefresh = true;
                getMessages();
            }
        });
        ((AppCompatActivity) getActivity()).getSupportActionBar()
                .setTitle(getResources().getString(R.string.messages));
        getMessages();
        mMessagesListView = (ListView) mBaseView.findViewById(R.id.main_messages);
        chatWithList = new ArrayList<>();
        mMessagesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity().getApplicationContext(),
                        ConversationActivity.class);
                ChatModel chatModel = chatWithList.get(i);
                intent.putExtra("id", chatModel.getId());
                intent.putExtra("name", chatModel.getFullName());
                intent.putExtra("status", chatModel.isAvailable_to_chat());
                intent.putExtra("image_url", chatModel.getImageUrl());
                startActivity(intent);
            }
        });
        adapter = new Adapter(getActivity().getApplicationContext(), chatWithList);
        mMessagesListView.setAdapter(adapter);
        return mBaseView;
    }

    public void getMessages() {
        HttpRequest request = new HttpRequest(getActivity().getApplicationContext());
        request.setOnReadyStateChangeListener(this);
        request.setOnErrorListener(this);
        request.open("GET", String.format("%smessages_metadata", AppGlobals.BASE_URL));
        request.setRequestHeader("Authorization", "Token " +
                AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN));
        request.send();
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

    @Override
    public void onReadyStateChange(HttpRequest httpRequest, int i) {
        switch (i) {
            case HttpRequest.STATE_DONE:
                swipeRefresh = false;
                swipeRefreshLayout.setRefreshing(false);
                Helpers.dismissProgressDialog();
                switch (httpRequest.getStatus()) {
                    case HttpURLConnection.HTTP_OK:
                        Log.i("TAG", httpRequest.getResponseText());
                        chatWithList = new ArrayList<>();
                        adapter = new Adapter(getActivity().getApplicationContext(), chatWithList);
                        mMessagesListView.setAdapter(adapter);
                        try {
                            JSONArray jsonArray = new JSONArray(httpRequest.getResponseText());
                            for (int j = 0; j < jsonArray.length();j++) {
                                JSONObject singleMessage = jsonArray.getJSONObject(j);
                                ChatModel chatModel = new ChatModel();
                                chatModel.setFullName(singleMessage.getString("full_name"));
                                chatModel.setId(singleMessage.getInt("user_id"));
                                chatModel.setMessage(singleMessage.getString("latest_text"));
                                chatModel.setImageUrl(AppGlobals.SERVER_IP+singleMessage.getString("photo_url"));
                                chatModel.setAvailable_to_chat(singleMessage.getBoolean("available_to_chat"));
                                chatWithList.add(chatModel);
                                adapter.notifyDataSetChanged();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                }
                break;
        }

    }

    @Override
    public void onError(HttpRequest httpRequest, int i, short i1, Exception e) {
        swipeRefreshLayout.setRefreshing(false);
        swipeRefresh = false;
        switch (i) {
            case HttpRequest.ERROR_CONNECTION_TIMED_OUT:
                Helpers.showSnackBar(getView(), getResources().getString(R.string.connection_time_out));
                break;
            case HttpRequest.ERROR_NETWORK_UNREACHABLE:
                Helpers.showSnackBar(getView(), e.getLocalizedMessage());
                break;

        }

    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private class Adapter extends ArrayAdapter<String> {

        private ArrayList<ChatModel> messagesList;
        private ViewHolder viewHolder;

        public Adapter(Context context,  ArrayList<ChatModel> messagesList) {
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
                viewHolder.status = (ImageView) convertView.findViewById(R.id.status);
                viewHolder.navigateButton = (ImageButton) convertView.findViewById(R.id.navigate_button);
                viewHolder.drName.setTypeface(AppGlobals.typefaceNormal);
                viewHolder.date.setTypeface(AppGlobals.typefaceNormal);
                viewHolder.time.setTypeface(AppGlobals.typefaceNormal);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            ChatModel chatModel = messagesList.get(position);
//            String url = String.format("%s"+messagesList.get(position)[0], AppGlobals.SERVER_IP);
//            Helpers.getBitMap(url, viewHolder.profilePicture);
            Helpers.getBitMap(chatModel.getImageUrl(), viewHolder.profilePicture);
            viewHolder.drName.setText(chatModel.getFullName());
            viewHolder.specialist.setText(chatModel.getMessage());
            if (!chatModel.isAvailable_to_chat()) {
                viewHolder.status.setImageResource(R.mipmap.ic_offline_indicator);
            } else {
                viewHolder.status.setImageResource(R.mipmap.ic_online_indicator);
            }
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
        ImageView status;
        ImageButton navigateButton;
    }
}
