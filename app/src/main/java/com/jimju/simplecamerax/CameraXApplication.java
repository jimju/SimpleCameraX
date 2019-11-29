package com.jimju.simplecamerax;

import android.app.Application;

import com.tencent.bugly.Bugly;

public class CameraXApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Bugly.init(this,"64784c015f",true);
    }
}
