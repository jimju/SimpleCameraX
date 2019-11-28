package com.jimju.simplecamerax.utils;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.display.DisplayManager;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Size;
import android.view.Display;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;

import java.lang.ref.WeakReference;
import java.util.Objects;

import javax.microedition.khronos.opengles.GL10;

public class AutoFitPreviewBuilder {

    private PreviewConfig config;
    private WeakReference<TextureView> viewFinderRef;
    Preview useCase;
    private int bufferRotation = 0;
    private int viewFinderRotation = 0;
    private Size bufferDimens = new Size(0, 0);
    private Size viewFinderDimens = new Size(0, 0);
    private int viewFinderDisplay = -1;
    private DisplayManager displayManager;
    private int mOESTextureId = -1;
    private Renderer mRender = new Renderer();
    private DisplayManager.DisplayListener displayListener = new DisplayManager.DisplayListener() {
        @Override
        public void onDisplayAdded(int displayId) {

        }

        @Override
        public void onDisplayRemoved(int displayId) {

        }

        @Override
        public void onDisplayChanged(int displayId) {
            TextureView viewFinder = viewFinderRef.get();
            if (displayId != viewFinderDisplay) {
                Display display = displayManager.getDisplay(displayId);
                int rotation = getDisplaySurfaceRotation(display);
                updateTransfrom(viewFinder, rotation, bufferDimens, viewFinderDimens);
            }
        }
    };

    private AutoFitPreviewBuilder(PreviewConfig config, WeakReference<TextureView> viewFinderRef) {
        this.config = config;
        this.viewFinderRef = viewFinderRef;
        init();
    }

    private void init() {
        TextureView viewFinder = viewFinderRef.get();
        viewFinderDisplay = viewFinder.getDisplay().getDisplayId();
        viewFinderRotation = getDisplaySurfaceRotation(viewFinder.getDisplay());
        useCase = new Preview(config);

        useCase.setOnPreviewOutputUpdateListener(new Preview.OnPreviewOutputUpdateListener() {
            @Override
            public void onUpdated(final Preview.PreviewOutput output) {
                final TextureView viewFinder = viewFinderRef.get();
                ViewGroup parent = (ViewGroup) viewFinder.getParent();
                parent.removeView(viewFinder);
                parent.addView(viewFinder, 0);
                viewFinder.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                    @Override
                    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                        mOESTextureId = createOESTextureObject();
                        mRender.init(viewFinder, mOESTextureId);
                        mRender.initOESTexture(output.getSurfaceTexture());
                    }

                    @Override
                    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

                    }

                    @Override
                    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                        return false;
                    }

                    @Override
                    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

                    }
                });
                bufferRotation = output.getRotationDegrees();
                int rotation = getDisplaySurfaceRotation(viewFinder.getDisplay());
                updateTransfrom(viewFinder, rotation, output.getTextureSize(), viewFinderDimens);
            }
        });

        // Every time the orientation of device changes, recompute layout
        displayManager = (DisplayManager) viewFinder.getContext().getSystemService(Context.DISPLAY_SERVICE);
        displayManager.registerDisplayListener(displayListener, null);
        viewFinder.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {

            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                displayManager.unregisterDisplayListener(displayListener);
            }
        });

    }

    private void updateTransfrom(TextureView textureView, int rotation, Size newBufferDimens, Size newViewFinderDimens) {
        if (rotation == viewFinderRotation && Objects.equals(newBufferDimens, bufferDimens) && Objects.equals(newViewFinderDimens,viewFinderDimens)) {
            return;
        }

        viewFinderRotation = rotation;
        if (newBufferDimens.getWidth() == 0 || newBufferDimens.getHeight() == 0) {
            return;
        } else {
            bufferDimens = newBufferDimens;
        }
        if (newViewFinderDimens.getHeight() == 0 || newViewFinderDimens.getWidth() == 0) {
           return;
        }else{
            viewFinderDimens = newViewFinderDimens;
        }
        Matrix matrix = new Matrix();
        float centerX = viewFinderDimens.getWidth() / 2;
        float centerY = viewFinderDimens.getHeight() / 2;
        matrix.postRotate(-viewFinderRotation, centerX, centerY);
        float bufferRatio = bufferDimens.getHeight() / bufferDimens.getWidth();
        int scaleWidth, scaledHeight;
        if (viewFinderDimens.getWidth() > viewFinderDimens.getHeight()) {
            scaledHeight = viewFinderDimens.getWidth();
            scaleWidth = Math.round(viewFinderDimens.getWidth() * bufferRatio);
        } else {
            scaledHeight = viewFinderDimens.getHeight();
            scaleWidth = Math.round(viewFinderDimens.getHeight() * bufferRatio);
        }

        float xScale = scaleWidth / viewFinderDimens.getWidth();
        float yScale = scaledHeight / viewFinderDimens.getHeight();

        matrix.preScale(xScale, yScale, centerX, centerY);
        textureView.setTransform(matrix);
    }

    public static int getDisplaySurfaceRotation(Display display) {

        switch (display.getRotation()) {
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 90;
            default:
                return 0;

        }
    }

    public static Preview build(PreviewConfig config, TextureView viewFinder){
        return new AutoFitPreviewBuilder(config,new WeakReference<>(viewFinder)).useCase;
    }

    private int createOESTextureObject() {
        int[] tex = new int[1];
        GLES20.glGenTextures(1, tex, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, tex[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        return tex[0];
    }
}
