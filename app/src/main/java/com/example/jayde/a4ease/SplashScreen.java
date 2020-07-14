package com.example.jayde.a4ease;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

public class SplashScreen extends AppCompatActivity {

    private User objUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        loadUser();

        int secondsDelayed = 2;
        new Handler().postDelayed(() -> checkUser(), secondsDelayed * 1000);

    }

    private void loadUser() {
        SharedPreferences sharedPreferences = getSharedPreferences(Misc.user, MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(Misc.user, null);

        objUser = gson.fromJson(json, User.class);
    }

    public void checkUser(){
        if(objUser != null) {
            if (objUser.getUserRole().equals(Misc.roleRenter)) {
                finish();
                startActivity(new Intent(this, RenterDashBoard.class));
            } else if (objUser.getUserRole().equals(Misc.roleCustomer)) {
                finish();
                startActivity(new Intent(this, CustomerDashBoard.class));
            }else if(objUser.getUserRole().equals(Misc.admin)){
                finish();
                startActivity(new Intent(this, AdminDashBoard.class));
            }
        }else{
            finish();
            startActivity(new Intent(this, login.class));
        }
    }

}
