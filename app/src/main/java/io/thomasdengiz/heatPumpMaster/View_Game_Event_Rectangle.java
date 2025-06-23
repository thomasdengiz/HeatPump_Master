package io.thomasdengiz.heatPumpMaster;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;

/*
This class defines a custom simple rectangular view of the different View_Game_Event_Rectangle (Solar, Wind, Fossil). The game events
are moving from right to left in the upper part of the game fragment and the user has to heat at the correct time slot when a View_Game_Event_Rectangle is under the red line in the middle of the screen.
 */
@SuppressLint("ViewConstructor")
public class View_Game_Event_Rectangle extends View {

    private boolean isActive;
    private int numberOfTimeSlotsAfterFinishing;
    private final String eventType;
    private final int startingTimeSlot;
    private final int duration;


    public View_Game_Event_Rectangle(Context context, String eventType, int startingTimeSlot, int duration ) {
        super(context);
        this.eventType = eventType;
        this.startingTimeSlot = startingTimeSlot;
        this.duration = duration;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public int getNumberOfTimeSlotsAfterFinishing() {
        return numberOfTimeSlotsAfterFinishing;
    }

    public void incrementNumberOfTimeSlotsAfterFinishing() {
        this.numberOfTimeSlotsAfterFinishing = this.numberOfTimeSlotsAfterFinishing + 1;
    }

    public String getEventType() {
        return eventType;
    }

    public int getStartingTimeSlot() {
        return startingTimeSlot;
    }

    public int getDuration() {
        return duration;
    }

}
