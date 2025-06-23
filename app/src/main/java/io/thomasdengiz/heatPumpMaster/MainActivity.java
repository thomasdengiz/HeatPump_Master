package io.thomasdengiz.heatPumpMaster;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import io.thomasdengiz.heatPumpMaster.databinding.ActivityMainBinding;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Objects;

/*
This class is the main activity of the App. It stores the currently selected language in a SharedPreferences object and the SQLite database.
 */
public class MainActivity extends AppCompatActivity {

    public static DB_SQLite_Asset_Helper sqLite_DB;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        io.thomasdengiz.heatPumpMaster.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(view);

        //Delete the old database and load the new one if an updated has been made (indicated by changing the version number DATABASE_VERSION in DB_SQLite_Asset_Helper)
        String PREFS_NAME    = "app_prefs";
        String PREF_DB_VER   = "db_version_applied";
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int appliedVersion = prefs.getInt(PREF_DB_VER, 0);

        if (appliedVersion < DB_SQLite_Asset_Helper.DATABASE_VERSION) {
            File dbFile = getDatabasePath(DB_SQLite_Asset_Helper.DATABASE_NAME);
            if (dbFile.exists()) {
                dbFile.delete();
            }
            prefs.edit()
                    .putInt(PREF_DB_VER, DB_SQLite_Asset_Helper.DATABASE_VERSION)
                    .apply();
        }
        sqLite_DB = new DB_SQLite_Asset_Helper(this);
    }


    @Override
    protected void attachBaseContext(Context newBase) {
        String language = getLanguage(newBase);
        super.attachBaseContext(LanguageContextWrapper.wrap(newBase, language));
        setLocale(getLanguage(newBase));
    }

    private void setLocale(String language) {
        Locale locale = new Locale(language);
        Resources resources = getBaseContext().getResources();
        Configuration conf = resources.getConfiguration();
        conf.setLocale(locale);
        resources.updateConfiguration(conf, resources.getDisplayMetrics());
    }

    private static final String LANGUAGE = "LANGUAGE";
    private static final String SHARED_PREFS_NAME= "SHARED_PREFS_NAME";

    private static String getLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(LANGUAGE, "en");
    }



    /*
    This method writes the result data for the played level after each game (co2 savings, optimality percentage, gas savings and level unlocked) to the shared preferences XML file to store user progress.
     */
    public static void writeResultsToSharedPreferences(Context context, double resultPercentageCurrentRun, int co2SavingsCurrentRun, double gasSavingsCurrentRun, int levelNumberCurrentRun, int nextLevelUnlockedCurrentRun, int numberOfLevels) {
        SharedPreferences prefs = context.getSharedPreferences("user_level_progress", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        String keyBase = "level_" + levelNumberCurrentRun;

        // Load old values
        float oldPercentage = prefs.getFloat(keyBase + "_percentage", 0f);
        int oldCO2 = prefs.getInt(keyBase + "_co2", 0);
        double oldGas = Double.longBitsToDouble(prefs.getLong(keyBase + "_gas", 0));

        // Compute new values
        float updatedPercentage = new BigDecimal(Math.max(oldPercentage, resultPercentageCurrentRun)).setScale(1, RoundingMode.HALF_UP).floatValue();
        int updatedCO2 = oldCO2 + co2SavingsCurrentRun;
        double updatedGas = oldGas + gasSavingsCurrentRun;

        // Save updated values
        editor.putFloat(keyBase + "_percentage", updatedPercentage);
        editor.putInt(keyBase + "_co2", updatedCO2);
        editor.putLong(keyBase + "_gas", Double.doubleToRawLongBits(updatedGas));
        editor.apply();

        // Unlock next level if applicable
        if (levelNumberCurrentRun < numberOfLevels && nextLevelUnlockedCurrentRun == 1) {
            String nextKeyUnlocked = "level_" + (levelNumberCurrentRun + 1) + "_unlocked";
            boolean alreadyUnlocked = prefs.getBoolean(nextKeyUnlocked, false);
            if (!alreadyUnlocked) {
                editor.putBoolean(nextKeyUnlocked, true);
                editor.apply();
            }
        }
    }

}