package com.example.jayde.a4ease;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class DisplayCars extends AppCompatActivity {

    public Car clickedCar;
    private String renterCnic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_ren_house);

        Misc.isNetworkConnected(this);
        findViewById(R.id.cLDisplayCars).getBackground().setAlpha(Misc.alphaMain);

        manageScreen();

        Bundle intentExtras = getIntent().getExtras();
        assert intentExtras != null;

        renterCnic = intentExtras.getString(Misc.inputExtraCnic);
        Gson gson = new Gson();
        String str =Objects.requireNonNull(intentExtras.get(Misc.inputExtraCarsHashMap)).toString();

        User objRenter = gson.fromJson(str, User.class);
        loadUser();

        if(objRenter != null && objRenter.getCarsArrayList() != null){
            for (int i = 0; i < objRenter.getCarsArrayList().size(); i++) {
                Car car = objRenter.getCarsArrayList().get(i);
                assert renterCnic != null;
                DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference(Misc.user).child(Misc.roleRenter)
                        .child(renterCnic).child(Misc.folderVehicle).child(car.getVehicleId());

                mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        addCarInfo(car, dataSnapshot.child(Misc.folderImages));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }
        }


    }

    public void addCarInfo(Car objCar, DataSnapshot imageSnapShot)  {
        LinearLayout llMain = makeLinearLayout(1);
        llMain.setBackgroundResource(getResources().getIdentifier(String.valueOf(R.drawable.bg_white), null, null));
        LinearLayout.LayoutParams p = new  LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.setMargins(40,10,40,0);
        llMain.setLayoutParams(p);

        llMain.setGravity(Gravity.CENTER_HORIZONTAL);
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
        LinearLayout llImages = makeLinearLayout(2);
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
            llMain.addView(makTextView("No images", 1));
        }else{
            llMain.addView(imagesScroll);
        }

        Button btnBook = Misc.makeBtn("Book", this);
        btnBook.setBackgroundResource(getResources().getIdentifier(String.valueOf(R.drawable.bg_blue_shadow), null, null));
        btnBook.setTextColor(Color.WHITE);

        llMain.addView(btnBook);

        btnBook.setOnClickListener(v->{
            clickedCar = objCar;
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Book Car");
            alertDialog.setMessage("Do you want to book this car?");
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                (dialog, which) -> {
                    Intent intent = new Intent(this, CarBooking.class);
                    Gson gson = new Gson();
                    String json = gson.toJson(objCar);
                    intent.putExtra(Misc.inputExtra,json);
                    intent.putExtra(Misc.inputExtraCnic,renterCnic);
                    startActivity(intent);
                });
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                (dialog, which) -> {
                });
            alertDialog.show();
        });

        LinearLayout linearLayoutVehicles = findViewById(R.id.mainLayoutDisplayRentHouse);
        linearLayoutVehicles.addView(llMain);
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

    private void loadUser() {
        SharedPreferences sharedPreferences = getSharedPreferences(Misc.user, MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(Misc.user, null);

        User objUser = gson.fromJson(json, User.class);

        TextView temp = findViewById(R.id.txt_UserName);
        temp.setText(objUser.getName());
    }

    private void manageScreen(){
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int incHeight = (int) (dm.heightPixels*0.25);
        int svHeight =(int) (dm.heightPixels * 0.7);

        ScrollView sv = findViewById(R.id.scrollViewDRH);
        View inc = findViewById(R.id.includeLogo);

        ViewGroup.LayoutParams p = inc.getLayoutParams();
        p.height = incHeight;
        inc.setLayoutParams(p);

        p = sv.getLayoutParams();
        p.height = svHeight;
        sv.setLayoutParams(p);
    }

}