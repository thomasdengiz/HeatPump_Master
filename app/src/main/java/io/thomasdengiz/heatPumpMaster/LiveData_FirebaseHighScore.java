package io.thomasdengiz.heatPumpMaster;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


/*
This class is a LiveData class that is used to listen to changes in the Firebase database for the high scores of a level.
 */
public class LiveData_FirebaseHighScore extends LiveData<DataSnapshot> {
    private static final String LOG_TAG = "Tag_Dialog";

    private Query query;
    private final MyValueEventListener listener = new MyValueEventListener();

    public LiveData_FirebaseHighScore(Query query) {
        this.query = query;
    }

    public void changeQuery(Query newQuery) {
        this.query = newQuery;
        onActive();
    }

    public void forceUpdate() {
        setValue(getValue());
    }

    @Override
    protected void onActive() {
        query.addValueEventListener(listener);
    }

    @Override
    protected void onInactive() {
        query.removeEventListener(listener);
    }

    private class MyValueEventListener implements ValueEventListener {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            setValue(dataSnapshot);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.e(LOG_TAG, "LiveData: Can't listen to query " + query, databaseError.toException());
        }
    }
}
