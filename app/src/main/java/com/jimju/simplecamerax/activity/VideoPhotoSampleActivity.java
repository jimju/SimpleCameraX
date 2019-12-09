package com.jimju.simplecamerax.activity;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.VideoCapture;
import androidx.camera.view.CameraView;


import com.jimju.simplecamerax.R;
import com.jimju.simplecamerax.widget.CaptureButton;

import java.io.File;

public class VideoPhotoSampleActivity extends AppCompatActivity implements CaptureButton.CaptureListener, VideoCapture.OnVideoSavedListener, ImageCapture.OnImageSavedListener {
    CameraView mCameraView;
    CaptureButton mButton;
    private static final int MIN_REC_DURATION = 500;
    CameraView.CaptureMode mCaptureMode = CameraView.CaptureMode.IMAGE;
    private boolean isTooShort = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_photo_sample);
        mCameraView = findViewById(R.id.camera_view);
        mButton = new CaptureButton(this, getResources().getDimensionPixelSize(R.dimen.round_button_medium));
        FrameLayout.LayoutParams btn_capture_param = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        btn_capture_param.gravity = Gravity.CENTER;
        mButton.setLayoutParams(btn_capture_param);
        FrameLayout frameLayout = findViewById(R.id.frame);
        frameLayout.addView(mButton);
        mCameraView.bindToLifecycle(this);
        mButton.setCaptureLisenter(this);
        mButton.setDuration(15000);
        mButton.setMinDuration(MIN_REC_DURATION);
    }


    @Override
    public void takePictures() {
        if (mCaptureMode == CameraView.CaptureMode.VIDEO) {
            mCaptureMode = CameraView.CaptureMode.IMAGE;
            mCameraView.setCaptureMode(CameraView.CaptureMode.IMAGE);
        }
        File file = new File(CameraSampleActivity.getOutputDirectory(this), System.currentTimeMillis() + ".jpg");
        mCameraView.takePicture(file, this);
    }

    @Override
    public void recordShort(long time) {
        print("视频录制时间过短");
        isTooShort = true;
        mCameraView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mCameraView.stopRecording();
            }
        }, 1500 - time);
    }

    public void recordStart() {
        if (mCaptureMode == CameraView.CaptureMode.IMAGE) {
            mCaptureMode = CameraView.CaptureMode.VIDEO;
            mCameraView.setCaptureMode(CameraView.CaptureMode.VIDEO);
        }
        File file = new File(CameraSampleActivity.getOutputDirectory(this), System.currentTimeMillis() + ".mp4");
        mCameraView.startRecording(file, this);
    }



    @Override
    public void recordEnd(long time) {
        isTooShort = false;
        if (mCameraView.isRecording())
            mCameraView.stopRecording();
    }

    @Override
    public void recordZoom(float zoom) {
//        print("recordZoom" + zoom);
    }

    @Override
    public void recordError() {
        print("recordError");
    }

    private void print(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    /**
     * CameraView录像完成
     * @param file
     */
    @Override
    public void onVideoSaved(@NonNull File file) {
        if (isTooShort) {
            file.delete();
        } else
            ReviewSamleActivity.actionStart(VideoPhotoSampleActivity.this, file.getAbsolutePath());
    }

    /**
     * CameraView录像异常
     * @param videoCaptureError
     * @param message
     * @param cause
     */
    @Override
    public void onError(@NonNull VideoCapture.VideoCaptureError videoCaptureError, @NonNull String message, @Nullable Throwable cause) {

    }

    /**
     * CameraView拍照完成
     * @param file
     */
    @Override
    public void onImageSaved(@NonNull File file) {
        ReviewSamleActivity.actionStart(VideoPhotoSampleActivity.this, file.getAbsolutePath());
    }

    /**
     * CameraView拍照异常
     * @param imageCaptureError
     * @param message
     * @param cause
     */
    @Override
    public void onError(@NonNull ImageCapture.ImageCaptureError imageCaptureError, @NonNull String message, @Nullable Throwable cause) {

    }
}
