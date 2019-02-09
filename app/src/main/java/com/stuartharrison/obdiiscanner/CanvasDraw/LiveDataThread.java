package com.stuartharrison.obdiiscanner.CanvasDraw;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.SurfaceHolder;

/**
 * @author Stuart Harrison
 * @author Bobby Law
 * @version 1.0
 */
public class LiveDataThread extends Thread {

    //Variables
    private int canvasWidth;
    private int canvasHeight;

    private Boolean firstRun = true; //For checking anything that needs to be done when the thread first starts
    private Boolean run  = false;

    private final SurfaceHolder holder;
    private Paint paint;
    private LiveDataSurfaceView view;

    /**
     * Default constructor for the class, setups the necessary variables to run the drawing to
     * canvas
     * @param holder The SurfaceHolder
     * @param view The SurfaceView
     */
    public LiveDataThread(SurfaceHolder holder, LiveDataSurfaceView view) {
        this.holder = holder;
        this.paint = new Paint();
        this.view = view;
    }

    /**
     * Method for handling any checks or operations to be conducted before the main thread begins
     * its loop
     */
    public void doStart() {
        synchronized(holder) {
            firstRun = false;
        }
    }

    /**
     * The main method of the class which gets looped until the run variable is changed to false.
     * Repeatedly re-draws everything onto the canvas
     */
    public void run() {
        while(run) {
            Canvas c = null; //Reset Canvas
            try {
                c = holder.lockCanvas(); //Stop anything else interfering with the draw
                synchronized (holder) {
                    svDraw(c); //Draw objects onto the canvas
                }
            }
            finally {
                if (c != null) {
                    holder.unlockCanvasAndPost(c); //Unlock canvas to allow other threads to modify it
                }
            }
        }
    }

    /**
     * Method for telling the thread whether to keep running, or to terminate
     * @param bool True is the thread is to continue running, false if not
     */
    public void setRunning(boolean bool) {
        run = bool;
    }

    /**
     * Called when the Screen size changes
     * @param width Width of the Screen that the canvas is being displayed on
     * @param height Height of the Screen that the canvas is being displayed on
     */
    public void setSurfaceSize(int width, int height) {
        synchronized (holder) {
            canvasWidth = width;
            canvasHeight = height;
            doStart();
        }
    }

    /**
     * Method for drawing all the objects onto the Canvas/Screen
     * @param canvas The object that the application will paint/draw too
     */
    private void svDraw(Canvas canvas) {
        if (run) {
            //Save and Restore previous iteration of the canvas
            canvas.save();
            canvas.restore();
            //Draw the entire canvas background colour
            canvas.drawColor(Color.WHITE);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.BLACK); //Set the 'pen' colour to black
            drawRPM(canvas); //Draw RPM
        }
    }

    /**
     * Draws the rectangles and texts which represents the current RPM value that has been
     * received from the connected cars ECU
     * @param canvas The object that the application will paint/draw too
     */
    private void drawRPM(Canvas canvas) {
        //Setup values needed
        int value = this.view.getLiveRPM();
        int reSizer = 6000 / canvasWidth;
        if (value > 0) { value = value / reSizer; }
        this.paint.setTextSize(32);
        //Create my rectangles for drawing
        Rect rect = new Rect(10, 50, canvasWidth - 10, canvasHeight / 3);
        Rect rpmRect = new Rect(10, 50, value, canvasHeight / 3);
        //Draw the text
        canvas.drawText("RPM: " + this.view.getLiveRPM(), 10, 30, this.paint);
        //Draw the background of the bar
        canvas.drawRect(rect, this.paint);
        //Change the colour of the top bar, then draw it on-top of the background bar
        paint.setColor(Color.RED);
        canvas.drawRect(rpmRect, this.paint);
    }
}
