package com.jimju.simplecamerax;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.jimju.simplecamerax.activity.CameraSampleActivity;
import com.jimju.simplecamerax.activity.MinCameraXSampleActivity;
import com.jimju.simplecamerax.activity.QrcodeSampleActivity;
import com.jimju.simplecamerax.activity.VideoPhotoSampleActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.sample_camera_fragment).setOnClickListener(this);
        findViewById(R.id.sample_camera).setOnClickListener(this);
        findViewById(R.id.sample_qrcode).setOnClickListener(this);
        findViewById(R.id.sample_video_and_photo).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.sample_camera_fragment:
                intent.setClass(this, CameraSampleActivity.class);
                break;
            case R.id.sample_camera:
                intent.setClass(this, MinCameraXSampleActivity.class);
                break;
            case R.id.sample_qrcode:
                intent.setClass(this, QrcodeSampleActivity.class);
                break;
            case R.id.sample_video_and_photo:
                intent.setClass(this, VideoPhotoSampleActivity.class);
                break;
        }
        startActivity(intent);
    }
}
