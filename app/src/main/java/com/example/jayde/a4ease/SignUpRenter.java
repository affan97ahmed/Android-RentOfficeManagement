package com.example.jayde.a4ease;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class SignUpRenter extends Activity {

    private Button btn;
    public String url;
    public  static String cnic;
    public User objUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up_renter);

        Misc.isNetworkConnected(this);
        Button button = findViewById(R.id.btnQR);
        btn = findViewById(R.id.btnSignUp);
        LinearLayout ll = findViewById(R.id.llSignUpRenter);
        findViewById(R.id.cLSignUpRenter).getBackground().setAlpha(Misc.alphaMain);

        final Activity activity = this;
        button.setOnClickListener(view -> {
            IntentIntegrator integrator = new IntentIntegrator(activity);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
            integrator.setCaptureActivity(AnyOrientationCaptureActivity.class);
            integrator.setOrientationLocked(false);
            integrator.setPrompt("Scan");
            integrator.setCameraId(0);
            integrator.setBeepEnabled(false);
            integrator.setBarcodeImageEnabled(false);
            integrator.initiateScan();
        });


    }

    private void checkCnic(String substring) {
        FirebaseDatabase.getInstance().getReference().child(Misc.user).child(Misc.roleRenter).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                btn.setEnabled(true);

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User objUser = snapshot.getValue(User.class);
                    assert objUser != null;
                    if(objUser.getCnic().equals(substring)){
                        btn.setEnabled(false);
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
                Toast.makeText(this, url.substring(12,25), Toast.LENGTH_LONG).show();
                checkCnic(url.substring(12,25));
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void signupRenter(View view) {
        EditText username = findViewById(R.id.txt);
        EditText pass = findViewById(R.id.pass);
        EditText cpass = findViewById(R.id.cpass);
        EditText phn = findViewById(R.id.phn);
        String userName = username.getText().toString();
        String password = pass.getText().toString();
        String conPassword = cpass.getText().toString();
        String phoneNo =   phn.getText().toString();

        if (userName.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Enter User Name !", Toast.LENGTH_SHORT).show();
            return;
        }else if (password.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
            return;
        }else if (password.length() < 6) {
            Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters!",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        else if(phoneNo.length()<11)
        {
            Toast.makeText(this,"Invalid mobile number",Toast.LENGTH_SHORT).show();
        }else if (!password.equals(conPassword)) {
            Toast.makeText(this, "Password do not Match ! Enter Again.", Toast.LENGTH_SHORT).show();
            return;
        } else if(url.isEmpty())
        {
            Toast.makeText(this, "First Scan QR Code ", Toast.LENGTH_SHORT).show();
        } else {
            objUser = new User();
            cnic = url.substring(12, 25);
            objUser.setCnic(cnic);
            objUser.setPassword(password);
            objUser.setUserRole(Misc.roleRenter);
            objUser.setPhNo(phoneNo);
            objUser.setName(userName);

            Intent intent=new Intent(SignUpRenter.this, RenterGetLocation.class);
            Gson gson = new Gson();
            String json = gson.toJson(objUser);
            intent.putExtra(Misc.inputExtra,json);

            startActivity(intent);
        }
        Toast.makeText(this, "You Successfully Signed Up !", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onBackPressed() {
        finish();
        startActivity(new Intent(this, login.class));
    }

    public void hideKeyBoard(View view){
        InputMethodManager imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }

}
