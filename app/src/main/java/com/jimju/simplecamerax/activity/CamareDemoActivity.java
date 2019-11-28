package com.jimju.simplecamerax.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.view.Display;
import android.view.KeyEvent;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.jimju.simplecamerax.MainActivity;
import com.jimju.simplecamerax.R;
import com.jimju.simplecamerax.utils.AutoFitPreviewBuilder;
import com.jimju.simplecamerax.utils.ImageUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class CamareDemoActivity extends AppCompatActivity implements View.OnClickListener {

    private final static int PERMISSIONS_REQUEST_CODE = 10;
    private String[] PERMISSIONS_REQUIRED = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO};


    private static final String KEY_EVENT_ACTION = "key_event_action";
    private static final String KEY_EVENT_EXTRA = "key_event_extra";
    private final static String FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS";
    private static final long IMMERSIVE_FLAG_TIMEOUT = 500L;
    private TextureView viewFinder;
    private File outputDirector;
    private CameraX.LensFacing lensFacing = CameraX.LensFacing.BACK;
    private ImageCapture imageCapture;
    private ImageButton shutter;
//    private Job job = new Job();
//    private CoroutineContext

    private BroadcastReceiver volumeDownReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int keyCode = intent.getIntExtra(KEY_EVENT_EXTRA, KeyEvent.KEYCODE_UNKNOWN);
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                shutter.callOnClick();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_demo);
        if (!hasPermissions() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(PERMISSIONS_REQUIRED, PERMISSIONS_REQUEST_CODE);
        } else
            init();

    }

    private boolean hasPermissions() {
        for (String s : PERMISSIONS_REQUIRED) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, s)) {
                return false;
            }
        }
        return true;
    }

    private void init() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(KEY_EVENT_ACTION);
        shutter = findViewById(R.id.camera_capture_button);
        shutter.setOnClickListener(this);
        LocalBroadcastManager.getInstance(this).registerReceiver(volumeDownReceiver, filter);
        viewFinder = findViewById(R.id.view_finder);
        viewFinder.post(new Runnable() {
            @Override
            public void run() {
                bindCameraUseCases();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(volumeDownReceiver);
        CameraX.unbindAll();
    }

    private void bindCameraUseCases() {
        CameraX.unbindAll();
        DisplayMetrics metrics = new DisplayMetrics();
        Display display = viewFinder.getDisplay();
        display.getRealMetrics(metrics);
        Size screenSize = new Size(metrics.widthPixels, metrics.heightPixels);
        Rational screenAspectRation = new Rational(metrics.widthPixels, metrics.heightPixels);
        PreviewConfig viewFinderConfig = new PreviewConfig.Builder().setLensFacing(lensFacing).setTargetAspectRatio(screenAspectRation)
                .setTargetResolution(screenSize).setTargetRotation(viewFinder.getDisplay().getRotation()).build();

        Preview preview = AutoFitPreviewBuilder.build(viewFinderConfig, viewFinder);

        ImageCaptureConfig imageCaptureConfig = new ImageCaptureConfig.Builder().setLensFacing(lensFacing).setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
                .setTargetAspectRatio(screenAspectRation).setTargetRotation(viewFinder.getDisplay().getRotation()).build();
        imageCapture = new ImageCapture(imageCaptureConfig);
        ImageAnalysisConfig.Builder analyzerBuilder = new ImageAnalysisConfig.Builder();
        analyzerBuilder.setLensFacing(lensFacing);
        HandlerThread analyzerThread = new HandlerThread("LuminosityAnalysis");
        analyzerThread.start();
        analyzerBuilder.setCallbackHandler(new Handler(analyzerThread.getLooper()));
        analyzerBuilder.setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE);

        ImageAnalysis imageAnalyzer = new ImageAnalysis(analyzerBuilder.build());
        CameraX.bindToLifecycle(this, imageAnalyzer, imageCapture, preview);
    }

    private void setGalleryThumbnail(File file) {
        ImageButton thumbnail = findViewById(R.id.photo_view_button);
        Bitmap bitmap = null;
        try {
            bitmap = ImageUtils.decodeBitmap(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bitmap thumbnailBitmap = ImageUtils.cropCircularThumbnail(bitmap, 10);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            thumbnail.setForeground(new BitmapDrawable(thumbnailBitmap));
        } else {
            Glide.with(this).load(thumbnailBitmap).into(thumbnail);
        }
//        if ()
    }

    /*

     */
    private ImageCapture.OnImageSavedListener imageSavedListener = new ImageCapture.OnImageSavedListener() {
        @Override
        public void onImageSaved(@NonNull File file) {
            setGalleryThumbnail(file);
        }

        @Override
        public void onError(@NonNull ImageCapture.UseCaseError useCaseError, @NonNull String message, @Nullable Throwable cause) {
            Log.e("ImageSaveError", message);
        }


    };

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.camera_capture_button) {
            File photoFile = new File(MainActivity.getOutputDirectory(this), new SimpleDateFormat(FILENAME, Locale.CHINA).format(System.currentTimeMillis()) + ".jpg");
            ImageCapture.Metadata metadata = new ImageCapture.Metadata();
            metadata.isReversedHorizontal = lensFacing == CameraX.LensFacing.FRONT;
            imageCapture.takePicture(photoFile, imageSavedListener, metadata);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                init();
            } else
                finish();
        }
    }
}
