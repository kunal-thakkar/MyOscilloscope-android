package com.example.kunal.myscope;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by kmt911046 on 10/10/2017.
 */

public class ChartView extends SurfaceView {

    private static final String TAG = "ChartView";
    private static final int LINE_COLOR = 0xff00ff00;

    private SurfaceHolder holder;
    private int chartWidth;
    private int chartHeight;
    private boolean isSurfaceActive;

    private int maxChartPoint = 0;
    private float[] yPoints;
    private float[] xPoints;
    private float pointMaxX;
    private float pointMaxY;
    private int pointCounter = 0;

    private final int refreshInterval = 100;
    private final int timeDiffBetweenPoints = 10;
    private final int xAxisInterval = 100;

    private final int bottomOffset = 80;
    private final int leftOffset = 80;
    private final float minY = 0.5f, maxY = 5.5f, dX = maxChartPoint, dY = maxY - minY;

    private Thread plotterThread;

    public ChartView(Context context) {
        super(context);
    }

    public ChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void addPoint(float data){
        data = (pointMaxY - ((data - minY) / dY) * pointMaxY);
        if(pointCounter == maxChartPoint){
            pointCounter = 0;
        }
        yPoints[pointCounter++] = data;
    }

    public boolean isSurfaceActive() {
        return isSurfaceActive;
    }

    public void init(){
        holder = getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                isSurfaceActive = true;
                plotterThread = new Thread(new ChartPlotter());
                plotterThread.start();
            }
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                chartWidth = width;
                chartHeight = height;
                pointMaxX = chartWidth - leftOffset;
                pointMaxY = chartHeight - bottomOffset;
                maxChartPoint = (int) pointMaxX / timeDiffBetweenPoints;
                xPoints = new float[maxChartPoint];
                yPoints = new float[maxChartPoint];
                for(int i = 0; i < maxChartPoint; i++){
                    xPoints[i] = leftOffset + (i/dX) * pointMaxX;
                }
            }
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                isSurfaceActive = false;
                plotterThread.interrupt();
                plotterThread = null;
            }
        });
    }

    class ChartPlotter implements Runnable{
        @Override
        public void run() {
            Paint mLinePaint = new Paint();
            mLinePaint.setAntiAlias(true);
            mLinePaint.setStyle(Paint.Style.STROKE);
            mLinePaint.setStrokeWidth(2);
            mLinePaint.setColor(LINE_COLOR);

            Paint mTextPaint = new TextPaint();
            mTextPaint.setAntiAlias(true);
            mTextPaint.setTextAlign(Paint.Align.LEFT);
            mTextPaint.setTextSize(26);

            Paint mAxisPaint = new Paint();
            mAxisPaint.setAntiAlias(true);
            mAxisPaint.setStyle(Paint.Style.STROKE);
            mAxisPaint.setStrokeWidth(2);
            mAxisPaint.setColor(Color.BLACK);
            Canvas canvas;
            while (isSurfaceActive && !Thread.interrupted()){
                canvas = holder.lockCanvas(null);
                if(canvas == null) { continue; }
                try {
                    canvas.drawColor(0xFFFFFFFF, PorterDuff.Mode.CLEAR);
                    for (int y = pointCounter+2, x = 1; y < maxChartPoint; y++, x++) {
                        canvas.drawLine(xPoints[x - 1], yPoints[y - 1],
                                xPoints[x], yPoints[y], mLinePaint);
                        //canvas.drawRect(x, y, x + 20.0f, y + 20.0f, paint);
                    }
                    mTextPaint.setTextAlign(Paint.Align.LEFT);
                    for (int i = 1; i < 5; i++) {
                        double value = (maxY / 4) * i;
                        float yPos = pointMaxY - (pointMaxY / 4) * i;
                        canvas.drawLine(leftOffset - 10, yPos, leftOffset, yPos, mAxisPaint);
                        if (yPos < 1) {
                            yPos = 30;
                        }
                        canvas.drawText(value + "", 0, yPos, mTextPaint);
                    }
                    mTextPaint.setTextAlign(Paint.Align.CENTER);
                    for (int i = 1; i < dX; i++) {
                        double value = dX * i;
                        float xPos = leftOffset + (pointMaxX / 4) * i;
                        canvas.drawLine(xPos, pointMaxY, xPos, pointMaxY + 10, mAxisPaint);
                        canvas.drawText(value + "", xPos, chartHeight - 10, mTextPaint);
                    }
                    canvas.drawLine(leftOffset, pointMaxY, leftOffset, 0, mAxisPaint);
                    canvas.drawLine(leftOffset, pointMaxY, chartWidth, pointMaxY, mAxisPaint);
                } finally {
                    holder.unlockCanvasAndPost(canvas);
                }
                try {
                    Thread.sleep(refreshInterval);
                } catch (InterruptedException e) {
                }
            }
        }
    }

}
