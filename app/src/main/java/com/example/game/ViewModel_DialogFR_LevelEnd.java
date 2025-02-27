package com.example.game;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class ViewModel_DialogFR_LevelEnd extends ViewModel {


    public static final String FIREBASE_URL = "https://heatinggamehighscores-default-rtdb.europe-west1.firebasedatabase.app/";
    public static String FIREBASE_NODE_LEVEL = "";
    public static final String FIREBASE_DATE_IN_MILLISECONDS = "date_in_milliseconds";


    public void setFirebaseNodeLevel(int levelNumber) {
        FIREBASE_NODE_LEVEL = "level_" + levelNumber;
    }

    public void setPastTimeMillis (int pastDaysForDisplayingScores) {
        long pastTimeMillis = System.currentTimeMillis() - (pastDaysForDisplayingScores * 24L * 60 * 60 * 1000);
        Query QUERY_DATE = FirebaseDatabase.getInstance(FIREBASE_URL).getReference().child(FIREBASE_NODE_LEVEL).orderByChild(FIREBASE_DATE_IN_MILLISECONDS).startAt(pastTimeMillis);

        liveData.changeQuery(QUERY_DATE);
    }

    Query QUERY_DATE = FirebaseDatabase.getInstance(FIREBASE_URL).getReference().child(FIREBASE_NODE_LEVEL).orderByChild(FIREBASE_DATE_IN_MILLISECONDS);

    private final LiveData_FirebaseHighScore liveData = new LiveData_FirebaseHighScore(QUERY_DATE);

    public LiveData_FirebaseHighScore getData() {

        return liveData;
    }
}