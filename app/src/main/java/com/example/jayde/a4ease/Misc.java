package com.example.jayde.a4ease;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Misc {

    static String deleted = "deleted";
    static String inputExtraCar = "car";
    public static String accountDeleted = "Account Deleted";
    static String lat = "Latitude ";
    static String lng = "Longitude ";
    static String location = "Location";
    static String admin = "admin";
    static String statusCanceled = "canceled";
    static String inProcessBooking = "savedBooking";
    static String paypalPrice = "payPalPrice";
    static String notiTypeAddCar = "AddCar";
    static String accepted = "Accepted";
    static String statusCompleted = "complete";
    static String statusOnGoing = "onGoing";
    static String statusPending = "pending";
    static String folderImages = "images";
    static String bookings = "bookings";
    static int alphaMain = 10;
    static int alphaBtnHigh = 90;
    static int alphaBtnLow = 10;
    static String inputExtra = "arrayList";
    static String inputExtraCarsHashMap = "hashMap";
    static String inputExtraFolderName = "folderName";
    static String inputExtraCnic = "cnic";
    static String logKey = "logKey";
    static String roleRenter = "Renters";
    static String folderVehicle = "vehicles";
    static String roleCustomer = "Customers";
    static String user = "users";
    static String storageFolder = "uploads";
    static String selfOwner = "self";
    static String temp = "temp";
    static String notifications = "notifications";
    static String rejected = "rejected";
    static String paypalId = "AVCoZSSKcO2FdCBueU61L9JNmN08DA2Dp09cKBUVYpUvptdOC6DJnTxsg9U-Qn9cBsELDqLmeDE_mADK";
    static String inputExtraPaymentDetails = "PaymentDetails";
    static String inputExtraAmount = "amount";

    static TextView makeTextView(String txt, int type, Context context){
        TextView justTextView = new TextView(context);
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

    static LinearLayout makeLinearLayout(int orientation, Context context){
        LinearLayout ll = new LinearLayout(context);
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

    static Button makeBtn(String text, Context context){
        Button btn = new Button(context);

        btn.setText(text);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        btn.setLayoutParams(p);

        p.setMargins(0,5,0,0);

        return btn;
    }

    public static void hideKeyBoard(View view, Context context){
        InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }

    static void turnGPSOn(Activity activity){
        final LocationManager manager = (LocationManager) activity.getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps(activity);
        }
    }

    private static void buildAlertMessageNoGps(Activity activity) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage("Your GPS seems to be disabled, You have to enable it")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, id) -> activity.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                .setNegativeButton("No", (dialog, id) ->{
                    activity.finish();
                    dialog.cancel();
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private static void buildAlertMessageNoInternetConnection(Activity activity) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage("Your Internet seems to be disabled, You have to enable it")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, id) -> activity.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)))
                .setNegativeButton("No", (dialog, id) ->{
                    activity.finish();
                    dialog.cancel();
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    static void isNetworkConnected(Activity activity) {
        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);

        if(!(cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected())){
            buildAlertMessageNoInternetConnection(activity);
        }
    }
}
