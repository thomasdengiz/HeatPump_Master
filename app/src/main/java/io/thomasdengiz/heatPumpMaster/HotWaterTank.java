package io.thomasdengiz.heatPumpMaster;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

/*
This class defines the custom view for the hot water tank. The hot water tank is a central part of the game. When the heat pump uses electricity it can heat the hot water tank.
As there are standing losses (in reality) the amount of hot water is constantly slightly reducing. When a shower is taken (by clicking on an incoming flying shower button), the amount of hot water is strongly reduced
 */

public class HotWaterTank extends View {
    private Paint mInnerCirclePaint;
    private int innerCircleCenter;
    private int hotWaterColor = Color.parseColor("#327bff");
    private Bitmap bitmap;
    private int left;
    private int top;

    private int lineEndY;
    private int lineStartY;


    /*
    Set the variables for the positions of the water bar
     */

    public double getPositionOfWaterBar() {
        return positionOfWaterBar;
    }

    double positionOfWaterBar = 0.5;

    //0.607= empty, - 0.04 = full,
    final double value_positionOfWaterBar_Empty = 0.607;
    final double value_positionOfWaterBar_Full = -0.04;

    final double value_positionOfWaterBar_upperLimit = -0.09;
    final double value_positionOfWaterBar_lowerLimit = 0.615;

    public HotWaterTank(Context context) {
        this(context, null);
    }

    public HotWaterTank(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HotWaterTank(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        if (attrs != null) {

            @SuppressLint("CustomViewStyleable") final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Thermometer, defStyle, 0);

            hotWaterColor = a.getColor(R.styleable.Thermometer_therm_color, hotWaterColor);

            a.recycle();
        }

        init();
    }

    private void init() {
        mInnerCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mInnerCirclePaint.setColor(hotWaterColor);
        mInnerCirclePaint.setStyle(Paint.Style.FILL);

        //Use of a custom bitmap for the hot water tank
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.hot_water_tank_container_thick);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // init bitmap

        int width = getWidth();
        int height = getHeight();

        mInnerCirclePaint.setStrokeWidth( (float) (width * 0.9));

        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);

        innerCircleCenter = getWidth() / 2;

        left = (getWidth() - bitmap.getWidth()) / 2;
        top = (getHeight() - bitmap.getHeight()) / 2;


        lineStartY = ((int)(bitmap.getHeight() / 4.6f) + top) + (int) (positionOfWaterBar * bitmap.getHeight());
        lineEndY = (top + bitmap.getHeight()) - (int)(bitmap.getHeight() / 7f);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        // the actual dimensions of your water tank image
        // these are just here to set the aspect ratio and the 'max' dimensions (we will make it smaller in the xml)
        int desiredWidth = 421;
        int desiredHeight = 693;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            //Must be this size
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            height = Math.min(desiredHeight, heightSize);
        } else {
            //Be whatever you want
            height = desiredHeight;
        }

        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            //Must be this size
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            width = heightSize * desiredWidth / desiredHeight;
        } else {
            //Be whatever you want
            width = desiredWidth;
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        drawHowWaterTank(canvas);
    }


    private void drawHowWaterTank(Canvas canvas) {

        canvas.drawLine(innerCircleCenter , lineStartY, innerCircleCenter, lineEndY, mInnerCirclePaint);
        canvas.drawBitmap(bitmap, left, top, new Paint());

    }

    /*
    This method is used to change the position of the water bar in the hot water tank.
     */
    public void changeVolumeBar( double percentageChangeOfTheWholeBar) {
        double appliedPercentageChangeOfTheWholeBar = percentageChangeOfTheWholeBar / 100;
        if (appliedPercentageChangeOfTheWholeBar>1) {
            appliedPercentageChangeOfTheWholeBar = 1;
        }
        if (appliedPercentageChangeOfTheWholeBar <-1) {
            appliedPercentageChangeOfTheWholeBar = -1;
        }


        double absolutValueSpanForTheWholeBar = value_positionOfWaterBar_Full - value_positionOfWaterBar_Empty;

        positionOfWaterBar = positionOfWaterBar + appliedPercentageChangeOfTheWholeBar * absolutValueSpanForTheWholeBar;
        if (positionOfWaterBar < value_positionOfWaterBar_upperLimit) {
            positionOfWaterBar = value_positionOfWaterBar_upperLimit;
        }

        if(positionOfWaterBar > value_positionOfWaterBar_lowerLimit) {
            positionOfWaterBar = value_positionOfWaterBar_lowerLimit;
        }

        lineStartY = ((int)(bitmap.getHeight() / 4.6f) + top) + (int) (positionOfWaterBar * bitmap.getHeight());
        lineEndY = (top + bitmap.getHeight()) - (int)(bitmap.getHeight() / 7f);
    }


}