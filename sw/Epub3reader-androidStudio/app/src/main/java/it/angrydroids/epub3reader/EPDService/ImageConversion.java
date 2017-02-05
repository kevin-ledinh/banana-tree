package it.angrydroids.epub3reader.EPDService;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by kevin on 29/06/2016.
 */
public class ImageConversion {
    private final String TAG = ImageConversion.class.getSimpleName();
    byte [] imageHeader = { 0x33, 0x01, (byte)0x90, 0x01, 0x2C, 0x01, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };

    int Wp, Hp;
    public byte [] run(Bitmap image) {
        Wp = 400;
        Hp = 300;
        byte[] fullImageData = new byte[15016];
        try {

            Bitmap img = resizeImage(image, Wp, Hp);

            // Image conversion code snippet:
            // Convert to gray
            img = ConvertTools.downsampleTo8bitGrayScale(img);

            int[] rawIntPixelData = ConvertTools.toIntArray(img);

            rawIntPixelData = ConvertTools.downsampleTo1bitGrayScale(rawIntPixelData);

            // convert to byte array
            byte[] rawBytePixelData = ConvertTools.toByteArray(rawIntPixelData);
            byte[] convertedBytePixelData = ConvertTools.convertTo1bit_PixelFormatType2(rawBytePixelData, Wp, Hp);
            fullImageData = combine(imageHeader, convertedBytePixelData);

//        Path p = Paths.get("./testpicture.epd");

//        try (OutputStream out = new BufferedOutputStream(
//                Files.newOutputStream(p, CREATE, TRUNCATE_EXISTING ))) {
//            out.write(fullImageData, 0, fullImageData.length);
//            out.close();
//        } catch (IOException x) {
//            System.err.println(x);
//        }

            Log.d(TAG, "Image Header Length: " + imageHeader.length);
            Log.d(TAG, "rawBytePixelData Length: " + rawBytePixelData.length);
            Log.d(TAG, "convertedBytePixelData Length: " + convertedBytePixelData.length);
            Log.d(TAG, "fullImageData Length: " + fullImageData.length);
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
            ex.printStackTrace();
        }
        return fullImageData;
    }

    public byte [] DrawTextOnBitmap(String text) {
        byte[] fullImageData = new byte[15016];


        return fullImageData;
    }

    private static byte[] combine(byte[] a, byte[] b){
        int length = a.length + b.length;
        byte[] result = new byte[length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
    private static Bitmap resizeImage(Bitmap originalImage, int newWidth, int newHeight){
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(originalImage, 0, 0, width, height, matrix, false);
        originalImage.recycle();
        return resizedBitmap;
    }
}
