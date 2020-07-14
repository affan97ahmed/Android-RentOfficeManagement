package com.example.jayde.a4ease;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;

import java.math.BigDecimal;

public class OnlinePayment extends AppCompatActivity {

    public static final int PAYPAL_REQUEST_CODE = 7171;

    private static PayPalConfiguration config = new PayPalConfiguration().environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId(Misc.paypalId);

    public Button btnPaypal;
    public String amount;
    public TextView txtAdvance, txtComplete;
    private String str;
    private int paid;
    private Booking obj;

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_payment);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int w = dm.widthPixels;
        int h = dm.heightPixels;

        getWindow().setLayout((int)(w*.8),(int)(h*.3));

        Gson gson = new Gson();
        str = getIntent().getStringExtra(Misc.inputExtra);

        obj = gson.fromJson(str, Booking.class);

        btnPaypal = findViewById(R.id.btn_paypal);
        txtAdvance = findViewById(R.id.txtAdvance);
        txtComplete = findViewById(R.id.txtComplete);

        startPayPalService();

        txtComplete.getBackground().setAlpha(Misc.alphaBtnLow);
        txtAdvance.getBackground().setAlpha(Misc.alphaBtnHigh);

        txtComplete.setText(txtComplete.getText().toString() + "\n" + obj.getCost());

        long diff = obj.getBookedTo().getTime() - obj.getBookedFrom().getTime();
        float dayCount = (float) diff / (24 * 60 * 60 * 1000) + 1;
        if(dayCount == 0){
            txtAdvance.setText(txtAdvance.getText().toString() + "\n" + obj.getCost());
        }else{
            txtAdvance.setText(txtAdvance.getText().toString() + "\n" + obj.getCost() / dayCount);
        }

        amount = obj.getCost() / dayCount + "";
        paid = (int)(obj.getCost() / dayCount);

        txtComplete.setOnClickListener(v->{
            amount = obj.getCost() + "";
            txtComplete.getBackground().setAlpha(Misc.alphaBtnHigh);
            txtAdvance.getBackground().setAlpha(Misc.alphaBtnLow);
            paid = obj.getCost();
        });

        txtAdvance.setOnClickListener(v->{
            amount = (int)(obj.getCost() / dayCount) + "";
            paid = (int)(obj.getCost() / dayCount);
            txtComplete.getBackground().setAlpha(Misc.alphaBtnLow);
            txtAdvance.getBackground().setAlpha(Misc.alphaBtnHigh);
        });

        btnPaypal.setOnClickListener(v->{
            obj.setPaidCost(paid);
            obj.setPendingCost(obj.getCost() - paid);
            processPayment();
        });

    }

    private void startPayPalService() {
        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);
    }

    private void processPayment() {

        PayPalPayment payment = new PayPalPayment(new BigDecimal(String.valueOf("1")),"USD"
        ,"For VeRop",PayPalPayment.PAYMENT_INTENT_SALE);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Misc.paypalPrice, "1");
        editor.apply();

        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION,config);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);
        startActivityForResult(intent, PAYPAL_REQUEST_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)  {
        if(requestCode == PAYPAL_REQUEST_CODE){
            if(resultCode == RESULT_OK){
                PaymentConfirmation confirmation = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if(confirmation != null){
                    try{
                        String paymentDetails = confirmation.toJSONObject().toString(4);
                        Gson gson = new Gson();
                        String strObj = gson.toJson(obj);
                        startActivity(new Intent(this, PaymentDetails.class).putExtra(Misc.inputExtraPaymentDetails,paymentDetails)
                        .putExtra(Misc.inputExtraAmount, amount).putExtra(Misc.inputExtra,strObj));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }else if(resultCode == Activity.RESULT_CANCELED){
                Toast.makeText(this, "Canceld", Toast.LENGTH_LONG).show();
            }
        }else if(resultCode == PaymentActivity.RESULT_EXTRAS_INVALID){
            Toast.makeText(this,"Invalid", Toast.LENGTH_LONG).show();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

}
