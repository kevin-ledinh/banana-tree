package com.nordicsemi.nrfUARTv2;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.*;
/**
 * Created by Phuc on 02/01/2016.
 */
public class SamplePic {
    private byte pic1 [];
    private byte pic2 [];

    private int pic1Length;
    private int pic2Length;

    public SamplePic(Context context) {
        pic1 = new byte[32767];
        pic2 = new byte[32767];
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
    public int GetPic1Size()
    {
        return pic1Length;
    }
    public int GetPic2Size()
    {
        return pic2Length;
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
        } catch (Exception ex) {
            Log.e("ERROR", ex.getMessage());
        } finally {

        }
    }
}
