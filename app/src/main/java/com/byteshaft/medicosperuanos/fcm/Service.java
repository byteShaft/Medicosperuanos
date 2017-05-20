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
import android.util.Log;

import com.byteshaft.medicosperuanos.MainActivity;
import com.byteshaft.medicosperuanos.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Objects;


public class Service extends FirebaseMessagingService {
//    private String message;

    private static int APPOINTMENT_NOTIFICATION_ID = 101;
    private static int REPLY_NOTIFICATION_ID = 202;
    private static String KEY_TEXT_REPLY = "key_text_reply";

    private String doctorName;
    private String appointmentReason;
    private String appointmentState;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.i("DATA" + " good ", remoteMessage.getData().toString());
        doctorName = remoteMessage.getData().get("doctor_name");
        appointmentReason = remoteMessage.getData().get("appointment_reason");
        appointmentState = remoteMessage.getData().get("reason");
        if (Objects.equals(remoteMessage.getData().get("type"), "appointment")) {
            sendNotification();
        } else {
            replyNotification();
        }

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

        notificationManager.notify(REPLY_NOTIFICATION_ID,
                newMessageNotification);
    }

    private void sendNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, APPOINTMENT_NOTIFICATION_ID, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setLargeIcon(bm)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setTicker(appointmentReason)
                .setContentTitle(doctorName)
                .setContentText("Has " + appointmentState + " your appointment")
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(APPOINTMENT_NOTIFICATION_ID, notificationBuilder.build());
    }
}
