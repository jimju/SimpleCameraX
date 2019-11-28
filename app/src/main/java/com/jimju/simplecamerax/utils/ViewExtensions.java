package com.jimju.simplecamerax.utils;

import android.os.Build;
import android.view.DisplayCutout;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.ImageButton;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

import java.io.File;

public class ViewExtensions {
    /**
     * Combination of all flags required to put activity into immersive mode
     * 将活动置于沉浸式模式所需的所有标志的组合
     */
    public static final int FLAGS_FULLSCREEN =
            View.SYSTEM_UI_FLAG_LOW_PROFILE |
                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

    /**
     * Milliseconds used for UI animations
     * 用于UI动画的毫秒数
     */
    public static final long ANIMATION_FAST_MILLIS = 50L;
    public static final long ANIMATION_SLOW_MILLIS = 100L;

    public static void simulateClick(final ImageButton btn, long delay) {
        btn.performClick();
        btn.setPressed(true);
        btn.invalidate();
        btn.postDelayed(new Runnable() {
            @Override
            public void run() {
                btn.invalidate();
                btn.setPressed(false);
            }
        }, delay);
    }

    public static void simulateClick(final ImageButton btn) {
        simulateClick(btn, ANIMATION_FAST_MILLIS);
    }

    /**
     * Pad this view with the insets provided by the device cutout (i.e. notch)
     * 用设备切口（即切口）提供的嵌件填充此视图*/
    @RequiresApi(Build.VERSION_CODES.P)
    public static void padWithDisplayCutout(View v) {
        DisplayCutout cutout = v.getRootWindowInsets().getDisplayCutout();
        v.setPadding(cutout.getSafeInsetLeft(), cutout.getSafeInsetTop(), cutout.getSafeInsetRight(), cutout.getSafeInsetBottom());
        v.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                DisplayCutout cutout = insets.getDisplayCutout();
                v.setPadding(cutout.getSafeInsetLeft(), cutout.getSafeInsetTop(), cutout.getSafeInsetRight(), cutout.getSafeInsetBottom());
                return insets;
            }
        });
    }
    /** Same as [AlertDialog.show] but setting immersive mode in the dialog's window
     *  与[AlertDialog.show]相同，但在对话框窗口中设置沉浸式模式
     */
    public static void showImmersive(AlertDialog dialog){
        Window window = dialog.getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        // Make sure that the dialog's window is in full screen
        window.getDecorView().setSystemUiVisibility(FLAGS_FULLSCREEN);
        dialog.show();
        // Set the dialog to focusable again
        //再次将对话框设置为可聚焦
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    }

    public static String extension(File file){
        String extension = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf(".") + 1);
        return extension;
    }
}
