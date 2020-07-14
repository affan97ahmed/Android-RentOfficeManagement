package com.example.jayde.a4ease;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

public class AddVehicle extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private DatabaseReference mref=FirebaseDatabase.getInstance().getReference(Misc.user);
    private LinearLayout llAddCarImages;
    private ArrayList<Uri> imagesToUpload;
    private ProgressBar pB;
    private String userRole;
    private DatabaseReference mDatabaseRef;
    private User objUser;
    private User renter;
    private EditText carName;
    private EditText carModel;
    private EditText carCompany;
    private EditText carNumber;
    private EditText costPerDay;
    private EditText trackerId;
    private Car editCar;
    public ArrayList<String> urlList = new ArrayList<>();
    private ArrayList<Car> carsOfRentHouse;
    ConstraintLayout cLAddVehicle;
    private String folderName;

    @SuppressLint({"CutPasteId", "SetTextI18n"})
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_vehicle);

        userRole = getIntent().getStringExtra(Misc.inputExtra);

        String str = getIntent().getStringExtra(Misc.inputExtraCar);

        loadRentHouses();
        loadUser();

        imagesToUpload = new ArrayList<>();

        cLAddVehicle = findViewById(R.id.cLAddVehicle);
        pB = this.findViewById(R.id.progressBarAddCar);
        carCompany = this.findViewById(R.id.etxtCompany);
        carName = this.findViewById(R.id.etxtCarName);
        carModel = this.findViewById(R.id.etxtModel);
        carNumber = this.findViewById(R.id.etxtCarNumber);
        costPerDay = this.findViewById(R.id.etxtCost);
        trackerId = this.findViewById(R.id.etxtTracker);

        if(!str.equals("null")){
            Gson gson = new Gson();
            editCar = gson.fromJson(str, Car.class);
            carCompany.setText(editCar.getCompany());
            carNumber.setText(editCar.getNumber());
            carName.setText(editCar.getName());
            carModel.setText(editCar.getModel());
            costPerDay.setText(editCar.getCostPerDay() + "");
            trackerId.setText(editCar.getTrackerId());
            folderName = editCar.getVehicleId();
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child(Misc.user).child(Misc.roleRenter).child(objUser.getCnic())
                    .child(Misc.folderVehicle).child(editCar.getVehicleId()).child(Misc.folderImages);
            dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    downloadImage(dataSnapshot);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }else{
            folderName = System.currentTimeMillis() + "";
        }

        ImageButton mButtonChooseImage = this.findViewById(R.id.button_choose_image);
        Button mButtonUpload = this.findViewById(R.id.button_upload);
        llAddCarImages = this.findViewById(R.id.llCarPics);

        mDatabaseRef = FirebaseDatabase.getInstance().getReference(Misc.user);

        cLAddVehicle.getBackground().setAlpha(Misc.alphaMain);

        mButtonChooseImage.setOnClickListener(view -> openFileChooser());

        mButtonUpload.setOnClickListener(view -> {
            if(costPerDay.getText().toString().isEmpty()){
                costPerDay.setError("Can not be Empty");
            } else if(carName.getText().toString().isEmpty()){
                carName.setError("Can not be Empty");
            } else if(carModel.getText().toString().isEmpty()){
                carModel.setError("Can not be Empty");
            } else if(carCompany.getText().toString().isEmpty()){
                carCompany.setError("Can not be Empty");
            } else if(carNumber.getText().toString().isEmpty()){
                carName.setError("Can not be empty");
            }else if(checkCar(carNumber.getText().toString())){
                Toast.makeText(this, "Sorry, This car is already uploaded!", Toast.LENGTH_LONG).show();
            }else {
                uploadFile();
            }
        });

    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            Uri mImageUri = data.getData();
            imagesToUpload.add(mImageUri);
            addImageView(mImageUri);
        }
    }

    private void uploadFile() {
        pB.setVisibility(View.VISIBLE);
        mref = mref.child(objUser.getCnic());
        DatabaseReference dbRefForCustomer = FirebaseDatabase.getInstance().getReference().child(Misc.user).child(Misc.roleCustomer).child(objUser.getCnic());

        Intent intent;

        Car obj = new Car();
        obj.setName(carName.getText().toString());
        obj.setCompany(carCompany.getText().toString());
        obj.setModel(carModel.getText().toString());
        obj.setNumber(carNumber.getText().toString());
        obj.setTrackerId(trackerId.getText().toString());
        obj.setCostPerDay(Integer.parseInt(costPerDay.getText().toString()));
        obj.setVehicleId(folderName);
        obj.setBookedBy("null");
        obj.setBookedTo("null");
        obj.setBookedFrom("null");

        if(userRole.equals(Misc.roleRenter)){
            Gson objGson = new Gson();
            renter = objGson.fromJson(getIntent().getStringExtra(Misc.roleRenter),User.class);
            obj.setOwner(objUser.getCnic());
            intent = new Intent(this, CustomerDashBoard.class);
            mDatabaseRef.child(Misc.temp).child(renter.getCnic()).child(folderName).setValue(obj);
            dbRefForCustomer.child(Misc.folderVehicle).child(folderName).setValue("pending");

        }else{
            obj.setOwner(Misc.selfOwner);
            mDatabaseRef.child(Misc.roleRenter).child(objUser.getCnic()).child(Misc.folderVehicle).child(folderName).setValue(obj);
            for(String str : urlList){
                mDatabaseRef.child(Misc.roleRenter).child(objUser.getCnic()).child(Misc.folderVehicle).child(folderName).child(Misc.folderImages)
                .child(System.currentTimeMillis() + "").setValue(str);
            }
            intent = new Intent(this, RenterDashBoard.class);
        }


        Intent serviceIntent = new Intent(this, UploadService.class);
        pB.setVisibility(View.GONE);

        if (imagesToUpload.size() > 0){

            serviceIntent.putExtra(Misc.inputExtra, imagesToUpload);
            serviceIntent.putExtra(Misc.inputExtraFolderName, folderName);
            if (userRole.equals(Misc.roleRenter)){
                serviceIntent.putExtra(Misc.inputExtraCnic, renter.getCnic());
                serviceIntent.putExtra(Misc.temp, Misc.temp);
            }else{
                serviceIntent.putExtra(Misc.temp, "null");
                serviceIntent.putExtra(Misc.inputExtraCnic,objUser.getCnic());
            }


            ContextCompat.startForegroundService(this, serviceIntent);

            Toast.makeText(this, "Pictures are uploading in background", Toast.LENGTH_LONG).show();

        }
        startActivity(intent);
        finish();

    }

    private void addImageView(Uri img){
        ImageView newImageView = new ImageView(this);
        newImageView.setId((int) Math.random());
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                200,200
        );
        newImageView.setLayoutParams(p);
        newImageView.setImageURI(img);

        newImageView.setOnClickListener(v->{
            newImageView.setVisibility(View.GONE);
            imagesToUpload.remove(img);
//                Intent intent = new Intent(this, ZoomImage.class);
//                intent.putExtra(Misc.inputExtra, imgUrl);
//                startActivity(intent);
        });


        llAddCarImages.addView(newImageView);
    }

    private void loadUser() {
        SharedPreferences sharedPreferences = getSharedPreferences(Misc.user, MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(Misc.user, null);

        objUser = new User();
        objUser = gson.fromJson(json, User.class);
        TextView temp = findViewById(R.id.txt_UserName);
        temp.setText(objUser.getName());
        if(objUser.getUserRole().equals(Misc.roleRenter)){
            TextView temp1 = findViewById(R.id.txtROrC);
            temp1.setText("Manager");
        }
        if(objUser.getStatus() != null){
            Toast.makeText(this, "Your account is band by admin you can not Add or Edit any car.", Toast.LENGTH_LONG).show();
            finish();
            if(objUser.getUserRole().equals(Misc.roleCustomer)){
                startActivity(new Intent(this, CustomerDashBoard.class));
            }else{
                startActivity(new Intent(this, RenterDashBoard.class));
            }
        }
    }

    public void hideKeyBoard(View view){
        InputMethodManager imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }

    public void downloadImage(DataSnapshot dataSnapshot){
        int i = 0;
        for (DataSnapshot image: dataSnapshot.getChildren()) {
            i++;
            String imgUrl = Objects.requireNonNull(image.getValue()).toString();
            urlList.add(imgUrl);
            ImageView firstImage = new ImageView(this);
            firstImage.setId((int) Math.random());
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(200, 200);
            firstImage.setLayoutParams(p);
            Picasso.with(this).load(imgUrl).into(firstImage);

            firstImage.setOnClickListener(v->{
                firstImage.setVisibility(View.GONE);
                urlList.remove(imgUrl);
//                Intent intent = new Intent(this, ZoomImage.class);
//                intent.putExtra(Misc.inputExtra, imgUrl);
//                startActivity(intent);
            });
            llAddCarImages.addView(firstImage);

        }

    }

    private  void loadRentHouses(){
        DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference(Misc.user).child(Misc.roleRenter);
        carsOfRentHouse = new ArrayList<>();

        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    User objUser = postSnapshot.getValue(User.class);

                    assert objUser != null;
                    if(objUser.getStatus() == null){

                        mDatabaseRef.child(objUser.getCnic()).child(Misc.folderVehicle).addValueEventListener(new ValueEventListener() {

                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot vehi : dataSnapshot.getChildren()) {

                                    Car objCar = vehi.getValue(Car.class);
                                    assert objCar != null;
                                    if(objCar.getStatus() == null){
                                        carsOfRentHouse.add(objCar);
                                    }
                                }

                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(AddVehicle.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private boolean checkCar(String number){
            for(Car obj: carsOfRentHouse){
                if(obj.getNumber().equals(number)){
                    return true;
                }
            }
        return false;
    }

}