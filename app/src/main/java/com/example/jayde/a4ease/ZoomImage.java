package com.example.jayde.a4ease;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

public class ZoomImage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zoom_image);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int w = dm.widthPixels;
        int h = dm.heightPixels;
        getWindow().setLayout((int)(w*.8),(int)(h*.5));

        ImageView imageView = findViewById(R.id.imageViewZoom);

        String str = getIntent().getStringExtra(Misc.inputExtra);

        Picasso.with(this).load(str).into(imageView);

        h = imageView.getLayoutParams().height;
        w = imageView.getLayoutParams().width;
        getWindow().setLayout(w,h);
    }

}
