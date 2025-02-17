package com.example.game;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DB_SQLite_Helper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Internal_Database_SQLite_v112.db";
    public static final int DBVERSION = 1;

    public static final int numberOfLevels = 8;

    public static final String TABLE_LEVEL_ELEMENTS = "Level_Elements";
    public static final String TABLE_LEVEL_INFOS = "Level_Infos";

    public static final String UNLOCKED = "Unlocked";

    public static final String NEEDED_PERCENTAGE_SCORE_FOR_THE_LEVEL = "Needed_Percentage_Score_For_The_Level";

    public static final String BASELINE_SCORE_FOR_THE_LEVEL = "Baseline_Score_For_The_Level";

    public static final String CO2_SAVINGS_TOTAL_G = "CO2_Savings_Total_g";
    public static final String GAS_SAVINGS_TOTAL_KWH = "Gas_Savings_Total_kWh";
    public static final String BEST_RESULT_PERCENTAGE = "Best_Result_Percentage";


    public static final String LEVEL_NUMBER = "Level_Number";
    public static final String LEVEL = "Level";
    public static final String PV = "PV";
    public static final String WIND = "Wind";
    public static final String FOSSIL = "Fossil";


    public DB_SQLite_Helper(Context context) {
        super(context,DATABASE_NAME,null,DBVERSION);
        if (!ifDBExists(context)) {
            if (!copyDBFromAssets(context)) {
                throw new RuntimeException("Failed to Copy Database From Assets Folder");
            }
        }
        mDB = this.getWritableDatabase();
    }

    SQLiteDatabase mDB;

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public int [] getDataDB_TableLevelElements (int levelNumber, String type) {
        int [] resultingValues;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select " +  type +  " from " +  TABLE_LEVEL_ELEMENTS + " where "
                + LEVEL + " = '" + levelNumber + "'", null);


        if(res!=null && res.getCount()>0) {
            resultingValues = new int[res.getCount()];
            res.moveToFirst();
            int helpIndex =0;
            while (res.moveToNext()) {
                resultingValues [helpIndex] = res.getInt(0);
                helpIndex++;
            }
            return resultingValues;
        }
        else {
            resultingValues = new int[0];
        }

        // Ensure the Cursor is closed
        if (res != null) {
            res.close();
        }

        return resultingValues;
    }


    public Cursor getCursor_TableLevelInfos () {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("select * from " +  TABLE_LEVEL_INFOS , null);
    }

    public double getNeededPercentageScoreForTheLevel (int levelNumber) {
        double neededPercentageScore = 0;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select " + NEEDED_PERCENTAGE_SCORE_FOR_THE_LEVEL +" from " +  TABLE_LEVEL_INFOS + " where "
                + LEVEL_NUMBER + " = '" + levelNumber + "'", null);
        if (res!=null && res.getCount()>0) {
            res.moveToFirst();
            neededPercentageScore = res.getInt(0);
        }

        // Ensure the Cursor is closed
        if (res != null) {
            res.close();
        }

        return neededPercentageScore;
    }


    public double getBaselineScoreForTheLevel(int levelNumber) {
        double neededPercentageScore = 0;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select " + BASELINE_SCORE_FOR_THE_LEVEL +" from " +  TABLE_LEVEL_INFOS + " where "
                + LEVEL_NUMBER + " = '" + levelNumber + "'", null);
        if (res!=null && res.getCount()>0) {
            res.moveToFirst();
            neededPercentageScore = res.getInt(0);
        }

        // Ensure the Cursor is closed
        if (res != null) {
            res.close();
        }

        return neededPercentageScore;
    }

    public void writeResults_TableInfo (double resultPercentageCurrentRun, int co2SavingsCurrentRun, double gasSavingsCurrentRun, int levelNumberCurrentRun, int nextLevelUnlockedCurrentRun) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        //Read previous data from db
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " +  TABLE_LEVEL_INFOS + " where "
                + LEVEL_NUMBER + " = '" + levelNumberCurrentRun + "'", null);
        double resultPercentageOld = 0;
        int co2SavingsOld = 0;
        double gasSavingsOld = 0;


        if (res!=null && res.getCount()>0) {
            res.moveToFirst();
            resultPercentageOld = res.getDouble(2);
            co2SavingsOld = res.getInt(3);
            gasSavingsOld = res.getDouble(4);
        }

        //Update the data
        double resultPercentageUpdate = resultPercentageOld;
        int co2SavingsUpdate = co2SavingsOld + co2SavingsCurrentRun;
        double gasSavingsUpdate = gasSavingsOld + gasSavingsCurrentRun;

        if (resultPercentageOld<resultPercentageCurrentRun) {
            resultPercentageUpdate = resultPercentageCurrentRun;
        }



        //Write into db


        contentValues.put(BEST_RESULT_PERCENTAGE, resultPercentageUpdate);
        contentValues.put(CO2_SAVINGS_TOTAL_G, co2SavingsUpdate);
        contentValues.put(GAS_SAVINGS_TOTAL_KWH, gasSavingsUpdate);


        sqLiteDatabase.update(TABLE_LEVEL_INFOS, contentValues, LEVEL_NUMBER + " = ?", new String[]{String.valueOf(levelNumberCurrentRun)});



        //Set the unlocked status for the next level
        if (levelNumberCurrentRun <numberOfLevels ) {

            //Check if the next level had been unlocked before

            res = db.rawQuery("select * from " +  TABLE_LEVEL_INFOS + " where "
                    + LEVEL_NUMBER + " = '" + (levelNumberCurrentRun + 1) + "'", null);

            res.moveToFirst();
            int nextLevelUnlocked = res.getInt(1);
            if (nextLevelUnlocked==1) {
                nextLevelUnlockedCurrentRun = 1;
            }

            contentValues = new ContentValues();
            contentValues.put(UNLOCKED, nextLevelUnlockedCurrentRun);
        }

        // Ensure the Cursor is closed
        if (res != null) {
            res.close();
        }

        sqLiteDatabase.update(TABLE_LEVEL_INFOS, contentValues, LEVEL_NUMBER + " = ?", new String[]{String.valueOf(levelNumberCurrentRun + 1)});


    }










    ////////////////////////////////////////////////////////////////////////////

    /*
     Copies the database from the assets folder to the apps database folder (with logging)
     note databases folder is typically data/data/the_package_name/database
          however using getDatabasePath method gets the actual path (should it not be as above)
     This method can be significantly reduced one happy that it works.
  */
    @SuppressWarnings("IOStreamConstructor")
    private boolean copyDBFromAssets(Context context) {
        Log.d("CPYDBINFO","Starting attempt to cop database from the assets file.");
        String DBPATH = context.getDatabasePath(DATABASE_NAME).getPath();
        InputStream is;
        OutputStream os;
        int buffer_size = 8192;
        int length = buffer_size;
        long bytes_read = 0;
        long bytes_written = 0;
        byte[] buffer = new byte[length];

        try {

            is = context.getAssets().open(DATABASE_NAME);
        } catch (IOException e) {
            Log.e("CPYDB FAIL - NO ASSET","Failed to open the Asset file " + DATABASE_NAME);
            e.printStackTrace();
            return false;
        }

        try {
            os = new FileOutputStream(DBPATH);
        } catch (IOException e) {
            Log.e("CPYDB FAIL - OPENDB","Failed to open the Database File at " + DBPATH);
            e.printStackTrace();
            return false;
        }
        Log.d("CPYDBINFO","Initiating copy from asset file" + DATABASE_NAME + " to " + DBPATH);
        while (length >= buffer_size) {
            try {
                length = is.read(buffer,0,buffer_size);
            } catch (IOException e) {
                Log.e("CPYDB FAIL - RD ASSET",
                        "Failed while reading in data from the Asset. " +
                                bytes_read +
                                " bytes read successfully."
                );
                e.printStackTrace();
                return false;
            }
            bytes_read = bytes_read + length;
            try {
                os.write(buffer,0,buffer_size);
            } catch (IOException e) {
                Log.e("CPYDB FAIL - WR ASSET","failed while writing Database File " +
                        DBPATH +
                        ". " +
                        bytes_written +
                        " bytes written successfully.");
                e.printStackTrace();
                return false;

            }
            bytes_written = bytes_written + length;
        }
        Log.d("CPYDBINFO",
                "Read " + bytes_read + " bytes. " +
                        "Wrote " + bytes_written + " bytes."
        );
        try {
            os.flush();
            is.close();
            os.close();
        } catch (IOException e ) {
            Log.e("CPYDB FAIL - FINALISING","Failed Finalising Database Copy. " +
                    bytes_read +
                    " bytes read." +
                    bytes_written +
                    " bytes written."
            );
            e.printStackTrace();
            return false;
        }
        return true;
    }
    /*
    Checks to see if the database exists if not will create the respective directory (database)
    Creating the directory overcomes the NOT FOUND error
 */
    private boolean ifDBExists(Context context) {
        String dbparent = context.getDatabasePath(DATABASE_NAME).getParent();
        File f = context.getDatabasePath(DATABASE_NAME);
        if (!f.exists()) {
            Log.d("NODB MKDIRS", "Database file not found, making directories."); //<<<< remove before the App goes live.
            if (dbparent != null) {
                File d = new File(dbparent);
                if (!d.mkdirs() && !d.isDirectory()) { // Check if mkdirs() failed and directory wasn't created
                    Log.e("NODB MKDIRS", "Failed to create directories for the database.");
                    return false;
                }
            }
        }
        return f.exists();
    }


}//Class
