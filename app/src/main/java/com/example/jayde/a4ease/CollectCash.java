package com.example.jayde.a4ease;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

public class CollectCash extends AppCompatActivity {

    private int amount;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_colect_cash);

        Misc.isNetworkConnected(this);

        String str = getIntent().getStringExtra(Misc.inputExtra);
        Booking objBooking = new Gson().fromJson(str, Booking.class);

        TextView title = findViewById(R.id.txt_collectCash);

        title.setText(title.getText().toString() + objBooking.getPendingCost());

        Button btnOk, btnCancel;
        EditText enteredCost;
        LinearLayout ll = findViewById(R.id.ll_collectCash);
        btnCancel = findViewById(R.id.btn_cancel);
        btnOk = findViewById(R.id.btn_ok);
        enteredCost = findViewById(R.id.et_cost);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int w = dm.widthPixels;
        int h = dm.heightPixels;

        getWindow().setLayout((int)(w*.8),(int)(h*.5));


        ll.getBackground().setAlpha(50);

        btnOk.setOnClickListener(v->{
            amount = Integer.parseInt(enteredCost.getText().toString());

            if(enteredCost.getText().toString().equals("")){
                enteredCost.setError("Please enter payment");
                return;
            }

            if(amount < objBooking.getPendingCost()){

                AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle("Insufficient Amount");
                alertDialog.setMessage("Amount you entered is not equal to pending. If you Submit once you can not able to enter amount again.");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                        (dialog, which) -> {
                            setValue(objBooking);
                        });

                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                        (dialog, which) -> {
                        });
                alertDialog.show();

            }else if(amount > objBooking.getPendingCost()){

                AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle("Extra Amount");
                alertDialog.setMessage("Amount you entered is not equal to pending. If you Submit once you can not able to enter amount again.");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                        (dialog, which) -> {
                            setValue(objBooking);
                        });

                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                        (dialog, which) -> {
                        });
                alertDialog.show();
            }else if(amount == objBooking.getPendingCost()){
                setValue(objBooking);
            }else{
                Toast.makeText(this, "You Canceled", Toast.LENGTH_LONG).show();
                finish();
            }
        });

        btnCancel.setOnClickListener(v->{
            finish();
        });
    }

    private  void setValue(Booking objBooking){
        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child(Misc.bookings).child(objBooking.getId());
        db.child("paidCost").setValue(objBooking.getPaidCost() + amount);
        db.child("pendingCost").setValue(objBooking.getPendingCost() - amount);
        db.child("status").setValue(Misc.statusCompleted);
        startActivity(new Intent(this, RenterDashBoard.class));

    }
}
