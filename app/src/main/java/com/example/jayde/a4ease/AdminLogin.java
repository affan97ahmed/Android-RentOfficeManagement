package com.example.jayde.a4ease;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
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

import static com.example.jayde.a4ease.SignUpRenter.cnic;

public class AdminLogin extends AppCompatActivity {

    private Button btnLogIn;
    private EditText pass;
    private Button btnQrCnic;
    final Activity activity = this;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        Misc.isNetworkConnected(this);

        btnLogIn = findViewById(R.id.btn_login);
        pass = findViewById(R.id.et_pass);
        btnQrCnic = findViewById(R.id.btn_qp);

        findViewById(R.id.cLAdminLogin).getBackground().setAlpha(Misc.alphaMain);

        btnLogIn.setOnClickListener(view -> login());

        btnQrCnic.setOnClickListener(view -> {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
            IntentIntegrator integrator = new IntentIntegrator(activity);
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


    }

    public void login() {
        String txtPass=pass.getText().toString();

        if(txtPass.isEmpty())
        {
            pass.setError("Please Enter Password");
        } else if(url.equals(""))
        {
            btnQrCnic.setError("Please Scan QR Code for CNIC");
        } else {
            cnic = url.substring(12, 25);
            checkUser( txtPass, cnic);

        }
    }

    public void checkUser(String txtPass, String CNIC){
        Toast toast = Toast.makeText(this, "User does not exist", Toast.LENGTH_LONG);
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference().child(Misc.admin);
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data: dataSnapshot.getChildren()){
                    User objUser = data.getValue(User.class);

                    assert objUser != null;
                    if(objUser.getCnic().equals(CNIC) && objUser.getPassword().equals(txtPass)){
                        SharedPreferences sharedPreferences = getSharedPreferences(Misc.user, MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        Gson gson = new Gson();
                        String json = gson.toJson(objUser);
                        editor.putString(Misc.user, json);
                        editor.apply();
                        finish();
                        startActivity(new Intent(AdminLogin.this, AdminDashBoard.class));
                        return;
                    }

                }
                toast.show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null){
            if(result.getContents()==null){
                Toast.makeText(this, "You cancelled the scanning", Toast.LENGTH_LONG).show();
            }
            else {

                url = result.getContents();
                Toast.makeText(this, url,Toast.LENGTH_LONG).show();
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void hideKeyBoard(View view){
        InputMethodManager imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }


}
