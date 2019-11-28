package com.jimju.simplecamerax.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.media.ThumbnailUtils;

import androidx.camera.core.Exif;
import androidx.camera.core.ImageProxy;
import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ImageUtils {

    /**
     * Helper function used to convert an EXIF orientation enum into a transformation matrix that can be applied to a bitmap.
     * Helper函数，用于将EXIF方向枚举转换为可应用于位图的转换矩阵。
     *
     * @param orientation
     * @return
     */
    private static Matrix decodeExifOrientation(int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                break;
            case ExifInterface.ORIENTATION_UNDEFINED:
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(270);
                break;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.postScale(-1f, 1f);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.postScale(1f, -1f);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.postScale(-1f, 1f);
                matrix.postRotate(270);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.postScale(-1f, 1f);
                matrix.postRotate(90);
                break;
            default:
                throw new IllegalArgumentException("Invalid orientation :  " + orientation);
        }
        return matrix;
    }


    /**
     * Decode a bitmap from a file and apply the transformations described in its EXIF data
     * 从文件中解码位图并应用exif数据的转换
     *
     * @param file - The image file to be read using [BitmapFactory.decodeFile]
     */
    public static Bitmap decodeBitmap(File file) throws IOException {
        // First, decode EXIF data and retrieve transformation matrix
        //首先解码EXIF数据，并且获取变化的像素矩阵
        ExifInterface exif = new ExifInterface(file.getAbsolutePath());
        Matrix transformtion = decodeExifOrientation(exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_ROTATE_90));

        // Read bitmap using factory methods, and transform it using EXIF data
        //用factory中的方法读取Bitma并转换它的图像信息
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        return Bitmap.createBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()), 0, 0, bitmap.getWidth(), bitmap.getHeight(), transformtion, true);
    }

    public static Bitmap decodeBitmap(ImageProxy image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.capacity()];
        buffer.get(bytes);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    /**
     * This function cuts out a circular thumbnail from the provided bitmap. This is done by
     * first scaling the image down to a square with width of [diameter], and then marking all
     * pixels outside of the inner circle as transparent.
     * 此函数用于从提供的位图中剪切圆形缩略图。这是通过先将图像缩小到宽度为[直径]的正方形，然后将内圈外部的所有像素标记为透明来完成的。
     *
     * @param bitmap
     * @param diameter
     * @return
     */
    public static Bitmap cropCircularThumbnail(Bitmap bitmap, int diameter) {
        // Extract a much smaller bitmap to serve as thumbnail
        //提取更小的位图作为缩略图
        Bitmap thumbnail = ThumbnailUtils.extractThumbnail(bitmap, diameter, diameter);

        // Create an additional bitmap of same size as thumbnail to carve a circle out of
        //创建另一个与缩略图大小相同的位图，以便从中划出一个圆
        Bitmap circular = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888);

        // Paint will be used as a mask to cut out the circle
        //用填充工具作遮罩，用来剪出圆圈
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);

        Canvas canvas = new Canvas(circular);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(diameter / 2, diameter / 2, diameter / 2 - 8, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        Rect rect = new Rect(0,0,diameter,diameter);;
        canvas.drawBitmap(thumbnail,rect,rect,paint);
        return circular;
    }

    public static Bitmap cropCircularThumbnail(Bitmap bitmap){
        return cropCircularThumbnail(bitmap,128);
    }

}
