package com.byteshaft.medicosperuanos.fcm;

import android.util.Log;

import com.byteshaft.medicosperuanos.utils.AppGlobals;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class FireBaseService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String token = FirebaseInstanceId.getInstance().getToken();
        AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY__FCM_TOKEN, token);
        Log.e("TAG", "Token " + token);
    }
}
