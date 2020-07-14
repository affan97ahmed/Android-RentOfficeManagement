package com.example.jayde.a4ease;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
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
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.ArrayList;

import static com.example.jayde.a4ease.App.CHANNEL_ID;

public class DownloadService extends Service {
    DatabaseReference mDbRef = FirebaseDatabase.getInstance().getReference();
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ArrayList<Uri> imagesToUpload = intent.getParcelableArrayListExtra(Misc.inputExtra);
        String  folderName = intent.getStringExtra(Misc.inputExtraFolderName);
        String  userCnic = intent.getStringExtra(Misc.inputExtraCnic);

        Intent notificationIntent = new Intent(this, CustomerSignUp.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Uploading")
            .setContentText("Uploading Images you have added")
            .setSmallIcon(R.drawable.logo)
            .setContentIntent(pendingIntent)
            .build();

        startForeground(1, notification);

        StorageReference renter = FirebaseStorage.getInstance().getReference(Misc.storageFolder).child(userCnic + "/");
        int i = 0;
        for (Uri temp : imagesToUpload) {
            i++;
            StorageReference fileReference = renter.child(folderName + "/" + i +"." + getFileExtension(temp));

            String name = i + "";
            StorageTask<UploadTask.TaskSnapshot> mUploadTask = fileReference.putFile(temp)
                .addOnSuccessListener(taskSnapshot -> {
                    Handler handler = new Handler();
                    handler.postDelayed(() -> {
                    }, 500);

                    Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!urlTask.isSuccessful()) ;
                    String downloadUrl = urlTask.getResult() + "";

                    Toast.makeText(getApplicationContext(), "Upload successful", Toast.LENGTH_SHORT).show();
                    mDbRef.child(Misc.user).child(Misc.roleRenter).child(userCnic).child(Misc.folderVehicle).child(folderName).child(Misc.folderImages).child(name).setValue(downloadUrl);

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

    private Target picassoImageTarget(Context context, final String imageDir, final String imageName) {
        Log.d("picassoImageTarget", " picassoImageTarget");
        ContextWrapper cw = new ContextWrapper(context);
        final File directory = cw.getDir(imageDir, Context.MODE_PRIVATE); // path to /data/data/yourapp/app_imageDir
        return new Target() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return null;
            }

            @Override
            public ElementType[] value() {
                return new ElementType[0];
            }

            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final File myImageFile = new File(directory, imageName); // Create image file
                        FileOutputStream fos = null;
                        try {
                            fos = new FileOutputStream(myImageFile);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                fos.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        Log.i("image", "image saved to >>>" + myImageFile.getAbsolutePath());

                    }
                }).start();
            }

            public void onBitmapFailed(Drawable errorDrawable) {
            }
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                if (placeHolderDrawable != null) {}
            }
        };
    }

}
