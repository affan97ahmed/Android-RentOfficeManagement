package com.example.jayde.a4ease;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;

import static com.example.jayde.a4ease.RenterGetLocation.PERMISSION_REQUEST_CODE;
import static com.example.jayde.a4ease.RenterGetLocation.PLAY_SERVICES_PERMISSION_CODE;

public class CustomerDashBoard extends AppCompatActivity implements OnMapReadyCallback {

    private LinearLayout llSearch;
    private float lng;
    private float lat;
    public int id;
    private ArrayList<User> rentHousesArray;
    private ArrayList<Car> carsOfRentHouse = new ArrayList<>();
    private User objUser;
    public ArrayList<String> bookingsKey = new ArrayList<>();
    public LinearLayout llBC, llNoti;
    private ImageButton btnNotifications;
    public Car car;
    private User customer;
    public ArrayList<Booking> listBookins = new ArrayList<>();
    private long backPressedTime = 0;
    private GoogleMap mMap;
    private SearchView sv;
    private int key;
    private ArrayList<Notification> listNotifications = new ArrayList<>();
    private ArrayList<Notification> listNoti = new ArrayList<>();
    private int countListNoti = 0;
    private int countListNotifications = 0;
    public Boolean dsFlag = true, svFlag = false;
    public DataSnapshot ds;
    private LinearLayout llPb;
    public Toast myToast;


    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_dash_board);

        Misc.isNetworkConnected(this);
        Misc.turnGPSOn(this);
        findViewById(R.id.cLCustomerDashboard).getBackground().setAlpha(Misc.alphaMain);

        myToast = Toast.makeText(this, "No car available", Toast.LENGTH_LONG);
        manageScreen();

        key = 1;
        inItGoogleMap();

        rentHousesArray = new ArrayList<>();
        carsOfRentHouse = new ArrayList<>();

        llNoti = findViewById(R.id.ll_noti);
        llPb = findViewById(R.id.myProgressBar);

        loadUser();
        loadBookings();
        loadNotifications();
        refreshNotifications();

        Button buttonSearch = findViewById(R.id.btnSearch);
        Button buttonBookings = findViewById(R.id.btnBookingsC);
        sv = findViewById(R.id.searchViewCar);
        llSearch = findViewById(R.id.ll_search);
        llBC = findViewById(R.id.llBookingC);
        ImageButton btnAddCar = findViewById(R.id.btn_add_car);
        btnNotifications = findViewById(R.id.btn_notifications_customers);

        btnNotifications.setEnabled(false);

        buttonBookings.getBackground().setAlpha(Misc.alphaBtnLow);

        ImageButton buttonLogout = findViewById(R.id.btn_logout_customer);

        buttonLogout.setOnClickListener(v ->{
            android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(this).create();
            alertDialog.setTitle("Logout");
            alertDialog.setMessage("Are you sure?");
            alertDialog.setButton(android.app.AlertDialog.BUTTON_POSITIVE, "Yes",
                    (dialog, which) -> {
                        deleteUser();
                        startActivity(new Intent(this, login.class));
                        finish();
                    });
            alertDialog.setButton(android.app.AlertDialog.BUTTON_NEGATIVE, "No",
                    (dialog, which) -> {
                    });
            alertDialog.show();
        });

        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                llSearch.removeAllViews();
                svFlag = true;
                loadRentHouses(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //    adapter.getFilter().filter(newText);
                return false;
            }
        });

        buttonBookings.setOnClickListener(v -> {
            buttonSearch.getBackground().setAlpha(Misc.alphaBtnLow);
            buttonBookings.getBackground().setAlpha(100);
            sv.setVisibility(View.GONE);
            llSearch.setVisibility(View.GONE);
            llBC.setVisibility(View.VISIBLE);
            llNoti.setVisibility(View.GONE);
        });

        buttonSearch.setOnClickListener(v ->  {
            buttonBookings.getBackground().setAlpha(Misc.alphaBtnLow);
            buttonSearch.getBackground().setAlpha(100);
            sv.setVisibility(View.VISIBLE);
            llSearch.setVisibility(View.VISIBLE);
            llNoti.setVisibility(View.GONE);
            llBC.setVisibility(View.GONE);
        });

        btnAddCar.setOnClickListener(v->{
            llNoti.setVisibility(View.GONE);
            Intent intent = new Intent(this, CarsCustomer.class);
            intent.putExtra(Misc.inputExtra, Misc.roleCustomer);
            startActivity(intent);
        });

        btnNotifications.setOnClickListener(v->{
            buttonBookings.getBackground().setAlpha(Misc.alphaBtnLow);
            buttonSearch.getBackground().setAlpha(Misc.alphaBtnLow);
            sv.setVisibility(View.GONE);
            llSearch.setVisibility(View.GONE);
            llNoti.setVisibility(View.VISIBLE);
            llBC.setVisibility(View.GONE);
        });

    }

    private void loadUser() {
        SharedPreferences sharedPreferences = getSharedPreferences(Misc.user, MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(Misc.user, null);

        objUser = gson.fromJson(json, User.class);

        TextView temp = findViewById(R.id.txt_UserName);
        temp.setText(objUser.getName());
    }

    private  void loadRentHouses(String text){
        llPb.setVisibility(View.VISIBLE);
        DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference(Misc.user).child(Misc.roleRenter);

        rentHousesArray = new ArrayList<>();
        carsOfRentHouse.clear();

        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    User objUser = postSnapshot.getValue(User.class);

                    assert objUser != null;
                    float[] objDistance = new float[10];
                    Location.distanceBetween(objUser.getLat(),objUser.getLng(),lat, lng, objDistance);

                    if(objUser.getStatus() == null){
                        if(text == null){
                            mDatabaseRef.child(objUser.getCnic()).child(Misc.folderVehicle).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    carsOfRentHouse = new ArrayList<>();

                                    for (DataSnapshot vehi : dataSnapshot.getChildren()) {
                                        Car objCar = vehi.getValue(Car.class);
                                        assert objCar != null;
                                        if(objCar.getStatus() == null){
                                            carsOfRentHouse.add(objCar);
                                        }
                                    }

                                    objUser.setCarsArrayList(new ArrayList<>(carsOfRentHouse));
                                    if(rentHousesArray.size() == 0){
                                        rentHousesArray.add(objUser);
                                    }else{
                                        Log.d(Misc.logKey, rentHousesArray.size() + "");
                                        boolean flag = true;
                                        for(int i = 0; i < rentHousesArray.size(); i++){
                                            float[] previousDistance = new float[10];
                                            Location.distanceBetween(rentHousesArray.get(i).getLat(),
                                                    rentHousesArray.get(i).getLng(),lat, lng, previousDistance);
                                            if(objDistance[0] < previousDistance[0]){
                                                flag = false;
                                                rentHousesArray.add(i, objUser);
                                                break;
                                            }
                                        }
                                        if(flag){
                                            rentHousesArray.add(objUser);
                                        }
                                    }

                                    llSearch.removeAllViews();
                                    for(int i = 0; i < rentHousesArray.size(); i++){
                                        addRentHouses(rentHousesArray.get(i), i);
                                    }
                                    llPb.setVisibility(View.GONE);
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                        else {
                            mDatabaseRef.child(objUser.getCnic()).child(Misc.folderVehicle).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    boolean b = true;
                                    carsOfRentHouse = new ArrayList<>();

                                    for (DataSnapshot vehi : dataSnapshot.getChildren()) {

                                        Car objCar = vehi.getValue(Car.class);
                                        assert objCar != null;
                                        if(objCar.getName().equalsIgnoreCase(text) && objCar.getStatus() == null){
                                            b = false;
                                            carsOfRentHouse.add(objCar);
                                        }
                                    }

                                    if(!b){
                                        objUser.setCarsArrayList(new ArrayList<>(carsOfRentHouse));
                                        if(rentHousesArray.size() == 0){
                                            rentHousesArray.add(objUser);
                                        }else{
                                            Log.d(Misc.logKey, rentHousesArray.size() + "");
                                            boolean flag = true;
                                            for(int i = 0; i < rentHousesArray.size(); i++){
                                                float[] previousDistance = new float[10];
                                                Location.distanceBetween(rentHousesArray.get(i).getLat(),
                                                        rentHousesArray.get(i).getLng(),lat, lng, previousDistance);
                                                if(objDistance[0] < previousDistance[0]){
                                                    flag = false;
                                                    rentHousesArray.add(i, objUser);
                                                    break;
                                                }
                                            }
                                            if(flag){
                                                rentHousesArray.add(objUser);
                                            }
                                        }


                                        llSearch.removeAllViews();
                                        if(rentHousesArray == null){
                                            myToast.show();
                                        }
                                        for(int i = 0; i < rentHousesArray.size(); i++){
                                            addRentHouses(rentHousesArray.get(i), i);
                                        }

                                    }else{
                                        myToast.show();

                                    }
                                    llPb.setVisibility(View.GONE);
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(CustomerDashBoard.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                llPb.setVisibility(View.GONE);
            }
        });
    }

    @SuppressLint("ResourceType")
    public void addRentHouses(User renter, int id){
        LinearLayout llMain = makeLinearLayout(1);
        llMain.setId(id);
        llMain.setBackgroundResource(getResources().getIdentifier(String.valueOf(R.drawable.bg_white), null, null));
        LinearLayout.LayoutParams p = new  LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.setMargins(40,10,40,0);

        llMain.setLayoutParams(p);
        llMain.setPadding(20,20,20,20);

        llMain.addView(makTextView("Owner Name", 2));
        llMain.addView(makTextView(renter.getName(), 1));

        LinearLayout llCompanyAndNumber = makeLinearLayout(2);
        llMain.addView(llCompanyAndNumber);

        LinearLayout ll1 = makeLinearLayout(1);
        ll1.addView(makTextView("Office", 2));
        ll1.addView(makTextView(renter.getOfficeTitle(), 1));
        p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.weight = 1;

        ll1.setLayoutParams(p);

        LinearLayout ll2 = makeLinearLayout(1);

        ll2.addView(makTextView("Contact No", 2));
        ll2.addView(makTextView(renter.getPhNo(), 1));
        ll2.setLayoutParams(p);

        llCompanyAndNumber.addView(ll1);
        llCompanyAndNumber.addView(ll2);

        LinearLayout llDistance = Misc.makeLinearLayout(1,this);

        float[] result = new float[10];
        Location.distanceBetween(renter.getLat(),renter.getLng(),lat, lng, result);
        int res = (int) result[0];

        llDistance.addView(makTextView("Distance", 2));
        llDistance.addView(makTextView("" + res +  " m", 1));
        p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.weight = 1;
        llDistance.setLayoutParams(p);


        LinearLayout llDBtn = Misc.makeLinearLayout(2, this);

        llMain.addView(llDBtn);
        llDBtn.addView(llDistance);
        Button btnLocation = Misc.makeBtn("Location", this);
        btnLocation.setTextColor(Color.WHITE);

        llDBtn.addView(btnLocation);
        btnLocation.setBackgroundResource(getResources().getIdentifier(String.valueOf(R.drawable.bg_blue_shadow), null, null));
        btnLocation.setLayoutParams(p);

        btnLocation.setOnClickListener(v->{
            Intent intent = new Intent(this, DisplayLocation.class);
            Gson gson = new Gson();
            String str = gson.toJson(renter);
            intent.putExtra(Misc.inputExtra, str);
            startActivity(intent);

        });

        llMain.setOnClickListener(v ->{
            Log.d(Misc.logKey, llMain.getId() + "");
            Intent intent = new Intent(this, DisplayCars.class);
            Gson gson = new Gson();
            String json = gson.toJson(rentHousesArray.get(llMain.getId()));
            intent.putExtra(Misc.inputExtraCarsHashMap,json);
            intent.putExtra(Misc.inputExtraCnic, renter.getCnic());
            startActivity(intent);
        });

        llSearch.addView(llMain);
    }

    private TextView makTextView(String txt, int type){
        TextView justTextView = new TextView(this);
        LinearLayout.LayoutParams p;
        if(type == 1){
            p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 60);
            justTextView.setTextSize(20f);
        }else{
            p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 30);
            justTextView.setTextSize(10f);
        }
        p.weight = 1;
        justTextView.setLayoutParams(p);

        justTextView.setText(txt);
        return  justTextView;
    }

    private LinearLayout makeLinearLayout(int orientation){
        LinearLayout ll = new LinearLayout(this);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if(orientation == 1){
            ll.setOrientation(LinearLayout.VERTICAL);
        }else {
            ll.setOrientation(LinearLayout.HORIZONTAL);
            ll.setWeightSum(2);
        }
        ll.setLayoutParams(p);

        return ll;
    }

    private void deleteUser(){
        SharedPreferences.Editor editor = getSharedPreferences(Misc.user, MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();
    }

    private void loadBookings(){
        DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference(Misc.user).child(Misc.roleCustomer)
                .child(objUser.getCnic()).child(Misc.bookings);
        DatabaseReference dbRefForBookings = FirebaseDatabase.getInstance().getReference(Misc.bookings);

        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    bookingsKey.add(postSnapshot.getKey());
                }

                dbRefForBookings.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot post: dataSnapshot.getChildren()){
                            Booking bookings = post.getValue(Booking.class);
                            String temp = post.getKey();

                            for (String key: bookingsKey){
                                if (key.equals(temp)){
                                    getBookedCar(bookings);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(CustomerDashBoard.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }

        });
    }

    private void getBookedCar(Booking obj){
        FirebaseDatabase.getInstance().getReference().child(Misc.user)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        customer = dataSnapshot.child(Misc.roleCustomer).child(obj.getCustomerId()).getValue(User.class);
                        car = dataSnapshot.child(Misc.roleRenter).child(obj.getRenterId()).child(Misc.folderVehicle)
                                .child(obj.getVehicleId()).getValue(Car.class);
                        addBookings(obj);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void addBookings(Booking obj){
        listBookins.add(obj);

        LinearLayout llMain = Misc.makeLinearLayout(1, this);
        llMain.setBackgroundResource(getResources().getIdentifier(String.valueOf(R.drawable.bg_white), null, null));
        LinearLayout.LayoutParams p = new  LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.setMargins(40,10,40,0);
        llMain.setLayoutParams(p);

        llMain.setPadding(20,20,20,20);

        p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.weight = 1;


        LinearLayout llNameAndId = makeLinearLayout(2);
        llMain.addView(llNameAndId);

        LinearLayout ll = makeLinearLayout(1);
        LinearLayout ll0 = makeLinearLayout(1);
        llNameAndId.addView(ll);
        llNameAndId.addView(ll0);
        ll.setLayoutParams(p);
        ll0.setLayoutParams(p);

        ll.addView(Misc.makeTextView("Car Name", 2,this));
        ll.addView(Misc.makeTextView(car.getName(), 1, this));

        ll0.addView(Misc.makeTextView("Booking ID", 2,this));
        ll0.addView(Misc.makeTextView(obj.getId(), 1, this));


        LinearLayout llCompanyAndNumber = Misc.makeLinearLayout(2,this);
        llMain.addView(llCompanyAndNumber);

        LinearLayout ll1 = Misc.makeLinearLayout(1, this);
        ll1.setLayoutParams(p);
        ll1.addView(Misc.makeTextView("Customer Name", 2, this));
        ll1.addView(Misc.makeTextView(customer.getName(), 1, this));

        LinearLayout ll2 = Misc.makeLinearLayout(1, this);
        ll2.setLayoutParams(p);
        ll2.addView(Misc.makeTextView("Customer CNIC",2, this));
        ll2.addView(Misc.makeTextView(customer.getCnic(), 1, this));

        llCompanyAndNumber.addView(ll1);
        llCompanyAndNumber.addView(ll2);

        LinearLayout llModelAndCondition = Misc.makeLinearLayout(2, this);
        llMain.addView(llModelAndCondition);

        LinearLayout ll3 = Misc.makeLinearLayout(1,this);
        ll3.setLayoutParams(p);

        int m = obj.getBookedFrom().getMonth() +1;

        ll3.addView(Misc.makeTextView("Booked From", 2, this));
        ll3.addView(Misc.makeTextView(obj.getBookedFrom().getDate() + "/" +
                m + "/" + obj.getBookedFrom().getYear(), 1,this));

        LinearLayout ll4 = Misc.makeLinearLayout(1,this);
        ll4.setLayoutParams(p);

        int m1 = obj.getBookedTo().getMonth() + 1;

        ll4.addView(Misc.makeTextView("Booked To", 2, this));
        ll4.addView(Misc.makeTextView(obj.getBookedTo().getDate() + "/" +
                m1 + "/" + obj.getBookedTo().getYear(), 1,this));


        llModelAndCondition.addView(ll3);
        llModelAndCondition.addView(ll4);

        LinearLayout llCost = Misc.makeLinearLayout(2, this);

        LinearLayout ll5 = Misc.makeLinearLayout(1,this);
        ll3.setLayoutParams(p);

        ll3.addView(Misc.makeTextView("Total Cost", 2, this));
        ll3.addView(Misc.makeTextView(obj.getCost() + "", 1,this));

        LinearLayout ll6 = Misc.makeLinearLayout(1,this);
        ll4.setLayoutParams(p);

        ll4.addView(Misc.makeTextView("Paid", 2, this));
        ll4.addView(Misc.makeTextView(obj.getPaidCost() + "", 1,this));


        llCost.addView(ll5);
        llCost.addView(ll6);

        llMain.addView(llCost);
        llBC.addView(llMain);
    }

    private void loadNotifications(){
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference().child(Misc.user).child(Misc.roleCustomer).child(objUser.getCnic());
        databaseRef.child(Misc.notifications).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Notification objNotification = postSnapshot.getValue(Notification.class);
                    if(objNotification != null && objNotification.getType() != null){

                        if(objNotification.getType().equals(Misc.notiTypeAddCar)){
                            addNoti(objNotification);
                        }else{
                            addNotification(objNotification);
                        }
                        btnNotifications.setEnabled(true);
                        btnNotifications.setImageResource(getResources().getIdentifier(String.valueOf(R.drawable.ic_notifications_blue_24dp), null, null));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(CustomerDashBoard.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addNotification(Notification objNotification) {
        LinearLayout llMain = Misc.makeLinearLayout(1, this);

        llNoti.addView(llMain);
        listNotifications.add(objNotification);
        llMain.setId(countListNotifications);
        countListNotifications++;

        llMain.setBackgroundResource(getResources().getIdentifier(String.valueOf(R.drawable.bg_white), null, null));
        LinearLayout.LayoutParams p = new  LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.setMargins(40,10,40,0);
        llMain.setLayoutParams(p);

        llMain.setPadding(20,20,20,20);

        TextView temp = Misc.makeTextView(objNotification.getMessage(), 1, this);
        LinearLayout.LayoutParams pTxt = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        temp.setLayoutParams(pTxt);
        llMain.addView(Misc.makeTextView("Collect Cash", 2,this));
        llMain.addView(temp);

        Button btn = Misc.makeBtn("Collected", this);
        btn.setBackgroundResource(getResources().getIdentifier(String.valueOf(R.drawable.bg_blue_shadow), null, null));

        llMain.addView(btn);

        btn.setOnClickListener(v-> {
            Intent intent = new Intent(this, CustomerDashBoard.class);
            android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(this).create();
            alertDialog.setTitle("Confirm");
            alertDialog.setMessage("Are you sure?");
            alertDialog.setButton(android.app.AlertDialog.BUTTON_POSITIVE, "Yes",
                    (dialog, which) -> {
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                        Query applesQuery;
                        applesQuery = ref.child(Misc.user).child(Misc.roleCustomer).child(objUser.getCnic())
                                .child(Misc.notifications).child(listNotifications.get(llMain.getId()).getId());

                        applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                                    appleSnapshot.getRef().removeValue();
                                }

                                finish();
                                startActivity(intent);
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.e(Misc.logKey, "onCancelled", databaseError.toException());
                            }
                        });
                    });
            alertDialog.setButton(android.app.AlertDialog.BUTTON_NEGATIVE, "No",
                    (dialog, which) -> {
                    });
            alertDialog.show();
        });

    }

    private void addNoti(Notification objNotification) {
        LinearLayout llMain = Misc.makeLinearLayout(1, this);

        llNoti.addView(llMain);

        llMain.setId(countListNoti);
        countListNoti++;

        listNoti.add(objNotification);

        llMain.setBackgroundResource(getResources().getIdentifier(String.valueOf(R.drawable.bg_white), null, null));
        LinearLayout.LayoutParams p = new  LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.setMargins(40,10,40,0);
        llMain.setLayoutParams(p);

        llMain.setPadding(20,20,20,20);

        llMain.addView(Misc.makeTextView("Car Request", 2,this));
        TextView txt = Misc.makeTextView(objNotification.getStatus(), 1, this);
        p = new  LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        txt.setLayoutParams(p);
        llMain.addView(txt);

        Button btn = Misc.makeBtn("Ok", this);
        btn.setBackgroundResource(getResources().getIdentifier(String.valueOf(R.drawable.bg_blue_shadow), null, null));

        llMain.addView(btn);

        btn.setOnClickListener(v-> {
            Intent intent = new Intent(this, CustomerDashBoard.class);
            android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(this).create();
            alertDialog.setTitle("Confirm");
            alertDialog.setMessage("Are you sure?");
            alertDialog.setButton(android.app.AlertDialog.BUTTON_POSITIVE, "Yes",
                    (dialog, which) -> {
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                        Query applesQuery;
                        applesQuery = ref.child(Misc.user).child(Misc.roleCustomer).child(objUser.getCnic())
                                .child(Misc.notifications).orderByChild("vehicleId").equalTo(listNoti.get(llMain.getId()).getVehicleId());

                        applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                                    appleSnapshot.getRef().removeValue();
                                }
                                finish();
                                startActivity(intent);
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.e(Misc.logKey, "onCancelled", databaseError.toException());
                            }
                        });

                    });
            alertDialog.setButton(android.app.AlertDialog.BUTTON_NEGATIVE, "No",
                    (dialog, which) -> {
                    });
            alertDialog.show();
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        // Check if we were successful in obtaining the map.
        if (mMap != null) {
            mMap.setOnMyLocationChangeListener(arg0 -> {
                mMap.addMarker(new MarkerOptions().position(new LatLng(arg0.getLatitude(), arg0.getLongitude())).title("It's Me!"));
                lat = (float) arg0.getLatitude();
                lng = (float) arg0.getLongitude();
                if (key == 1){
                    loadRentHouses(null);
                    key = 0;
                }
            });
        }
        else{
            Toast.makeText(this, "Can not fetch you location !!!", Toast.LENGTH_LONG).show();
            sv.setSubmitButtonEnabled(false);
        }
    }

    private void inItGoogleMap() {
        if(isServiceOk())
        { SupportMapFragment supportMapFragment=SupportMapFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.map_fragment_car_booking,supportMapFragment).commit();
            supportMapFragment.getMapAsync(CustomerDashBoard.this);
            if(!checkLocationPermission())
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
            @SuppressLint("ShowToast") Dialog dialog = googleApi.getErrorDialog(this,result,PLAY_SERVICES_PERMISSION_CODE,
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
    public void onBackPressed() {
        if(svFlag){
            llSearch.removeAllViews();
            loadRentHouses(null);
            svFlag = false;
            return;
        }

        Toast backToast;
        backToast = Toast.makeText(this, "Press back again to exit", Toast.LENGTH_LONG);

        if(backPressedTime + 2000 > System.currentTimeMillis()){
            backToast.cancel();
            super.onBackPressed();
            finish();
            return;
        }else{
            backToast.show();
        }

        backPressedTime = System.currentTimeMillis();
    }

    private void refreshNotifications() {
        DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference(Misc.user).child(Misc.roleCustomer).child(objUser.getCnic());

        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dsFlag){
                    ds = dataSnapshot;
                    dsFlag = false;
                }
                else {
                    if(ds != dataSnapshot){
                        llNoti.removeAllViews();
                        loadNotifications();
                    }
                }

                ds = dataSnapshot;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void manageScreen(){
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int incHeight = (int) (dm.heightPixels * 0.25);
        int svHeight =(int) (dm.heightPixels * 0.5);

        ScrollView scrollView = findViewById(R.id.scrollViewCDB);
        View inc = findViewById(R.id.includeCDB);

        ViewGroup.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, incHeight);
        inc.setLayoutParams(p);

        p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, svHeight);
        scrollView.setLayoutParams(p);
    }

}
