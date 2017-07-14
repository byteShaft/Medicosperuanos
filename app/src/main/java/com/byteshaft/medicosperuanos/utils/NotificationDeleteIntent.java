package com.byteshaft.medicosperuanos.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Set;

/**
 * Created by s9iper1 on 7/14/17.
 */

public class NotificationDeleteIntent extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int notificationId = intent.getExtras().getInt("com.byteshaft.medicosperuanos.notificationId");
        if (notificationId == AppGlobals.REPLY_NOTIFICATION_ID) {
            int senderId = intent.getExtras().getInt("senderId");
            Set<String> alreadyExisting = AppGlobals.getUnReadMessages();
            if (alreadyExisting.contains(String.valueOf(senderId))) {
                Log.i("TAG", "unread messages already exist");
            } else {
                Log.i("TAG", "unread messages doesnot exist");
                alreadyExisting.add(String.valueOf(senderId));
                AppGlobals.setUnreadMessages(alreadyExisting);
            }
        }
    }
}
