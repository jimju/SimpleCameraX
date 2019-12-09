package com.jimju.simplecamerax.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.FrameLayout;

import com.jimju.simplecamerax.R;
import com.jimju.simplecamerax.utils.ViewExtensions;

import java.io.File;

public class CameraSampleActivity extends AppCompatActivity {
    public final static String KEY_EVENT_ACTION = "key_event_action";
    public final static String KEY_EVENT_EXTRA = "key_event_extra";
    public final long IMMERSIVE_FLAG_TIMEOUT = 500L;
    private FrameLayout container;

    public static File getOutputDirectory(Context context){
        Context appContext = context.getApplicationContext();
        File mediaDir = new File(appContext.getExternalMediaDirs()[0],"CameraX Basic");
        if (!mediaDir.exists()){
            mediaDir.mkdirs();
        }
        return mediaDir;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_sample);
        container = findViewById(R.id.fragment_container);
    }

    @Override
    protected void onResume() {
        super.onResume();
        container.postDelayed(new Runnable() {
            @Override
            public void run() {
                container.setSystemUiVisibility(ViewExtensions.FLAGS_FULLSCREEN);
            }
        },IMMERSIVE_FLAG_TIMEOUT);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
            Intent intent = new Intent(KEY_EVENT_ACTION);
            intent.putExtra(KEY_EVENT_EXTRA,keyCode);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
