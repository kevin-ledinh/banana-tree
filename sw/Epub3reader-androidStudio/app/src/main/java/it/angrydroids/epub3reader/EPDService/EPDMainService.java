package it.angrydroids.epub3reader.EPDService;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.Paint;
import android.graphics.Rect;

import java.util.List;

/**
 * Created by kevin on 02/11/2016.
 */
public class EPDMainService {
    private final String TAG = this.getClass().getSimpleName();
    private final int MAX_CHARACTERS_ON_PAGE = 150;

    /* Ebook text buffer */
    private StringBuilder ChapterText;
    private int CurrentChapterTextLength;

    /* EPD file format support */
    private byte EPDPageBytes [];
    private ImageConversion imageConversion;
    private int EPDPageBytesLength;

    public int GetCurrentChapterTextLength() { return CurrentChapterTextLength; }
    public int GetRemainingChapterLength() { return ChapterText.length(); }
    public int GetEPDBytesLength() { return EPDPageBytesLength; }

    public EPDMainService() {
        ChapterText = new StringBuilder();
        CurrentChapterTextLength = 0;
        imageConversion = new ImageConversion();
        EPDPageBytes = new byte[32767];
    }

    public void SetCurrentChapterText( String text ){
        CurrentChapterTextLength = text.length();
        ChapterText.setLength(0);
        ChapterText.append(text);
    }

    public byte [] GetEPDPageFromCurrentPosition(){
        ConvertTextToEPDPage( GetTextFromCurrentChapter( MAX_CHARACTERS_ON_PAGE ) );
        return EPDPageBytes;
    }

    private String GetTextFromCurrentChapter( int TextLength ){
        String returnText = "";
        if( CurrentChapterTextLength != 0 )
        {
            if( ChapterText.length() >= TextLength ) {
                returnText = ChapterText.substring(0, TextLength);
                ChapterText.replace(0, TextLength, "");
            } else {
                returnText = ChapterText.toString();
            }
        }
        return returnText;
    }

    private void ConvertTextToEPDPage(String text){
        Bitmap bmpGrayscale = Bitmap.createBitmap(400, 300, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ColorMatrix cm = new ColorMatrix();

        c.drawBitmap(bmpGrayscale, 0, 0, paint);
        c.drawColor(Color.WHITE);

        // text color - #3D3D3D
        paint.setColor(Color.BLACK);
        // text size in pixels
        paint.setTextSize(14);

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
        EPDPageBytesLength = EPDPageBytes.length;
    }
}
