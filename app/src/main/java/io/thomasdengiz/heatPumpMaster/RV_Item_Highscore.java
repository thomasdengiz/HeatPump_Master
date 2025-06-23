package io.thomasdengiz.heatPumpMaster;

import androidx.annotation.NonNull;

/*
This class is an item for the RecyclerView of the highscore list (inside the DialogFR_LevelEnd).
 */
public class RV_Item_Highscore {

    private String name;
    private final int co2Score;
    private final String date;
    private int level;

    private final int position;

    public RV_Item_Highscore(String name, int co2Score, String date, int level, int position) {
        this.name = name;
        this.co2Score = co2Score;
        this.date = date;
        this.level = level;
        this.position =position;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCo2Score() {
        return co2Score;
    }

    public String getDate() {
        return date;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getPosition() {
        return position;
    }

    // Override toString() method to print object attributes
    @NonNull
    @Override
    public String toString() {
        return "RV_Item_Highscore{" +
                "name='" + name + '\'' +
                ", co2Score=" + co2Score +
                ", date='" + date + '\'' +
                ", level=" + level +
                ", position=" + position +
                '}';
    }
}
