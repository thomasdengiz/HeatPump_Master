package io.thomasdengiz.heatPumpMaster;

/*
This class is an item for the RecyclerView of the level selection fragment.
 */
public class RV_Item_Level_Selection_Menu {

    private final int levelNumber;
    private final double bestResultThisLevel;
    private final int totalCO2SavingsThisLevel;
    private final int gasSavingsTotalThisLevel;
    private final boolean levelUnlocked;

    private boolean isSelected;

    public RV_Item_Level_Selection_Menu(int levelNumber, double bestResultThisLevel, int totalCO2SavingsThisLevel, int gasSavingsTotalThisLevel, boolean levelUnlocked,boolean isSelected) {
        this.levelNumber = levelNumber;
        this.bestResultThisLevel = bestResultThisLevel;
        this.totalCO2SavingsThisLevel = totalCO2SavingsThisLevel;
        this.gasSavingsTotalThisLevel = gasSavingsTotalThisLevel;
        this.levelUnlocked = levelUnlocked;
        this.isSelected = isSelected;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public double getBestResultThisLevel() {
        return bestResultThisLevel;
    }

    public int getTotalCO2SavingsThisLevel() {
        return totalCO2SavingsThisLevel;
    }

    public int getGasSavingsTotalThisLevel() {
        return gasSavingsTotalThisLevel;
    }

    public boolean isLevelUnlocked() {
        return levelUnlocked;
    }

}
