package io.thomasdengiz.heatPumpMaster;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;


/*
This class is used to for the internal SQLite database. The database has 2 tables "Level_Elements" and "Level_Infos". The database contains level design information for the creation of the different levels (Table Level_Elements).
Further, the database contains information about the required baseline score of a level (Table Level_Infos). This information is statically stored in the database, meaning that it is not adjusted during the game by the user. The values have been determined by the developer of the game.
The database is read from the assets folder (app\src\main\assets)
 */

public class DB_SQLite_Asset_Helper extends SQLiteAssetHelper {

    public static final String DATABASE_NAME = "internal_database.db";
    public static final int DATABASE_VERSION = 1;

    public static final String TABLE_LEVEL_ELEMENTS = "Level_Elements";
    public static final String TABLE_LEVEL_INFOS = "Level_Infos";

    public static final String NEEDED_PERCENTAGE_SCORE_FOR_THE_LEVEL = "Needed_Percentage_Score_For_The_Level";

    public static final String BASELINE_SCORE_FOR_THE_LEVEL = "Baseline_Score_For_The_Level";

    public static final String LEVEL_NUMBER = "Level_Number";
    public static final String LEVEL = "Level";
    public static final String PV = "PV";
    public static final String WIND = "Wind";
    public static final String FOSSIL = "Fossil";

    public static final String SPEED_MULTIPLICATOR = "Speed_Multiplicator";

    public DB_SQLite_Asset_Helper (Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    /*
    This method retrieves the data from the database table "Level_Elements" for a specific level. It gets data about the level design regarding the the different game rectangles (Solar, Wind, Fossil) and their corresponding timeslots in the level.
     */
    public int[] getDataDB_TableLevelElements(int levelNumber, String type) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = null;
        int[] resultingValues = new int[0];

        try {
            res = db.rawQuery(
                    "SELECT " + type + " FROM " + TABLE_LEVEL_ELEMENTS + " WHERE " + LEVEL + " = ?",
                    new String[]{String.valueOf(levelNumber)}
            );

            if (res != null && res.moveToFirst()) {
                resultingValues = new int[res.getCount()];
                int index = 0;
                do {
                    resultingValues[index++] = res.getInt(0);
                } while (res.moveToNext());
            }
        } finally {
            if (res != null) {
                res.close();
            }
        }

        return resultingValues;
    }


    /*
    This method returns the needed percentage score in relation to the baseline score to successfully pass a specific level. The data is specified in the database.
     */
    public double getNeededPercentageScoreForTheLevel(int levelNumber) {
        double neededPercentageScore = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = null;
        String[] selectionArgs = {String.valueOf(levelNumber)};
        String sql = "SELECT " + NEEDED_PERCENTAGE_SCORE_FOR_THE_LEVEL +
                " FROM " + TABLE_LEVEL_INFOS +
                " WHERE " + LEVEL_NUMBER + " = ?";

        try {
            res = db.rawQuery(sql, selectionArgs);
            if (res != null && res.moveToFirst()) {
                neededPercentageScore = res.getDouble(res.getColumnIndexOrThrow(NEEDED_PERCENTAGE_SCORE_FOR_THE_LEVEL));
            }
        } catch (Exception e) {
            Log.e("DB_SQLite_Asset_Helper", "Error getting baseline score for level " + levelNumber + ": " + e.getMessage(), e);
        } finally {
            if (res != null) {
                res.close();
            }
        }
        return neededPercentageScore;
    }


    /*
    This method returns the baseline score for a specific level. The data is specified in the database and had been determined by the developer of the game (playing multiple times the same level and recording the scores).
     */
  public double getBaselineScoreForTheLevel(int levelNumber) {
      double baselineScore = 0;
      SQLiteDatabase db = this.getReadableDatabase();
      Cursor res = null;

      String sql = "SELECT " + BASELINE_SCORE_FOR_THE_LEVEL +
              " FROM " + TABLE_LEVEL_INFOS +
              " WHERE " + LEVEL_NUMBER + " = ?";

      try {
          res = db.rawQuery(sql, new String[]{String.valueOf(levelNumber)});
          if (res.moveToFirst()) {
              baselineScore = res.getDouble(0);
          }
      } catch (Exception e) {
          Log.e("DB_Helper", "Error fetching baseline score: " + e.getMessage(), e);
      } finally {
          if (res != null) {
              res.close();
          }
      }

      return baselineScore;
  }


    /*
    This method returns the speed multiplicator for a specific level from the database. The speedmultiplicator specifies how fast the game elements are flowing through the screen. A lower value means that they will flow faster.
    */
  public double getSpeedMultiplicator(int levelNumber) {
      double speedMultiplicator = 1.0; // Default value in case not found or error
      SQLiteDatabase db = this.getReadableDatabase();
      Cursor cursor = null;

      try {
          cursor = db.rawQuery("SELECT " + SPEED_MULTIPLICATOR + " FROM Level_Infos WHERE Level_Number = ?", new String[]{String.valueOf(levelNumber)});
          if (cursor != null && cursor.moveToFirst()) {
              speedMultiplicator = cursor.getDouble(cursor.getColumnIndexOrThrow("Speed_Multiplicator"));
          }
      } catch (Exception e) {
          Log.e("DB_READ", "Error reading Speed_Multiplicator: " + e.getMessage(), e);
      } finally {
          if (cursor != null) {
              cursor.close();
          }
      }

      return speedMultiplicator;
  }


  /**
   * Returns the total number of rows (levels) in the Level_Infos table.
   */
    public int getNumberOfLevels() {
        int count = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_LEVEL_INFOS, null);
            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e("DB_READ", "Error counting levels: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return count;
    }


}//Class
