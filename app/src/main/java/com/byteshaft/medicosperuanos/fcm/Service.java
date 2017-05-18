package com.byteshaft.medicosperuanos.fcm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.RemoteInput;
import android.support.v4.content.ContextCompat;

import com.byteshaft.medicosperuanos.MainActivity;
import com.byteshaft.medicosperuanos.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


public class Service extends FirebaseMessagingService {
//    private String message;

    private static int notificationId = 101;
    private static String KEY_TEXT_REPLY = "key_text_reply";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        sendNotification();
    }


    public void replyNotification() {

        String replyLabel = "Enter your reply here";
        RemoteInput remoteInput =
                new RemoteInput.Builder(KEY_TEXT_REPLY)
                        .setLabel(replyLabel)
                        .build();

        Intent resultIntent =
                new Intent(this, MainActivity.class);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        NotificationCompat.Action replyAction =
                new NotificationCompat.Action.Builder(
                        android.R.drawable.ic_dialog_info,
                        "Reply", resultPendingIntent)
                        .addRemoteInput(remoteInput)
                        .build();

        Notification newMessageNotification =
                new NotificationCompat.Builder(this)
                        .setColor(ContextCompat.getColor(this,
                                R.color.colorPrimary))
                        .setSmallIcon(
                                android.R.drawable.ic_dialog_info)
                        .setContentTitle("My Notification")
                        .setContentText("This is a test message")
                        .addAction(replyAction).build();

        NotificationManager notificationManager =
                (NotificationManager)
                        getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(notificationId,
                newMessageNotification);
    }

    private void sendNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setLargeIcon(bm)
                .setTicker("text")
                .setContentTitle("text")
                .setContentText("text")
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
//
//    public void sendSMS(String phoneNo, String msg) {
//        try {
//            SmsManager smsManager = SmsManager.getDefault();
//            Log.i("TAG", "sending sms to " + msg + " Number " + phoneNo);
//            smsManager.sendTextMessage(phoneNo, null, msg, null, null);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }

//    private void sendKey(String token, String name) {
//        HttpRequest request = new HttpRequest(this);
//        request.setOnReadyStateChangeListener(this);
//        request.setOnErrorListener(this);
//        request.open("POST", String.format("%skey", AppGlobals.BASE_URL));
//        JSONObject jsonObject = new JSONObject();
//        try {
//            jsonObject.put(AppGlobals.KEY, token);
//            jsonObject.put("full_name", name);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        request.send(jsonObject.toString());
//        AppGlobals.showProgressDialog(MainActivity.this, "Please wait...");
//    }
}
