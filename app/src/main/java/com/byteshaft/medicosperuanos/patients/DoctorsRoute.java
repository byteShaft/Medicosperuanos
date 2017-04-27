package com.byteshaft.medicosperuanos.patients;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.byteshaft.medicosperuanos.R;
import com.byteshaft.medicosperuanos.gettersetter.DoctorLocations;
import com.byteshaft.medicosperuanos.gettersetter.Services;
import com.byteshaft.medicosperuanos.utils.AppGlobals;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

/**
 * Created by husnain on 2/23/17.
 */

public class DoctorsRoute extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private LatLng latLng;
    private ArrayList<DoctorLocations> arrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Doctors");
        setContentView(R.layout.activity_doctors_route);
        ArrayList<Services> hashMap = (ArrayList<Services>) getIntent().getSerializableExtra("services_array");
        arrayList = (ArrayList<DoctorLocations>) getIntent().getSerializableExtra("location_array");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return false;

        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        for (DoctorLocations doctorLocations : arrayList) {
            String[] location = doctorLocations.getLocation().split(",");
            latLng = new LatLng(Double.parseDouble(location[0]), Double.parseDouble(location[1]));
            BitmapDescriptor bitmap;

            if (doctorLocations.isAvailableToChat()) {
                bitmap = BitmapDescriptorFactory.fromResource(R.mipmap.ic_green_pin);
            } else {
                bitmap = BitmapDescriptorFactory.fromResource(R.mipmap.ic_red_pin);
            }
            mMap.addMarker(new MarkerOptions()
                        .position(latLng).title(doctorLocations.getName()).icon(bitmap));

        }
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng).zoom(10).build();
        mMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        LatLng destination = marker.getPosition();
        String[] myLocation = AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_LOCATION).split(",");
        Log.i("TAG", "my loc" + AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_LOCATION));
        String uri = "http://maps.google.com/maps?saddr=" + myLocation[0] + ","
                + myLocation[1] + "&daddr=" + destination.latitude + ","
                + destination.longitude;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");
        startActivity(intent);
        return false;
    }
}