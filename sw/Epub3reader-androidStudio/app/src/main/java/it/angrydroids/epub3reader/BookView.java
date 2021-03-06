/*
The MIT License (MIT)

Copyright (c) 2013, V. Giacometti, M. Giuriato, B. Petrantuono

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package it.angrydroids.epub3reader;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import it.angrydroids.epub3reader.EPDService.EPDMainService;

// Panel specialized in visualizing EPUB pages
public class BookView extends SplitPanel {
    private final String TAG = this.getClass().getSimpleName();

	public ViewStateEnum state = ViewStateEnum.books;
	protected String viewedPage;
	protected WebView view;
	protected float swipeOriginX, swipeOriginY;
    protected Button PreButton;
    protected Button TopButton;
    protected Button FwdButton;


	
	@Override
	public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState)	{
		super.onCreateView(inflater, container, savedInstanceState);
		View v = inflater.inflate(R.layout.activity_book_view, container, false);
		return v;
	}
	
	@Override
    public void onActivityCreated(Bundle saved) {
		super.onActivityCreated(saved);
		view = (WebView) getView().findViewById(R.id.Viewport);
		
		// enable JavaScript for cool things to happen!
		view.getSettings().setJavaScriptEnabled(true);
		
		// ----- SWIPE PAGE
		view.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
	
				if (state == ViewStateEnum.books)
					swipePage(v, event, 0);
								
				WebView view = (WebView) v;
				return view.onTouchEvent(event);
			}
		});

		// ----- NOTE & LINK
		view.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
					Message msg = new Message();
					msg.setTarget(new Handler() {
						@Override
						public void handleMessage(Message msg) {
							super.handleMessage(msg);
							String url = msg.getData().getString(
									getString(R.string.url));
							if (url != null)
								navigator.setNote(url, index);
						}
					});
					view.requestFocusNodeHref(msg);
				
				return false;
			}
		});
		
		view.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				try {
					navigator.setBookPage(url, index);
				} catch (Exception e) {
					errorMessage(getString(R.string.error_LoadPage));
				}
				return true;
			}
		});

		view.addJavascriptInterface(new MyJavaScriptInterface(), "INTERFACE");
		view.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url)
			{
				view.loadUrl("javascript:window.INTERFACE.processContent(document.getElementsByTagName('body')[0].innerText);");
			}
		});

        PreButton = (Button) getView().findViewById(R.id.btn_prev_chunk);
        TopButton = (Button) getView().findViewById(R.id.btn_display_from_top);
        FwdButton = (Button) getView().findViewById(R.id.btn_fwd_chunk);

        PreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BtnPrevChunkOnClickListner(v);
            }
        });

        TopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BtnDisplayTopOnClickListner(v);
            }
        });

        FwdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BtnFwdChunkOnClickListner(v);
            }
        });
		loadPage(viewedPage);
	}
	
	public void loadPage(String path)
	{
		viewedPage = path;
		if(created)
			view.loadUrl(path);
	}
	
	// Change page
	protected void swipePage(View v, MotionEvent event, int book) {
		int action = MotionEventCompat.getActionMasked(event);

		switch (action) {
		case (MotionEvent.ACTION_DOWN):
			swipeOriginX = event.getX();
			swipeOriginY = event.getY();
			break;

		case (MotionEvent.ACTION_UP):
			int quarterWidth = (int) (screenWidth * 0.25);
			float diffX = swipeOriginX - event.getX();
			float diffY = swipeOriginY - event.getY();
			float absDiffX = Math.abs(diffX);
			float absDiffY = Math.abs(diffY);

			if ((diffX > quarterWidth) && (absDiffX > absDiffY)) {
				try {
					navigator.goToNextChapter(index);
				} catch (Exception e) {
					errorMessage(getString(R.string.error_cannotTurnPage));
				}
			} else if ((diffX < -quarterWidth) && (absDiffX > absDiffY)) {
				try {
					navigator.goToPrevChapter(index);
				} catch (Exception e) {
					errorMessage(getString(R.string.error_cannotTurnPage));
				}
			}
			break;
		}

	}
	
	@Override
	public void saveState(Editor editor) {
		super.saveState(editor);
		editor.putString("state"+index, state.name());
		editor.putString("page"+index, viewedPage);
	}
	
	@Override
	public void loadState(SharedPreferences preferences)
	{
		super.loadState(preferences);
		loadPage(preferences.getString("page"+index, ""));
		state = ViewStateEnum.valueOf(preferences.getString("state"+index, ViewStateEnum.books.name()));
	}

    public void BtnPrevChunkOnClickListner(View v)
    {
        Log.d(TAG, "Prev Chunk Button Click");
		// next, notify the navigator that the prev chunk is required
    }

    public void BtnDisplayTopOnClickListner(View v)
    {
        Log.d(TAG, "Display Top Button Click");
		// next, notify the navigator that the top of the chapter is required
    }

    public void BtnFwdChunkOnClickListner(View v)
    {
        Log.d(TAG, "Fwd Chunk Button Click");
		// next, notify the navigator that the next chunk is required
    }

	/* An instance of this class will be registered as a JavaScript interface */
	class MyJavaScriptInterface
	{
		@JavascriptInterface
		public void processContent(String aContent)
		{
            if( aContent.equals("") ) {
                return;
            }

            try {
                Bundle newChapter = new Bundle();
                newChapter.putString( EPDMainService.MSG_NEW_CHAPTER_TEXT, aContent );
                Message msg = Message.obtain( null, EPDMainService.MSG_NEW_CHAPTER_AVAILABLE );
                msg.setData(newChapter);
                msg.replyTo = ((MainActivity) getActivity()).mMainActivityMessenger;
                if (((MainActivity) getActivity()).mEPDMainService != null) {
                    ((MainActivity) getActivity()).mEPDMainService.send(msg);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
		}
	}
}
