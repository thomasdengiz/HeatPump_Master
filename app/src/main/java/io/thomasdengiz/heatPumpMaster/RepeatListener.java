package io.thomasdengiz.heatPumpMaster;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.content.ContextCompat;

/**
 * A class, that can be used as a TouchListener on any view (e.g. a Button).
 * It cyclically runs a clickListener, emulating keyboard-like behaviour. First
 * click is fired immediately, next one after the initialInterval, and subsequent
 * ones after the normalInterval.
 * It is used for the heating button of the game fragment. When the user keeps pushing this
 * bottom, the building or the hot water tank are heated.
 *
 */
public class RepeatListener implements View.OnTouchListener {

    private final Handler handler = new Handler();

    private final int initialInterval;
    private final int normalInterval;
    private final View.OnClickListener clickListener;
    private View touchedView;

    private final Context context;



    private static View_Game_Event_Rectangle currentlyActiveGameRectangle;



    private final Runnable handlerRunnable = new Runnable() {
        @Override
        public void run() {
            if(touchedView.isEnabled()) {
                handler.postDelayed(this, normalInterval);
                clickListener.onClick(touchedView);
            } else {
                // if the view was disabled by the clickListener, remove the callback
                handler.removeCallbacks(handlerRunnable);
                touchedView.setPressed(false);
                touchedView = null;
            }
        }
    };

    /**
     * @param initialInterval The interval after first click event
     * @param normalInterval The interval after second and subsequent click
     *       events
     * @param clickListener The OnClickListener, that will be called
     *       periodically
     */
    public RepeatListener(int initialInterval, int normalInterval, Context context,
                          View.OnClickListener clickListener) {
        if (clickListener == null)
            throw new IllegalArgumentException("null runnable");
        if (initialInterval < 0 || normalInterval < 0)
            throw new IllegalArgumentException("negative interval");

        this.initialInterval = initialInterval;
        this.normalInterval = normalInterval;
        this.clickListener = clickListener;
        this.context = context;
    }

    /*
    This method handles the touch event of the heat button. It changes the background of the button and the
    backgrounds of the currently active game rectangle (if the corresponding one is under the target rectangle of the game).
     */

    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:

                handler.removeCallbacks(handlerRunnable);
                handler.postDelayed(handlerRunnable, initialInterval);
                touchedView = view;
                touchedView.setPressed(true);

                // Change background when pressed
                touchedView.setBackground(ContextCompat.getDrawable(context, R.drawable.heat_button_background_pressed));

                clickListener.onClick(view);

                if (currentlyActiveGameRectangle !=null) {
                    if(currentlyActiveGameRectangle.getEventType().equals(FR_Game.VIEW_EVENT_RECTANGLE_SOLAR)) {
                        currentlyActiveGameRectangle.setBackground(ContextCompat.getDrawable(context, R.drawable.game_event_rectangle_solar_2));
                    }
                    if(currentlyActiveGameRectangle.getEventType().equals(FR_Game.VIEW_EVENT_RECTANGLE_WIND)) {
                        currentlyActiveGameRectangle.setBackground(ContextCompat.getDrawable(context, R.drawable.game_event_rectangle_wind_2));
                    }
                    if(currentlyActiveGameRectangle.getEventType().equals(FR_Game.VIEW_EVENT_RECTANGLE_GAS)) {
                        currentlyActiveGameRectangle.setBackground(ContextCompat.getDrawable(context, R.drawable.game_event_rectangle_gas_2));
                    }
                    if(currentlyActiveGameRectangle.getEventType().equals(FR_Game.VIEW_EVENT_RECTANGLE_COAL)) {
                        currentlyActiveGameRectangle.setBackground(ContextCompat.getDrawable(context, R.drawable.game_event_rectangle_coal_2));
                    }
                }
                return true;
            case MotionEvent.ACTION_UP:

                if (touchedView != null) {
                    touchedView.setPressed(false);
                    handler.removeCallbacks(handlerRunnable);

                    // Restore normal background
                    touchedView.setBackground(ContextCompat.getDrawable(context, R.drawable.heat_button_background_normal));
                }

                if (currentlyActiveGameRectangle !=null) {
                    if(currentlyActiveGameRectangle.getEventType().equals(FR_Game.VIEW_EVENT_RECTANGLE_SOLAR)) {
                        currentlyActiveGameRectangle.setBackground(ContextCompat.getDrawable(context, R.drawable.game_event_rectangle_solar_1));
                    }
                    if(currentlyActiveGameRectangle.getEventType().equals(FR_Game.VIEW_EVENT_RECTANGLE_WIND)) {
                        currentlyActiveGameRectangle.setBackground(ContextCompat.getDrawable(context, R.drawable.game_event_rectangle_wind_1));
                    }
                    if(currentlyActiveGameRectangle.getEventType().equals(FR_Game.VIEW_EVENT_RECTANGLE_GAS)) {
                        currentlyActiveGameRectangle.setBackground(ContextCompat.getDrawable(context, R.drawable.game_event_rectangle_gas_1));
                    }
                    if(currentlyActiveGameRectangle.getEventType().equals(FR_Game.VIEW_EVENT_RECTANGLE_COAL)) {
                        currentlyActiveGameRectangle.setBackground(ContextCompat.getDrawable(context, R.drawable.game_event_rectangle_coal_1));
                    }
                }
                return true;
            case MotionEvent.ACTION_CANCEL:
                handler.removeCallbacks(handlerRunnable);
                touchedView.setPressed(false);
                touchedView = null;
                return true;
        }

        return false;
    }

    /*
    This method set the currently active game rectangle of the game fragment, such that this information is known by the RepeatListener.
     */
    public static void setCurrentlyActiveGameRectangle(View_Game_Event_Rectangle currentlyActiveGameRectangle) {
        RepeatListener.currentlyActiveGameRectangle = currentlyActiveGameRectangle;
    }

}