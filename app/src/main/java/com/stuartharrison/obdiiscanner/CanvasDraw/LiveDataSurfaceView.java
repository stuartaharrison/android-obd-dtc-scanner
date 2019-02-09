package com.stuartharrison.obdiiscanner.CanvasDraw;

import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * @author Stuart Harrison
 * @author Bobby Law
 * @version 1.0
 */
public class LiveDataSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    //Variables
    private SurfaceHolder surfaceHolder;
    private int liveRPM = 0;
    LiveDataThread drawingThread = null;

    //Public Getters
    public LiveDataThread getDrawingThread() { return drawingThread; }
    public int getLiveRPM() { return liveRPM; }

    //Public Setters
    public void setLiveRPM(int value) { this.liveRPM = value; }

    /**
     * Default class constructor
     * @param context The current applications Context
     */
    public LiveDataSurfaceView(Context context) {
        super(context);
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        drawingThread = new LiveDataThread(getHolder(), this);
        setFocusable(true);
    }

    /**
     * Fired when the surface view is created. Sets the thread loop to true and starts
     * the drawing thread loop
     * @param holder The SurfaceHolder
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        drawingThread.setRunning(true);
        drawingThread.start();
    }

    /**
     * Method 'fired' when the Screen changes size.
     * @param holder The SurfaceHolder
     * @param format The Format
     * @param width The new Screen Width
     * @param height The new Screen Height
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        drawingThread.setSurfaceSize(width, height);
    }

    /**
     * Method for joining/cancelling the drawing thread class. Called when the view is being
     * destroyed
     * @param holder The SurfaceHolder
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Boolean retry = true;
        drawingThread.setRunning(false);
        while(retry) {
            try {
                drawingThread.join();
                retry = false;
            }
            catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }
}
