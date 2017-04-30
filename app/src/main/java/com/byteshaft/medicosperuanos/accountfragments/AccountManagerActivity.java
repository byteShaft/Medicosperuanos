package com.byteshaft.medicosperuanos.accountfragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.byteshaft.medicosperuanos.MainActivity;
import com.byteshaft.medicosperuanos.R;
import com.byteshaft.medicosperuanos.introscreen.IntroScreen;
import com.byteshaft.medicosperuanos.utils.AppGlobals;

/**
 * Created by s9iper1 on 3/16/17.
 */

public class AccountManagerActivity extends AppCompatActivity {

    private static AccountManagerActivity sInstance;

    public static AccountManagerActivity getInstance() {
        return sInstance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AppGlobals.isLogin()) {
            loadFragment(new Login());
        } else if (!AppGlobals.isInfoAvailable()) {
            loadFragment();
        } else {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }
        if (IntroScreen.getInstance() != null) {
            IntroScreen.getInstance().finish();
        }
        setContentView(R.layout.activity_account_manager);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        IntroScreen.getInstance().finish();
        sInstance = this;
    }

    public void loadFragment() {
        Bundle bundle = new Bundle();
        bundle.putBoolean("boolean", true);
        UserBasicInfoStepOne userBasicInfoStepOne = new UserBasicInfoStepOne();
        userBasicInfoStepOne.setArguments(bundle);
        String backStateName = userBasicInfoStepOne.getClass().getName();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
        fragmentTransaction.replace(R.id.container, userBasicInfoStepOne, backStateName);
        FragmentManager manager = getSupportFragmentManager();
        Log.i("TAG", backStateName);
//        if (fragment.isVisible()) {
        boolean fragmentPopped = manager.popBackStackImmediate(backStateName, 0);
        if (!fragmentPopped) {
            fragmentTransaction.addToBackStack(backStateName);
            fragmentTransaction.commit();
//            }
        }
    }

    public void loadFragment(Fragment fragment) {
        String backStateName = fragment.getClass().getName();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
        fragmentTransaction.replace(R.id.container, fragment, backStateName);
        FragmentManager manager = getSupportFragmentManager();
        Log.i("TAG", backStateName);
//        if (fragment.isVisible()) {
            boolean fragmentPopped = manager.popBackStackImmediate(backStateName, 0);
            if (!fragmentPopped) {
                fragmentTransaction.addToBackStack(backStateName);
                fragmentTransaction.commit();
//            }
        }
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager()
                .findFragmentByTag("com.byteshaft.medicosperuanos.accountfragments.AccountActivationCode");
        if (fragment instanceof AccountActivationCode) {
            Log.i("TAG", "fragment " + fragment.isVisible());

        } else {
            Log.i("TAG", "count " + getSupportFragmentManager().getBackStackEntryCount());
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getFragmentManager().popBackStack();
                Log.i("TAG", "count again" + getSupportFragmentManager().getBackStackEntryCount());
                Fragment accountActivation = getSupportFragmentManager()
                        .findFragmentByTag("com.byteshaft.medicosperuanos.accountfragments.UserBasicInfoStepOne");
                Fragment closeLogin = getSupportFragmentManager()
                        .findFragmentByTag("com.byteshaft.medicosperuanos.accountfragments.Login");
                if (getSupportFragmentManager().getBackStackEntryCount() == 1 && accountActivation != null &&
                        accountActivation.isVisible()) {
                    super.onBackPressed();
                }

                if (getSupportFragmentManager().getBackStackEntryCount() == 1 && closeLogin != null &&
                        closeLogin.isVisible()) {
                    super.onBackPressed();
                }
            } else if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                this.finish();
            }
            super.onBackPressed();
        }
    }
}
