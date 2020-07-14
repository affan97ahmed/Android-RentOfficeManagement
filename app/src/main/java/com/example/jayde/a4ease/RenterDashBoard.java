package com.example.jayde.a4ease;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

public class RenterDashBoard extends AppCompatActivity {

    private ImageButton btnAddVehicle;
    public int idForLayout = 0;
    private User objUser;
    LinearLayout linearLayoutVehicles;
    private ImageButton btnNotification;
    private LinearLayout llNotifications;
    private ArrayList<Car> carsList;
    private int id;
    public ArrayList<String> bookingsKey = new ArrayList<String>();
    LinearLayout linearLayoutBooking;
    public Car car;
    String lat = null, lng = null;
    public User customer;
    public ArrayList<Booking> listBookins = new ArrayList<>();
    private ArrayList<Car> listCars = new ArrayList<>();
    private SearchView sv;
    private int countListCars = 0;
    private long backPressedTime = 0;
    LinearLayout llNotStarted, llOnGoing, llCompleted, temp;
    private boolean flag, svFlag;
    public DataSnapshot ds, dsNotification;
    boolean dsFlag = true;
    boolean dsFlagNotification= true;
    private LinearLayout llPb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_renter_dash_board);

        Misc.isNetworkConnected(this);
        svFlag = true;

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int incHeight = (int) (dm.heightPixels * 0.25);
        int svHeight =(int) (dm.heightPixels * 0.67);

        LinearLayout lin = findViewById(R.id.linearLayoutRDB);
        View inc = findViewById(R.id.includeRDB);

        ViewGroup.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, incHeight);
        inc.setLayoutParams(p);

        p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, svHeight);
        lin.setLayoutParams(p);

        Button buttonVehicle = findViewById(R.id.btnVehicles);
        Button buttonBookings = findViewById(R.id.btnBookings);
        ImageButton buttonLogout = findViewById(R.id.btn_logout);
        linearLayoutVehicles = this.findViewById(R.id.llVehicles);
        linearLayoutBooking = findViewById(R.id.llBooking);
        btnAddVehicle = findViewById(R.id.btn_add_Vehicle);
        btnNotification = findViewById(R.id.btn_notifications);
        llNotifications = findViewById(R.id.ll_notifications);
        sv = findViewById(R.id.searchViewBookings);
        btnNotification.setEnabled(false);
        llPb = findViewById(R.id.myProgressBar);

        findViewById(R.id.cLRenter).getBackground().setAlpha(Misc.alphaMain);

        buttonBookings.getBackground().setAlpha(Misc.alphaBtnLow);
        buttonVehicle.getBackground().setAlpha(Misc.alphaBtnHigh);

        carsList = new ArrayList<>();
        ArrayList<String> imageUrlList = new ArrayList<>();

        loadUser();
        loadCars();
        loadNotifications();
        loadBookings(null);
        makeTempLL();

        refreshNotifications();
        refreshPage();

        buttonLogout.setOnClickListener(v ->{

            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Logout");
            alertDialog.setMessage("Are you sure?");
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                    (dialog, which) -> {
                        deleteUser();
                        startActivity(new Intent(this, login.class));
                        finish();
                    });
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                    (dialog, which) -> {
                    });
            alertDialog.show();
        });

        btnNotification.setOnClickListener(v->{
            buttonBookings.getBackground().setAlpha(Misc.alphaBtnLow);
            buttonVehicle.getBackground().setAlpha(Misc.alphaBtnLow);
            linearLayoutVehicles.setVisibility(View.GONE);
            linearLayoutBooking.setVisibility(View.GONE);
            btnAddVehicle.setVisibility(View.GONE);
            sv.setVisibility(View.GONE);
            llNotifications.setVisibility(View.VISIBLE);
            lin.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (dm.heightPixels * .67)));
        });

        btnAddVehicle.setOnClickListener(v ->{
            Intent intent = new Intent(this,AddVehicle.class);
            intent.putExtra(Misc.inputExtraCar, "null");
            intent.putExtra(Misc.inputExtra, Misc.roleCustomer);
            startActivity(intent);
        });

        buttonVehicle.setOnClickListener(v -> {
            buttonBookings.getBackground().setAlpha(Misc.alphaBtnLow);
            buttonVehicle.getBackground().setAlpha(Misc.alphaBtnHigh);
            sv.setVisibility(View.GONE);
            linearLayoutVehicles.setVisibility(View.VISIBLE);
            linearLayoutBooking.setVisibility(View.GONE);
            llNotifications.setVisibility(View.GONE);
            btnAddVehicle.setVisibility(View.VISIBLE);
            lin.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (dm.heightPixels * .67)));
        });

        buttonBookings.setOnClickListener(v -> {
            sv.setVisibility(View.VISIBLE);
            buttonBookings.getBackground().setAlpha(Misc.alphaBtnHigh);
            buttonVehicle.getBackground().setAlpha(Misc.alphaBtnLow);
            linearLayoutVehicles.setVisibility(View.GONE);
            llNotifications.setVisibility(View.GONE);
            linearLayoutBooking.setVisibility(View.VISIBLE);
            btnAddVehicle.setVisibility(View.GONE);
            LinearLayout.LayoutParams p1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (dm.heightPixels * .55));
            lin.setLayoutParams(p1);

        });

        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                llOnGoing.removeAllViews();
                llCompleted.removeAllViews();
                llNotStarted.removeAllViews();
                makeTempLL();
                svFlag = false;
                loadBookings(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //    adapter.getFilter().filter(newText);
                return false;
            }
        });

    }

    private void refreshPage() {
        DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference(Misc.user).child(Misc.roleRenter).child(objUser.getCnic())
                .child(Misc.folderVehicle);
        Intent intent = new Intent(this, RenterDashBoard.class);

        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dsFlag){
                    ds = dataSnapshot;
                    dsFlag = false;
                }
                if(ds != dataSnapshot){
                    finish();
                    startActivity(intent);
                }
                ds = dataSnapshot;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(RenterDashBoard.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCars(){
        llPb.setVisibility(View.VISIBLE);
        DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference(Misc.user).child(Misc.roleRenter).child(objUser.getCnic()).child(Misc.folderVehicle);
        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dsFlag){
                    ds = dataSnapshot;
                }
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Car objCar = postSnapshot.getValue(Car.class);
                    assert objCar != null;
                    if(objCar.getStatus() == null){
                        addCarInfo(objCar, postSnapshot.child(Misc.folderImages), linearLayoutVehicles, 0);
                    }
                }
                llPb.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(RenterDashBoard.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                llPb.setVisibility(View.GONE);
            }
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

    private void deleteUser(){
        SharedPreferences.Editor editor = getSharedPreferences(Misc.user, MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();
    }

    public void addCarInfo(Car objCar, DataSnapshot imageSnapShot,LinearLayout ll, int llId)  {
        LinearLayout llMain = Misc.makeLinearLayout(1, this);
        llMain.setId(countListCars);
        listCars.add(objCar);
        countListCars++;
        llMain.setBackgroundResource(getResources().getIdentifier(String.valueOf(R.drawable.bg_white), null, null));
        LinearLayout.LayoutParams p = new  LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.setMargins(40,10,40,0);
        llMain.setLayoutParams(p);

        llMain.setPadding(20,20,20,20);
        p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.weight = 1;

        llMain.addView(Misc.makeTextView("Name", 2,this));
        llMain.addView(Misc.makeTextView(objCar.getName(), 1, this));

        LinearLayout llCompanyAndNumber = Misc.makeLinearLayout(2,this);
        llMain.addView(llCompanyAndNumber);

        LinearLayout ll1 = Misc.makeLinearLayout(1, this);
        ll1.setLayoutParams(p);
        ll1.addView(Misc.makeTextView("Company", 2, this));
        ll1.addView(Misc.makeTextView(objCar.getCompany(), 1, this));

        LinearLayout ll2 = Misc.makeLinearLayout(1, this);
        ll2.setLayoutParams(p);
        ll2.addView(Misc.makeTextView("Model",2, this));
        ll2.addView(Misc.makeTextView(objCar.getModel(), 1, this));

        llCompanyAndNumber.addView(ll1);
        llCompanyAndNumber.addView(ll2);

        LinearLayout llModelAndCondition = Misc.makeLinearLayout(2, this);
        llMain.addView(llModelAndCondition);

        LinearLayout ll3 = Misc.makeLinearLayout(1,this);
        ll3.setLayoutParams(p);

        ll3.addView(Misc.makeTextView("Number", 2, this));
        ll3.addView(Misc.makeTextView(objCar.getNumber(), 1,this));

        LinearLayout ll4 = Misc.makeLinearLayout(1,this);
        ll4.setLayoutParams(p);

        ll4.addView(Misc.makeTextView("Cost per day", 2, this));
        ll4.addView(Misc.makeTextView(objCar.getCostPerDay() + "", 1, this));

        llModelAndCondition.addView(ll3);
        llModelAndCondition.addView(ll4);

        String imgUrl = null;

        HorizontalScrollView imagesScroll = new HorizontalScrollView(this);
        LinearLayout llImages = Misc.makeLinearLayout(2,this);
        ViewGroup.LayoutParams p1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        llImages.setLayoutParams(p1);
        imagesScroll.addView(llImages);

        int i = 0;
        for (DataSnapshot image: imageSnapShot.getChildren()) {
            i++;
            imgUrl = Objects.requireNonNull(image.getValue()).toString();
            ImageView firstImage = new ImageView(this);
            firstImage.setId((int) Math.random());
            p = new LinearLayout.LayoutParams(200, 200);
            firstImage.setLayoutParams(p);
            Picasso.with(this).load(imgUrl).into(firstImage);

            String finalImgUrl = imgUrl;
            firstImage.setOnClickListener(v->{
                Intent intent = new Intent(this, ZoomImage.class);
                intent.putExtra(Misc.inputExtra, finalImgUrl);
                startActivity(intent);
            });
            llImages.addView(firstImage);
        }

        if(i == 0){
            llMain.addView(Misc.makeTextView("No images", 1, this));
        }else{
            llMain.addView(imagesScroll);
        }


        if(ll == llNotifications){
            LinearLayout llBtns = Misc.makeLinearLayout(2, this);
            llMain.addView(llBtns);

            llBtns.addView(makeBtn("Accept", llId, ll, llMain));
            llBtns.addView(makeBtn("Decline", llId, ll, llMain));
        }else {
            LinearLayout llBtn = Misc.makeLinearLayout(2,this);

            llBtn.setWeightSum(3);
            Button btnEdit = Misc.makeBtn("Edit", this);
            Button btnRemove = Misc.makeBtn("Remove", this);
            Button btnTrack = Misc.makeBtn("Track", this);

            btnEdit.setBackgroundResource(getResources().getIdentifier(String.valueOf(R.drawable.bg_blue_shadow), null, null));
            btnRemove.setBackgroundResource(getResources().getIdentifier(String.valueOf(R.drawable.bg_blue_shadow), null, null));
            btnTrack.setBackgroundResource(getResources().getIdentifier(String.valueOf(R.drawable.bg_blue_shadow), null, null));

            p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
            p.setMargins(5,5,5,5);

            btnEdit.setLayoutParams(p);
            btnRemove.setLayoutParams(p);
            btnTrack.setLayoutParams(p);

            btnEdit.setTextColor(Color.WHITE);
            btnRemove.setTextColor(Color.WHITE);
            btnTrack.setTextColor(Color.WHITE);

            llBtn.addView(btnEdit);
            llBtn.addView(btnRemove);
            llBtn.addView(btnTrack);

            llMain.addView(llBtn);

            btnRemove.setOnClickListener(v->{
                Car selectedCar = listCars.get(llMain.getId());
                flag = true;
                Toast toast = Toast.makeText(this, "Car is booked by customer, You can not delete it", Toast.LENGTH_LONG);
                Intent intent = new Intent(this, RenterDashBoard.class);
                AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle("Delete");
                alertDialog.setMessage("Are you sure?");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                        (dialog, which) -> {
                            DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference(Misc.bookings);
                            mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                        Booking objBooking = postSnapshot.getValue(Booking.class);
                                        assert objBooking != null;
                                        if(objBooking.getVehicleId().equals(selectedCar.getVehicleId())){
                                            if(objBooking.getStatus().equals(Misc.statusPending) || objBooking.getStatus().equals(Misc.statusOnGoing)){
                                                flag = false;
                                            }
                                        }
                                    }
                                    if(flag){
                                        if(!selectedCar.getOwner().equals(Misc.selfOwner)){
                                            Notification objNotification = new Notification();
                                            objNotification.setType(Misc.notiTypeAddCar);
                                            objNotification.setVehicleId(selectedCar.getVehicleId());
                                            objNotification.setRenterId(objUser.getCnic());
                                            objNotification.setStatus("Car \"" + selectedCar.getName() + "\" deleted by Manager");
                                            FirebaseDatabase.getInstance().getReference().child(Misc.user).child(Misc.roleCustomer)
                                                    .child(selectedCar.getOwner()).child(Misc.notifications).child(System.currentTimeMillis() + "")
                                                    .setValue(objNotification);
                                        }
                                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                                        ref.child(Misc.user).child(Misc.roleRenter).child(objUser.getCnic()).child(Misc.folderVehicle)
                                                .child(listCars.get(llMain.getId()).getVehicleId()).child("status").setValue(Misc.deleted);
                                        startActivity(intent);

                                    }else {
                                        toast.show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Toast.makeText(RenterDashBoard.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                }

                            });


                        });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                        (dialog, which) -> {
                        });
                alertDialog.show();
            });

            btnEdit.setOnClickListener(v->{
                Car selectedCar = listCars.get(llMain.getId());
                Gson gson = new Gson();
                String str = gson.toJson(selectedCar);
                Intent intent = new Intent(this, AddVehicle.class);
                intent.putExtra(Misc.inputExtra, Misc.roleCustomer);
                intent.putExtra(Misc.inputExtraCar, str);
                startActivity(intent);

            });

            btnTrack.setOnClickListener(v->{

                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child(Misc.location)
                        .child(listCars.get(llMain.getId()).getTrackerId());
                Intent intent = new Intent(this, DisplayLocation.class);

                Toast toast = Toast.makeText(this, "GPS is not responding", Toast.LENGTH_LONG);

                dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for(DataSnapshot ds: dataSnapshot.getChildren()){
                            if(Objects.equals(ds.getKey(), Misc.lat)) {
                                Log.d(Misc.logKey, ds.getKey());
                                lat = ds.getValue(String.class);
                            }else{
                                lng = dataSnapshot.child(Misc.lng).getValue(String.class);
                            }

                        }

                        if(lat == null && lng == null){
                            toast.setText("GPS is not Available");
                            toast.show();
                            return;
                        }else if(Objects.equals(lat, "Invalid") || Objects.equals(lng, "Invalid")){
                            toast.show();
                            return;
                        }
                        Gson gson = new Gson();
                        objUser.setLat(Double.parseDouble(Objects.requireNonNull(lat)));
                        objUser.setLng(Double.parseDouble(Objects.requireNonNull(lng)));
                        String str = gson.toJson(objUser);
                        intent.putExtra(Misc.inputExtra, str);
                        startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(RenterDashBoard.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            });


        }

        ll.addView(llMain);
    }

    private Button makeBtn(String text, int id ,LinearLayout ll, LinearLayout del){
        boolean decision;
        Button btn = new Button(this);

        btn.setText(text);
        btn.setBackgroundResource(getResources().getIdentifier(String.valueOf(R.drawable.bg_blue_shadow), null, null));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        btn.setTextColor(Color.WHITE);
//        LinearLayout.LayoutParams p = (LinearLayout.LayoutParams) new ViewGroup.LayoutParams(100,50);
        btn.setLayoutParams(p);
        btn.setId(id);

        if(text.equals("Accept")){
            p.setMargins(0,5,0,0);
            decision = true;
        }else {
            p.setMargins(20,5,0,0);
            decision = false;
        }

        btn.setOnClickListener(v->{
            ll.removeView(del);
            acceptOrDecline(decision, btn.getId());
        });

        return btn;
    }

    private void loadNotifications(){
        llNotifications.removeAllViews();
        DatabaseReference DatabaseRef = FirebaseDatabase.getInstance().getReference().child(Misc.user).child(Misc.temp).child(objUser.getCnic());
        id = 0;
        DatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Car objCar = postSnapshot.getValue(Car.class);

                    assert objCar != null;
                    carsList.add(objCar);
//                    imageUrlList.add(postSnapshot.child(Misc.folderImages).getValue().toString());
                    addCarInfo(objCar, postSnapshot.child(Misc.folderImages), llNotifications, id);
                    id++;
                    btnNotification.setEnabled(true);
                    btnNotification.setImageResource(getResources().getIdentifier(String.valueOf(R.drawable.ic_notifications_blue_24dp), null, null));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(RenterDashBoard.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void acceptOrDecline(boolean decision, int id){
        Car selectedCar = carsList.get(id);

        Notification objNotification = new Notification();
        objNotification.setType(Misc.notiTypeAddCar);
        objNotification.setVehicleId(selectedCar.getVehicleId());
        objNotification.setRenterId(objUser.getCnic());

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child(Misc.user).child(Misc.roleCustomer).child(selectedCar.getOwner());

        DatabaseReference dbRefForBookings = FirebaseDatabase.getInstance().getReference()
                .child(Misc.user).child(Misc.roleRenter).child(objUser.getCnic())
                .child(Misc.folderVehicle).child(selectedCar.getVehicleId());

        if(decision){

            objNotification.setStatus(Misc.accepted);

            ref.child(Misc.notifications).child(selectedCar.getVehicleId())
                    .setValue(objNotification);
            ref.child(Misc.folderVehicle).child(selectedCar.getVehicleId()).setValue(objUser.getCnic());

            dbRefForBookings.setValue(carsList.get(id));
//            dbRefForBookings.child(Misc.folderImages).child("1").setValue(imageUrlList.get(id));
            DatabaseReference imagesRef = FirebaseDatabase.getInstance().getReference().child(Misc.user).child(Misc.temp).child(objUser.getCnic()).
                    child(selectedCar.getVehicleId()).child(Misc.folderImages);
            imagesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    int i = 0;
                    for (DataSnapshot image: dataSnapshot.getChildren()) {
                        String imgUrl = Objects.requireNonNull(image.getValue()).toString();
                        dbRefForBookings.child(Misc.folderImages).child(i + "").setValue(imgUrl);
                        i++;
                    }
                    removeNotification(id);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }else{
            objNotification.setStatus(Misc.rejected);
            ref.child(Misc.notifications).child(selectedCar.getVehicleId())
                    .setValue(objNotification);
            ref.child(Misc.folderVehicle).child(selectedCar.getVehicleId()).setValue(Misc.rejected);

            removeNotification(id);
        }
    }

    private void removeNotification(int id){
        Car selectedCar = carsList.get(id);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        Query applesQuery = ref.child(Misc.user).child(Misc.temp).child(objUser.getCnic()).
                orderByChild("vehicleId").equalTo(selectedCar.getVehicleId());

        applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                    appleSnapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(Misc.logKey, "onCancelled", databaseError.toException());
            }
        });
    }

    private void loadBookings(String text){
        bookingsKey.clear();
        DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference(Misc.user).child(Misc.roleRenter)
                .child(objUser.getCnic()).child(Misc.bookings);
        DatabaseReference dbRefForBookings = FirebaseDatabase.getInstance().getReference(Misc.bookings);

        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    if(text != null){
                        Log.d(Misc.logKey, postSnapshot.getKey());
                        if(postSnapshot.getKey().equals(text)){
                            bookingsKey.add(postSnapshot.getKey());
                            Log.d(Misc.logKey, "Equal");
                        }
                    }
                    else{
                        bookingsKey.add(postSnapshot.getKey());
                    }
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
                Toast.makeText(RenterDashBoard.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }

        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
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

        LinearLayout llNameAndId = Misc.makeLinearLayout(2, this);
        llMain.addView(llNameAndId);

        LinearLayout ll = Misc.makeLinearLayout(1, this);
        LinearLayout ll0 = Misc.makeLinearLayout(1, this);
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
        LinearLayout llBtn = Misc.makeLinearLayout(2, this);
        llMain.addView(llBtn);

        if(obj.getStatus().equals(Misc.statusOnGoing)){
            Button btnEnd = Misc.makeBtn("End", this);
            btnEnd.setBackgroundResource(getResources().getIdentifier(String.valueOf(R.drawable.bg_blue_shadow), null, null));
            btnEnd.setId(idForLayout);
            llMain.addView(btnEnd);
            llOnGoing.addView(llMain);

            btnEnd.setOnClickListener(v->{
                endBooking(listBookins.get(btnEnd.getId()));
            });
        }else if(obj.getStatus().equals(Misc.statusPending)){
            Button btnStart = Misc.makeBtn("Start", this);
            btnStart.setBackgroundResource(getResources().getIdentifier(String.valueOf(R.drawable.bg_blue_shadow), null, null));

            btnStart.setId(idForLayout);
            btnStart.setOnClickListener(v-> {
                startBooking(listBookins.get(btnStart.getId()));
            });

            Button btnCancel = Misc.makeBtn("Cancel", this);
            btnCancel.setBackgroundResource(getResources().getIdentifier(String.valueOf(R.drawable.bg_blue_shadow), null, null));

            btnCancel.setId(idForLayout);

            btnCancel.setOnClickListener(v-> {
                cancelBooking(listBookins.get(btnCancel.getId()));
            });

            LinearLayout llBtn1 = Misc.makeLinearLayout(2, this);
            llBtn1.setGravity(Gravity.CENTER_HORIZONTAL);
            LinearLayout.LayoutParams p1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            p1.setMargins(5,5,5,5);
            btnCancel.setLayoutParams(p1);
            btnStart.setLayoutParams(p1);
            llBtn1.addView(btnCancel);
            llBtn1.addView(btnStart);

            llMain.addView(llBtn1);
            llNotStarted.addView(llMain);
        }else{
            llCompleted.addView(llMain);
        }

        llMain.setGravity(Gravity.CENTER_HORIZONTAL);
        idForLayout++;
    }

    private void startBooking(Booking obj) {

        Intent intent = getIntent();

        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Are you sure?");
        alertDialog.setMessage("Are you sure you want to start this ride?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                (dialog, which) -> {
                    FirebaseDatabase.getInstance().getReference().child(Misc.bookings).child(obj.getId()).child("status").setValue(Misc.statusOnGoing);
                    finish();
                    startActivity(intent);
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                (dialog, which) -> {
                });
        alertDialog.show();

    }

    private void endBooking(Booking obj) {
        Intent intent = new Intent(this,CollectCash.class);
        Gson objGson = new Gson();
        String str = objGson.toJson(obj);
        intent.putExtra(Misc.inputExtra, str);


        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Are you Sure?");
        alertDialog.setMessage("Are you sure you want to end this ride");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                (dialog, which) -> {
                    startActivity(intent);
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                (dialog, which) -> {
                });
        alertDialog.show();
    }

    private void cancelBooking(Booking obj){

        Notification objNotification = new Notification();
        String id = System.currentTimeMillis() + "";
        objNotification.setType(Misc.bookings);
        objNotification.setMessage("You ride is got canceled by " + objUser.getOfficeTitle() + " You can get 2000 Rs by showing this message to renter.");
        objNotification.setId(id);
        objNotification.setBookingId(obj.getId());
        objNotification.setStatus(Misc.statusPending);

        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Cancel?");
        alertDialog.setMessage("If you cancel this ride you have to pay 2000 to your customer. Are you sure?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                (dialog, which) -> {
                    FirebaseDatabase.getInstance().getReference().child(Misc.bookings).child(obj.getId()).child("status").setValue(Misc.statusCanceled);
                    FirebaseDatabase.getInstance().getReference().child(Misc.user)
                            .child(Misc.roleCustomer).child(obj.getCustomerId()).child(Misc.notifications).child(id).setValue(objNotification);
                    finish();
                    startActivity(new Intent(this, RenterDashBoard.class));
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                (dialog, which) -> {
                });
        alertDialog.show();

    }

    private void getBookedCar(Booking obj){
        FirebaseDatabase.getInstance().getReference().child(Misc.user)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        customer = dataSnapshot.child(Misc.roleCustomer).child(obj.getCustomerId()).getValue(User.class);
                        car = dataSnapshot.child(Misc.roleRenter).child(obj.getRenterId()).child(Misc.folderVehicle).child(obj.getVehicleId()).getValue(Car.class);
                        addBookings(obj);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @SuppressLint("ResourceAsColor")
    private void makeTempLL(){
        llOnGoing = Misc.makeLinearLayout(1, this);
        llNotStarted = Misc.makeLinearLayout(1, this);
        llCompleted = Misc.makeLinearLayout(1, this);

        TextView temp = Misc.makeTextView("On Going", 1, this);
        TextView temp1 = Misc.makeTextView("Up Coming", 1, this);
        TextView temp2 = Misc.makeTextView("Completed", 1, this);

        temp.setTextSize(15f);
        temp.setTextColor(R.color.light_blue);

        temp1.setTextSize(15f);
        temp1.setTextColor(R.color.light_blue);

        temp2.setTextSize(15f);
        temp2.setTextColor(R.color.light_blue);

        temp.setPadding(20,0,0,20);
        temp1.setPadding(20,0,0,20);
        temp2.setPadding(20,0,0,20);

        llOnGoing.addView(temp);
        linearLayoutBooking.addView(llOnGoing);

        llNotStarted.addView(temp1);
        linearLayoutBooking.addView(llNotStarted);

        llCompleted.addView(temp2);
        linearLayoutBooking.addView(llCompleted);
        
    }

    @Override
    public void onBackPressed() {
        if(!svFlag){
            startActivity(new Intent(this, RenterDashBoard.class));
            return;
        }
        Toast backToast;
        backToast = Toast.makeText(this, "Press back again to exit", Toast.LENGTH_LONG);

        if(backPressedTime + 2000 > System.currentTimeMillis()){
            backToast.cancel();
            super.onBackPressed();
            return;
        }else{
            backToast.show();
        }

        backPressedTime = System.currentTimeMillis();
    }

    private void refreshNotifications() {
        DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference().child(Misc.user).child(Misc.temp).child(objUser.getCnic());

        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dsFlagNotification){
                    dsNotification = dataSnapshot;
                    dsFlagNotification = false;
                }
                else if(dsNotification != dataSnapshot){
                    loadNotifications();
                }
                dsNotification = dataSnapshot;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

}
