package com.example.jayde.a4ease;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

public class StartUp extends Activity {
    private Button bcus,brent;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.startup);

        LinearLayout ll = findViewById(R.id.llPopUp);
        brent= findViewById(R.id.renter);
        bcus = findViewById(R.id.customer);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int w = dm.widthPixels;
        int h = dm.heightPixels;
        getWindow().setLayout((int)(w*.8),(int)(h*.5));

        ll.getBackground().setAlpha(50);

        bcus.setOnClickListener(v -> {
            Intent in1=new Intent(StartUp.this, CustomerSignUp.class);
            in1.putExtra(Misc.inputExtra, Misc.roleCustomer);
            finish();
            startActivity(in1);
        });

        brent.setOnClickListener(v -> {
            Intent in2=new Intent(StartUp.this, SignUpRenter.class);
            finish();
            startActivity(in2);
        });


    }
}
