package io.thomasdengiz.heatPumpMaster;

import androidx.lifecycle.ViewModel;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

/*
This class is the ViewModel for the DialogFR_LevelEnd that is being displayed after the user has finished a level.
It contains the Firebase Realtime Database query and the relevant information for it. It also manages the LiveData object for the Highscore list.
 */
public class ViewModel_DialogFR_LevelEnd extends ViewModel {


    public static final String FIREBASE_URL = BuildConfig.FIREBASE_URL;
    public static String FIREBASE_NODE_LEVEL = "";
    public static final String FIREBASE_DATE_IN_MILLISECONDS = "date_in_milliseconds";



    public int pastDaysForDisplayingScores =0;

    public void setFirebaseNodeLevel(int levelNumber) {
        FIREBASE_NODE_LEVEL = "levels/" + levelNumber;
    }

    public void setPastTimeMillis (int pastDaysForDisplayingScores) {
        this.pastDaysForDisplayingScores = pastDaysForDisplayingScores;
        long pastTimeMillis = System.currentTimeMillis() - (pastDaysForDisplayingScores * 24L * 60 * 60 * 1000);
        Query QUERY_DATE = FirebaseDatabase.getInstance(FIREBASE_URL).getReference().child(FIREBASE_NODE_LEVEL).orderByChild(FIREBASE_DATE_IN_MILLISECONDS).startAt(pastTimeMillis);

        liveData.changeQuery(QUERY_DATE);
    }

    Query QUERY_DATE = FirebaseDatabase.getInstance(FIREBASE_URL).getReference().child(FIREBASE_NODE_LEVEL).orderByChild(FIREBASE_DATE_IN_MILLISECONDS);

    private final LiveData_FirebaseHighScore liveData = new LiveData_FirebaseHighScore(QUERY_DATE);

    public int getPastDaysForDisplayingScores() {
        return pastDaysForDisplayingScores;
    }

    public void setPastDaysForDisplayingScores(int pastDaysForDisplayingScores) {
        this.pastDaysForDisplayingScores = pastDaysForDisplayingScores;
    }

    public LiveData_FirebaseHighScore getData() {

        return liveData;
    }
}