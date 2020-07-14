package com.example.jayde.a4ease;

import android.annotation.SuppressLint;
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
import android.widget.ImageButton;
import android.widget.LinearLayout;
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

import java.util.ArrayList;

public class AdminDashBoard extends AppCompatActivity {

    private long backPressedTime = 0;
    private LinearLayout llRenters, llCustomers;
    private Button btnCustomers;
    private Button btnRenters;
    private ArrayList<User> rentersList, customersList;
    private int idRenter;
    private int idCustomer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        Misc.isNetworkConnected(this);

        findViewById(R.id.cLAdmin).getBackground().setAlpha(Misc.alphaMain);

        manageScreen();

        idRenter = 0;
        idCustomer = 0;
        llRenters = findViewById(R.id.llRenters);
        llCustomers = findViewById(R.id.llCustomers);
        btnCustomers = findViewById(R.id.btnCustomers);
        btnRenters = findViewById(R.id.btnRenters);
        ImageButton btnAddAdmin = findViewById(R.id.btn_add_admin);
        ImageButton btnLogout = findViewById(R.id.btn_logout_admin);

        rentersList = new ArrayList<>();
        customersList = new ArrayList<>();

        loadUser();
        loadRentHouses();
        loadCustomers();

        btnCustomers.getBackground().setAlpha(Misc.alphaBtnLow);
        btnRenters.getBackground().setAlpha(Misc.alphaBtnHigh);

        btnLogout.setOnClickListener(v->{
            android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(this).create();
            alertDialog.setTitle("Logout");
            alertDialog.setMessage("Are you sure?");
            alertDialog.setButton(android.app.AlertDialog.BUTTON_POSITIVE, "Yes",
                    (dialog, which) -> {
                        deleteUser();
                        finish();
                        startActivity(new Intent(this, login.class));
                    });
            alertDialog.setButton(android.app.AlertDialog.BUTTON_NEGATIVE, "No",
                    (dialog, which) -> {
                    });
            alertDialog.show();

        });

        btnAddAdmin.setOnClickListener(v->{
            Intent intent = new Intent(this, CustomerSignUp.class);
            intent.putExtra(Misc.inputExtra, Misc.admin);
            startActivity(intent);
        });

        btnRenters.setOnClickListener(v -> {
            btnCustomers.getBackground().setAlpha(Misc.alphaBtnLow);
            btnRenters.getBackground().setAlpha(Misc.alphaBtnHigh);
            llRenters.setVisibility(View.VISIBLE);
            llCustomers.setVisibility(View.GONE);
        });

        btnCustomers.setOnClickListener(v -> {
            btnCustomers.getBackground().setAlpha(Misc.alphaBtnHigh);
            btnRenters.getBackground().setAlpha(Misc.alphaBtnLow);
            llRenters.setVisibility(View.GONE);
            llCustomers.setVisibility(View.VISIBLE);
        });

    }

    private  void loadCustomers(){
        DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference(Misc.user).child(Misc.roleCustomer);

        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    User objUser = postSnapshot.getValue(User.class);

                    assert objUser != null;
                    addUser(objUser, Misc.roleCustomer, llCustomers);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(AdminDashBoard.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private  void loadRentHouses(){

        DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference(Misc.user).child(Misc.roleRenter);

        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    User objUser = postSnapshot.getValue(User.class);

                    assert objUser != null;
                        addUser(objUser, Misc.roleRenter, llRenters);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(AdminDashBoard.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("ResourceType")
    public void addUser(User user, String userRole, LinearLayout mainLL){
        LinearLayout llMain = Misc.makeLinearLayout(1, this);
        llMain.setBackgroundResource(getResources().getIdentifier(String.valueOf(R.drawable.bg_white), null, null));
        LinearLayout.LayoutParams p = new  LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.setMargins(40,10,40,0);

        llMain.setLayoutParams(p);
        llMain.setPadding(20,20,20,20);
        LinearLayout llNameAnd = Misc.makeLinearLayout(2,this);
        llMain.addView(llNameAnd);


        LinearLayout ll = Misc.makeLinearLayout(1 ,this);
        p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.weight = 1;

        ll.setLayoutParams(p);
        llNameAnd.addView(ll);

        if (userRole.equals(Misc.roleCustomer)) {
            customersList.add(user);
            llMain.setId(idCustomer);
            idCustomer++;
            ll.addView(Misc.makeTextView("Customer Name", 2, this));

        }else{
            rentersList.add(user);
            llMain.setId(idRenter);
            idRenter++;
            ll.addView(Misc.makeTextView("Owner Name", 2, this));
        }
        ll.addView(Misc.makeTextView(user.getName(), 1, this));

        LinearLayout llCompanyAndNumber = Misc.makeLinearLayout(2,this);
        llMain.addView(llCompanyAndNumber);

        LinearLayout ll1 = Misc.makeLinearLayout(1 ,this);

        if(userRole.equals(Misc.roleCustomer)){
            ll1.addView(Misc.makeTextView("CNIC", 2, this));
            ll1.addView(Misc.makeTextView(user.getCnic(), 1, this));
        }else{
            ll1.addView(Misc.makeTextView("Office", 2, this));
            ll1.addView(Misc.makeTextView(user.getOfficeTitle(), 1, this));
        }
        p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.weight = 1;

        ll1.setLayoutParams(p);

        LinearLayout ll2 = Misc.makeLinearLayout(1, this);

        ll2.addView(Misc.makeTextView("Contact No", 2, this));
        ll2.addView(Misc.makeTextView(user.getPhNo(), 1, this));
        ll2.setLayoutParams(p);

        llCompanyAndNumber.addView(ll1);
        if(userRole.equals(Misc.roleCustomer)){
            llNameAnd.addView(ll2);
        }else{
            llCompanyAndNumber.addView(ll2);
        }

        LinearLayout llBtn = Misc.makeLinearLayout(2, this);
        llMain.addView(llBtn);

        if(!userRole.equals(Misc.roleCustomer)){
            Button btnLocation = Misc.makeBtn("Location", this);
            llBtn.addView(btnLocation);
            btnLocation.setBackgroundResource(getResources().getIdentifier(String.valueOf(R.drawable.bg_blue_shadow), null, null));
            btnLocation.setTextColor(Color.WHITE);

            btnLocation.setOnClickListener(v->{
                Intent intent = new Intent(this, DisplayLocation.class);
                Gson gson = new Gson();
                String str = gson.toJson(user);
                intent.putExtra(Misc.inputExtra, str);
                startActivity(intent);
            });
        }

        if(user.getStatus() == null){
            Button btnDelete = Misc.makeBtn("Restrict", this);
            btnDelete.setBackgroundResource(getResources().getIdentifier(String.valueOf(R.drawable.bg_blue_shadow), null, null));
            btnDelete.setTextColor(Color.WHITE);

            btnDelete.setOnClickListener(v->{
                Notification objNotification = new Notification();

                objNotification.setStatus("Your account got banned by admin");
                objNotification.setType(Misc.notiTypeAddCar);

                AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle("Are you sure?");
                alertDialog.setMessage("Do you want to remove this account?");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                        (dialog, which) -> {
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                            if (userRole.equals(Misc.roleCustomer)) {
                                objNotification.setVehicleId(System.currentTimeMillis() + "");
                                ref.child(Misc.user).child(userRole).child(customersList.get(llMain.getId()).getCnic())
                                        .child("status").setValue(Misc.deleted);
                                ref.child(Misc.user).child(userRole).child(customersList.get(llMain.getId()).getCnic())
                                        .child(Misc.notifications).child(objNotification.getVehicleId()).setValue(objNotification);
                                mainLL.removeAllViews();
                                loadCustomers();
                                loadRentHouses();
                            }else{
                                ref.child(Misc.user).child(userRole).child(rentersList.get(llMain.getId()).getCnic())
                                        .child("status").setValue(Misc.deleted);
                                ref.child(Misc.user).child(userRole).child(rentersList.get(llMain.getId()).getCnic())
                                        .child(Misc.notifications).child(System.currentTimeMillis() + "").setValue(objNotification);
                                mainLL.removeAllViews();
                                loadCustomers();
                                loadRentHouses();
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                        (dialog, which) -> {
                        });
                alertDialog.show();
            });

            llBtn.addView(btnDelete);
            llBtn.setGravity(Gravity.CENTER);

        }else{
            Button btnDelete = Misc.makeBtn(" Un restrict ", this);
            btnDelete.setTextColor(Color.WHITE);
            btnDelete.setBackgroundResource(getResources().getIdentifier(String.valueOf(R.drawable.bg_blue_shadow), null, null));

            btnDelete.setOnClickListener(v->{
                Notification objNotification = new Notification();

                objNotification.setStatus("Your account got un restricted by admin");
                objNotification.setType(Misc.notiTypeAddCar);

                AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle("Are you sure?");
                alertDialog.setMessage("Do you want to Un restrict this account?");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                        (dialog, which) -> {
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                            if (userRole.equals(Misc.roleCustomer)) {
                                objNotification.setVehicleId(System.currentTimeMillis() + "");
                                ref.child(Misc.user).child(userRole).child(customersList.get(llMain.getId()).getCnic())
                                        .child("status").setValue(null);
                                ref.child(Misc.user).child(userRole).child(customersList.get(llMain.getId()).getCnic())
                                        .child(Misc.notifications).child(objNotification.getVehicleId()).setValue(objNotification);
                                mainLL.removeAllViews();
                                loadCustomers();
                                loadRentHouses();
                            }else{
                                ref.child(Misc.user).child(userRole).child(rentersList.get(llMain.getId()).getCnic())
                                        .child("status").setValue(null);
                                ref.child(Misc.user).child(userRole).child(rentersList.get(llMain.getId()).getCnic())
                                        .child(Misc.notifications).child(System.currentTimeMillis() + "").setValue(objNotification);
                                mainLL.removeAllViews();
                                loadCustomers();
                                loadRentHouses();
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                        (dialog, which) -> {
                        });
                alertDialog.show();
            });
            llBtn.addView(btnDelete);
        }

        llMain.setOnClickListener(v ->{

        });

        mainLL.addView(llMain);
    }

    private void deleteUser(){
        SharedPreferences.Editor editor = getSharedPreferences(Misc.user, MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();
    }

    @Override
    public void onBackPressed() {
        Toast backToast;
        backToast = Toast.makeText(this, "Press back again to exit", Toast.LENGTH_LONG);

        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            backToast.cancel();
            super.onBackPressed();
            finish();
            return;
        } else {
            backToast.show();
        }
        backPressedTime = System.currentTimeMillis();
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
        int incHeight = (int) (dm.heightPixels * 0.25);
        int svHeight =(int) (dm.heightPixels * 0.6);

        LinearLayout ll = findViewById(R.id.llAdmin);
        View inc = findViewById(R.id.includeAdmin);

        ConstraintLayout.LayoutParams p = (ConstraintLayout.LayoutParams) ll.getLayoutParams();
        p.height = svHeight;
        ll.setLayoutParams(p);

        p = (ConstraintLayout.LayoutParams) inc.getLayoutParams();
        p.height = incHeight;
        inc.setLayoutParams(p);
    }

}
