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
import com.byteshaft.medicosperuanos.messages.ChatModel;
import com.byteshaft.medicosperuanos.messages.ConversationActivity;
import com.byteshaft.medicosperuanos.utils.AppGlobals;
import com.byteshaft.medicosperuanos.utils.Helpers;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


public class Service extends FirebaseMessagingService {
//    private String message;

    private static int APPOINTMENT_NOTIFICATION_ID = 101;
    private static int REPLY_NOTIFICATION_ID = 202;
    private static String KEY_TEXT_REPLY = "key_text_reply";

    private String doctorName;
    private String appointmentReason;
    private String appointmentState;
    private String senderName;
    private String messageBody;
    private int senderId;
    private String senderImageUrl;
    private String attachment;
    private String createdAt;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.i("DATA" + " good ", remoteMessage.getData().toString());
        Log.i("DATA" + " boolean ", String.valueOf(remoteMessage.getData().containsKey("status")));
        if (remoteMessage.getData().containsKey("status")) {
            if (remoteMessage.getData().get("status").equals("OK")) {
                Helpers.sendKey(AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_FCM_TOKEN));
                FirebaseMessaging.getInstance().unsubscribeFromTopic(String.format("doctor-activate-%s",
                        AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_USER_ID)));
                Log.i("DATA" + " good ", AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_FCM_TOKEN));

            }
        } else {
            doctorName = remoteMessage.getData().get("doctor_name");
            appointmentReason = remoteMessage.getData().get("appointment_reason");
            appointmentState = remoteMessage.getData().get("reason");
            senderName = remoteMessage.getData().get("sender_name");
            messageBody = remoteMessage.getData().get("text");
            senderId = Integer.parseInt(remoteMessage.getData().get("sender_id"));
            senderImageUrl = remoteMessage.getData().get("sender_image_url");
            if (remoteMessage.getData().containsKey("attachment")) {
                attachment = remoteMessage.getData().get("attachment");
            }

            if (remoteMessage.getData().get("type").equals("appointment")) {
                sendNotification();
            } else {
                if (!ConversationActivity.foreground) {
                    replyNotification();
                } else {
                    createdAt = remoteMessage.getData().get("created_at");
//                {sender_image_url=/media/1494489383303.jpg, text=pppp, type=message, sender_id=2, sender_name=Bilal Shahid}
                    ChatModel chatModel = new ChatModel();
                    chatModel.setFullName(senderName);
                    chatModel.setTimeStamp(createdAt);
                    chatModel.setId(senderId);
                    chatModel.setMessage(messageBody);
                    if (attachment != null && !attachment.trim().isEmpty()) {
                        chatModel.setImageUrl(attachment
                                .replace("http://localhost", AppGlobals.SERVER_IP));
                    }
                    chatModel.setSenderProfilePic(senderImageUrl);
                    ConversationActivity.messages.add(chatModel);
                    ConversationActivity.getInstance().notifyData();
                }
            }
        }
    }

    public void replyNotification() {
        String replyLabel = "Enter your reply here";
        RemoteInput remoteInput =
                new RemoteInput.Builder(KEY_TEXT_REPLY)
                        .setLabel(replyLabel)
                        .build();

        Intent resultIntent =
                new Intent(this, ConversationActivity.class);
        resultIntent.putExtra("notification", true);
        resultIntent.putExtra("sender_id", senderId);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        NotificationCompat.Action replyAction =
                new NotificationCompat.Action.Builder(
                        R.drawable.msg,
                        "Reply", resultPendingIntent)
                        .addRemoteInput(remoteInput)
                        .build();

        Notification newMessageNotification =
                new NotificationCompat.Builder(this)
                        .setColor(ContextCompat.getColor(this,
                                R.color.colorPrimary))
                        .setSmallIcon(
                                R.drawable.msg)
                        .setContentTitle(senderName)
                        .setContentText(messageBody)
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
