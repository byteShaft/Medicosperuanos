package com.byteshaft.medicosperuanos.accountfragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.byteshaft.medicosperuanos.MainActivity;
import com.byteshaft.medicosperuanos.R;
import com.byteshaft.medicosperuanos.utils.AppGlobals;
import com.byteshaft.medicosperuanos.utils.Helpers;
import com.byteshaft.medicosperuanos.utils.RotateUtil;
import com.byteshaft.requests.HttpRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;
import static com.byteshaft.medicosperuanos.utils.Helpers.getBitMap;

@SuppressLint("ValidFragment")
public class UserBasicInfoStepOne extends Fragment implements DatePickerDialog.OnDateSetListener,
        View.OnClickListener, RadioGroup.OnCheckedChangeListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, HttpRequest.OnReadyStateChangeListener,
        HttpRequest.OnFileUploadProgressListener {


    public UserBasicInfoStepOne(boolean value) {
        fromAccountManager = value;
    }

    private View mBaseView;
    private static final int REQUEST_CAMERA = 1;
    private static final int SELECT_FILE = 2;
    private File destination;
    private Uri selectedImageUri;
    private static String imageUrl = "";
    private Bitmap profilePic;
    private CircleImageView mProfilePicture;
    private EditText mDocID;
    private EditText mFirstName;
    private EditText mLastName;
    private EditText mDateOfBirth;
    private EditText mAddress;
    private Button mNextButton;
    private RadioGroup mRadioGroup;
    private RadioButton genderButton;

    private TextView mAddressTextView;

    private DatePickerDialog datePickerDialog;

    private String mDocIDString;
    private String mFirstNameString;
    private String mLastNameString;
    private String mDateOfBirthString;
    private String mGenderButtonString;
    private String mAddressString;
    private String mLocationString;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private HttpRequest mRequest;

    private String city = "";
    private String address = "";
    private String zipCode = "";
    private String houseNumber = "";

    private int locationCounter = 0;
    private static final int LOCATION_PERMISSION = 1;
    private static final int STORAGE_PERMISSION = 2;
    private boolean fromAccountManager;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBaseView = inflater.inflate(R.layout.fragment_user_basic_info_step_one, container, false);
        if (AppGlobals.isLogin() && AppGlobals.isInfoAvailable()) {
            ((AppCompatActivity) getActivity()).getSupportActionBar()
                    .setTitle(getResources().getString(R.string.update_profile));
        } else {
            ((AppCompatActivity) getActivity()).getSupportActionBar()
                    .setTitle(getResources().getString(R.string.sign_up));
        }
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            fromAccountManager = bundle.getBoolean("boolean", false);
        }
        mProfilePicture = (CircleImageView) mBaseView.findViewById(R.id.user_dp);
        mDocID = (EditText) mBaseView.findViewById(R.id.doctor_id_edit_text);
        mFirstName = (EditText) mBaseView.findViewById(R.id.first_name_edit_text);
        mLastName = (EditText) mBaseView.findViewById(R.id.last_name_edit_text);
        mDateOfBirth = (EditText) mBaseView.findViewById(R.id.birth_date_edit_text);
        mAddress = (EditText) mBaseView.findViewById(R.id.address_edit_text);
        mAddressTextView = (TextView) mBaseView.findViewById(R.id.pick_for_current_location);

        mNextButton = (Button) mBaseView.findViewById(R.id.next_button);
        mRadioGroup = (RadioGroup) mBaseView.findViewById(R.id.radio_group);

        mDocID.setTypeface(AppGlobals.typefaceNormal);
        mFirstName.setTypeface(AppGlobals.typefaceNormal);
        mLastName.setTypeface(AppGlobals.typefaceNormal);
        mDateOfBirth.setTypeface(AppGlobals.typefaceNormal);
        mAddress.setTypeface(AppGlobals.typefaceNormal);

        if (AppGlobals.isLogin() && AppGlobals.isInfoAvailable()) {
            mDocID.setText(AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_DOC_ID));
            mFirstName.setText(AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_FIRST_NAME));
            mLastName.setText(AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_LAST_NAME));
            mDateOfBirth.setText(AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_DATE_OF_BIRTH));
            mAddress.setText(AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_ADDRESS));
            mLocationString = AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_LOCATION);
            String gender = AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_GENDER);
            if (gender.contains("M")) {
                mRadioGroup.check(R.id.radio_button_male);
                mGenderButtonString = gender;
            } else {
                mRadioGroup.check(R.id.radio_button_female);
                mGenderButtonString = gender;
            }

            if (AppGlobals.isLogin() && AppGlobals.isInfoAvailable() && AppGlobals.getStringFromSharedPreferences(AppGlobals.SERVER_PHOTO_URL) != null) {
                String url = String.format("%s" + AppGlobals
                        .getStringFromSharedPreferences(AppGlobals.SERVER_PHOTO_URL), AppGlobals.SERVER_IP);
                getBitMap(url, mProfilePicture);
            }
            if (AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_IMAGE_URL) != null) {
                Bitmap bitmap = BitmapFactory.decodeFile(AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_IMAGE_URL));
                mProfilePicture.setImageBitmap(bitmap);
            }
            mAddress.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    mLocationString = null;

                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
        }
        mNextButton.setOnClickListener(this);
        mAddressTextView.setOnClickListener(this);
        mDateOfBirth.setOnClickListener(this);
        mRadioGroup.setOnCheckedChangeListener(this);
        mProfilePicture.setOnClickListener(this);

        final Calendar calendar = Calendar.getInstance();
        datePickerDialog = new DatePickerDialog(getActivity(),
                this,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        if (imageUrl.trim().isEmpty() && imageUrl != null) {
            profilePic = Helpers.getBitMapOfProfilePic(imageUrl);
            mProfilePicture.setImageBitmap(profilePic);
        }
        return mBaseView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        mDateOfBirth.setText(i2 + "/" + (i1 + 1) + "/" + i);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.user_dp:
                if (ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            STORAGE_PERMISSION);
                } else {
                    selectImage();
                }
                break;
            case R.id.next_button:
                if (validateEditText() && mGenderButtonString != null && !mGenderButtonString.isEmpty()) {
                    if (mLocationString == null) {
                        LatLng location = getLocationFromAddress(AppGlobals.getContext(), mAddressString);
                        mLocationString = String.format("%s,%s", location.latitude, location.longitude);
                        System.out.println(mLocationString + "location");
                    }
                    AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_DOC_ID, mDocIDString);
                    AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_FIRST_NAME, mFirstNameString);
                    AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_LAST_NAME, mLastNameString);
                    AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_DATE_OF_BIRTH, mDateOfBirthString);
                    AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_GENDER, mGenderButtonString);
                    AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_ADDRESS, mAddressString);
                    AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_LOCATION, mLocationString);
                    if (!imageUrl.trim().isEmpty()) {
                        AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_IMAGE_URL, imageUrl);
                    }
                    if (!AppGlobals.isDoctor()) {
                        if (fromAccountManager) {
                            AccountManagerActivity.getInstance().loadFragment(new UserBasicInfoStepTwo());
                        } else {
                            MainActivity.getInstance().loadFragment(new UserBasicInfoStepTwo());
                        }
                    } else {
                        if (fromAccountManager) {
                            AccountManagerActivity.getInstance().loadFragment(new DoctorsBasicInfo());
                        } else {
                            MainActivity.getInstance().loadFragment(new DoctorsBasicInfo());
                        }
                    }
                }
                break;
            case R.id.pick_for_current_location:
                locationCounter = 0;
                if (ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                    alertDialogBuilder.setTitle(getResources().getString(R.string.permission_dialog_title));
                    alertDialogBuilder.setMessage(getResources().getString(R.string.permission_dialog_message))
                            .setCancelable(false).setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                    LOCATION_PERMISSION);
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

                } else {
                    if (Helpers.locationEnabled()) {
                        new LocationTask().execute();
                    } else {
                        Helpers.dialogForLocationEnableManually(getActivity());
                    }
                }
                break;
            case R.id.birth_date_edit_text:
                datePickerDialog.show();
                break;

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (Helpers.locationEnabled()) {
                        new LocationTask().execute();
                    } else {
                        Helpers.dialogForLocationEnableManually(getActivity());
                    }
                } else {
                    Helpers.showSnackBar(getView(), R.string.permission_denied);
                }

                break;
            case STORAGE_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    selectImage();
                } else {
                    Helpers.showSnackBar(getView(), R.string.permission_denied);
                }
                break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
        genderButton = (RadioButton) mBaseView.findViewById(checkedId);
        if (genderButton.getText().toString().contains("Male")) {
            mGenderButtonString = "M";
            System.out.println(mGenderButtonString);
        } else {
            mGenderButtonString = "F";
            System.out.println(mGenderButtonString);
        }
    }

    public LatLng getLocationFromAddress(Context context, String strAddress) {

        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;

        try {
            // May throw an IOException
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();

            p1 = new LatLng(location.getLatitude(), location.getLongitude());

        } catch (IOException ex) {

            ex.printStackTrace();
        }

        return p1;
    }

    private boolean validateEditText() {
        boolean valid = true;
        mDocIDString = mDocID.getText().toString();
        mFirstNameString = mFirstName.getText().toString();
        mLastNameString = mLastName.getText().toString();
        mDateOfBirthString = mDateOfBirth.getText().toString();
        mAddressString = mAddress.getText().toString();
        if (mDocIDString.trim().isEmpty()) {
            mDocID.setError("please provide DocID");
            valid = false;
        } else {
            mDocID.setError(null);
        }
        if (mFirstNameString.trim().isEmpty()) {
            mFirstName.setError("please provide firstName");
            valid = false;
        } else {
            mFirstName.setError(null);
        }
        if (mLastNameString.trim().isEmpty()) {
            mLastName.setError("please provide lastName");
            valid = false;
        } else {
            mLastName.setError(null);
        }

        if (mDateOfBirthString.trim().isEmpty()) {
            mDateOfBirth.setError("please provide please provide date of birth");
            valid = false;
        } else {
            mDateOfBirth.setError(null);
        }
        if (mGenderButtonString == null) {
            Helpers.showSnackBar(getView(), R.string.choose_your_gender);
            valid = false;
        }
        if (mAddressString.trim().isEmpty()) {
            mAddress.setError(getString(R.string.enter_address));
            Helpers.showSnackBar(getView(), R.string.enter_address);
            valid = false;
        } else {
            mAddress.setError(null);
        }
        return valid;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        locationCounter++;
        if (locationCounter > 1) {
            stopLocationUpdate();
            mLocationString = location.getLatitude() + "," + location.getLongitude();
            System.out.println("Lat: " + location.getLatitude() + "Long: " + location.getLongitude());
            getAddress(location.getLatitude(), location.getLongitude());
//        getLocationFromAddress(AppGlobals.getContext(), "314 E 4th St,Seiling, OK 73663");
//        System.out.println("Latlong from address" + getLocationFromAddress(AppGlobals.getContext(), "314 E 4th St,Seiling, OK 73663"));
        }
    }


    public void buildGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(AppGlobals.getContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
        }
    }

    public void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        createLocationRequest();
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    private void stopLocationUpdate() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
    }


    protected void createLocationRequest() {
        long INTERVAL = 1000;
        long FASTEST_INTERVAL = 1000;
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    // Dialog with option to capture image or choose from gallery
    private void selectImage() {
        final CharSequence[] items = {"Take Photo", "Choose from Library", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select Photo");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUEST_CAMERA);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
                mProfilePicture.setImageBitmap(orientedBitmap);
            } else if (requestCode == SELECT_FILE) {
                selectedImageUri = data.getData();
                String[] projection = {MediaStore.MediaColumns.DATA};
                CursorLoader cursorLoader = new CursorLoader(getActivity(),
                        selectedImageUri, projection, null, null,
                        null);
                Cursor cursor = cursorLoader.loadInBackground();
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                cursor.moveToFirst();
                String selectedImagePath = cursor.getString(column_index);
                profilePic = Helpers.getBitMapOfProfilePic(selectedImagePath);
                Bitmap orientedBitmap = RotateUtil.rotateBitmap(selectedImagePath, profilePic);
                mProfilePicture.setImageBitmap(orientedBitmap);
                imageUrl = String.valueOf(selectedImagePath);
            }
        } else if (requestCode == AppGlobals.LOCATION_ENABLE) {
            new LocationTask().execute();
        }
    }


    @Override
    public void onReadyStateChange(HttpRequest request, int readyState) {

    }

    @Override
    public void onFileUploadProgress(HttpRequest request, File file, long loaded, long total) {

    }

    private void getAddress(double latitude, double longitude) {
        final StringBuilder result = new StringBuilder();
        try {
            Geocoder geocoder = new Geocoder(AppGlobals.getContext(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                result.append(address.getLocality()).append(" ").append(address.getCountryName());
            }
        } catch (IOException e) {
            Log.e("tag", e.getMessage());
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAddress.setText(result.toString());
            }
        });
    }

    class LocationTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Helpers.showSnackBar(getView(), R.string.acquiring_location);
        }

        @Override
        protected String doInBackground(String... strings) {
            buildGoogleApiClient();
            return null;
        }
    }
}
