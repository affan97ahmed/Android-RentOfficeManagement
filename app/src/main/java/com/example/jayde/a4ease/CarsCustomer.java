package com.example.jayde.a4ease;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class CarsCustomer extends AppCompatActivity {

    private LinearLayout llRentHouses;
    private LinearLayout llViewCars;
    private ArrayList<User> rentHousesArray;
    public List<List<Car>> indexForRentHouse;
    public List<Car> carsOfRentHouse;
    public int idForLayout = 0;
    private User objUser;
    private HashMap<String ,String> carIds;

    public CarsCustomer() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cars_customer);

        carsOfRentHouse = new ArrayList<>();
        indexForRentHouse = new ArrayList<>();
        rentHousesArray = new ArrayList<>();
        carIds = new HashMap<>();

        Misc.isNetworkConnected(this);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int incHeight = (int) (dm.heightPixels * 0.25);
        int svHeight =(int) (dm.heightPixels * 0.7);

        ScrollView scrollView = findViewById(R.id.svCC);
        View inc = findViewById(R.id.includeCC);

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,incHeight);
        inc.setLayoutParams(p);

        p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, svHeight);
        scrollView.setLayoutParams(p);

        loadUser();
        loadCars();

        ConstraintLayout cLCarsCustomer =findViewById(R.id.cLCarsCustomer);
        ImageButton btnAdd = findViewById(R.id.btn_add_Vehicle_customer);
        llRentHouses= findViewById(R.id.ll_rent_houses);
        llViewCars = findViewById(R.id.llVehiclesCustomer);

        cLCarsCustomer.getBackground().setAlpha(Misc.alphaMain);

        loadRentHouses();

        btnAdd.setOnClickListener(v->{
            llRentHouses.setVisibility(View.VISIBLE);
            llViewCars.setVisibility(View.GONE);
        });
    }

    private  void loadRentHouses(){

        DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference(Misc.user).child(Misc.roleRenter);

        carsOfRentHouse.clear();
        indexForRentHouse.clear();
        rentHousesArray.clear();

        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    User objUser = postSnapshot.getValue(User.class);
                    assert objUser != null;

                    if(objUser.getStatus() == null){

                        addRentHouses(objUser);
                        rentHousesArray.add(objUser);
                        idForLayout = rentHousesArray.size() - 1;
                        for (DataSnapshot vehi : postSnapshot.child(Misc.folderVehicle).getChildren()) {
                            Car objCar = vehi.getValue(Car.class);
                            carsOfRentHouse.add(objCar);
                        }
                        indexForRentHouse.add(carsOfRentHouse);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(CarsCustomer.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void addRentHouses(User renter){
        LinearLayout llMain = makeLinearLayout(1);
        llMain.setId(idForLayout);
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

//        llMain.addView(makTextView("Distance", 2));
//
//        float[] result = new float[10];
//        Location.distanceBetween(renter.getLat(),renter.getLng(),lat, lng, result);
//
//        llMain.addView(makTextView("" + result[0], 1));

        llMain.setOnClickListener(v ->{
            Intent intent = new Intent(this, AddVehicle.class);
            Gson gson = new Gson();
            String json = gson.toJson(renter);

            intent.putExtra(Misc.inputExtraCar,"null");
            intent.putExtra(Misc.roleRenter,json);
            intent.putExtra(Misc.inputExtra, Misc.roleRenter);

            startActivity(intent);

        });


        llRentHouses.addView(llMain);
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

    private void loadCars(){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child(Misc.user).child(Misc.roleCustomer)
                .child(objUser.getCnic()).child(Misc.folderVehicle);
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot keys : dataSnapshot.getChildren()) {
                    carIds.put(keys.getKey(), Objects.requireNonNull(keys.getValue()).toString());

                    if(!"pending".equals(keys.getValue().toString()) && !keys.getValue().toString().equals(Misc.rejected)){

                        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child(Misc.user).child(Misc.roleRenter)
                                .child(keys.getValue().toString()).child(Misc.folderVehicle);
                        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Car objCar = Objects.requireNonNull(dataSnapshot.child(Objects.requireNonNull(keys.getKey())).getValue(Car.class));
                                if(objCar.getStatus() == null){
                                    addCarInfo(objCar, dataSnapshot.child(keys.getKey()).child(Misc.folderImages));
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(CarsCustomer.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(CarsCustomer.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void addCarInfo(Car objCar, DataSnapshot imageSnapShot)  {
        LinearLayout llMain = makeLinearLayout(1);
        llMain.setBackgroundResource(getResources().getIdentifier(String.valueOf(R.drawable.bg_white), null, null));
        LinearLayout.LayoutParams p = new  LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.setMargins(40,10,40,0);
        llMain.setLayoutParams(p);
        llMain.setPadding(20,20,20,20);

        p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.weight = 1;

        llMain.addView(makTextView("Name", 2));
        llMain.addView(makTextView(objCar.getName(), 1));

        LinearLayout llCompanyAndNumber = makeLinearLayout(2);
        llMain.addView(llCompanyAndNumber);

        LinearLayout ll1 = makeLinearLayout(1);
        ll1.setLayoutParams(p);
        ll1.addView(makTextView("Company", 2));
        ll1.addView(makTextView(objCar.getCompany(), 1));

        LinearLayout ll2 = makeLinearLayout(1);
        ll2.setLayoutParams(p);
        ll2.addView(makTextView("Model",2));
        ll2.addView(makTextView(objCar.getModel(), 1));

        llCompanyAndNumber.addView(ll1);
        llCompanyAndNumber.addView(ll2);

        LinearLayout llModelAndCondition = makeLinearLayout(2);
        llMain.addView(llModelAndCondition);

        LinearLayout ll3 = makeLinearLayout(1);
        ll3.setLayoutParams(p);

        ll3.addView(makTextView("Number", 2));
        ll3.addView(makTextView(objCar.getNumber(), 1));

        LinearLayout ll4 = makeLinearLayout(1);
        ll4.setLayoutParams(p);

        ll4.addView(makTextView("Cost per day", 2));
        ll4.addView(makTextView(objCar.getCostPerDay() + "", 1));

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

        llViewCars.addView(llMain);
    }

    private void loadUser() {
        SharedPreferences sharedPreferences = getSharedPreferences(Misc.user, MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(Misc.user, null);

        objUser = gson.fromJson(json, User.class);
        TextView temp = findViewById(R.id.txt_UserName);
        temp.setText(objUser.getName());
    }

}
