package com.jimju.simplecamerax.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.jimju.simplecamerax.R;

public class ReviewSamleActivity extends AppCompatActivity {
    public static void actionStart(Context context, String path) {
        Intent intent = new Intent(context, ReviewSamleActivity.class);
        intent.putExtra("path", path);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_sample);
        Toolbar toolbar = findViewById(R.id.toolbar);
        ImageView imageView = findViewById(R.id.iv);
        String path = getIntent().getStringExtra("path");
        VideoView mVideoLocal = findViewById(R.id.video);
        if (path.contains("mp4")) {
            mVideoLocal.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);
            MediaController localMediaController = new MediaController(this);
            mVideoLocal.setMediaController(localMediaController);
            mVideoLocal.setVideoPath(path);
            mVideoLocal.start();
        } else
            Glide.with(this).asBitmap().load(path).into(imageView);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDefaultDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("文件预览");
        actionBar.setSubtitle(path);
    }
}
