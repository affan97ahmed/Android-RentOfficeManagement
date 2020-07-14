package com.example.jayde.a4ease;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import static com.example.jayde.a4ease.RenterGetLocation.PERMISSION_REQUEST_CODE;
import static com.example.jayde.a4ease.RenterGetLocation.PLAY_SERVICES_PERMISSION_CODE;

public class DisplayLocation extends AppCompatActivity implements OnMapReadyCallback{

    private GoogleMap mGoogleMap;
    private User objUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_location);

            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);

            int w = dm.widthPixels;
            int h = dm.heightPixels;

            getWindow().setLayout((int)(w*.8),(int)(h*.5));

            Gson gson = new Gson();
            objUser = gson.fromJson(getIntent().getStringExtra(Misc.inputExtra), User.class);

            inItGoogleMap();
    }


    private void inItGoogleMap() {
        if(isServiceOk())
        { SupportMapFragment supportMapFragment=SupportMapFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.map_fragment_location,supportMapFragment).commit();
            supportMapFragment.getMapAsync(DisplayLocation.this);
            if(checkLocationPermission())
            {
                Toast.makeText(this,"Ready To Map !",Toast.LENGTH_SHORT).show();
            }
            else
            {
                requestLocationPermission();
            }
        }
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private  void requestLocationPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
            {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_REQUEST_CODE);
            }
        }
    }

    private boolean isServiceOk() {
        GoogleApiAvailability googleApi = GoogleApiAvailability.getInstance();
        int result = googleApi.isGooglePlayServicesAvailable(this);
        if(result== ConnectionResult.SUCCESS)
        {
            return true;
        }else if(googleApi.isUserResolvableError(result))
        {
            Dialog dialog = googleApi.getErrorDialog(this,result,PLAY_SERVICES_PERMISSION_CODE,
                    (task)->Toast.makeText(this, "Dialoge is Cancelled By user", Toast.LENGTH_SHORT));
            dialog.show();
        }
        else
        {
            Toast.makeText(this,"Permission Is Rquired By This Application",Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public void onMapReady(GoogleMap googleMap) {

        mGoogleMap=googleMap;
        // mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        mGoogleMap.setMyLocationEnabled(true);
        //String key=mRef.push().getKey();
        double islamabad_lat = objUser.getLat();
        double islamabad_lng = objUser.getLng();
        goToLocation(islamabad_lat, islamabad_lng);

    }

    private void goToLocation(double lat,double lng) {
        LatLng latLng=new LatLng(lat,lng);
        CameraUpdate cameraUpdate= CameraUpdateFactory.newLatLngZoom(latLng,10);
        mGoogleMap.moveCamera(cameraUpdate);
        LatLng latilngi=new LatLng(lat,lng);
        MarkerOptions markerOptions=new MarkerOptions().position(latilngi).title(objUser.getOfficeTitle());
        mGoogleMap.addMarker(markerOptions);
    }

}
