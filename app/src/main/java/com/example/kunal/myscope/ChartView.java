package com.example.kunal.myscope;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by kmt911046 on 10/10/2017.
 */

public class ChartView extends SurfaceView {

    private SurfaceHolder holder;
    private int chartWidth;
    private int chartHeight;
    private boolean isSurfaceActive;

    private float[] chartPoints;
    private int pointCounter = 0;
    private int maxChartPoint = 2000;
    private int bottomOffset = 80;
    private int leftOffset = 80;
    private float pointMaxX;
    private float minY, maxY;


    public ChartView(Context context) {
        super(context);
    }

    public ChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void addPoint(int data){
        if(pointCounter == maxChartPoint){
            pointCounter = 0;
        }
        chartPoints[pointCounter++] = data;
    }

    public boolean isSurfaceActive() {
        return isSurfaceActive;
    }

    public void init(){
        chartPoints = new float[maxChartPoint];
        pointMaxX = chartWidth - leftOffset;
        minY = -0.5f;
        maxY = 5.5f;

        holder = getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                isSurfaceActive = true;
            }
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                chartWidth = width;
                chartHeight = height;
            }
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                isSurfaceActive = false;
            }
        });

    }


}
