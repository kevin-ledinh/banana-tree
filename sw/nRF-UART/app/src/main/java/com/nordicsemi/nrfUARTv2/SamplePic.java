package com.nordicsemi.nrfUARTv2;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Environment;
import android.util.Log;

import java.io.*;
/**
 * Created by Phuc on 02/01/2016.
 */
public class SamplePic {
    private byte pic1 [];
    private byte pic2 [];
    private byte pic3 [];
    private byte pic4 [];

    private int pic1Length;
    private int pic2Length;
    private int pic3Length;
    private int pic4Length;

    private ImageConversion imageConversion;

    public SamplePic(Context context) {
        pic1 = new byte[32767];
        pic2 = new byte[32767];
        pic3 = new byte[32767];
        pic4 = new byte[32767];
        imageConversion = new ImageConversion();
        ReadSamplePics(context);
    }

    public byte [] GetPic1()
    {
        return pic1;
    }
    public byte [] GetPic2()
    {
        return pic2;
    }
    public byte [] GetPic3()
    {
        return pic3;
    }
    public byte [] GetPic4()
    {
        return pic4;
    }
    public int GetPic1Size()
    {
        return pic1Length;
    }
    public int GetPic2Size()
    {
        return pic2Length;
    }

    public int GetPic3Size()
    {
        return pic3Length;
    }
    public int GetPic4Size()
    {
        return pic4Length;
    }

    private void ReadSamplePics(Context context) {
        try {
            InputStream fin = context.getResources().openRawResource(R.raw.sample1);
            if(fin != null) {
                pic1Length = fin.read(pic1);
            }
            fin = context.getResources().openRawResource(R.raw.sample2);
            if(fin != null) {
                pic2Length = fin.read(pic2);
            }
            fin.close();

            Bitmap originalImg = BitmapFactory.decodeResource(context.getResources(), R.raw.lou);

            pic3 = imageConversion.run(originalImg);
            pic3Length = pic3.length;

            ReadSampleText(context);

        } catch (Exception ex) {
            Log.e("ERROR", ex.getMessage());
        } finally {

        }
    }

    private void ReadSampleText(Context context) {
        try {
            String gText = "Hello World";
            Resources resources = context.getResources();
            float scale = resources.getDisplayMetrics().density;

            Bitmap bmpGrayscale = Bitmap.createBitmap(400, 300, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(bmpGrayscale);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            ColorMatrix cm = new ColorMatrix();
            cm.setSaturation(0);
            ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
            paint.setColorFilter(f);
            c.drawBitmap(bmpGrayscale, 0, 0, paint);

            // text color - #3D3D3D
            paint.setColor(Color.rgb(61, 61, 61));
            // text size in pixels
            paint.setTextSize((int) (14 * scale));
            // text shadow
            paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);

            // draw text to the Canvas center
            Rect bounds = new Rect();
            paint.getTextBounds(gText, 0, gText.length(), bounds);
            int x = (bmpGrayscale.getWidth() - bounds.width()) / 2;
            int y = (bmpGrayscale.getHeight() + bounds.height()) / 2;

            c.drawText(gText, x, y, paint);

            pic4 = imageConversion.run(bmpGrayscale);
            pic4Length = pic4.length;

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
