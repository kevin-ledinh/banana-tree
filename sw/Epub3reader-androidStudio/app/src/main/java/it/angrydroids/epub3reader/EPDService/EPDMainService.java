package it.angrydroids.epub3reader.EPDService;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kevin on 02/11/2016.
 */
public class EPDMainService extends Service {
    private final String TAG = this.getClass().getSimpleName();
    private final int MAX_CHARACTERS_ON_PAGE = 450;

    public static final int MSG_NEW_CHAPTER_AVAILABLE = 1;
    public static final int MSG_NEXT_CHAPTER_CHUNK_REQ = 2;
    public static final int MSG_PREV_CHAPTER_CHUNK_REQ = 3;
    public static final int MSG_CHAPTER_CHUNK_AVAILABLE = 4;
    public static final int MSG_RESEND_CHAPTER_CHUNK = 5;
    public static final int MSG_REGISTER_CLIENT = 6;

    public static final String MSG_NEW_CHAPTER_TEXT = "it.angrydroids.epub3reader.EPDService.MSG_NEW_CHAPTER_TEXT";
    public static final String MSG_BLE_DATA_AVAILABLE = "it.angrydroids.epub3reader.EPDService.MSG_BLE_DATA_AVAILABLE";

    // Variables for Android Service
    private final Messenger mEPDMainServiceMessenger = new Messenger(new IncomingHandler());
    private Messenger mEPDClient;

    /* Ebook text buffer */
    private List<String> ChapterTextList = new ArrayList<String>();
    private int CurrentChapterReadIdx = 0;

    /* EPD file format support */
    private byte EPDPageBytes [] = new byte[32767];
    private ImageConversion imageConversion = new ImageConversion();

    /**************************************************
     * Start of code to handle Android Service
     *
     **************************************************/
    @Override
    public IBinder onBind(Intent intent) {
        return mEPDMainServiceMessenger.getBinder();
    }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_NEW_CHAPTER_AVAILABLE:
                    SetCurrentChapterText( msg.getData().getString( MSG_NEW_CHAPTER_TEXT ) );
                    SendMessageToBLEDevice( MSG_CHAPTER_CHUNK_AVAILABLE , MSG_BLE_DATA_AVAILABLE , GetEPDPageFromCurrentPosition() , mEPDClient );
                    break;
                case MSG_NEXT_CHAPTER_CHUNK_REQ:
                    if( IsNextPageAvailable( true ) ) {
                        SendMessageToBLEDevice( MSG_CHAPTER_CHUNK_AVAILABLE , MSG_BLE_DATA_AVAILABLE , GetEPDPageFromCurrentPosition() , mEPDClient );
                    } else {
                        // TODO: Handle empty data here
                    }
                    break;
                case MSG_PREV_CHAPTER_CHUNK_REQ:
                    if( IsNextPageAvailable( false ) ) {
                        SendMessageToBLEDevice( MSG_CHAPTER_CHUNK_AVAILABLE , MSG_BLE_DATA_AVAILABLE , GetEPDPageFromCurrentPosition() , mEPDClient );
                    } else {
                        // TODO: Handle empty data here
                    }
                    break;
                case MSG_RESEND_CHAPTER_CHUNK:
                    SendMessageToBLEDevice( MSG_CHAPTER_CHUNK_AVAILABLE , MSG_BLE_DATA_AVAILABLE , GetEPDPageFromCurrentPosition() , mEPDClient );
                    break;
                case MSG_REGISTER_CLIENT:
                    mEPDClient = msg.replyTo;
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
    /**************************************************
     * End of code to handle Android Service
     *
     **************************************************/

    private void SetCurrentChapterText( String text ){
        int index = 0;
        CurrentChapterReadIdx = 0;

        ChapterTextList.clear();
        while (index < text.length()) {
            ChapterTextList.add(text.substring(index, Math.min(index + MAX_CHARACTERS_ON_PAGE,text.length())));
            index += MAX_CHARACTERS_ON_PAGE;
        }
    }

    private byte [] GetEPDPageFromCurrentPosition(){
        ConvertTextToEPDPage( ChapterTextList.get(CurrentChapterReadIdx) );
        return EPDPageBytes;
    }

    private boolean IsNextPageAvailable( boolean forwards ) {
        boolean pageAvai = false;
        int NextChapterIdx = CurrentChapterReadIdx;
        if( forwards ) {
            NextChapterIdx++;
            if ( NextChapterIdx < ChapterTextList.size() ) {
                pageAvai = true;
                CurrentChapterReadIdx = NextChapterIdx;
            }
        } else {
            NextChapterIdx--;
            if( NextChapterIdx >= 0 ) {
                pageAvai = true;
                CurrentChapterReadIdx = NextChapterIdx;
            }
        }
        return pageAvai;
    }

    private void ConvertTextToEPDPage(String text){
        if(text == ""){
            return;
        }

        Bitmap bmpGrayscale = Bitmap.createBitmap(400, 300, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ColorMatrix cm = new ColorMatrix();

        c.drawBitmap(bmpGrayscale, 0, 0, paint);
        c.drawColor(Color.WHITE);

        // text color - #3D3D3D
        paint.setColor(Color.BLACK);
        // text size in pixels
        paint.setTextSize(16);

        // draw text to the Canvas center
        Rect bounds = new Rect();

        List<String> stringList = WrapString.wrap(text, paint, 400 - 30 );

        int i = 0;
        for (String s:stringList) {
            paint.getTextBounds(s, 0, s.length(), bounds);
            c.drawText(s, 10 , 20 + 20 * i, paint);
            i++;
        }
        EPDPageBytes = imageConversion.run(bmpGrayscale);
    }
    private Message SendMessageToBLEDevice(int what, String key , byte [] data, Messenger mClient ){
        Message msg = Message.obtain( null, what );
        try {
            Bundle MsgContent = new Bundle();
            MsgContent.putByteArray( key , data );
            msg.setData(MsgContent);
            mClient.send( msg );
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return msg;
    }
}
