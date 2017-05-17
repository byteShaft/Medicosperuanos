package com.byteshaft.medicosperuanos.doctors;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.byteshaft.medicosperuanos.R;

import uk.co.senab.photoview.PhotoViewAttacher;

public class FullscreenImageView extends Activity {

    private ImageView imageView;
    private PhotoViewAttacher photoViewAttacher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_fullscreen_image_view);
        imageView = (ImageView) findViewById(R.id.image_view);
        photoViewAttacher = new PhotoViewAttacher(imageView);
        photoViewAttacher.update();
        Intent intent = getIntent();
        String imagePosition = intent.getStringExtra("url");
    }
}
