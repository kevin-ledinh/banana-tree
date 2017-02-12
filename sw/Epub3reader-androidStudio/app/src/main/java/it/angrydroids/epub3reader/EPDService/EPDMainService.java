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
    private final int MAX_CHARACTERS_ON_PAGE = 400;

    /* Ebook text buffer */
    private StringBuilder ChapterText;
    private int CurrentChapterTextLength;
    private int CurrentChapterReadIdx;
    private boolean lastChapterChunk;

    /* EPD file format support */
    private byte EPDPageBytes [];
    private ImageConversion imageConversion;
    private int EPDPageBytesLength;

    public int GetCurrentChapterTextLength() { return CurrentChapterTextLength; }
    public int GetRemainingChapterLength() { return ( ChapterText.length() - CurrentChapterReadIdx ); }
    public int GetEPDBytesLength() { return EPDPageBytesLength; }

    public EPDMainService() {
        ChapterText = new StringBuilder();
        CurrentChapterTextLength = 0;
        CurrentChapterReadIdx = 0;
        lastChapterChunk = false;
        imageConversion = new ImageConversion();
        EPDPageBytes = new byte[32767];
    }

    public void SetCurrentChapterText( String text ){
        CurrentChapterTextLength = text.length();
        CurrentChapterReadIdx = 0;
        lastChapterChunk = false;
        ChapterText.setLength(0);
        ChapterText.append(text);
    }

    public byte [] GetEPDPageFromCurrentPosition( boolean forwards ){
        ConvertTextToEPDPage( GetTextFromCurrentChapter( MAX_CHARACTERS_ON_PAGE, forwards ) );
        return EPDPageBytes;
    }

    public boolean IsNextPageAvailable() {
        boolean pageAvai = false;
        if( !lastChapterChunk ) {
            if( GetRemainingChapterLength() > 0 ) {
                pageAvai = true;
            }
        }
        return pageAvai;
    }

    private String GetTextFromCurrentChapter( int TextLength, boolean forwards ){
        String returnText = "";
        if( GetRemainingChapterLength() >= TextLength ) {
            returnText = ChapterText.substring(CurrentChapterReadIdx, CurrentChapterReadIdx + TextLength);
            CurrentChapterReadIdx += TextLength;
        } else {
            returnText = ChapterText.substring(CurrentChapterReadIdx, ChapterText.length());
            lastChapterChunk = true;
        }
        return returnText;
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
        EPDPageBytesLength = EPDPageBytes.length;
    }
}
