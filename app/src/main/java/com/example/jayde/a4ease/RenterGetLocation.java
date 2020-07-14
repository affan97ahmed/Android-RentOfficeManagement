package com.example.jayde.a4ease;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.Map;

public class RenterGetLocation extends AppCompatActivity implements OnMapReadyCallback {

    private DatabaseReference mRef = FirebaseDatabase.getInstance().getReference(Misc.user);
    private EditText etOfficeTitle;
    private static final String TAG = "MapDebug";
    private GoogleMap mGoogleMap;
    public static final int PERMISSION_REQUEST_CODE = 9003;
    public static final int PLAY_SERVICES_PERMISSION_CODE = 9003;
    public static final int GPS_SERVICES_PERMISSION_CODE = 9001;
    public double lang;
    public double lat;
    private FusedLocationProviderClient mLocationClient;
    public User objUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.startup_renter);
        Misc.isNetworkConnected(this);
        inItGoogleMap();
        isGpsEnabled();

        Misc.turnGPSOn(this);

        Gson gson = new Gson();
        String  json = getIntent().getStringExtra(Misc.inputExtra);
        objUser = new User();
        objUser = gson.fromJson(json, User.class);

       etOfficeTitle = findViewById(R.id.etTitle);
    }

    private void setMarker() {
        mRef.child(Misc.user).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshtt:dataSnapshot.getChildren()) {
                    Map<String, Object> data2 = (Map<String, Object>) snapshtt.getValue();

                    double lt =  (double)data2.get("lat");
                    double lg =  (double) data2.get("lang");
                    String t=(String)data2.get("Title");

                    LatLng latilngi=new LatLng(lt,lg);
                    MarkerOptions markerOptions=new MarkerOptions().position(latilngi).title(t);
                    mGoogleMap.addMarker(markerOptions);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void finallySignup(View view) {
        if(etOfficeTitle.getText().toString().equals("")){
            Toast.makeText(this, "Please enter rent office name", Toast.LENGTH_LONG).show();
            return;
        }
        mLocationClient=new FusedLocationProviderClient(RenterGetLocation.this);

        mLocationClient.getLastLocation().addOnCompleteListener(task -> {
            if(task.isSuccessful())
            {
                Location location=task.getResult();
                if (location != null) {
                    lang=location.getLongitude();
                    lat =location.getLatitude();
                    LatLng latilngi=new LatLng(lat,lang);
                    MarkerOptions markerOptions=new MarkerOptions().position(latilngi).title(etOfficeTitle.getText().toString());
                    mGoogleMap.addMarker(markerOptions);
                    objUser.setLat(lat);
                    objUser.setLng(lang);
                    objUser.setOfficeTitle(etOfficeTitle.getText().toString());
                    Log.d(Misc.logKey,latilngi.toString());
                    mRef.child(objUser.getUserRole()).child(objUser.getCnic()).setValue(objUser);
                    setMarker();
                }
                Toast.makeText(RenterGetLocation.this,"You Successfully Signed Up !",Toast.LENGTH_SHORT).show();
                startActivity(new Intent(RenterGetLocation.this, login.class));
            }
        });
    }

    private void inItGoogleMap() {
        if(isServiceOk())
        { SupportMapFragment supportMapFragment=SupportMapFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.map_fragment,supportMapFragment).commit();
            supportMapFragment.getMapAsync( this);
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

    private void goToLocation(double lat,double lng) {
        LatLng latLng=new LatLng(lat,lng);
        CameraUpdate cameraUpdate= CameraUpdateFactory.newLatLngZoom(latLng,10);
        mGoogleMap.moveCamera(cameraUpdate);
    }

    private void isGpsEnabled() {
        LocationManager locationManager= (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean providerEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(providerEnabled)
        { Toast.makeText(this,"GPS is Enabled ",Toast.LENGTH_SHORT).show();
            //return true;
        }else
        {    //Toast.makeText(this,"GPS is notEnabled ",Toast.LENGTH_SHORT).show();
            AlertDialog alertDialog=new AlertDialog.Builder(this)
                    .setTitle("GPS Permission")
                    .setMessage("GPS is required to Add your Current Location. Please Enable GPS.")
                    .setPositiveButton("Yes",((dialogInterface,i)-> {
                        Intent intent=new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(intent,GPS_SERVICES_PERMISSION_CODE);
                    })).setCancelable(false).show();

        }

        //return false;
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
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
                    (task)->Toast.makeText(this, "Dialoge is Cancelled By user", Toast.LENGTH_SHORT).show());
            dialog.show();
        }
        else
        {
            Toast.makeText(this,"Permission Is Rquired By This Application",Toast.LENGTH_SHORT).show();
        }
        return false;
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

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mGoogleMap=googleMap;
        mGoogleMap.setMyLocationEnabled(true);
        double islamabad_lat = 33.690904;
        double islamabad_lng = 73.051865;
        goToLocation(islamabad_lat, islamabad_lng);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==GPS_SERVICES_PERMISSION_CODE)
        {  LocationManager locationManager= (LocationManager) getSystemService(LOCATION_SERVICE);
            boolean providerEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if(providerEnabled)
            {
                Toast.makeText(this,"GPS is Enabled ",Toast.LENGTH_SHORT).show();
            }else
            {
                Toast.makeText(this,"GPS is not Enabled ",Toast.LENGTH_SHORT).show();
            }
        }
    }

}
