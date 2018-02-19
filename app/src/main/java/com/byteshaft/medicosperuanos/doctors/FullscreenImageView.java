package com.byteshaft.medicosperuanos.doctors;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.byteshaft.medicosperuanos.R;

public class FullscreenImageView extends Activity {

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_fullscreen_image_view);
        imageView = (ImageView) findViewById(R.id.image_view);
        Intent intent = getIntent();
        String url = intent.getStringExtra("url");
        Glide.with(getApplicationContext()).load(url)
                .override(150, 150)
                .centerCrop()
                .into(imageView);
    }
}
