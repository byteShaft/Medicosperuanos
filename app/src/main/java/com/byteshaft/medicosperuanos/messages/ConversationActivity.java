package com.byteshaft.medicosperuanos.messages;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.RemoteInput;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.byteshaft.medicosperuanos.R;
import com.byteshaft.medicosperuanos.doctors.FullscreenImageView;
import com.byteshaft.medicosperuanos.utils.AppGlobals;
import com.byteshaft.medicosperuanos.utils.Helpers;
import com.byteshaft.medicosperuanos.utils.RotateUtil;
import com.byteshaft.requests.FormData;
import com.byteshaft.requests.HttpRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;

/**
 * Created by s9iper1 on 3/23/17.
 */

public class ConversationActivity extends AppCompatActivity implements View.OnClickListener, HttpRequest.OnReadyStateChangeListener, HttpRequest.OnErrorListener {

    private View view;
    private ImageButton deleteButton;
    private ImageView sendButton;
    private CircleImageView cameraButton;
    private EditText writeMessageEditText;
    private boolean isBlock = false;
    private File destination;
    private Uri selectedImageUri;
    private Bitmap profilePic;
    private static final int REQUEST_CAMERA = 1;
    private static final int SELECT_FILE = 2;
    private static final int STORAGE_PERMISSION = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 101;
    private String nextUrl;
    private String previousUrl;
    private ChatAdapter chatAdapter;
    public static ArrayList<ChatModel> messages;
    private RecyclerView conversation;
    private int id = -1;
    private static String KEY_TEXT_REPLY = "key_text_reply";
    public static boolean foreground = false;
    private boolean status;
    private String name;
    private static ConversationActivity sInstance;
    private String imageUrl;
    private String photoUrl;
    private LinearLayoutManager linearLayoutManager;
    private boolean loading = false;
    private boolean loadingPrevious = false;
    private int scrollPosition = 0;

    public static ConversationActivity getInstance() {
        return sInstance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        LayoutInflater inflater = LayoutInflater.from(this);
        sInstance = this;
        View v = inflater.inflate(R.layout.action_bar_for_messages, null);
        TextView userName = (TextView)v.findViewById(R.id.action_bar_title);
        userName.setTypeface(AppGlobals.typefaceNormal);
        ImageView backPress = (ImageView) v.findViewById(R.id.back_press);
        backPress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        TextView userStatus = (TextView) v.findViewById(R.id.user_online);
        getSupportActionBar().setCustomView(v);
        setContentView(R.layout.activity_conversation);
        foreground = true;
        id = getIntent().getIntExtra("id", -1);
        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().getBoolean("notification")) {
                getMessageText(getIntent(), getIntent().getIntExtra("sender_id", -1));
                id = getIntent().getIntExtra("sender_id", -1);
                status = getIntent().getBooleanExtra("status", false);
                photoUrl = getIntent().getStringExtra("image_url");
                this.name = getIntent().getStringExtra("name");
            }
        }
        status = getIntent().getBooleanExtra("status", false);
        photoUrl = getIntent().getStringExtra("image_url");
        Log.i("TAG", "image url " + imageUrl);
        this.name = getIntent().getStringExtra("name");
        userName.setText(name);
        status = getIntent().getBooleanExtra("status", false);
        if (status) {
            userStatus.setText("online");
        } else {
            userStatus.setText("offline");
        }
        messages = new ArrayList<>();
        conversation = (RecyclerView) findViewById(R.id.conversation);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        conversation.setLayoutManager(linearLayoutManager);
        conversation.canScrollVertically(1);
        conversation.setHasFixedSize(true);
        getMyMessages(id);
        view = (View) findViewById(R.id.include);
        deleteButton = (ImageButton) findViewById(R.id.dustbin_messages);
        sendButton = (ImageView) view.findViewById(R.id.send_button);
        cameraButton = (CircleImageView) view.findViewById(R.id.camera_button);
        writeMessageEditText = (EditText) view.findViewById(R.id.write_message_edit_text);
        writeMessageEditText.setTypeface(AppGlobals.typefaceNormal);
        deleteButton.setOnClickListener(this);
        sendButton.setOnClickListener(this);
        cameraButton.setOnClickListener(this);
        if (imageUrl != null && imageUrl.trim().isEmpty()) {
            profilePic = Helpers.getBitMapOfProfilePic(imageUrl);
            cameraButton.setImageResource(0);
            cameraButton.setImageBitmap(profilePic);
        }
        chatAdapter = new ChatAdapter(messages);
        conversation.setAdapter(chatAdapter);
        conversation.setOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (loading) {
//                    Helpers.showSnackBar(findViewById(android.R.id.content),
//                            getResources().getString(R.string.loading));
                    return;
                }
                scrollPosition = linearLayoutManager.findFirstVisibleItemPosition();
                if (!recyclerView.canScrollVertically(-1)) {
                    onScrolledToTop();
                } else if (!recyclerView.canScrollVertically(1)) {
                    onScrolledToBottom();
                } else if (dy < 0) {
                    onScrolledUp();
                } else if (dy > 0) {
                    onScrolledDown();
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                    // Do something
                } else if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    // Do something
                } else {
                    // Do something
                }
            }
        });
        writeMessageEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    conversation.scrollToPosition(messages.size() - 1);
                } else {
                }
            }
        });
    }

    public void onScrolledUp() {


    }

    public void onScrolledDown() {

    }

    public void onScrolledToTop() {
        Log.i("TAG", "onScrolledToTop");
        if (nextUrl != null)
        getNextMessages(nextUrl);

    }

    public void onScrolledToBottom() {


    }

    @Override
    protected void onPause() {
        super.onPause();
        foreground = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        foreground = true;
    }

    private CharSequence getMessageText(Intent intent, int senderId) {
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {
            Log.i("TAG", String.valueOf(remoteInput.getCharSequence(KEY_TEXT_REPLY)));
            sendMessage(senderId, String.valueOf(remoteInput.getCharSequence(KEY_TEXT_REPLY)),
                    imageUrl);
            return remoteInput.getCharSequence(KEY_TEXT_REPLY);
        }
        return null;
    }

    public void notifyData() {
        runOnUiThread(new Runnable() {
            public void run() {
                chatAdapter.notifyDataSetChanged();
                conversation.scrollToPosition(messages.size() - 1);
            }
        });
    }

    private void sendMessage(int id, String message, final String attachment) {
        HttpRequest request = new HttpRequest(getApplicationContext());
        request.setOnReadyStateChangeListener(new HttpRequest.OnReadyStateChangeListener() {
            @Override
            public void onReadyStateChange(HttpRequest httpRequest, int i) {
                switch (i) {
                    case HttpRequest.STATE_DONE:
                        switch (httpRequest.getStatus()) {
                            case HttpURLConnection.HTTP_CREATED:
                                imageUrl = null;
                                cameraButton.setImageBitmap(null);
                                NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                                notificationManager.cancel(202);
                                JSONObject singleMessage;
                                Log.i("TAG", httpRequest.getResponseText());
                                try {
                                    singleMessage = new JSONObject(httpRequest.getResponseText());
                                    if (singleMessage.isNull("attachment")) {
                                        ChatModel chatModel = new ChatModel();
                                        chatModel.setId(singleMessage.getInt("creator"));
                                        chatModel.setPatientId(singleMessage.getInt("patient"));
                                        chatModel.setDoctorId(singleMessage.getInt("doctor"));
                                        chatModel.setMessage(singleMessage.getString("text"));
                                        if (!singleMessage.isNull("attachment")) {
                                            chatModel.setImageUrl(singleMessage.getString("attachment"));
                                        }
                                        chatModel.setTimeStamp(singleMessage.getString("created_at"));
                                        messages.add(chatModel);
                                        chatAdapter.notifyDataSetChanged();
                                        conversation.scrollToPosition(messages.size() - 1);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case HttpURLConnection.HTTP_BAD_REQUEST:
                                Log.i("TAG", httpRequest.getResponseText());
                                break;
                        }
                }

            }
        });
        request.setOnErrorListener(new HttpRequest.OnErrorListener() {
            @Override
            public void onError(HttpRequest httpRequest, int i, short i1, Exception e) {
                switch (i) {
                    case HttpRequest.ERROR_CONNECTION_TIMED_OUT:
                        Helpers.showSnackBar(findViewById(android.R.id.content), getResources().getString(R.string.connection_time_out));
                        break;
                    case HttpRequest.ERROR_NETWORK_UNREACHABLE:
                        Helpers.showSnackBar(findViewById(android.R.id.content), e.getLocalizedMessage());
                        break;
                    case HttpRequest.ERROR_LOST_CONNECTION:
                        Helpers.showSnackBar(findViewById(android.R.id.content), getResources().getString(R.string.connection_lost));
                        break;
                }
            }
        });
        request.open("POST", String.format("%smessages/", AppGlobals.BASE_URL));
        request.setRequestHeader("Authorization", "Token " +
                AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN));

        FormData formData = new FormData();
        String msg;
        if (message.trim().isEmpty()) {
            msg = "attachment";
        } else msg = message;
        String user = "doctor";
        if (AppGlobals.isDoctor()) {
            user = "patient";
        }
        formData.append(FormData.TYPE_CONTENT_TEXT, user, String.valueOf(id));
        formData.append(FormData.TYPE_CONTENT_TEXT, "text", msg);
        if (attachment != null && !attachment.trim().isEmpty()) {
            formData.append(FormData.TYPE_CONTENT_FILE, "attachment", attachment);
            request.setOnFileUploadProgressListener(new HttpRequest.OnFileUploadProgressListener() {
                @Override
                public void onFileUploadProgress(HttpRequest httpRequest, File file, long l, long l1) {
                    Log.i("TAG", String.valueOf(l));

                }
            });
        }
        request.send(formData);
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            ChatModel chatModel = new ChatModel();
//            if (AppGlobals.isDoctor()) {
//                chatModel.setId(4);
//            } else {
                chatModel.setId(Integer.parseInt(AppGlobals.
                        getStringFromSharedPreferences(AppGlobals.KEY_PROFILE_ID)));
//            }
            chatModel.setMessage(message);
            chatModel.setImageUrl(imageUrl);
            SimpleDateFormat formatter = new SimpleDateFormat("DD/MM/yyyy HH:mm", Locale.getDefault());
            Date date = new Date();
            chatModel.setTimeStamp(formatter.format(date));
            messages.add(chatModel);
            chatAdapter.notifyDataSetChanged();
            conversation.scrollToPosition(messages.size() - 1);
        }
    }


    private void getMyMessages(int secondPersonId) {
        loading = true;
        HttpRequest request = new HttpRequest(getApplicationContext());
        request.setOnReadyStateChangeListener(this);
        request.setOnErrorListener(this);
        request.open("GET", String.format("%smessages/%s", AppGlobals.BASE_URL, secondPersonId));
        request.setRequestHeader("Authorization", "Token " +
                AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN));
        request.send();
    }

    private void getNextMessages(String url) {
        loading = true;
        loadingPrevious = true;
        HttpRequest request = new HttpRequest(getApplicationContext());
        request.setOnReadyStateChangeListener(this);
        request.setOnErrorListener(this);
        request.open("GET", url);
        request.setRequestHeader("Authorization", "Token " +
                AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN));
        request.send();
    }

    @Override
    public void onReadyStateChange(HttpRequest httpRequest, int i) {
        switch (i) {
            case HttpRequest.STATE_DONE:
                Log.i("TAG", httpRequest.getResponseURL());
                Helpers.dismissProgressDialog();
                loading = false;
                switch (httpRequest.getStatus()) {
                    case HttpURLConnection.HTTP_OK:
//                        Log.i("TAG", httpRequest.getResponseText());
                        ArrayList<ChatModel> previousMessages = new ArrayList<>();
                        if (loadingPrevious) {
                            for (ChatModel chatModel: messages) {
                                previousMessages.add(chatModel);
                            }
                            Log.i("TAg", "previousMessages");
                        }
                        try {
                            JSONObject jsonObject = new JSONObject(httpRequest.getResponseText());
                            if (!jsonObject.isNull("next")) {
                                nextUrl = jsonObject.getString("next")
                                        .replace("http://localhost/api/", AppGlobals.BASE_URL);
                            } else {
                                nextUrl = null;
                            }
//                            if (!jsonObject.isNull("previous")) {
//                                previousUrl = jsonObject.getString("previous");
//                            }
                            JSONArray jsonArray = jsonObject.getJSONArray("results");
                            int length = jsonArray.length()-1;
                            if (loadingPrevious) {
                                messages = new ArrayList<>();
                            }
                            for(int j = length; j >= 0; j--) {
                                Log.i("TAG", "Looping");
                                JSONObject singleMessage = jsonArray.getJSONObject(j);
                                ChatModel chatModel = new ChatModel();
                                chatModel.setPatientId(singleMessage.getInt("patient"));
                                chatModel.setDoctorId(singleMessage.getInt("doctor"));
                                chatModel.setId(singleMessage.getInt("creator"));
                                chatModel.setMessage(singleMessage.getString("text"));
                                if (!singleMessage.isNull("attachment")) {
                                    chatModel.setImageUrl(singleMessage.getString("attachment")
                                            .replace("http://localhost", AppGlobals.SERVER_IP));
                                }
                                chatModel.setTimeStamp(singleMessage.getString("created_at"));
                                messages.add(chatModel);
                                chatAdapter.notifyDataSetChanged();
                                Log.i("TAG", "added " + j);
                                if (!loadingPrevious)
                                chatAdapter.notifyDataSetChanged();
                                conversation.scrollToPosition(messages.size() - 1);
                            }
                            Log.i("TAg", "Size " + messages.size());
                            if (loadingPrevious) {
                                Log.i("TAg", "added all");
                                for (ChatModel chatModel: previousMessages) {
                                    messages.add(chatModel);
                                }
                                ChatAdapter chatAdapter = new ChatAdapter(messages);
                                conversation.setAdapter(chatAdapter);
                                conversation.scrollToPosition(scrollPosition);
                            }
                            Log.i("TAg", "Size " + messages.size());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                }
                loadingPrevious = false;
        }

    }

    @Override
    public void onError(HttpRequest httpRequest, int i, short i1, Exception e) {
        loading = false;
        loadingPrevious = false;
        switch (i) {
            case HttpRequest.ERROR_CONNECTION_TIMED_OUT:
                Helpers.showSnackBar(findViewById(android.R.id.content), getResources().getString(R.string.connection_time_out));
                break;
            case HttpRequest.ERROR_NETWORK_UNREACHABLE:
                Helpers.showSnackBar(findViewById(android.R.id.content), e.getLocalizedMessage());
                break;

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.block, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.block_unblock:
                if (isBlock) {
                    item.setTitle("Block");
                    isBlock = false;
                } else {
                    item.setTitle("Unblock");
                    isBlock = true;
                }

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dustbin_messages:
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle("Confirmation");
                alertDialogBuilder.setMessage("Do you really want to delete?")
                        .setCancelable(false).setPositiveButton("Delete",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
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
                break;
            case R.id.camera_button:
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.CAMERA},
                            STORAGE_PERMISSION);
                } else {
                    selectImage();
                }
                break;
            case R.id.send_button:
                String message = writeMessageEditText.getText().toString();
                Log.i("TAG", "edittext " + String.valueOf(writeMessageEditText == null));
                Log.i("TAG", "value " + String.valueOf(message == null));
                if (message == null && imageUrl == null) {
                    Helpers.showSnackBar(findViewById(android.R.id.content),
                            getResources().getString(R.string.message_error));
                    break;
                }
                if (!writeMessageEditText.getText().toString().trim().isEmpty() || !imageUrl.trim().isEmpty()) {
                    sendMessage(id, writeMessageEditText.getText().toString(), imageUrl);
                    writeMessageEditText.getText().clear();
                } else {
                    Helpers.showSnackBar(findViewById(android.R.id.content),
                            getResources().getString(R.string.message_error));
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case STORAGE_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    selectImage();
                } else {
                    Helpers.showSnackBar(findViewById(android.R.id.content), R.string.permission_denied);
                }

                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
                destination = new File(Environment.getExternalStorageDirectory(),
                        System.currentTimeMillis() + ".jpg");
                imageUrl = destination.getAbsolutePath();
                FileOutputStream fileOutputStream;
                try {
                    destination.createNewFile();
                    fileOutputStream = new FileOutputStream(destination);
                    fileOutputStream.write(bytes.toByteArray());
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                profilePic = Helpers.getBitMapOfProfilePic(destination.getAbsolutePath());
                Bitmap orientedBitmap = RotateUtil.rotateBitmap(destination.getAbsolutePath(), profilePic);
                cameraButton.setImageBitmap(orientedBitmap);
            } else if (requestCode == SELECT_FILE) {
                selectedImageUri = data.getData();
                String[] projection = {MediaStore.MediaColumns.DATA};
                CursorLoader cursorLoader = new CursorLoader(this,
                        selectedImageUri, projection, null, null,
                        null);
                Cursor cursor = cursorLoader.loadInBackground();
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                cursor.moveToFirst();
                String selectedImagePath = cursor.getString(column_index);
                profilePic = Helpers.getBitMapOfProfilePic(selectedImagePath);
                Bitmap orientedBitmap = RotateUtil.rotateBitmap(selectedImagePath, profilePic);
                cameraButton.setImageBitmap(orientedBitmap);
                imageUrl = String.valueOf(selectedImagePath);

            }
        }
    }

    // Dialog with option to capture image or choose from gallery
    private void selectImage() {
        final CharSequence[] items = {"Take Photo", "Choose from Library", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Photo");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    if (ContextCompat.checkSelfPermission(ConversationActivity.this,
                            Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(ConversationActivity.this,
                                new String[]{Manifest.permission.CAMERA},
                                REQUEST_CAMERA_PERMISSION);
                    } else {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(intent, REQUEST_CAMERA);
                    }
                } else if (items[item].equals("Choose from Library")) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(
                            Intent.createChooser(intent, "Select File"),
                            SELECT_FILE);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }

            }
        });
        builder.show();
    }

    private class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyChatViewHolder> {


        private static final int RIGHT_MSG = 0;
        private static final int LEFT_MSG = 1;
        private static final int RIGHT_MSG_IMG = 2;
        private static final int LEFT_MSG_IMG = 3;
        private ArrayList<ChatModel> modelArrayList;


        public ChatAdapter(ArrayList<ChatModel> modelArrayList) {
            super();
            this.modelArrayList = modelArrayList;
        }

        @Override
        public int getItemCount() {
            return modelArrayList.size();
        }

        @Override
        public MyChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            if (viewType == RIGHT_MSG) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_right, parent, false);
                return new MyChatViewHolder(view);
            } else if (viewType == LEFT_MSG) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_left, parent, false);
                return new MyChatViewHolder(view);
            } else if (viewType == RIGHT_MSG_IMG) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_right_img, parent, false);
                return new MyChatViewHolder(view);
            } else {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_left_img, parent, false);
                return new MyChatViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(MyChatViewHolder holder, int position) {
            ChatModel chatModel = modelArrayList.get(position);
            holder.setProfilePhoto(chatModel.getSenderProfilePic());
            holder.setTxtMessage(chatModel.getMessage());
            holder.setTvTimestamp(chatModel.getTimeStamp());
            if (chatModel.getImageUrl() != null) {
                holder.setPicInChat(chatModel.getImageUrl());
            }
        }


        @Override
        public int getItemViewType(int position) {
            ChatModel model = modelArrayList.get(position);
            if (model.getImageUrl() != null) {
                if (model.getImageUrl() != null && model.getId() != Integer.valueOf(
                        AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_USER_ID))) {
                    return LEFT_MSG_IMG;
                } else {
                    return RIGHT_MSG_IMG;
                }
            } else if (model.getId() == Integer.valueOf(
                    AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_USER_ID))) {
                return RIGHT_MSG;
            } else {
                return LEFT_MSG;
            }
        }

        public class MyChatViewHolder extends RecyclerView.ViewHolder {

            TextView tvTimestamp;
            EmojiconTextView txtMessage;
            ImageView profilePhoto;
            ImageView picInChat;

            public MyChatViewHolder(View itemView) {
                super(itemView);
                tvTimestamp = (TextView) itemView.findViewById(R.id.timestamp);
                txtMessage = (EmojiconTextView) itemView.findViewById(R.id.txtMessage);
                picInChat = (ImageView) itemView.findViewById(R.id.img_chat);
                profilePhoto = (ImageView) itemView.findViewById(R.id.ivUserChat);
            }


            public void setTxtMessage(String message) {
                if (txtMessage == null) return;
                txtMessage.setText(message);
            }

            public void setProfilePhoto(final String urlPhotoUser) {
                if (profilePhoto == null) return;
                Glide.with(profilePhoto.getContext()).load(photoUrl).centerCrop()
                        .transform(new CircleTransform(profilePhoto.getContext())).override(60, 60)
                        .into(profilePhoto);
            }

            public String getDate(String createdAt) {
                // Create a DateFormatter object for displaying date in specified format.
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.UK);
                formatter.setTimeZone(TimeZone.getTimeZone("GMT +05:00"));
                Date date = null;
                try {
                    date = formatter.parse(createdAt);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return String.valueOf(date.getTime());
            }

            public void setTvTimestamp(String timestamp) {
                if (tvTimestamp == null) return;
                tvTimestamp.setText(converteTimestamp(getDate(timestamp)));
            }

            public void setPicInChat(String url) {
                if (picInChat == null) return;
                Glide.with(picInChat.getContext()).load(url)
                        .override(150, 150)
                        .centerCrop()
                        .into(picInChat);
                picInChat.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.i("TAG", "click");
                        int position = getAdapterPosition();
                        ChatModel model = modelArrayList.get(position);
                        Intent intent = new Intent(getApplicationContext(), FullscreenImageView.class);
                        intent.putExtra("url", model.getImageUrl());
                        startActivity(intent);
                    }
                });
            }

        }

        private CharSequence converteTimestamp(String mileSegundos) {
            return DateUtils.getRelativeTimeSpanString(Long.parseLong(mileSegundos), System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
        }
    }
}
