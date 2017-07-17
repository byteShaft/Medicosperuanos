package com.byteshaft.medicosperuanos.fcm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.RemoteInput;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;

import com.byteshaft.medicosperuanos.MainActivity;
import com.byteshaft.medicosperuanos.R;
import com.byteshaft.medicosperuanos.messages.ChatModel;
import com.byteshaft.medicosperuanos.messages.ConversationActivity;
import com.byteshaft.medicosperuanos.utils.AppGlobals;
import com.byteshaft.medicosperuanos.utils.Helpers;
import com.byteshaft.medicosperuanos.utils.NotificationDeleteIntent;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import static com.byteshaft.medicosperuanos.utils.AppGlobals.sImageLoader;


public class Service extends FirebaseMessagingService {
//    private String message;

    private static int APPOINTMENT_NOTIFICATION_ID = 101;
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
    private String photo;
    private String patientName;
    private boolean isMale = false;
    private boolean chatStatus = false;

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
            patientName = remoteMessage.getData().get("patient_name");
            appointmentReason = remoteMessage.getData().get("appointment_reason");
            appointmentState = remoteMessage.getData().get("reason");
            senderName = remoteMessage.getData().get("sender_name");
            messageBody = remoteMessage.getData().get("text");
            senderId = Integer.parseInt(remoteMessage.getData().get("sender_id"));
            senderImageUrl = remoteMessage.getData().get("sender_image_url");
            photo = remoteMessage.getData().get("sender_photo");
            chatStatus = Boolean.parseBoolean(remoteMessage.getData().get("available_to_chat"));

            if (remoteMessage.getData().get("type").equals("appointment") && remoteMessage.getData().get("gender").equals("M")) {
                isMale = true;
            } else {
                isMale = false;
            }
            if (remoteMessage.getData().containsKey("attachment")) {
                attachment = remoteMessage.getData().get("attachment");
            }

            if (remoteMessage.getData().get("type").equals("appointment")) {
                if (remoteMessage.getData().get("reason").equals("request")) {
                    final SpannableStringBuilder sb = new SpannableStringBuilder("Date");
                    final StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD); //Span to make text italic
                    sb.setSpan(bss, 0, 4, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    String message  = patientName+ " has requested appointment \n"+sb.toString()+": " +
                            remoteMessage.getData().get("date") + " Start Time: " +
                            remoteMessage.getData().get("start_time");
                    sendNotification(message, patientName, appointmentReason);
                } else {
                    String doctor;
                    if (isMale)
                        doctor = "Dr "+ doctorName;
                    else doctor = "Dra " + doctorName;
                    String message  = "Appointment " + appointmentState + " by " +doctor +"\n"
                            + "Appointment Reason: " + appointmentReason;
                    sendNotification(message, doctor, appointmentReason);
                }
            } else if (remoteMessage.getData().get("type").equals("subscription_expired")) {
                sendNotification("Your subscription has expired and your account is inactive. kindly contact admin" +
                        "to renew your subscription ", "Subscription Expired", "Subscription Expired");

            } else {
                if (!ConversationActivity.foreground) {
                    replyNotification();
                } else {
                    createdAt = remoteMessage.getData().get("created_at");
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

    private PendingIntent createOnDismissedIntent(Context context, int notificationId, int senderId) {
        Intent intent = new Intent(context, NotificationDeleteIntent.class);
        intent.putExtra("com.byteshaft.medicosperuanos.notificationId", notificationId);
        intent.putExtra("senderId", senderId);
        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(context.getApplicationContext(),
                        notificationId, intent, 0);
        return pendingIntent;
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
        resultIntent.putExtra("status", chatStatus);
        resultIntent.putExtra("name", senderName);
        resultIntent.putExtra("image_url", AppGlobals.SERVER_IP + senderImageUrl);

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
                        .setDeleteIntent(createOnDismissedIntent(this, AppGlobals.REPLY_NOTIFICATION_ID, senderId))
                        .addAction(replyAction).build();

        NotificationManager notificationManager =
                (NotificationManager)
                        getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(AppGlobals.REPLY_NOTIFICATION_ID,
                newMessageNotification);
    }

    private void sendNotification(String messageBody, String doctorName, String appointmentReason) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, APPOINTMENT_NOTIFICATION_ID, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        DisplayImageOptions options;
        options = new DisplayImageOptions.Builder()
                .showImageOnFail(R.mipmap.image_placeholder)
                .showImageOnLoading(R.mipmap.image_placeholder)
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .cacheInMemory(false)
                .cacheOnDisc(false).considerExifParams(true).build();
         Bitmap bitmap = sImageLoader.loadImageSync(AppGlobals.SERVER_IP+photo, options);


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setLargeIcon(bitmap)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setTicker(appointmentReason)
                .setStyle(new NotificationCompat.BigTextStyle())
                .setContentTitle(doctorName)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(APPOINTMENT_NOTIFICATION_ID, notificationBuilder.build());
    }
}
