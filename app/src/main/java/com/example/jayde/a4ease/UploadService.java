package com.example.jayde.a4ease;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;

import static com.example.jayde.a4ease.App.CHANNEL_ID;


public class UploadService extends Service {

    DatabaseReference mDbRef = FirebaseDatabase.getInstance().getReference();
    private Intent intent;

    @Override
    public void onCreate() {
        super.onCreate();
        intent =new Intent(this, RenterDashBoard.class);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ArrayList<Uri> imagesToUpload = intent.getParcelableArrayListExtra(Misc.inputExtra);
        String  folderName = intent.getStringExtra(Misc.inputExtraFolderName);
        String  userCnic = intent.getStringExtra(Misc.inputExtraCnic);
        String temp = intent.getStringExtra(Misc.temp);

        Intent notificationIntent = new Intent(this, CustomerSignUp.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Uploading Image")
                .setContentText("Uploading Images For VeROP")
                .setSmallIcon(R.drawable.logo)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        StorageReference renter = FirebaseStorage.getInstance().getReference(Misc.storageFolder).child(userCnic
                + "/");
        int i = 0;
        for (Uri tempNew : imagesToUpload) {
            i++;
            StorageReference fileReference = renter.child(folderName
                    + "/" + i +"." + getFileExtension(tempNew));

            String name = i + "";
            StorageTask<UploadTask.TaskSnapshot> mUploadTask = fileReference.putFile(tempNew)
                .addOnSuccessListener(taskSnapshot -> {
                    Handler handler = new Handler();
                    handler.postDelayed(() -> {
                    }, 500);

                    Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!urlTask.isSuccessful()) ;
                    String downloadUrl = urlTask.getResult() + "";

                    Toast.makeText(getApplicationContext(), "Upload successful", Toast.LENGTH_SHORT).show();
                    if(temp.equals("null")) {
                        mDbRef.child(Misc.user).child(Misc.roleRenter).child(userCnic).child(Misc.folderVehicle).child(folderName).child(Misc.folderImages).child(name).setValue(downloadUrl);
                    }else{
                        mDbRef.child(Misc.user).child(Misc.temp).child(userCnic).child(folderName).child(Misc.folderImages).child(name).setValue(downloadUrl);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                })
                .addOnProgressListener(taskSnapshot -> {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
//                            mProgressBar.setProgress((int) progress);
                });
        }

        //do heavy work on a background thread
        stopSelf();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }
}