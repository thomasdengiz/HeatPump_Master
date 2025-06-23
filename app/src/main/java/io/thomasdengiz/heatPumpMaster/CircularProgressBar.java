package io.thomasdengiz.heatPumpMaster;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;


/*
This class is used to create a circular progress bar that displays the remaining time for a level.
 */

public class CircularProgressBar extends View {
    private final Paint backgroundPaint;
    private final Paint progressPaint;
    private final RectF rectF;
    private float progress;

    public CircularProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        backgroundPaint = new Paint();
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(16);
        backgroundPaint.setColor(Color.parseColor("#adadad")); //

        progressPaint = new Paint();
        progressPaint.setAntiAlias(true);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(16); // Adjust the stroke width as needed
        progressPaint.setColor(Color.parseColor("#e6bb22"));

        rectF = new RectF();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        float centerX = (float) (width / 2.0);
        float centerY = (float) (height / 2.0);
        float radius = Math.min(centerX, centerY) - progressPaint.getStrokeWidth();

        rectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);

        // Draw the grey background circle
        canvas.drawArc(rectF, 0, 360, false, backgroundPaint);

        // Draw the progress arc on top (anti-clockwise)
        canvas.drawArc(rectF, -90, 360 * progress / 100, false, progressPaint);
    }

    public void setProgress(float progress) {
        this.progress = progress;
        invalidate();
    }
}
