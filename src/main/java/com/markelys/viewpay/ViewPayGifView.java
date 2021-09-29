package com.markelys.viewpay;

/**
 * Created by Herbert TOMBO on 22/02/2018.
 */

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.util.AttributeSet;
import android.view.View;


class ViewPayGifView extends View {

    private InputStream gifInputStream;
    private Movie gifMovie;
    private int movieWidth, movieHeight;
    private long movieDuration;
    private long movieRunDuration;
    private long lastTick;
    private long nowTick;

    private boolean repeat = true;
    private boolean running = true;

    private int scale = 2;

    public void setRepeat(boolean r) {
        repeat = r;
    }

    public void setRunning(boolean r) {
        running = r;
    }

    public ViewPayGifView(Context context) {
        super(context);
        init(context);
    }

    public ViewPayGifView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ViewPayGifView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {

        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        setFocusable(true);

        try {
            gifInputStream = new URL(ViewPayConstants.VP_LOADING_URL).openStream();
        }catch (IOException e){
            gifInputStream = context.getResources().openRawResource(+R.drawable.loading5);
        }

        gifMovie = Movie.decodeStream(gifInputStream);
        movieWidth = gifMovie.width();
        movieHeight = gifMovie.height();
        movieDuration = gifMovie.duration();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(scale*movieWidth, scale*movieHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if(gifMovie == null){
            return;
        }

        nowTick = android.os.SystemClock.uptimeMillis();
        if (lastTick == 0) {
            movieRunDuration = 0;
        }else{
            if(running){
                movieRunDuration += nowTick-lastTick;
                if(movieRunDuration > movieDuration){
                    if(repeat){
                        movieRunDuration = 0;
                    }else{
                        movieRunDuration = movieDuration;
                    }
                }
            }
        }

        gifMovie.setTime((int)movieRunDuration);

        canvas.scale(scale, scale);
        gifMovie.draw(canvas, 0, 0);

        lastTick = nowTick;
        invalidate();

    }
}
