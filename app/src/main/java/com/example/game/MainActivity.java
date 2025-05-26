package com.example.game;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.game.databinding.ActivityMainBinding;

import java.util.Locale;
import java.util.Objects;

/*
This class is the main activity of the App. It stores the currently selected language in a SharedPreferences object and the SQLite database.
 */
public class MainActivity extends AppCompatActivity {

    public static DB_SQLite_Asset_Helper sqLite_DB;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        com.example.game.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(view);

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

}