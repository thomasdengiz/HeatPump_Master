package io.thomasdengiz.heatPumpMaster;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Insets;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.widget.EditText;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.thomasdengiz.heatPumpMaster.databinding.DialoagFragmentLevelEndingBinding;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;


/*
This class is for displaying the Dialog Fragment at the end of each level. The fragment shows the results for the level and the highscores. It uses Firebase Realtime database queries and writing operations for the highscore lists.
 */

public class DialogFR_LevelEnd extends DialogFragment implements View.OnClickListener {


    public DialoagFragmentLevelEndingBinding binding;

    private double co2SavingsScoreCurrentRunScoreSubmit;
    private final int numberOfDisplayedHighScoreEntries = 10;

    public static double desiredComfortBonusScorePercentage = 13;

    public static int  maximumComfortBonusScorePercentage = 15;

    public static boolean isDialogShown = false;

    private String name_input_text = "";

    private RV_Adapter_Highscore adapter_HighScore;
    private ArrayList<RV_Item_Highscore> arrayList_HighScore;

    private int pastDaysForDisplayingScores = 7;

    private  int currentLevel;

    //Firebase components
    private ViewModel_DialogFR_LevelEnd viewModel;
    LiveData_FirebaseHighScore liveData;
    DatabaseReference rootRef_Firebase;

    String FIREBASE_CO_2_SCORE = "co2_score";
    String FIREBASE_DATE = "date";
    String FIREBASE_DATE_IN_MILLISECONDS = "date_in_milliseconds";
    String FIREBASE_LEVEL = "level";
    String FIREBASE_NAME = "name";


    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        //Set the dialog window size
        Objects.requireNonNull(getDialog()).setOnShowListener(
                dialog -> {
                    Point availableDisplaySize = getAvailableDisplaySize(requireActivity());
                    int targetWidth = (int) (availableDisplaySize.x * .98f);
                    int targetHeight = (int) (availableDisplaySize.y * .95f);
                    WindowManager.LayoutParams params = Objects.requireNonNull(getDialog().getWindow()).getAttributes();
                    params.width = targetWidth;
                    params.height = targetHeight;
                    getDialog().getWindow().setAttributes(params);
                }
        );
        isDialogShown = true;
    }

    /*
    This method makes sure that the app naivates back to FR_Menu when the back button is pressed
     */
    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_dialogFR_LevelEnd_to_FR_Menu);
    }


    @SuppressLint("SetTextI18n")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DialoagFragmentLevelEndingBinding.inflate(inflater, container, false);

        //Get the passed parameters from FR_Game using safeArgs
        double co2SavingsScoreCurrentRun = 0; // Initialize with a default value
        double neededCO2SavingsScore = 0; // Initialize with a default value
        int endComfortPercentage = 0; // Initialize with a default value


        Bundle args = getArguments();
        if (args != null) {

            co2SavingsScoreCurrentRun = DialogFR_LevelEndArgs.fromBundle(args).getCurrentCO2SavingsThisLevel();
            neededCO2SavingsScore = DialogFR_LevelEndArgs.fromBundle(args).getNeededCO2SavingsInTheLevel();
            endComfortPercentage = DialogFR_LevelEndArgs.fromBundle(args).getComfortPercentage();
            currentLevel = DialogFR_LevelEndArgs.fromBundle(args).getCurrentLevel();
        } else {
            Log.e("DialogFR_LevelEnd", "Arguments are null. Unable to retrieve data.");
        }


        //Calculate and display the comfort bonus
        double actualComfortBonus = Math.round((endComfortPercentage / 100.0) * (maximumComfortBonusScorePercentage / 100.0) * FR_Game.PERFECT_CO2SCORE_GRAM);
        double perfectComfortBonus = Math.round((1.0) * (maximumComfortBonusScorePercentage / 100.0) * FR_Game.PERFECT_CO2SCORE_GRAM);
        co2SavingsScoreCurrentRun = co2SavingsScoreCurrentRun + actualComfortBonus;

        //Calculate the optimality percentage of the score
        double resultPercentage = Math.round((((co2SavingsScoreCurrentRun / (FR_Game.PERFECT_CO2SCORE_GRAM +perfectComfortBonus)) * 100 ) * 10.0)) / 10.0;

        if (resultPercentage >100) {
            resultPercentage = 100;
        }
        if (resultPercentage <0) {
            resultPercentage = 0;
        }


        //Display the results
        binding.textViewComfortResult.setText("Your comfort score is " + endComfortPercentage + "%" + " --> Bonus points: " + (int) actualComfortBonus);
        binding.textViewTotalScore.setText("Total Score: " + (int) (co2SavingsScoreCurrentRun ) + " (Optimality: " + resultPercentage + "%)");
        binding.textViewLevelFinishedMessageCO2.setText("Objective: " + (int) neededCO2SavingsScore+ " g of CO₂-savings and you got " + (int) (co2SavingsScoreCurrentRun - actualComfortBonus  ) + " g + " + (int) actualComfortBonus + " g");
        double gasSavingsKWH = Math.round(((co2SavingsScoreCurrentRun - actualComfortBonus) / FR_Game.PERFECT_CO2SCORE_GRAM * FR_Game.PERFECT_GAS_SAVING_KWH) * 10.0) / 10.0;

        binding.textViewLevelFinishedMessageGas.setText("You saved " + gasSavingsKWH + " kWh of Gas");


        if(FR_Settings.getLanguage(requireContext()).equals("de")) {
            binding.textViewComfortResult.setText("Dein Komfort Score ist " + endComfortPercentage + "%" + " --> Bonus Punkte: " + (int) actualComfortBonus);
            binding.textViewTotalScore.setText("Gesamtpunktzahl: " + (int) (co2SavingsScoreCurrentRun ) + " (Optimalität: " + resultPercentage + "%)");
            binding.textViewLevelFinishedMessageCO2.setText("Ziel: " + (int) neededCO2SavingsScore+ " g CO₂ einsparen. Du hast " + (int) (co2SavingsScoreCurrentRun -actualComfortBonus) + " g + " + (int) actualComfortBonus + " g");
            binding.textViewLevelFinishedMessageGas.setText("Du hast " + gasSavingsKWH + " kWh Gas eingespart");

        }

        this.co2SavingsScoreCurrentRunScoreSubmit =co2SavingsScoreCurrentRun;


        //Check if the level was successful
        binding.textViewAllLevelsFinished.setVisibility(View.INVISIBLE);
        int nextLevelUnlockedBecauseOfCurrentRun = 0;

        if (co2SavingsScoreCurrentRun>= neededCO2SavingsScore) {
            //Level passed
            binding.imageViewCloseSymbol.setVisibility(View.INVISIBLE);
            binding.imageViewCheckmark.setVisibility(View.VISIBLE);
            nextLevelUnlockedBecauseOfCurrentRun = 1;

            if (currentLevel<MainActivity.sqLite_DB.getNumberOfLevels()) {
                binding.imageViewNextLevelSymbol.setClickable(true);
                binding.imageViewNextLevelSymbol.setEnabled(true);
                binding.imageViewNextLevelSymbol.setAlpha(1.0f);
            }

            //The last level has been successfully passed
            if (currentLevel>=MainActivity.sqLite_DB.getNumberOfLevels()) {
                binding.textViewNextLevel.setAlpha(0.5f);
                binding.imageViewNextLevelSymbol.setClickable(false);
                binding.imageViewNextLevelSymbol.setEnabled(false);
                binding.imageViewNextLevelSymbol.setAlpha(0.5f);

                binding.textViewAllLevelsFinished.setVisibility(View.VISIBLE);
            }

        }
        else {
            //Level not passed
            binding.imageViewCloseSymbol.setVisibility(View.VISIBLE);
            binding.imageViewCheckmark.setVisibility(View.INVISIBLE);


            binding.textViewNextLevel.setAlpha(0.5f);
            binding.imageViewNextLevelSymbol.setClickable(false);
            binding.imageViewNextLevelSymbol.setEnabled(false);
            binding.imageViewNextLevelSymbol.setAlpha(0.5f);

        }


        //Update shared preferences with the results
        MainActivity.writeResultsToSharedPreferences (getContext(), resultPercentage, (int) co2SavingsScoreCurrentRun, gasSavingsKWH, this.currentLevel, nextLevelUnlockedBecauseOfCurrentRun, MainActivity.sqLite_DB.getNumberOfLevels());


        //Initialize highScore array list and the RV adapter
        arrayList_HighScore = new ArrayList<>();
        adapter_HighScore = new RV_Adapter_Highscore(arrayList_HighScore);
        buildRecyclerView();


        //Disable Submit button at the beginning (if will be enabled, if a highscore is made)
        binding.buttonSubmit.setEnabled(false);
        binding.buttonSubmit.setClickable(false);
        binding.buttonSubmit.setAlpha(0.5f);


        //Firebase components
        String FIREBASE_URL = BuildConfig.FIREBASE_URL;
        if (FirebaseApp.getApps(requireContext()).isEmpty()) {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setApplicationId("1:248974012708:android:3f93234ba2066ed41643b1")
                    .setDatabaseUrl(FIREBASE_URL)
                    .build();
            FirebaseApp.initializeApp(requireContext(), options);
        }

        rootRef_Firebase = FirebaseDatabase.getInstance(FIREBASE_URL).getReference();
        rootRef_Firebase.keepSynced(true);


        // Obtain a new or prior instance of the ViewModel from the ViewModelProviders utility class.
        viewModel = new ViewModelProvider(this).get(ViewModel_DialogFR_LevelEnd.class);
        viewModel.setFirebaseNodeLevel(currentLevel);
        viewModel.setPastTimeMillis(pastDaysForDisplayingScores);

        liveData = viewModel.getData();

        liveData.observe(this, dataSnapshot -> {
            // Clear the previous data
            arrayList_HighScore.clear();


            // Check if the snapshot is not null
            if (dataSnapshot != null) {
                // Convert dataSnapshot to a list for sorting
                List<DataSnapshot> snapshotList = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    snapshotList.add(ds);
                }

                // Sort the list based on "co2_score" in descending order
                snapshotList.sort((dataSnapshot1, dataSnapshot2) -> {
                    Integer co2Score1 = dataSnapshot1.child(FIREBASE_CO_2_SCORE).getValue(Integer.class);
                    Integer co2Score2 = dataSnapshot2.child(FIREBASE_CO_2_SCORE).getValue(Integer.class);

                    // If both values are non-null, compare them
                    if (co2Score1 != null && co2Score2 != null) {
                        return co2Score2.compareTo(co2Score1); // Sorting in descending order
                    }

                    // Handle null values by placing them at the end of the list
                    if (co2Score1 == null) {
                        return 1;
                    } else {
                        return -1;
                    }
                });

                // Iterate through the sorted list
                int position = 1;

                for (DataSnapshot ds : snapshotList) {
                    // Extract data from each snapshot
                    String name = ds.child(FIREBASE_NAME).getValue(String.class);

                    // Use a default value if co2Score is null
                    Integer co2Score = ds.child(FIREBASE_CO_2_SCORE).getValue(Integer.class);
                    int co2ScoreValue = (co2Score != null) ? co2Score : 0;  // Default to 0 if null

                    // Use a default value if level is null
                    Integer level = ds.child(FIREBASE_LEVEL).getValue(Integer.class);
                    int levelValue = (level != null) ? level : 0;  // Default to 0 if null

                    String date = ds.child(FIREBASE_DATE).getValue(String.class);

                    arrayList_HighScore.add(new RV_Item_Highscore(name, co2ScoreValue, date, levelValue, position++));
                }
            }

            //Filter the arrayList such that it only contains the n best items with highest co2Score
            arrayList_HighScore = arrayList_HighScore.stream()
                    .sorted(Comparator.comparingInt(RV_Item_Highscore::getCo2Score).reversed()) // Sort by co2Score in descending order
                    .limit(numberOfDisplayedHighScoreEntries).collect(Collectors.toCollection(ArrayList::new));


            // Notify the adapter of the dataset changes
            adapter_HighScore.updateData(arrayList_HighScore);

        });

        //Register the listeners for the clicks
        binding.imageViewRepeatSymbol.setOnClickListener(this);
        binding.imageViewGoToMenu.setOnClickListener(this);
        binding.imageViewNextLevelSymbol.setOnClickListener(this);
        binding.tbuttonHighScoreLastMonth.setOnClickListener(this);
        binding.tbuttonHighScoreLastWeek.setOnClickListener(this);
        binding.tbuttonHighScoreOverall.setOnClickListener(this);
        binding.buttonSubmit.setOnClickListener(this);

        //Check Highscore
        checkHighScore(co2SavingsScoreCurrentRun);

        return binding.getRoot();

    }


    /*
    This method compares the user's score for this level (CO2 savings) with the highscores for this level that have been saved on a Firebase Realtime Database
     */
    private void checkHighScore(double co2SavingsScoreCurrentRun) {
        //Firebase query to get all Highscores for this level
        long millisecondsThresholdLastWeek = System.currentTimeMillis() -  (7 * 24L * 60 * 60 * 1000);
        long millisecondsThresholdLastMonth = System.currentTimeMillis() -  (30 * 24L * 60 * 60 * 1000);
        long millisecondsThresholdOverall = 0;


        ArrayList<RV_Item_Highscore> arrayList_HighScore_LastWeek = new ArrayList<>();
        ArrayList<RV_Item_Highscore> arrayList_HighScore_LastMonth = new ArrayList<>();
        ArrayList<RV_Item_Highscore> arrayList_HighScore_Overall = new ArrayList<>();

        String firebaseNodeLevel = "levels/" + String.valueOf(currentLevel);

        rootRef_Firebase.child(firebaseNodeLevel).orderByChild(FIREBASE_DATE_IN_MILLISECONDS).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                //Find out position of the current co2SavingsScoreCurrentRun regarding all entries for the week, month and overall
                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    String name = ds.child(FIREBASE_NAME).getValue(String.class);

                    // Use a default value if co2ScoreHighScoreEntry is null
                    Integer co2ScoreHighScoreEntry = ds.child(FIREBASE_CO_2_SCORE).getValue(Integer.class);
                    int co2Score = (co2ScoreHighScoreEntry != null) ? co2ScoreHighScoreEntry : 0;  // Default to 0 if null

                    String date = ds.child(FIREBASE_DATE).getValue(String.class);

                    // Use a default value if level is null
                    Integer level = ds.child(FIREBASE_LEVEL).getValue(Integer.class);
                    int levelValue = (level != null) ? level : 0;  // Default to 0 if null

                    // Use a default value if dateInMillisecondsFirebaseEntryLong is null
                    Long dateInMillisecondsFirebaseEntryLong = ds.child(FIREBASE_DATE_IN_MILLISECONDS).getValue(Long.class);
                    long dateInMilliseconds = (dateInMillisecondsFirebaseEntryLong != null) ? dateInMillisecondsFirebaseEntryLong : 0L;  // Default to 0L if null


                    if (dateInMilliseconds > millisecondsThresholdLastWeek) {
                        arrayList_HighScore_LastWeek.add(new RV_Item_Highscore(name, co2Score, date, levelValue, 0));
                    }

                    if (dateInMilliseconds > millisecondsThresholdLastMonth) {
                        arrayList_HighScore_LastMonth.add(new RV_Item_Highscore(name, co2Score, date, levelValue, 0));
                    }

                    if (dateInMilliseconds > millisecondsThresholdOverall) {
                        arrayList_HighScore_Overall.add(new RV_Item_Highscore(name, co2Score, date, levelValue, 0));
                    }
                }

                //Filter the data to calculate the positions for this week, month and overall
                int positionOfCurrentScoreInHishScoreListLastWeek = 1;
                int positionOfCurrentScoreInHishScoreListLastMonth = 1;
                int positionOfCurrentScoreInHishScoreListOverall = 1;

                for (int i = 0; i < arrayList_HighScore_LastWeek.size(); i++) {
                    RV_Item_Highscore item = arrayList_HighScore_LastWeek.get(i);
                    if (item.getCo2Score() > co2SavingsScoreCurrentRun) {
                        positionOfCurrentScoreInHishScoreListLastWeek++;
                    }
                }

                for (int i = 0; i < arrayList_HighScore_LastMonth.size(); i++) {
                    RV_Item_Highscore item = arrayList_HighScore_LastMonth.get(i);
                    if (item.getCo2Score() > co2SavingsScoreCurrentRun) {
                        positionOfCurrentScoreInHishScoreListLastMonth++;
                    }
                }

                for (int i = 0; i < arrayList_HighScore_Overall.size(); i++) {
                    RV_Item_Highscore item = arrayList_HighScore_Overall.get(i);
                    if (item.getCo2Score() > co2SavingsScoreCurrentRun) {
                        positionOfCurrentScoreInHishScoreListOverall++;
                    }
                }

                if (FR_Settings.getLanguage(requireContext()).equals("de")) {
                    String text = "Position " + positionOfCurrentScoreInHishScoreListLastWeek + " letzte Woche\n"
                            + "Position " + positionOfCurrentScoreInHishScoreListLastMonth + " letzten Monat";
                    if (positionOfCurrentScoreInHishScoreListOverall <= 10) {
                        text += "\nPosition " + positionOfCurrentScoreInHishScoreListOverall + " insgesamt";
                    }
                    binding.textViewHighscoreMessagePositions.setText(text);
                } else {
                    String text = "Position " + positionOfCurrentScoreInHishScoreListLastWeek + " last Week\n"
                            + "Position " + positionOfCurrentScoreInHishScoreListLastMonth + " last Month";
                    if (positionOfCurrentScoreInHishScoreListOverall <= 10) {
                        text += "\nPosition " + positionOfCurrentScoreInHishScoreListOverall + " overall";
                    }
                    binding.textViewHighscoreMessagePositions.setText(text);
                }


                //If the current score is in the top 10 in one category, display the "Submit" button and handle the clicking event:
                if (positionOfCurrentScoreInHishScoreListLastWeek<=10) {
                    binding.buttonSubmit.setEnabled(true);
                    binding.buttonSubmit.setClickable(true);
                    binding.buttonSubmit.setAlpha(1.0f);
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle potential errors here
            }
        });

    }


    /*
    This method creates the RecyclerView for displaying the highscores
     */
    public void buildRecyclerView() {
        RecyclerView recyclerView_HighScore = binding.rvHighScoreStatisticsToBeDisplayed;
        recyclerView_HighScore.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager_HighScore = new LinearLayoutManager(this.getContext());

        recyclerView_HighScore.setLayoutManager(layoutManager_HighScore);
        recyclerView_HighScore.setAdapter(adapter_HighScore);
    }


    /*
    This methods handles the clicks on the buttons in the dialog
    */
    @Override
    public void onClick(View view) {
        if (view instanceof ToggleButton clickedButton) {
            //Only one Toggle Button should be activated at a time
            if (clickedButton == binding.tbuttonHighScoreLastWeek) {
                binding.tbuttonHighScoreLastWeek.setChecked(true);
                binding.tbuttonHighScoreLastMonth.setChecked(false);
                binding.tbuttonHighScoreOverall.setChecked(false);
            } else if (clickedButton == binding.tbuttonHighScoreLastMonth) {
                binding.tbuttonHighScoreLastMonth.setChecked(true);
                binding.tbuttonHighScoreLastWeek.setChecked(false);
                binding.tbuttonHighScoreOverall.setChecked(false);
            } else if (clickedButton == binding.tbuttonHighScoreOverall) {
                binding.tbuttonHighScoreOverall.setChecked(true);
                binding.tbuttonHighScoreLastWeek.setChecked(false);
                binding.tbuttonHighScoreLastMonth.setChecked(false);
            }

            // set the parameters for the Firebase query to get the highscores
            if (binding.tbuttonHighScoreLastWeek.isChecked()) {
                this.pastDaysForDisplayingScores = 7;
            }
            if (binding.tbuttonHighScoreLastMonth.isChecked()) {
                this.pastDaysForDisplayingScores = 30;
            }
            if (binding.tbuttonHighScoreOverall.isChecked()) {
                this.pastDaysForDisplayingScores = 10000;
            }

        }

        NavController navController = Navigation.findNavController(requireActivity(), R.id.navHostfragment);
        viewModel.setPastTimeMillis(pastDaysForDisplayingScores);
        liveData.forceUpdate();

        //Repeat the level if the repeat button is pressed
        if (view.getId() == R.id.imageView_RepeatSymbol) {
            navController.navigate(DialogFR_LevelEndDirections.actionDialogFRLevelEndToFRGame());
        }

        if (view.getId() == R.id.imageView_goToMenu) {
            navController.navigate(DialogFR_LevelEndDirections.actionDialogFRLevelEndToFRMenu());
        }

        //Start the next level if the next level button is pressed
        if (view.getId() == R.id.imageView_NextLevelSymbol) {
            FR_Game.currentLevel++;
            navController.navigate(DialogFR_LevelEndDirections.actionDialogFRLevelEndToFRGame());
        }

        //Enter the name if the Submit button for highscore is pressed
        if (view.getId() == R.id.button_submit) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Name");

            // Set up the input
            final EditText input = new EditText(getActivity());
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
            input.setSingleLine(false);
            input.setLines(3);
            input.setText(name_input_text);
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton("OK", (dialog, which) -> {
                name_input_text = input.getText().toString();

                //Disable Submit button
                binding.buttonSubmit.setEnabled(false);
                binding.buttonSubmit.setClickable(false);
                binding.buttonSubmit.setAlpha(0.5f);

                //Show message that highscore was submitted
                binding.textViewHishScoreSubmitMessage.setText(R.string.highscore_submitted);


                // Construct the node name using the current date and time and 2 random characters
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss", Locale.getDefault());
                String entryName = "entry_" + dateFormat.format(calendar.getTime());
                Random random = new Random();
                char randomChar1 = (char) ('a' + random.nextInt(26)); // Random lowercase letter
                char randomChar2 = (char) ('a' + random.nextInt(26)); // Random lowercase letter
                entryName += "_" + randomChar1  + randomChar2;

                // Format the date string in "dd.MM.yyyy" format
                SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                String dateStringVariable = dateFormatter.format(calendar.getTime());

                // Get the time in milliseconds
                long dateInMillisecondsVariable = calendar.getTimeInMillis();
                // Construct the data object
                Map<String, Object> entryData = new HashMap<>();
                entryData.put("co2_score", co2SavingsScoreCurrentRunScoreSubmit);
                entryData.put("date", dateStringVariable);
                entryData.put("date_in_milliseconds", dateInMillisecondsVariable);
                entryData.put("level", currentLevel);
                entryData.put("name", name_input_text);

                // Get the reference to the level node
                String firebaseNodeLevel = "levels/" + String.valueOf(currentLevel);
                DatabaseReference levelRef = rootRef_Firebase.child(firebaseNodeLevel);

                // Write the data to Firebase
                levelRef.child(entryName).setValue(entryData)
                        .addOnSuccessListener(aVoid -> {
                            // Data was successfully written
                            Log.d("Firebase", "Data successfully written to Firebase");
                            binding.tbuttonHighScoreLastWeek.performClick();
                            binding.tbuttonHighScoreLastWeek.setChecked(true);
                        })
                        .addOnFailureListener(e -> {
                            // Handle any errors
                            Log.e("Firebase", "Error writing data to Firebase: " + e.getMessage());
                        });
            });
            // Set up the Privacy Policy button as a neutral button
            builder.setNeutralButton(R.string.privacy_policy, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    showPrivacyPolicyDialog();
                }
            });

            liveData.forceUpdate();
            builder.show();
        }
    }

    /*
    This method creates the dialog for the privacy policy. When you want to submit your highscore, you can read the privacy policy.
     */
    private void showPrivacyPolicyDialog() {
        AlertDialog.Builder policyBuilder = new AlertDialog.Builder(getActivity());
        policyBuilder.setTitle(R.string.privacy_policy);
        policyBuilder.setMessage(R.string.privacy_policy_message);

        policyBuilder.setPositiveButton("OK", null);
        AlertDialog policyDialog = policyBuilder.create();
        policyDialog.show();
    }


    /*
    This method is an auxiliary method for getting the size of the screen
     */
    static Point getAvailableDisplaySize(Context context) {
        if (context instanceof Activity && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowMetrics metrics = ((Activity) context).getWindowManager().getCurrentWindowMetrics();
            WindowInsets windowInsets = metrics.getWindowInsets();
            Insets insets = windowInsets.getInsetsIgnoringVisibility(
                    WindowInsets.Type.navigationBars()
                            | WindowInsets.Type.displayCutout()
            );
            int width = metrics.getBounds().width() - insets.right + insets.left;
            int height = metrics.getBounds().height() - insets.top + insets.bottom;
            return new Point(width, height);
        } else {
            Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            return size;
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        isDialogShown = false;
    }
}