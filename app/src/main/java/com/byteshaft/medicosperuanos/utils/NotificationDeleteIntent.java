package com.byteshaft.medicosperuanos.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.byteshaft.medicosperuanos.MainActivity;
import com.byteshaft.medicosperuanos.messages.MainMessages;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by s9iper1 on 7/14/17.
 */

public class NotificationDeleteIntent extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int notificationId = intent.getExtras().getInt("com.byteshaft.medicosperuanos.notificationId");
        if (MainMessages.foreground) {
            return;
        }
        if (notificationId == AppGlobals.REPLY_NOTIFICATION_ID) {
            int senderId = intent.getExtras().getInt("senderId");
            Set<String> alreadyExisting = AppGlobals.getUnReadMessages();
            Set<String> set = new HashSet<String>();
            set.addAll(alreadyExisting);
            if (alreadyExisting.contains(String.valueOf(senderId))) {
                Log.i("TAG", "unread messages already exist");
            } else {
                Log.i("TAG", "unread messages doesnot exist");
                set.add(String.valueOf(senderId));
                AppGlobals.setUnreadMessages(set);
                if (MainActivity.getInstance() != null) {
                    MainActivity.getInstance().updateMessages();
                }
            }
        }
    }
}
