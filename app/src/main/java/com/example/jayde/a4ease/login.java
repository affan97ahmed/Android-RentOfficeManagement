package com.example.jayde.a4ease;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class login extends AppCompatActivity{
    private Button btnLogIn;
    private EditText pass;
    boolean customer = false;
    boolean renter = false;
    private User userRenter, userCustomer;
    private DatabaseReference mRef=FirebaseDatabase.getInstance().getReference(Misc.user);
    private Button btnQrCnic;
    public String url;
    public String cnic;
    private User objUser;
    private LinearLayout llPb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        findViewById(R.id.cLLogin).getBackground().setAlpha(Misc.alphaMain);

        canToggleGPS();
        Misc.turnGPSOn( this);

        url = "";

        llPb = findViewById(R.id.myProgressBar);
        Button btnAdmin = findViewById(R.id.btn_admin);
        btnQrCnic = findViewById(R.id.btnQR);
        btnLogIn = findViewById(R.id.link_login);
        Button signup = findViewById(R.id.link_signup);
        pass = findViewById(R.id.txtPassword);
        btnLogIn.setEnabled(true);

        btnAdmin.setOnClickListener(v->{
            finish();
            startActivity(new Intent(this, AdminLogin.class));
        });

        btnQrCnic.setOnClickListener(view -> {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
            integrator.setCaptureActivity(AnyOrientationCaptureActivity.class);
            integrator.setOrientationLocked(false);
            integrator.setPrompt("Scan CNIC QR");
            integrator.setCameraId(0);
            integrator.setBeepEnabled(false);
            integrator.setBarcodeImageEnabled(false);
            integrator.initiateScan();
            btnLogIn.setEnabled(true);
        });

        signup.setOnClickListener(view -> {
            Intent intent=new Intent(login.this,StartUp.class);
            startActivity(intent);
        });

        btnLogIn.setOnClickListener(view -> {
            Misc.isNetworkConnected(this);
            login();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null){
            if(result.getContents()==null){
                Toast.makeText(this, "You cancelled the scanning", Toast.LENGTH_LONG).show();
            }else if(result.getContents().length() != 26){
                Toast.makeText(this, "Invalid QR Code", Toast.LENGTH_LONG).show();
            }
            else {

                url = result.getContents();
                Log.d(Misc.logKey,url.length() + "");
                Toast.makeText(this, url,Toast.LENGTH_LONG).show();
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void login() {
        String txtPass=pass.getText().toString();

        if(txtPass.isEmpty())
        {
            pass.setError("Please Enter Password");
        } else if(url.equals(""))
        {
            btnQrCnic.setError("Please Scan QR Code for CNIC");
        } else {
            cnic = url.substring(12, 25);
            checkUser("Customers", txtPass, cnic);

        }
    }

    public void checkUser(String userRole, String txtPass, String CNIC){
        llPb.setVisibility(View.VISIBLE);
        mRef.child(userRole).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    objUser = snapshot.getValue(User.class);
                    assert objUser != null;

                    if (objUser.getCnic().equals(CNIC) && objUser.getPassword().equals(txtPass)) {

                        if(userRole.equals(Misc.roleCustomer)){
                            customer = true;
                            userCustomer = snapshot.getValue(User.class);
                            checkUser(Misc.roleRenter, txtPass, CNIC);
                            return;
                        }

                        userRenter = snapshot.getValue(User.class);
                        assert userRenter != null;
                        renter = true;
                        break;
                    }
                }
                if(userRole.equals(Misc.roleCustomer)){
                    checkUser(Misc.roleRenter, txtPass, CNIC);
                    return;
                }
                llPb.setVisibility(View.GONE);
                decision(customer,renter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void decision(boolean customer, boolean renter){
        if(renter && customer){
            AlertDialog alertDialog = new AlertDialog.Builder(login.this).create();
            alertDialog.setTitle("Two Accounts");
            alertDialog.setMessage("You Have Renter and Customer account on same id select one to login.");
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Customer",
                    (dialog, which) -> {
                        Toast.makeText(getApplicationContext(), "Welcome Dear Customer", Toast.LENGTH_SHORT).show();
                        saveData(userCustomer);
                        finish();
                        startActivity(new Intent(login.this, CustomerDashBoard.class));
                    });
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Manager",
                    (dialog, which) -> {
                        saveData(userRenter);
                        finish();
                        Toast.makeText(getApplicationContext(), "Welcome Dear Manager", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(login.this, RenterDashBoard.class));
                    });
            alertDialog.show();
        }
        else if(renter){
            saveData(userRenter);
            finish();
            Toast.makeText(getApplicationContext(), "Welcome Dear Manager", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(login.this, RenterDashBoard.class));
        }
        else if(customer){
            saveData(userCustomer);
            finish();
            Toast.makeText(getApplicationContext(), "Welcome Dear Customer", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(login.this, CustomerDashBoard.class));
        }
        else{
            Toast.makeText(getApplicationContext(), "User Does Not Exist", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveData(User objUser) {
        SharedPreferences sharedPreferences = getSharedPreferences(Misc.user, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(objUser);
        editor.putString(Misc.user, json);
        editor.apply();
    }

    public void hideKeyBoard(View view){
        InputMethodManager imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }

    private void canToggleGPS() {
        PackageManager pacman = getPackageManager();
        PackageInfo pacInfo = null;

        try {
            pacInfo = pacman.getPackageInfo("com.android.settings", PackageManager.GET_RECEIVERS);
        } catch (PackageManager.NameNotFoundException e) {
            return; //package not found
        }

        if(pacInfo != null){
            for(ActivityInfo actInfo : pacInfo.receivers){
                //test if recevier is exported. if so, we can toggle GPS.
                if(actInfo.name.equals("com.android.settings.widget.SettingsAppWidgetProvider") && actInfo.exported){
                    return;
                }
            }
        }

    }

}