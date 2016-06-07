package com.test.augment.view;

import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class AugmentBaseActivity extends AppCompatActivity {
    public void loadImage(String url, int placeHolderResID, ImageView imageView) {
        Glide.with(this)
            .load(url)
            .placeholder(placeHolderResID)
            .crossFade()
            .into(imageView);
    }
}
