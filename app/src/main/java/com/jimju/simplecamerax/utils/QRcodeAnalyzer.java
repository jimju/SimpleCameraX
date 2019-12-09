package com.jimju.simplecamerax.utils;

import android.content.Context;
import android.graphics.ImageFormat;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.nio.ByteBuffer;
import java.util.EnumMap;
import java.util.Vector;

public class QRcodeAnalyzer implements ImageAnalysis.Analyzer {
    private MultiFormatReader reader = new MultiFormatReader();
    Handler handler;

    public QRcodeAnalyzer(Handler handler) {
        this.handler = handler;
        EnumMap hints = new EnumMap<DecodeHintType, Object>(DecodeHintType.class);
        hints.put(DecodeHintType.CHARACTER_SET, "utf-8");
        hints.put(DecodeHintType.TRY_HARDER, true);
        Vector decodeFormats = new Vector();
        decodeFormats.add(BarcodeFormat.QR_CODE);
        decodeFormats.add(BarcodeFormat.CODE_128);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
        reader.setHints(hints);

    }

    @Override
    public void analyze(ImageProxy image, int rotationDegrees) {
        if (ImageFormat.YUV_420_888 != image.getFormat()) {
            Log.e("BarcodeAnalyzer", "expect YUV_420_888, now = ${image.format}");
            return;
        }
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] data = new byte[buffer.remaining()];
        int height = image.getHeight();
        int width = image.getWidth();
        buffer.get(data);
        PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(data, width, height, 0, 0, width, height, false);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        try {
            Result result = reader.decodeWithState(bitmap);
            Message msg = new Message();
            msg.what = 1;
            msg.obj = result.getText();
            handler.sendMessage(msg);
        } catch (NotFoundException e) {
            Message msg = new Message();
            msg.what = 2;
            msg.obj = e.getMessage();
            e.printStackTrace();
        }finally {
            reader.reset();
        }
    }
}
