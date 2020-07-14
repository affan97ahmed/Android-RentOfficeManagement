package com.example.jayde.a4ease;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

public class PaymentDetails extends AppCompatActivity {

    TextView txtId, txtSt;
    private String str;
    DatabaseReference mRef = FirebaseDatabase.getInstance().getReference();
    private Booking objBooking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_details);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int w = dm.widthPixels;
        int h = dm.heightPixels;
        getWindow().setLayout((int)(w*.8),(int)(h*.5));

        Gson gson = new Gson();
        str = getIntent().getStringExtra(Misc.inputExtra);

        objBooking = gson.fromJson(str, Booking.class);

        txtSt = findViewById(R.id.txtSt);
        txtId = findViewById(R.id.txtId);

        findViewById(R.id.btnOk).setOnClickListener(v->{
            startActivity(new Intent(this, CustomerDashBoard.class));
        });

        Intent intent = getIntent();
        try {
            JSONObject jObj = new JSONObject(intent.getStringExtra(Misc.inputExtraPaymentDetails));
            showDetails(jObj.getJSONObject("response"), intent.getStringExtra(Misc.inputExtraAmount));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @SuppressLint("SetTextI18n")
    private void showDetails(JSONObject response, String stringExtra) throws JSONException {
        SharedPreferences mPrefs = getSharedPreferences(Misc.paypalPrice, MODE_PRIVATE); //add key
        @SuppressLint("CommitPrefEdits") SharedPreferences.Editor prefsEditor = mPrefs.edit();

        String data = mPrefs.getString(Misc.paypalPrice, null);
        txtSt.setText(txtSt.getText().toString() + objBooking.getPaidCost());

        mRef.child(Misc.user).child(Misc.roleRenter).child(objBooking.getRenterId())
                .child(Misc.bookings).child(objBooking.getId()).setValue(objBooking.getId());

        mRef.child(Misc.user).child(Misc.roleCustomer).child(objBooking.getCustomerId())
                .child(Misc.bookings).child(objBooking.getId()).setValue(objBooking.getId());

        mRef.child(Misc.bookings).child(objBooking.getId()).setValue(objBooking);

//        mRef.child(Misc.user).child(Misc.roleRenter).child(renterCnic).child(Misc.bookings).child(bookingId).setValue(bookingId);
//        mRef.child(Misc.user).child(Misc.roleCustomer).child(customer.getCnic()).child(Misc.bookings).child(bookingId).setValue(bookingId);
//        mRef.child(Misc.bookings).child(bookingId).setValue(objBooking);
    }
}
