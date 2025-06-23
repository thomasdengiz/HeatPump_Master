package io.thomasdengiz.heatPumpMaster;

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
This class defines the custom view for the thermometer. The thermometer is a central part of the game. When the heat pump uses electricity it can heat the building and thus increase the temperature of the thermometer.
As there are standing losses (in reality) the temperature of the building is constantly slightly reducing. When the building is aired (by clicking on an incoming flying window button), the temperature is strongly reduced.
 */
public class Thermometer extends View {
    private Paint mInnerCirclePaint;
    private int mInnerRadius;
    private int mThermometerColor = Color.RED;
    private Bitmap bitmap;
    private final int top =0;
    private int innerCircleCenter;
    private int circleHeight;
    private int lineEndY;
    private int lineStartY;
    double positionOfTemperatureBar = 0.128;

    public double getPositionOfTemperatureBar() {
        return positionOfTemperatureBar;
    }

    //0.378= 20°C, 0.2 = 21 °C, 0.022 = 22°C, 0.41 = lower limit, -0.03 = upper limit

    //0.31= 20°C, 0.128 = 21 °C, -0.06 = 22°C, 0.41 = lower limit, -0.17 = upper limit
    final double value_positionOfTemperatureBar_20Degrees = 0.31;
    final double value_positionOfTemperatureBar_22Degrees = -0.06;
    final double value_positionOfTemperatureBar_upperLimit = -0.17 ;
    final double value_positionOfTemperatureBar_lowerLimit = 0.41;




    public Thermometer(Context context) {
        this(context, null);
    }

    public Thermometer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Thermometer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        if (attrs != null) {

            final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Thermometer, defStyle, 0);

            mThermometerColor = a.getColor(R.styleable.Thermometer_therm_color, mThermometerColor);

            a.recycle();
        }

        init();
    }

    private void init() {
        mInnerCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mInnerCirclePaint.setColor(mThermometerColor);
        mInnerCirclePaint.setStyle(Paint.Style.FILL);

        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.thermometer_container_v2);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        bitmap = Bitmap.createScaledBitmap(bitmap,  getWidth(), getHeight(), true);

        mInnerRadius = bitmap.getWidth() / 5;
        innerCircleCenter = (int) (getWidth() / 2.004);
        circleHeight = (top + bitmap.getHeight()) - (int)(bitmap.getHeight() / 5f);

        mInnerCirclePaint.setStrokeWidth(bitmap.getWidth() / 7f);
        lineStartY = ((int)(bitmap.getHeight() / 6.4f) + top) + (int) (positionOfTemperatureBar * bitmap.getHeight());
        //lineStartY = (int)(bitmap.getHeight());
        lineEndY =-100+ (top + bitmap.getHeight()) - (int)(bitmap.getHeight() / 2f);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        drawThermometer(canvas);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = 558;
        int desiredHeight = 730;
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


    private void drawThermometer(Canvas canvas) {
        canvas.drawCircle(innerCircleCenter, circleHeight, mInnerRadius, mInnerCirclePaint);
        canvas.drawLine(innerCircleCenter, lineStartY, innerCircleCenter, lineEndY, mInnerCirclePaint);
        int left = 0;
        canvas.drawBitmap(bitmap, left, top, new Paint());
    }

    public void changeTemperature( double percentageChangeOfTheWholeBar) {
        double appliedPercentageChangeOfTheWholeBar = percentageChangeOfTheWholeBar / 100;
        if (appliedPercentageChangeOfTheWholeBar>1) {
            appliedPercentageChangeOfTheWholeBar = 1;
        }
        if (appliedPercentageChangeOfTheWholeBar <-1) {
            appliedPercentageChangeOfTheWholeBar = -1;
        }

        double absolutValueSpanForTheWholeBar = value_positionOfTemperatureBar_22Degrees - value_positionOfTemperatureBar_20Degrees;

        positionOfTemperatureBar = positionOfTemperatureBar + appliedPercentageChangeOfTheWholeBar * absolutValueSpanForTheWholeBar;
        if (positionOfTemperatureBar < value_positionOfTemperatureBar_upperLimit) {
            positionOfTemperatureBar = value_positionOfTemperatureBar_upperLimit;
        }

        if(positionOfTemperatureBar > value_positionOfTemperatureBar_lowerLimit) {
            positionOfTemperatureBar = value_positionOfTemperatureBar_lowerLimit;
        }


        lineStartY = ((int)(bitmap.getHeight() / 4.6f) + top) + (int) (positionOfTemperatureBar * bitmap.getHeight());
        lineEndY = 10+ (top + bitmap.getHeight()) - (int)(bitmap.getHeight() / 4f);

    }

    public void setTemperature( double setTemperatureDegreesCelsius) {


        double absolutValueSpanForTheWholeBar = value_positionOfTemperatureBar_22Degrees - value_positionOfTemperatureBar_20Degrees;

        positionOfTemperatureBar = value_positionOfTemperatureBar_20Degrees + ((setTemperatureDegreesCelsius - 20 )/2)  * absolutValueSpanForTheWholeBar;


        lineStartY = -100+ ((int)(bitmap.getHeight() / 4.6f) + top) + (int) (positionOfTemperatureBar * bitmap.getHeight());
        lineEndY = (top + bitmap.getHeight()) ;
    }


}
