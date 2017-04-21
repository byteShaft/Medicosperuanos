package com.byteshaft.medicosperuanos.accountfragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.byteshaft.medicosperuanos.R;
import com.byteshaft.medicosperuanos.utils.AppGlobals;
import com.byteshaft.medicosperuanos.utils.Helpers;
import com.byteshaft.requests.HttpRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;

public class SignUp extends Fragment implements View.OnClickListener,
        CompoundButton.OnCheckedChangeListener, HttpRequest.OnReadyStateChangeListener, HttpRequest.OnErrorListener {

    private View mBaseView;
    private EditText mEmail;
    private EditText mPassword;
    private EditText mVerifyPassword;
    private CheckBox mDoctorsCheckBox;
    private Button mSignUpButton;
    private TextView mLoginTextView;
    private String mUserNameString;
    private String mEmailAddressString;
    private String mPasswordString;
    private String mVerifyPasswordString;
    private String mCheckBoxString = "patient";
    private HttpRequest request;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBaseView = inflater.inflate(R.layout.fragment_sign_up, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar()
                .setTitle(getResources().getString(R.string.sign_up));
        setHasOptionsMenu(true);
        mEmail = (EditText) mBaseView.findViewById(R.id.email_edit_text);
        mPassword = (EditText) mBaseView.findViewById(R.id.password_edit_text);
        mVerifyPassword = (EditText) mBaseView.findViewById(R.id.verify_password_edit_text);
        mSignUpButton = (Button) mBaseView.findViewById(R.id.sign_up_button);
        mLoginTextView = (TextView) mBaseView.findViewById(R.id.login_text_view);
        mDoctorsCheckBox = (CheckBox) mBaseView.findViewById(R.id.doctor_checkbox);

        mEmail.setTypeface(AppGlobals.typefaceNormal);
        mPassword.setTypeface(AppGlobals.typefaceNormal);
        mVerifyPassword.setTypeface(AppGlobals.typefaceNormal);
        mSignUpButton.setTypeface(AppGlobals.typefaceNormal);
        mLoginTextView.setTypeface(AppGlobals.typefaceNormal);
        mDoctorsCheckBox.setTypeface(AppGlobals.typefaceNormal);

        mSignUpButton.setOnClickListener(this);
        mLoginTextView.setOnClickListener(this);
        mDoctorsCheckBox.setOnCheckedChangeListener(this);
        return mBaseView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                return true;
            default:return false;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sign_up_button:
                System.out.println("signUp button");
                if (validateEditText()) {
                    registerUser(mPasswordString, mEmailAddressString, mCheckBoxString);
                }
                System.out.println("signUp button" + mUserNameString);
                System.out.println("checkbox text" + mCheckBoxString);
                break;
            case R.id.login_text_view:
                AccountManagerActivity.getInstance().loadFragment(new Login());
                break;
        }

    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (mDoctorsCheckBox.isChecked()) {
            mCheckBoxString = "doctor";
            System.out.println(mCheckBoxString + "working");
        } else if (!mDoctorsCheckBox.isChecked()) {
            mCheckBoxString = "patient";
        }

    }

    private boolean validateEditText() {
        boolean valid = true;
        mEmailAddressString = mEmail.getText().toString();
        mPasswordString = mPassword.getText().toString();
        mVerifyPasswordString = mVerifyPassword.getText().toString();

        if (mEmailAddressString.trim().isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(mEmailAddressString).matches()) {
            mEmail.setError("please provide a valid email");
            valid = false;
        } else {
            mEmail.setError(null);
        }
        if (mPasswordString.trim().isEmpty() || mPasswordString.length() < 4) {
            mPassword.setError("enter at least 4 characters");
            valid = false;
        } else {
            mPassword.setError(null);
        }

        if (mVerifyPasswordString.trim().isEmpty() || mVerifyPasswordString.length() < 4 ||
                !mVerifyPasswordString.equals(mPasswordString)) {
            mVerifyPassword.setError("password does not match");
            valid = false;
        } else {
            mVerifyPassword.setError(null);
        }
        return valid;
    }

    @Override
    public void onReadyStateChange(HttpRequest request, int readyState) {
        switch (readyState) {
            case HttpRequest.STATE_DONE:
                Helpers.dismissProgressDialog();
                Log.i("TAG", "Response " + request.getResponseText());
                switch (request.getStatus()) {
                    case HttpRequest.ERROR_NETWORK_UNREACHABLE:
                        AppGlobals.alertDialog(getActivity(), "Registration Failed!", "please check your internet connection");
                        break;
                    case HttpURLConnection.HTTP_BAD_REQUEST:
                        AppGlobals.alertDialog(getActivity(), "Registration Failed!", "Email already in use");
                        break;
                    case HttpURLConnection.HTTP_CREATED:
                        System.out.println(request.getResponseText() + "working ");
                        Toast.makeText(getActivity(), "Activation code has been sent to you! Please check your Email", Toast.LENGTH_SHORT).show();
                        try {
                            JSONObject jsonObject = new JSONObject(request.getResponseText());
                            System.out.println(jsonObject + "working ");
                            String userId = jsonObject.getString(AppGlobals.KEY_USER_ID);
                            String email = jsonObject.getString(AppGlobals.KEY_EMAIL);
                            String accountType = jsonObject.getString(AppGlobals.KEY_ACCOUNT_TYPE);
                            //saving values
//                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_USER_NAME, username);
                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_EMAIL, email);
                            if (accountType.equals("doctor")) {
                                AppGlobals.userType(true);
                            } else {
                                AppGlobals.userType(false);
                            }
                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_USER_ID, userId);
                            FragmentManager fragmentManager = getFragmentManager();
                            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                            AccountManagerActivity.getInstance().loadFragment(new AccountActivationCode());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                }
        }

    }

    @Override
    public void onError(HttpRequest request, int readyState, short error, Exception exception) {
        Helpers.dismissProgressDialog();

    }

    private void registerUser(String password, String email, String accountType) {
        request = new HttpRequest(getActivity());
        request.setOnReadyStateChangeListener(this);
        request.setOnErrorListener(this);
        request.open("POST", String.format("%sregister", AppGlobals.BASE_URL));
        request.send(getRegisterData(password, email, accountType));
        Helpers.showProgressDialog(getActivity(), "Registering User ");
    }


    private String getRegisterData(String password, String email, String accountType) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("email", email);
            jsonObject.put("account_type", accountType);
            jsonObject.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();

    }

}
