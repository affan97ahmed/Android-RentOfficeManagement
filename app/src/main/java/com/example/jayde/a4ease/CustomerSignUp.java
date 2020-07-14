package com.example.jayde.a4ease;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.Objects;

public class CustomerSignUp extends AppCompatActivity {

    private FirebaseDatabase mData = FirebaseDatabase.getInstance();
    private DatabaseReference mRef = mData.getReference();

    private Button btnSign;
    private String url;
    private String inputExtra;
    private EditText username, pass, phn, cpass;
    private LinearLayout ll;
    private ScrollView sv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Misc.isNetworkConnected(this);
        inputExtra = getIntent().getStringExtra(Misc.inputExtra);

        sv = findViewById(R.id.sv);

        username = findViewById(R.id.txt);
        pass = findViewById(R.id.pass);
        cpass = findViewById(R.id.cpass);
        phn = findViewById(R.id.phn);
        LinearLayout ll = findViewById(R.id.llSignUpCustomer);

        Button btnQRCode = findViewById(R.id.btnQR);
        btnSign = findViewById(R.id.btnSignUp);
        TextView tvSignUp = findViewById(R.id.tVSignUp);

        findViewById(R.id.cLMain).getBackground().setAlpha(Misc.alphaMain);

        if(inputExtra.equals(Misc.admin)){
            tvSignUp.setText("Add Admin");
        }

        username.setOnFocusChangeListener((v, hasFocus) -> {
            ConstraintLayout.LayoutParams p1 = (ConstraintLayout.LayoutParams) sv.getLayoutParams();
            if(hasFocus){
                p1.setMargins(0,0,0,200);
            }else{
                p1.setMargins(0,0,0,0);
            }
            sv.setLayoutParams(p1);
        });

        pass.setOnFocusChangeListener((v, hasFocus) -> {
            ConstraintLayout.LayoutParams p1 = (ConstraintLayout.LayoutParams) sv.getLayoutParams();
            if(hasFocus){
                p1.setMargins(0,0,0,200);
            }else{
                p1.setMargins(0,0,0,0);
            }
            sv.setLayoutParams(p1);
        });

        phn.setOnFocusChangeListener((v, hasFocus) -> {
            ConstraintLayout.LayoutParams p1 = (ConstraintLayout.LayoutParams) sv.getLayoutParams();
            if(hasFocus){
                p1.setMargins(0,0,0,200);
            }else{
                p1.setMargins(0,0,0,0);
            }
            sv.setLayoutParams(p1);
        });

        cpass.setOnFocusChangeListener((v, hasFocus) -> {
            ConstraintLayout.LayoutParams p1 = (ConstraintLayout.LayoutParams) sv.getLayoutParams();
            if(hasFocus){
                p1.setMargins(0,0,0,200);
            }else{
                p1.setMargins(0,0,0,0);
            }
            sv.setLayoutParams(p1);
        });


        final Activity activity = this;
        btnQRCode.setOnClickListener(view -> {
            IntentIntegrator integrator = new IntentIntegrator(activity);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
            integrator.setCaptureActivity(AnyOrientationCaptureActivity.class);
            integrator.setOrientationLocked(false);
            integrator.setPrompt("Scan");
            integrator.setCameraId(0);
            integrator.setBeepEnabled(false);
            integrator.setBarcodeImageEnabled(false);
            integrator.initiateScan();
            btnSign.setEnabled(true);
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
                checkCnic(url.substring(12,25));
                Toast.makeText(this, url,Toast.LENGTH_LONG).show();
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void SignUpCustomer(View view) {

        String varUserName= username.getText().toString();
        String varPass= pass.getText().toString();
        String varConPass= cpass.getText().toString();
        String varPhNo = phn.getText().toString();
        String cnic;

        if (varUserName.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Enter User Name !", Toast.LENGTH_SHORT).show();
        }
        else if (varPass.length() < 6) {
            Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
        }else if(varPhNo.length()>11)
        {
            Toast.makeText(this,"Mobile Number cannot Exceed  Eleven Digits !",Toast.LENGTH_SHORT).show();
        }
        else if (!varPass.equals(varConPass)) {
            Toast.makeText(this, "Password do not Match ! Enter Again.", Toast.LENGTH_SHORT).show();
        } else if(url.isEmpty())
        {
            Toast.makeText(this,"Scan the QR code",Toast.LENGTH_SHORT).show();
        }
        else {
            cnic = url.substring(12, 25);
            User objUser = new User();
            objUser.setPhNo(varPhNo);
            objUser.setPassword(varPass);
            objUser.setName(varUserName);
            objUser.setCnic(cnic);

            if(inputExtra.equals(Misc.admin)){
                objUser.setUserRole(Misc.admin);
                mRef.child(Misc.admin).child(cnic).setValue(objUser);
                Toast.makeText(CustomerSignUp.this, "Admin created successfully", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, AdminDashBoard.class));
            }else{
                objUser.setUserRole(Misc.roleCustomer);
                mRef.child(Misc.user).child(Misc.roleCustomer).child(cnic).setValue(objUser);
                Toast.makeText(CustomerSignUp.this, "You Successfully Signed Up !", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, CustomerDashBoard.class));
            }

        }
    }

    public void hideKeyBoard(View view){
        InputMethodManager imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
        Objects.requireNonNull(this.getCurrentFocus()).clearFocus();
        ConstraintLayout.LayoutParams p1 = (ConstraintLayout.LayoutParams) sv.getLayoutParams();
        p1.setMargins(0,0,0,0);
        sv.setLayoutParams(p1);
    }

    private void checkCnic(String substring) {

        if(inputExtra.equals(Misc.admin)){
            FirebaseDatabase.getInstance().getReference().child(Misc.admin).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    btnSign.setEnabled(true);

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User objUser = snapshot.getValue(User.class);
                        assert objUser != null;
                        if(objUser.getCnic().equals(substring)){
                            btnSign.setEnabled(false);
                            userExist();
                            break;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });

        }
        FirebaseDatabase.getInstance().getReference().child(Misc.user).child(Misc.roleCustomer).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                btnSign.setEnabled(true);

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User objUser = snapshot.getValue(User.class);
                    assert objUser != null;
                    if(objUser.getCnic().equals(substring)){
                        btnSign.setEnabled(false);
                        userExist();
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void userExist(){
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("User Exist");
        alertDialog.setMessage("There is already a user exist on this cnic...!!! You can not use this CNIC. (Sorry)");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Ok",
                (dialog, which) -> {
                });
        alertDialog.show();
    }



}
