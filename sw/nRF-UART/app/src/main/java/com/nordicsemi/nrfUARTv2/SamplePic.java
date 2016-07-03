package com.nordicsemi.nrfUARTv2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

    private int pic1Length;
    private int pic2Length;
    private int pic3Length;

    public SamplePic(Context context) {
        pic1 = new byte[32767];
        pic2 = new byte[32767];
        pic3 = new byte[32767];
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

            ImageConversion imageConversion = new ImageConversion();
            Bitmap originalImg = BitmapFactory.decodeResource(context.getResources(), R.raw.lou);

            pic3 = imageConversion.run(originalImg);
            pic3Length = pic3.length;

        } catch (Exception ex) {
            Log.e("ERROR", ex.getMessage());
        } finally {

        }
    }
}
