package com.jimju.simplecamerax.fragments;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.util.TimeUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.jimju.simplecamerax.MainActivity;
import com.jimju.simplecamerax.R;
import com.jimju.simplecamerax.utils.AutoFitPreviewBuilder;
import com.jimju.simplecamerax.utils.ImageUtils;
import com.jimju.simplecamerax.utils.ViewExtensions;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.jimju.simplecamerax.MainActivity.KEY_EVENT_ACTION;
import static com.jimju.simplecamerax.MainActivity.KEY_EVENT_EXTRA;

public class CameraFragment extends Fragment {
    private final static String TAG = "CameraXBasic";
    private final static String FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS";
    private final static String PHOTO_EXTENSION = ".jpg";
    private ConstraintLayout container;
    private TextureView viewFinder;
    private File outputDirectory;

    private CameraX.LensFacing lensFacing = CameraX.LensFacing.BACK;
    private ImageCapture imageCapture;

    private BroadcastReceiver volumeDownReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int keyCode = intent.getIntExtra(KEY_EVENT_EXTRA, KeyEvent.KEYCODE_UNKNOWN);
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                ImageButton shutter = container.findViewById(R.id.camera_capture_button);
                ViewExtensions.simulateClick(shutter);
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(volumeDownReceiver);
        CameraX.unbindAll();
    }

    private void setGalleryThumbnail(File file) {
        ImageButton thumbnail = container.findViewById(R.id.photo_view_button);
        try {
            Bitmap bitmap = ImageUtils.decodeBitmap(file);
            Bitmap thumbnailBitmap = ImageUtils.cropCircularThumbnail(bitmap);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                thumbnail.setForeground(new BitmapDrawable(getResources(), thumbnailBitmap));
            } else {
                Glide.with(requireContext()).load(thumbnailBitmap).into(thumbnail);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ImageCapture.OnImageSavedListener imageSavedListener = new ImageCapture.OnImageSavedListener() {
        @Override
        public void onImageSaved(@NonNull File file) {
            // We can only change the foreground Drawable using API level 23+ API
            //我们只能改变前景图像使用API23之后的API
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Update the gallery thumbnail with latest picture taken
            //更新展示的缩略图为之后拍摄的照片
            setGalleryThumbnail(file);
//            }

            // Implicit broadcasts will be ignored for devices running API
            //隐性的广播将被忽略在设备运行的API上
            // level >= 24, so if you only target 24+ you can remove this statement
            //如果target在24以上，可以忽略
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                requireActivity().sendBroadcast(
                        new Intent(Camera.ACTION_NEW_PICTURE).setData(Uri.fromFile(file)));
            }
            String extension = ViewExtensions.extension(file);
            String mimeType = MimeTypeMap.getSingleton()
                    .getMimeTypeFromExtension(extension);
            MediaScannerConnection.scanFile(
                    requireContext(), new String[]{file.getAbsolutePath()}, new String[]{mimeType}, null);
        }

        @Override
        public void onError(@NonNull ImageCapture.UseCaseError useCaseError, @NonNull String message, @Nullable Throwable cause) {
            cause.printStackTrace();
        }
    };

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        container = (ConstraintLayout) view;
        viewFinder = container.findViewById(R.id.view_finder);
        IntentFilter filter = new IntentFilter();
        filter.addAction(KEY_EVENT_ACTION);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(volumeDownReceiver, filter);

        // Determine the output directory
        //指定输出路径
        outputDirectory = MainActivity.getOutputDirectory(requireContext());

        // Build UI and bind all camera use cases once the views have been laid out

        //构建UI并绑定所有摄像头使用的事件在控件布局好之后
        viewFinder.post(new Runnable() {
            @Override
            public void run() {
                updateCameraUi();
                bindCameraUseCases();
                File[] files = outputDirectory.listFiles();
                if (files!=null &&files.length > 0)
                    for (File file : files) {
                        if (ViewExtensions.extension(file).equals(GalleryFragment.EXTENSION_WHITELIST[0])) {
                            setGalleryThumbnail(file);
                            break;
                        }
                    }
            }
        });
    }

    private void bindCameraUseCases() {
        CameraX.unbindAll();
        DisplayMetrics metrics = new DisplayMetrics();
        viewFinder.getDisplay().getRealMetrics(metrics);
        Size screenSize = new Size(metrics.widthPixels, metrics.heightPixels);
        Rational screenAspectRatio = new Rational(metrics.widthPixels, metrics.heightPixels);
        PreviewConfig.Builder previewBuilder = new PreviewConfig.Builder();
        previewBuilder.setLensFacing(lensFacing);
        previewBuilder.setTargetResolution(screenSize);
        previewBuilder.setTargetAspectRatio(screenAspectRatio);
        previewBuilder.setTargetRotation(viewFinder.getDisplay().getRotation());
        PreviewConfig viewFinderConfig = previewBuilder.build();
        Preview preview = AutoFitPreviewBuilder.build(viewFinderConfig, viewFinder);

        ImageCaptureConfig.Builder imageCaptureBuilder = new ImageCaptureConfig.Builder();
        imageCaptureBuilder.setLensFacing(lensFacing);
        imageCaptureBuilder.setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY);
        imageCaptureBuilder.setTargetAspectRatio(screenAspectRatio);
        imageCaptureBuilder.setTargetRotation(viewFinder.getDisplay().getRotation());

        imageCapture = new ImageCapture(imageCaptureBuilder.build());

        ImageAnalysisConfig.Builder analysisBuilder = new ImageAnalysisConfig.Builder();
        analysisBuilder.setLensFacing(lensFacing);
        HandlerThread analyzerThread = new HandlerThread("luminosityAnalysis");
        analyzerThread.start();
        analysisBuilder.setCallbackHandler(new Handler(analyzerThread.getLooper()));
        analysisBuilder.setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE);
        ImageAnalysisConfig analyzerConfig = analysisBuilder.build();

        ImageAnalysis imageAnalysis = new ImageAnalysis(analyzerConfig);
        LuminosityAnalyzer luminosityAnalyzer = new LuminosityAnalyzer();
        luminosityAnalyzer.onFrameAnalyzed(new LuminosityAnalyzerListener() {
            @Override
            public void luminosionCallback(double luma) {
//                Log.d(TAG, String.format("Average luminosity: %d.Frames per second.", luma));
            }
        });
        imageAnalysis.setAnalyzer(luminosityAnalyzer);
        CameraX.bindToLifecycle(this, preview, imageCapture, imageAnalysis);
    }

    private void updateCameraUi() {
        ConstraintLayout constraintLayout = container.findViewById(R.id.camera_ui_container);
        container.removeView(constraintLayout);
        View controls = View.inflate(requireContext(), R.layout.camera_ui_container, container);
        controls.findViewById(R.id.camera_capture_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File photoFile = createFile(outputDirectory, FILENAME, PHOTO_EXTENSION);
                ImageCapture.Metadata metadata = new ImageCapture.Metadata();
                metadata.isReversedHorizontal = lensFacing == CameraX.LensFacing.FRONT;
                imageCapture.takePicture(photoFile, imageSavedListener, metadata);

              /*  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    container.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            container.setForeground(new ColorDrawable(Color.WHITE));
                            container.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    container.setForeground(null);
                                }
                            },ViewExtensions.ANIMATION_SLOW_MILLIS);
                        }
                    }, ViewExtensions.ANIMATION_SLOW_MILLIS);
                }*/
            }
        });
        controls.findViewById(R.id.camera_switch_button).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View v) {
                lensFacing = CameraX.LensFacing.FRONT == lensFacing ? CameraX.LensFacing.BACK : CameraX.LensFacing.FRONT;
                try {
                    CameraX.getCameraWithLensFacing(lensFacing);
                    bindCameraUseCases();
                } catch (Exception exc) {

                }
            }
        });

        controls.findViewById(R.id.photo_view_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle arguments = new Bundle();
                arguments.putString(GalleryFragment.KEY_ROOT_DIRECTORY, outputDirectory.getAbsolutePath());
                Navigation.findNavController(requireActivity(), R.id.fragment_container).navigate(R.id.action_camera_to_gallery, arguments);
            }
        });
    }


    /**
     * Our custom image analysis class.
     *
     * <p>All we need to do is override the function `analyze` with our desired operations. Here,
     * we compute the average luminosity of the image by looking at the Y plane of the YUV frame.
     * <p>
     * 我们需要做的就是用我们想要的操作覆盖函数“analyze”。在这里，我们通过观察YUV帧的Y平面来计算图像的平均亮度。
     */
    private class LuminosityAnalyzer implements ImageAnalysis.Analyzer {
        private int frameRateWindow = 8;
        private ArrayDeque<Long> frameTimestamps = new ArrayDeque(5);
        private ArrayList<LuminosityAnalyzerListener> listeners = new ArrayList();
        private long lastAnalyzedtimestamp = 0;
        public double framesPerSecond = -1.0;

        public void onFrameAnalyzed(LuminosityAnalyzerListener listener) {
            listeners.add(listener);
        }

        @Override
        public void analyze(ImageProxy image, int rotationDegrees) {
            if (listeners.isEmpty())
                return;
            frameTimestamps.push(System.currentTimeMillis());
            while (frameTimestamps.size() >= frameRateWindow) {
                frameTimestamps.removeLast();
            }
            framesPerSecond = 1.0 / ((frameTimestamps.peekFirst() - frameTimestamps.peekLast()) / frameTimestamps.size()) * 1000;
            if (frameTimestamps.peekFirst() - lastAnalyzedtimestamp >= TimeUnit.SECONDS.toMillis(1)) {
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] data = toByteArray(buffer);
                List<Integer> pixels = new ArrayList<>();
                double lumaCount = 0;
                for (int i = 0; i < data.length; i++) {
                    int cache = ((int) data[i]) | 0xFF;
                    pixels.add(cache);
                    lumaCount += cache;
                }
                double luma = lumaCount / pixels.size();
                for (LuminosityAnalyzerListener listener : listeners) {
                    listener.luminosionCallback(luma);
                }
                lastAnalyzedtimestamp = frameTimestamps.getFirst();

            }
        }

        private byte[] toByteArray(ByteBuffer buffer) {
            buffer.rewind();
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);
            return data;
        }

    }

    private interface LuminosityAnalyzerListener {
        void luminosionCallback(double luma);
    }

    /**
     * Helper function used to create a timestamped file
     */
    private File createFile(File baseFolder, String format, String extension) {
        return new File(baseFolder, new SimpleDateFormat(format, Locale.US).format(System.currentTimeMillis()) + extension);
    }
}
