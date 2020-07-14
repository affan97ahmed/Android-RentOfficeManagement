package com.example.jayde.a4ease;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static com.example.jayde.a4ease.RenterGetLocation.PERMISSION_REQUEST_CODE;
import static com.example.jayde.a4ease.RenterGetLocation.PLAY_SERVICES_PERMISSION_CODE;

public class CarBooking extends AppCompatActivity implements OnMapReadyCallback {

    public Booking objBooking;
    private Date dateFrom;
    private Date dateTo;
    private Car car;
    private String renterCnic;
    private User customer;
    private RadioButton withDriver;
    private RadioButton city;
    private ArrayList<Booking> bookingsList = new ArrayList<>();
    private GoogleMap mMap;
    ConstraintLayout cLCarBooking;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_booking);

        Misc.isNetworkConnected(this);
        String json = getIntent().getStringExtra(Misc.inputExtra);
        car = new Gson().fromJson(json, Car.class);

        inItGoogleMap();

        loadUser();
        renterCnic = getIntent().getStringExtra(Misc.inputExtraCnic);
        objBooking = new Booking();
        loadBookings();

        cLCarBooking = findViewById(R.id.cLCarBooking);
        Button btnDate = findViewById(R.id.btnDateFrom);
        Button btnDateTo = findViewById(R.id.btnDateTo);
        TextView txtFrom = findViewById(R.id.txtDateFrom);
        TextView txtTo = findViewById(R.id.txtDateTo);
        Button btnDone = findViewById(R.id.btnBook);
        withDriver = findViewById(R.id.rBtnDriver);
        city = findViewById(R.id.rBtnCity);

        cLCarBooking.getBackground().setAlpha(Misc.alphaMain);

        dateFrom = new Date();
        dateTo = new Date();

        btnDone.setOnClickListener(v-> {
            if(txtFrom.getText().toString().equals("")){
                txtFrom.setError("Please select date");
            }else if( txtTo.getText().toString().equals("")){
                txtTo.setError("Please select date");
            }else{
                book();
            }
        });

        btnDate.setOnClickListener (v-> dateFrom = datePicker(txtFrom));

        btnDateTo.setOnClickListener(v -> dateTo = datePicker(txtTo));
    }

    private void book() {

        if(dateFrom == null && dateTo == null){
            Toast.makeText(this, "Please select dates", Toast.LENGTH_LONG).show();
            return;
        }
        assert dateFrom != null;
        if(dateFrom.after(dateTo)){
            Toast.makeText(this, "please select dates carefully",Toast.LENGTH_LONG).show();
            return;
        }


        long diff = dateTo.getTime() - dateFrom.getTime();
        float dayCount = ((float) diff / (24 * 60 * 60 * 1000)) + 1;
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference();
        String bookingId = System.currentTimeMillis() + "";

        objBooking.setBookedFrom(dateFrom);

        objBooking.setBookedTo(dateTo);

        objBooking.setCustomerId(customer.getCnic());
        objBooking.setRenterId(renterCnic);
        objBooking.setVehicleId(car.getVehicleId());
        objBooking.setCost((int) dayCount * car.getCostPerDay());
        objBooking.setPendingCost((int) dayCount * car.getCostPerDay());
        objBooking.setPaidCost(0);
        objBooking.setStatus(Misc.statusPending);
        objBooking.setId(bookingId);

        if(!checkDate()){
            Toast.makeText(this,"Car is Already booked in these days.", Toast.LENGTH_LONG).show();
            return;
        }

        if(withDriver.isChecked()){
            objBooking.setWithInCity("1");
        }else {
            objBooking.setWithInCity("0");
        }
        if(city.isChecked()){
            objBooking.setWithDriver("1");
        }else{
            objBooking.setWithDriver("0");
        }

        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Total Cost " + ((int) dayCount * car.getCostPerDay()));
        alertDialog.setMessage("Do you want to pay Online?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                (dialog, which) -> {
                    objBooking.setCost((int) dayCount * car.getCostPerDay());
                    objBooking.setPaidCost(car.getCostPerDay());

                    Intent intent = new Intent(this, OnlinePayment.class);
                    Gson gson = new Gson();
                    String obj = gson.toJson(objBooking);
                    intent.putExtra(Misc.inputExtra, obj);

                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(Misc.inProcessBooking, obj);
                    editor.apply();

                    startActivity(intent);
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                (dialog, which) -> {
                    Toast.makeText(this,"Car booked successfully.", Toast.LENGTH_LONG).show();
                    mRef.child(Misc.user).child(Misc.roleRenter).child(renterCnic).child(Misc.bookings).child(bookingId).setValue(bookingId);
                    mRef.child(Misc.user).child(Misc.roleCustomer).child(customer.getCnic()).child(Misc.bookings).child(bookingId).setValue(bookingId);
                    mRef.child(Misc.bookings).child(bookingId).setValue(objBooking);
                    startActivity(new Intent(this, CustomerDashBoard.class));
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Cancel",
                (dialog, which) ->{

                } );
        alertDialog.show();

    }

    private Boolean checkDate() {

        for (Booking obj : bookingsList) {

            if (objBooking.getBookedFrom().after(obj.getBookedFrom()) && objBooking.getBookedFrom().before(obj.getBookedTo())) {
                return false;
            }
            else if(objBooking.getBookedTo().after(obj.getBookedFrom()) && objBooking.getBookedTo().before(obj.getBookedTo()) ){
                return  false;
            }
        }

        return true;
    }

    private Boolean loadBookings() {
        DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference(Misc.bookings);

        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Booking objBooking = postSnapshot.getValue(Booking.class);
                    assert objBooking != null;
                    if(objBooking.getVehicleId().equals(car.getVehicleId())){
                        bookingsList.add(objBooking);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(CarBooking.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }

        });

        return true;
    }

    @SuppressLint("SetTextI18n")
    public Date datePicker(TextView textView){
        Calendar cldr = Calendar.getInstance();
        int day = cldr.get(Calendar.DAY_OF_MONTH);
        int month  = cldr.get(Calendar.MONTH);
        int year = cldr.get(Calendar.YEAR);
        Date date = new Date();
        date.setTime(5);
        // date picker dialog
        @SuppressLint("SetTextI18n") DatePickerDialog picker;
        picker = new DatePickerDialog(
                this, (view, year1, month1, dayOfMonth) -> {
            textView.setText(dayOfMonth + "-" + (month1 + 1) + "-" + year1);
            date.setDate(dayOfMonth);
            date.setMonth(month1);
            date.setYear(year1);
        }, year, month, day);
        picker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        picker.show();
        return date;
    }

    private void loadUser() {
        SharedPreferences sharedPreferences = getSharedPreferences(Misc.user, MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(Misc.user, null);

        customer = gson.fromJson(json, User.class);
        if(customer.getStatus() != null){
            Toast.makeText(this, "Your account is band by admin you can not book any car.", Toast.LENGTH_LONG).show();
            finish();
            startActivity(new Intent(this, CustomerDashBoard.class));
        }
    }

    private void inItGoogleMap() {
        if(isServiceOk())
        { SupportMapFragment supportMapFragment=SupportMapFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.map_fragment_car_booking,supportMapFragment).commit();
            supportMapFragment.getMapAsync(CarBooking.this);
            if(checkLocationPermission())
            {
            }
            else
            {
                requestLocationPermission();
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        // Check if we were successful in obtaining the map.
        if (mMap != null) {
            mMap.setOnMyLocationChangeListener(arg0 -> {
                objBooking.setLat((float) arg0.getLatitude());
                objBooking.setLng((float) arg0.getLongitude());
            });
        }
    }

    public void hideKeyBoard(View view){
        InputMethodManager imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }

}
