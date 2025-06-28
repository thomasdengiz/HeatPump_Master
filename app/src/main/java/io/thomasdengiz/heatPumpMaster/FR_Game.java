package io.thomasdengiz.heatPumpMaster;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;

import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import io.thomasdengiz.heatPumpMaster.databinding.FragmentGameBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


/*
This is the main class for the game fragment. It contains the logic for the game.
 */

public class FR_Game extends Fragment  {


    /*
    Game variables
     */

    public static final int DELAY_IN_MILLIS = 100;
    public static final int TIME_OF_A_LEVEL_IN_SECONDS = 30;

    public static final int PERFECT_CO2SCORE_GRAM = 13566;
    public static final double PERFECT_GAS_SAVING_KWH = 65.5;
    private int totalTimeSlotsForTheLevel;

    private int neededCO2SavingsInTheLevel;
    
    private double perfectScoreInTheLevel;

    public static int currentLevel = -1;
    private int currentTimeLeftInTheLevel_MILLIS;
    private int currentTimeSlot;

    private final Handler handler = new Handler();

    private int currentComfort;
    private double currentComfortHelpValue;


    //Variables for handling the flying buttons
    private final String FLYING_BUTTON_SHOWER = "Flying Button Shower";
    private final String FLYING_BUTTON_AIR = "Flying Button Air";

    private boolean isFlyingButtonPressedShower = false;
    private boolean isFlyingButtonPressedAir  = false;

    private boolean isFlyingButtonGeneratedShower = false;
    private boolean isFlyingButtonGeneratedAir  = false;

    private int helpCounterFlyingButtonShowerPressed = 0;
    private int helpCounterFlyingButtonAirPressed = 0;

    private int helpCounterTimeSlotsFlyingButtonShowerGenerated = 0;
    private int helpCounterTimeSlotsFlyingButtonAirGenerated = 0;

    private final int limitTimeSlotsFlyingButtonAction = 14;

    private double probabilityForFlyingButtonShowerCurrentTimeslot;
    private double probabilityForFlyingButtonAirCurrentTimeslot;




    private boolean heatBuilding = true;
    private boolean heatDHWTank = false;

    float rotationAngle = 0;
    int widthDisplay;
    int heightDisplay;
    int helpCounterUpdateScreen =0;
    int helpCounterCountDownTime =0;

    //Type of View_Game_Events
    public static final String VIEW_EVENT_RECTANGLE_SOLAR = "Solar";
    public static final String VIEW_EVENT_RECTANGLE_WIND = "Wind";
    public static final String VIEW_EVENT_RECTANGLE_COAL = "Coal";
    public static final String VIEW_EVENT_RECTANGLE_GAS = "Gas";


    private FragmentGameBinding binding;

    private ConstraintLayout constraintLayout;
    ConstraintSet constraintSet ;

    //Variables for the single view event
    View_Game_Event_Rectangle[] viewEvent;
    boolean [] isViewEventActive;
    Drawable[] drawingsForTheViewEvents;
    ArrayList<View_Game_Event_Rectangle> arrayList_GameEventRectangles;

    private int currentPointsThisLevel;
    private int currentCO2SavingsThisLevel;

    private HotWaterTank hotWaterTank;
    private Thermometer thermometer;


    int helpUpdateCounterProgressBar = 0;//Just for testing

    boolean animationIsWindBladeRotating = false;


    private CountDownTimer cdt;
    private  final long DURATION = 900000L; //40sec
    private  final long DELAY_COUNT_DOWN_TIMER = 100; //100ms

    private double [] speedMultiplicatorLevel;

    View_Game_Event_Rectangle currentlyActiveEventRectangleInTarget = null;
    View_Game_Event_Rectangle lastIntervalActiveEventRectangleInTarget = null;


    private boolean sunIsShiningForImageViews = false;
    
    private boolean helpSolarGameRectangleCorrectlyCaughtPreviously = false;


    // Variables for the audio files
    private static final long PLAY_INTERVAL_AUDIO_MILLISECONDS = 8000;

    private final Map<Integer, Long> lastPlayTimesSounds = new HashMap<>();

    private SoundPool soundPool;
    private final Map<Integer, Integer> soundMap = new HashMap<>();



    private boolean helpIndicatorPointsAlreadyCountedDuringCurrentTimeSlot = false;

    public FR_Game() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int numberOfViewEventInArray = 10;
        viewEvent = new View_Game_Event_Rectangle[numberOfViewEventInArray];
        drawingsForTheViewEvents = new Drawable[numberOfViewEventInArray];
        arrayList_GameEventRectangles = new ArrayList<>();
        currentPointsThisLevel = 0;
        currentCO2SavingsThisLevel = 0;

        isViewEventActive = new boolean[numberOfViewEventInArray];

        //Initialize speed multiplicator values for the animated game elements for all levels
        speedMultiplicatorLevel = new double [MainActivity.sqLite_DB.getNumberOfLevels()];
        for (int i = 0; i<speedMultiplicatorLevel.length; i++) {
            speedMultiplicatorLevel[i] = MainActivity.sqLite_DB.getSpeedMultiplicator(i+1);
        }


        double averageNumberOfFlyingButtonPerLevelAir = 2.5;
        double averageNumberOfFlyingButtonPerLevelShower = 2.5;


        totalTimeSlotsForTheLevel = (int)(TIME_OF_A_LEVEL_IN_SECONDS * (1000.0/DELAY_IN_MILLIS)) ;
        probabilityForFlyingButtonShowerCurrentTimeslot = averageNumberOfFlyingButtonPerLevelShower /totalTimeSlotsForTheLevel;
        probabilityForFlyingButtonAirCurrentTimeslot = averageNumberOfFlyingButtonPerLevelAir /totalTimeSlotsForTheLevel;


        //Initialize SoundPool
        initSoundPool(getContext());

    }


    /*
    This method initializes the SoundPool by preloading the audio files for the sound effects from res/raw folder
     */
    private void initSoundPool(Context context) {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(4)
                .setAudioAttributes(audioAttributes)
                .build();

        int[] rawResIds = new int[] {
                R.raw.app_game_shower_1,
                R.raw.app_game_warning_1,
                R.raw.app_game_wind_1,
                R.raw.app_game_window_1
        };

        for (int resId : rawResIds) {
            int soundId = soundPool.load(context, resId, 1);
            soundMap.put(resId, soundId);
            lastPlayTimesSounds.put(Integer.valueOf(String.valueOf(resId)), 0L); // Initialize cooldown map
        }

        for (int resId : rawResIds) {
            // Ensure there’s a record—even if never played—so getOrDefault(resId,0L) finds something:
            lastPlayTimesSounds.put(resId, 0L);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentGameBinding.inflate(inflater, container, false);


        //Load the background with glide
        View rootView = binding.getRoot();

        // Decide which background to load based on screen size.
        String drawableName;
        if (getResources().getConfiguration().smallestScreenWidthDp >= 600) {
            drawableName = "game_background_tablet";
        } else {
            drawableName = "game_background_phone";
        }

        // Get the drawable resource ID by name.
        int drawableId;
        if (getResources().getConfiguration().smallestScreenWidthDp >= 600) {
            drawableId = R.drawable.game_background_tablet;
        } else {
            drawableId = R.drawable.game_background_phone;
        }
        // Proceed only if the drawable exists.
        if (drawableId != 0) {

            int width = rootView.getLayoutParams().width;   // From XML
            int height = rootView.getLayoutParams().height; // From XML

            // Use Glide to load and resize the drawable, then set it as the background.
            Glide.with(this)
                    .load(drawableId)
                    .override(width, height) // resize to the dimensions defined in XML
                    .into(new CustomTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            rootView.setBackground(resource);
                        }
                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            // Optionally handle cleanup.
                        }
                    });
        }


        WindowManager wm =  requireActivity().getWindowManager();
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        widthDisplay = size.x;
        heightDisplay = size.y;


        constraintLayout= binding.constraintLayout;
        constraintLayout.bringChildToFront(binding.imageViewTargetRectangle);



        hotWaterTank = binding.hotWaterTank;
        thermometer = binding.thermometer;
        hotWaterTank.changeVolumeBar(50);

        hotWaterTank.invalidate();
        thermometer.setTemperature(21);
        thermometer.invalidate();

        // Load the animation from the XML file for the start
        Animation blinkAnimation = AnimationUtils.loadAnimation(this.getContext(), R.anim.blink_animation);
        binding.imageViewTargetRectangle.startAnimation(blinkAnimation);
        binding.imageViewTargetRectangle.bringToFront();

        binding.imageViewSun.setVisibility(View.INVISIBLE);
        binding.imageViewSunRays.setVisibility(View.INVISIBLE);



        // Define and register RepeatListener on the heat button
        binding.buttonHeat.setOnTouchListener(new RepeatListener(30, 30, this.getContext(), view -> {
            rotationAngle -= 11.0F;
            binding.fan.setRotation(-rotationAngle);
            currentlyActiveEventRectangleInTarget = checkPositionsOfActiveElements(true);

            if (currentlyActiveEventRectangleInTarget!= lastIntervalActiveEventRectangleInTarget) {
                if (lastIntervalActiveEventRectangleInTarget !=null && lastIntervalActiveEventRectangleInTarget.getEventType().equals(FR_Game.VIEW_EVENT_RECTANGLE_SOLAR)) {
                    lastIntervalActiveEventRectangleInTarget.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.game_event_rectangle_solar_1));
                }
                if (lastIntervalActiveEventRectangleInTarget !=null && lastIntervalActiveEventRectangleInTarget.getEventType().equals(FR_Game.VIEW_EVENT_RECTANGLE_WIND)) {
                    lastIntervalActiveEventRectangleInTarget.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.game_event_rectangle_wind_1));
                }
                if (lastIntervalActiveEventRectangleInTarget !=null && lastIntervalActiveEventRectangleInTarget.getEventType().equals(FR_Game.VIEW_EVENT_RECTANGLE_GAS)) {
                    lastIntervalActiveEventRectangleInTarget.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.game_event_rectangle_gas_1));
                }
                if (lastIntervalActiveEventRectangleInTarget !=null && lastIntervalActiveEventRectangleInTarget.getEventType().equals(FR_Game.VIEW_EVENT_RECTANGLE_COAL)) {
                    lastIntervalActiveEventRectangleInTarget.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.game_event_rectangle_coal_1));
                }
                lastIntervalActiveEventRectangleInTarget = currentlyActiveEventRectangleInTarget;
            }
            else {
                if (lastIntervalActiveEventRectangleInTarget !=null && lastIntervalActiveEventRectangleInTarget.getEventType().equals(FR_Game.VIEW_EVENT_RECTANGLE_SOLAR)) {
                    lastIntervalActiveEventRectangleInTarget.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.game_event_rectangle_solar_2));
                }
                if (lastIntervalActiveEventRectangleInTarget !=null && lastIntervalActiveEventRectangleInTarget.getEventType().equals(FR_Game.VIEW_EVENT_RECTANGLE_WIND)) {
                    lastIntervalActiveEventRectangleInTarget.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.game_event_rectangle_wind_2));
                }
                if (lastIntervalActiveEventRectangleInTarget !=null && lastIntervalActiveEventRectangleInTarget.getEventType().equals(FR_Game.VIEW_EVENT_RECTANGLE_GAS)) {
                    lastIntervalActiveEventRectangleInTarget.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.game_event_rectangle_gas_2));
                }
                if (lastIntervalActiveEventRectangleInTarget !=null && lastIntervalActiveEventRectangleInTarget.getEventType().equals(FR_Game.VIEW_EVENT_RECTANGLE_COAL)) {
                    lastIntervalActiveEventRectangleInTarget.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.game_event_rectangle_coal_2));
                }
            }

            // the code to execute repeatedly if the Repeatlistener is hold pressed
            if(heatBuilding) {
                thermometer.changeTemperature(0.6);
                RepeatListener.setCurrentlyActiveGameRectangle(currentlyActiveEventRectangleInTarget);

            }

            if (heatDHWTank) {
                hotWaterTank.changeVolumeBar(0.6);
                RepeatListener.setCurrentlyActiveGameRectangle(currentlyActiveEventRectangleInTarget);
            }

            lastIntervalActiveEventRectangleInTarget = currentlyActiveEventRectangleInTarget;
            view.performClick();

        }));




        // Define and register OnCheckedChangeListener on the switch button
        binding.switchStorage.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                heatBuilding = false;
                heatDHWTank = true;
            }

            if (!isChecked) {
                heatBuilding = true;
                heatDHWTank = false;
            }

        });

        //Change color of the progress bars
        Drawable progressDrawable_CO2Bar = binding.progressBarCO2Savings.getProgressDrawable().mutate();
        progressDrawable_CO2Bar.setColorFilter(Color.GREEN, android.graphics.PorterDuff.Mode.SRC_IN);
        binding.progressBarCO2Savings.setProgressDrawable(progressDrawable_CO2Bar);

        Drawable progressDrawable_ComfortBar = binding.progressBarComfortLevel.getProgressDrawable().mutate();
        progressDrawable_ComfortBar.setColorFilter(Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
        binding.progressBarComfortLevel.setProgressDrawable(progressDrawable_ComfortBar);

        Drawable progressDrawable_TimeLeft = binding.progressBarTimeLeft.getProgressDrawable().mutate();
        progressDrawable_TimeLeft.setColorFilter(Color.BLUE, android.graphics.PorterDuff.Mode.SRC_IN);
        binding.progressBarTimeLeft.setProgressDrawable(progressDrawable_TimeLeft);


        //Set the inital values of the progress bars for CO2, comofort and time
        binding.progressBarCO2Savings.setProgress(50);
        binding.progressBarComfortLevel.setProgress(50);
        binding.progressBarTimeLeft.setProgress(100);


        //Adjust the translation of the wind turbine blades such that it fits the pole
        float fraction = (float)0.55;
        androidx.core.view.ViewKt.doOnPreDraw(binding.imageViewWindTurbineBlade, view -> {
            binding.imageViewWindTurbineBlade.setTranslationY( fraction * binding.imageViewWindTurbinePole.getHeight());
            return null;
        });


        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        constraintLayout = binding.constraintLayout;
        constraintSet = new ConstraintSet();
        startRound();
        return binding.getRoot();


    }//end onCreateView


    /*
    Returns the active element in the target rectangle and possibly performs a game step (checking which element is in the rectangle and performing actions accordingly)
     */
    int helpCounterCorrectTicks = 0;
    int helpCounterOverallNumberTicks = 0;
    public View_Game_Event_Rectangle checkPositionsOfActiveElements (boolean performGameStep) {
        boolean gameEventInTargetRectangle = false;
        String activeElementType = "";
        View_Game_Event_Rectangle activeElement = null;

        for (int currentElement =0; currentElement <arrayList_GameEventRectangles.size(); currentElement++) {

            if (arrayList_GameEventRectangles.get(currentElement).isActive()) {
                if (arrayList_GameEventRectangles.get(currentElement).getX() <= (binding.imageViewTargetRectangle.getX() + binding.imageViewTargetRectangle.getWidth()) && (arrayList_GameEventRectangles.get(currentElement).getX()+ arrayList_GameEventRectangles.get(currentElement).getWidth()) >= binding.imageViewTargetRectangle.getX() && currentTimeSlot >arrayList_GameEventRectangles.get(currentElement).getStartingTimeSlot() + 2){
                    gameEventInTargetRectangle = true;
                    activeElementType = arrayList_GameEventRectangles.get(currentElement).getEventType();
                    activeElement = arrayList_GameEventRectangles.get(currentElement);
                }
            }
        }

        helpCounterOverallNumberTicks++;
        if (performGameStep && !helpIndicatorPointsAlreadyCountedDuringCurrentTimeSlot) {

            if (!gameEventInTargetRectangle) {
                currentPointsThisLevel = currentPointsThisLevel - 1;
                binding.textViewPointsSymbol.setText("-");
                binding.textViewPointsSymbol.setTextColor(Color.parseColor("#fd5353"));
                new Handler().postDelayed(() -> {
                    helpSolarGameRectangleCorrectlyCaughtPreviously = false;
                    binding.textViewPointsSymbol.setText("");
                }, 500);
            }

            if (gameEventInTargetRectangle) {
                if (activeElementType.equals(VIEW_EVENT_RECTANGLE_SOLAR) || activeElementType.equals(VIEW_EVENT_RECTANGLE_WIND) ) {
                    currentPointsThisLevel = currentPointsThisLevel + 1;
                    helpCounterCorrectTicks++;
                    //binding.textViewHitNoHit.setText("Used Solar");
                    if (!helpSolarGameRectangleCorrectlyCaughtPreviously) {
                        binding.textViewPointsSymbol.setText("+");
                        binding.textViewPointsSymbol.setTextColor(Color.parseColor("#1af6a3"));
                        helpSolarGameRectangleCorrectlyCaughtPreviously = true;

                        new Handler().postDelayed(() -> {
                            helpSolarGameRectangleCorrectlyCaughtPreviously = false;
                            binding.textViewPointsSymbol.setText("");
                        }, 500);
                    }
                }



                if (activeElementType.equals(VIEW_EVENT_RECTANGLE_GAS) ) {
                    currentPointsThisLevel = currentPointsThisLevel - 2;

                    binding.textViewPointsSymbol.setText("--");
                    binding.textViewPointsSymbol.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50);
                    binding.textViewPointsSymbol.setTextColor(Color.parseColor("#fd5353"));

                    new Handler().postDelayed(() -> {
                        helpSolarGameRectangleCorrectlyCaughtPreviously = false;
                        binding.textViewPointsSymbol.setText("");
                    }, 500);
                }

                if ( activeElementType.equals(VIEW_EVENT_RECTANGLE_COAL)) {
                    currentPointsThisLevel = currentPointsThisLevel - 4;

                    binding.textViewPointsSymbol.setText("---");
                    binding.textViewPointsSymbol.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50);
                    binding.textViewPointsSymbol.setTextColor(Color.parseColor("#fd5353"));

                    new Handler().postDelayed(() -> {
                        helpSolarGameRectangleCorrectlyCaughtPreviously = false;
                        binding.textViewPointsSymbol.setText("");
                    }, 500);
                }

            }
            helpIndicatorPointsAlreadyCountedDuringCurrentTimeSlot = true;
        }

        return  activeElement;
    }



    /*
    This method starts the level
     */
    public void startRound () {
        if (currentLevel ==-1) {
            currentLevel = 1;
        }

        DialogFR_LevelEnd.isDialogShown = false;

        if (currentLevel >MainActivity.sqLite_DB.getNumberOfLevels()) {
            currentLevel = MainActivity.sqLite_DB.getNumberOfLevels();
        }

        perfectScoreInTheLevel = MainActivity.sqLite_DB.getBaselineScoreForTheLevel(currentLevel);
        neededCO2SavingsInTheLevel = (int) ((MainActivity.sqLite_DB.getNeededPercentageScoreForTheLevel(currentLevel)/100) * PERFECT_CO2SCORE_GRAM + ((DialogFR_LevelEnd.desiredComfortBonusScorePercentage/100) * PERFECT_CO2SCORE_GRAM));

        int[] pvValuesForTheLevel = MainActivity.sqLite_DB.getDataDB_TableLevelElements(currentLevel, DB_SQLite_Asset_Helper.PV);
        int[] windValuesForTheLevel = MainActivity.sqLite_DB.getDataDB_TableLevelElements(currentLevel, DB_SQLite_Asset_Helper.WIND);
        int[] fossilValuesForTheLevel = MainActivity.sqLite_DB.getDataDB_TableLevelElements(currentLevel, DB_SQLite_Asset_Helper.FOSSIL);

        currentTimeLeftInTheLevel_MILLIS =TIME_OF_A_LEVEL_IN_SECONDS * 1000;
        currentTimeSlot =0;
        currentComfort = 50;
        currentComfortHelpValue = currentComfort;

        //Set the goal stroke in the CO2 savings progress bar
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) binding.goalStroke.getLayoutParams();
        layoutParams.horizontalBias = (float) ((MainActivity.sqLite_DB.getNeededPercentageScoreForTheLevel(currentLevel)) / 100.0);
        binding.goalStroke.setLayoutParams(layoutParams);


        //Set the goal stroke in the comfort progress bar
        ConstraintLayout.LayoutParams layoutParamsComfort = (ConstraintLayout.LayoutParams) binding.goalStrokeComfort.getLayoutParams();
        layoutParamsComfort.horizontalBias = (float) ( DialogFR_LevelEnd.desiredComfortBonusScorePercentage / DialogFR_LevelEnd.maximumComfortBonusScorePercentage);
        binding.goalStrokeComfort.setLayoutParams(layoutParamsComfort);


        //Create the array list with the Game_Event_Rectangles
        for (int i = 0; i< pvValuesForTheLevel.length; i++) {

            if (pvValuesForTheLevel[i] >0) {

                arrayList_GameEventRectangles.add(new View_Game_Event_Rectangle(getActivity(), VIEW_EVENT_RECTANGLE_SOLAR, i+1, pvValuesForTheLevel[i]));
            }

            if (windValuesForTheLevel[i] >0) {
                arrayList_GameEventRectangles.add(new View_Game_Event_Rectangle(getActivity(), VIEW_EVENT_RECTANGLE_WIND, i+1, windValuesForTheLevel[i]));
            }

            if (fossilValuesForTheLevel[i] >0) {
                double randomNumber = Math.random();
                if (randomNumber >= 0.5) {
                    arrayList_GameEventRectangles.add(new View_Game_Event_Rectangle(getActivity(), VIEW_EVENT_RECTANGLE_GAS, i+1, fossilValuesForTheLevel[i]));
                }
                else{
                    arrayList_GameEventRectangles.add(new View_Game_Event_Rectangle(getActivity(), VIEW_EVENT_RECTANGLE_COAL, i+1, fossilValuesForTheLevel[i]));
                }

            }
        }

        countDownTime();


    }




    /*
    This method updates the screen of the fragment. It is repeatedly called after every time step of the game
     */
    @SuppressLint("SetTextI18n")
    private void updateScreen() {

        helpIndicatorPointsAlreadyCountedDuringCurrentTimeSlot = false;

        // Calculate current Co2 savings based on the current points
         currentCO2SavingsThisLevel = (int) (((double) currentPointsThisLevel / perfectScoreInTheLevel) * PERFECT_CO2SCORE_GRAM);

        //Display the points for this level
        binding.textViewScoreTotalCO2Value.setText(currentCO2SavingsThisLevel + " g");
        constraintLayout.bringChildToFront(binding.imageViewTargetRectangle);

        //Change progress bar
        helpUpdateCounterProgressBar++;
        if (helpUpdateCounterProgressBar ==50) {
            helpUpdateCounterProgressBar =0;
        }

        //Change progress bar for the time left
        int timeLeftProgress = currentTimeLeftInTheLevel_MILLIS/1000;
        //int timeLeftPercentage = (int)((currentTimeLeftInTheLevel_MILLIS / 1000) / TIME_OF_A_LEVEL_IN_SECONDS);
        int timeLeftPercentage = (int)(((double)currentTimeLeftInTheLevel_MILLIS / 1000 / TIME_OF_A_LEVEL_IN_SECONDS) * 100);



        binding.circularProgressBar.setProgress(timeLeftPercentage);
        binding.progressBarTimeLeft.setProgress(timeLeftProgress);



        //Change position of the bar events
        binding.textViewLevelValue.setText("" + currentLevel);
        int progressScoreOfTheLevel = (int) ((currentPointsThisLevel/perfectScoreInTheLevel)*100);
        binding.progressBarCO2Savings.setProgress(progressScoreOfTheLevel, true);



        currentComfort = (int) currentComfortHelpValue;
        //limit the values of current comfort
        if (currentComfort>100){
            currentComfort = 100;
        }
        if (currentComfort<0){
            currentComfort = 0;
        }
        binding.progressBarComfortLevel.setProgress(currentComfort, true);

        boolean visibilityWarningThermometer = false;
        boolean visibilityWarningHotWaterTank = false;

        //Check temperature and hot water level
        if (binding.thermometer.getPositionOfTemperatureBar() > binding.thermometer.value_positionOfTemperatureBar_20Degrees) {
            binding.textViewWarningTemperature.setText(getString(R.string.temperature_too_low_warning));
            currentComfortHelpValue = currentComfortHelpValue - 0.4;
            visibilityWarningThermometer = true;
        }
        if (binding.thermometer.getPositionOfTemperatureBar() < binding.thermometer.value_positionOfTemperatureBar_22Degrees) {
            binding.textViewWarningTemperature.setText(getString(R.string.temperature_too_high_warning));
            currentComfortHelpValue = currentComfortHelpValue - 0.4;
            visibilityWarningThermometer = true;
        }

        if (binding.thermometer.getPositionOfTemperatureBar() <= binding.thermometer.value_positionOfTemperatureBar_20Degrees && binding.thermometer.getPositionOfTemperatureBar() >= binding.thermometer.value_positionOfTemperatureBar_22Degrees) {
            binding.textViewWarningTemperature.setText("");
            currentComfortHelpValue = currentComfortHelpValue + 0.05;
            visibilityWarningThermometer = false;
        }


        if (binding.hotWaterTank.getPositionOfWaterBar() > binding.hotWaterTank.value_positionOfWaterBar_Empty) {
            binding.textViewWarningHotWater.setText(getString(R.string.water_not_engough_warning));
            currentComfortHelpValue = currentComfortHelpValue - 0.3;
            binding.imageViewWarning.setVisibility(View.VISIBLE);
            visibilityWarningHotWaterTank = true;
        }

        if (binding.hotWaterTank.getPositionOfWaterBar() < binding.hotWaterTank.value_positionOfWaterBar_Full) {
            binding.textViewWarningHotWater.setText(getString(R.string.water_too_much_warning));
            currentComfortHelpValue = currentComfortHelpValue - 0.3;
            binding.imageViewWarning.setVisibility(View.VISIBLE);
            visibilityWarningHotWaterTank = true;
        }

        if (binding.hotWaterTank.getPositionOfWaterBar() <= binding.hotWaterTank.value_positionOfWaterBar_Empty && binding.hotWaterTank.getPositionOfWaterBar() >= binding.hotWaterTank.value_positionOfWaterBar_Full) {
            binding.textViewWarningHotWater.setText("");
            currentComfortHelpValue = currentComfortHelpValue + 0.05;
            visibilityWarningHotWaterTank = false;
        }

        if (visibilityWarningThermometer || visibilityWarningHotWaterTank) {
            binding.imageViewWarning.setVisibility(View.VISIBLE);
            playAudioFile(R.raw.app_game_warning_1);
        }
        else {
            binding.imageViewWarning.setVisibility(View.INVISIBLE);
        }

        if (isFlyingButtonPressedShower || isFlyingButtonPressedAir) {
            currentComfortHelpValue = currentComfortHelpValue + 0.45;
        }


        //Set the visibility of the animated flying button image views (that occur after the buttons have been pressed)
        if (!isFlyingButtonPressedAir) {
            binding.imageViewOpenWindow.setVisibility(View.INVISIBLE);
        }
        if (isFlyingButtonPressedAir){
            binding.imageViewOpenWindow.setVisibility(View.VISIBLE);
        }

        if (!isFlyingButtonPressedShower) {
            binding.imageViewHotWaterShower.setVisibility(View.INVISIBLE);
        }
        if (isFlyingButtonPressedShower){
            binding.imageViewHotWaterShower.setVisibility(View.VISIBLE);
        }



        //Randomly create flying buttons
        double randomNumberFlyingButtonShower = Math.random();
        if (randomNumberFlyingButtonShower< probabilityForFlyingButtonShowerCurrentTimeslot && currentTimeSlot > 50 && !isFlyingButtonPressedShower && !isFlyingButtonGeneratedShower) {
            generateFlyingButton(FLYING_BUTTON_SHOWER);
            isFlyingButtonGeneratedShower  = true;
        }

        double randomNumberFlyingButtonAir = Math.random();
        if (randomNumberFlyingButtonAir< probabilityForFlyingButtonAirCurrentTimeslot  && currentTimeSlot > 50 && !isFlyingButtonPressedAir && !isFlyingButtonGeneratedAir) {
            generateFlyingButton(FLYING_BUTTON_AIR);
            isFlyingButtonGeneratedAir = true;

        }



        binding.imageViewWindTurbineBlade.bringToFront();



        //Do a wind animation if the current active element is a wind rectangle
        View_Game_Event_Rectangle currentlyActiveEventRectangleInTargetForAnimation = checkPositionsOfActiveElements(false);
        if (!animationIsWindBladeRotating && currentlyActiveEventRectangleInTargetForAnimation!= null && currentlyActiveEventRectangleInTargetForAnimation.getEventType().equals(VIEW_EVENT_RECTANGLE_WIND)) {
            ObjectAnimator rotationAnimator;
            float rotationAngle;
            if (Math.random() < 0.5) {
                rotationAngle = 360f;
            } else {
                rotationAngle = -360f;
            }

            long duration = (long)500 + (long)(Math.random() * 1400);


            rotationAnimator = ObjectAnimator.ofFloat(binding.imageViewWindTurbineBlade, "rotation", 0f, rotationAngle);
            rotationAnimator.setDuration(duration);
            rotationAnimator.setRepeatCount(2);
            rotationAnimator.start();
            animationIsWindBladeRotating = true;
            new Handler().postDelayed(() -> animationIsWindBladeRotating = false, 4000);

        }

        //Play the wind audio file if the current active element is a wind rectangle
        View_Game_Event_Rectangle currentlyActiveEventRectangleInTargetForSound = checkPositionsOfActiveElements(false);
        if (currentlyActiveEventRectangleInTargetForSound != null && currentlyActiveEventRectangleInTargetForSound.getEventType().equals(VIEW_EVENT_RECTANGLE_WIND)) {
            playAudioFile(R.raw.app_game_wind_1);
        }


        //Display the sun and the rays if the current active element is a sun rectangle

        if (!sunIsShiningForImageViews && currentlyActiveEventRectangleInTargetForAnimation!= null && currentlyActiveEventRectangleInTargetForAnimation.getEventType().equals(VIEW_EVENT_RECTANGLE_SOLAR)) {
            binding.imageViewSun.setVisibility(View.VISIBLE);
            binding.imageViewSunRays.setVisibility(View.VISIBLE);
            sunIsShiningForImageViews = true;
            // Use a Handler to make the ImageViews invisible after 3 seconds
            new Handler().postDelayed(() -> {
                binding.imageViewSun.setVisibility(View.INVISIBLE);
                binding.imageViewSunRays.setVisibility(View.INVISIBLE);
                sunIsShiningForImageViews = false;
            }, 3000); // 3000 milliseconds (3 seconds)
        }




        /*
        Iterate through all game event elements
         */
        for (int currentElement =0; currentElement <arrayList_GameEventRectangles.size(); currentElement++) {


            //Create view and set
            if (currentTimeSlot == arrayList_GameEventRectangles.get(currentElement).getStartingTimeSlot() - 15) {

                arrayList_GameEventRectangles.get(currentElement).setActive(true);


                //Set the parameters and the background of the view element
                arrayList_GameEventRectangles.get(currentElement).setLayoutParams(new ViewGroup.LayoutParams(0, 0));

                if(arrayList_GameEventRectangles.get(currentElement).getEventType().equals(VIEW_EVENT_RECTANGLE_SOLAR)) {
                    arrayList_GameEventRectangles.get(currentElement).setBackground(Objects.requireNonNull(ContextCompat.getDrawable(requireActivity(), R.drawable.game_event_rectangle_solar_1)).mutate());
                }

                if(arrayList_GameEventRectangles.get(currentElement).getEventType().equals(VIEW_EVENT_RECTANGLE_WIND)) {
                    arrayList_GameEventRectangles.get(currentElement).setBackground(Objects.requireNonNull(ContextCompat.getDrawable(requireActivity(), R.drawable.game_event_rectangle_wind_1)).mutate());
                }

                if(arrayList_GameEventRectangles.get(currentElement).getEventType().equals(VIEW_EVENT_RECTANGLE_GAS)) {
                    arrayList_GameEventRectangles.get(currentElement).setBackground(Objects.requireNonNull(ContextCompat.getDrawable(requireActivity(), R.drawable.game_event_rectangle_gas_1)).mutate());
                }

                if(arrayList_GameEventRectangles.get(currentElement).getEventType().equals(VIEW_EVENT_RECTANGLE_COAL)) {
                    arrayList_GameEventRectangles.get(currentElement).setBackground(Objects.requireNonNull(ContextCompat.getDrawable(requireActivity(), R.drawable.game_event_rectangle_coal_1)).mutate());
                }



                arrayList_GameEventRectangles.get(currentElement).setId(View.generateViewId());

                //Make the view invisible (before it's appearance time)
                arrayList_GameEventRectangles.get(currentElement).getBackground().setAlpha(0);

                constraintLayout.bringChildToFront(binding.imageViewTargetRectangle);
                // Set the ConstraintLayout programmatically for the view
                View view = arrayList_GameEventRectangles.get(currentElement);
                ViewGroup parent = (ViewGroup) view.getParent();

               // Check if the view already has a parent
                if (parent != null) {
                    parent.removeView(view); // Remove the view from its existing parent
                }

                constraintLayout.addView(view); // Now add the view to the constraintLayout


                constraintSet.clone(constraintLayout);
                float percentageHeightOfEventElement = 0.071f;
                constraintSet.constrainPercentHeight(arrayList_GameEventRectangles.get(currentElement).getId(), percentageHeightOfEventElement);

                float widthConstrainPercentage_element1 = (float)(arrayList_GameEventRectangles.get(currentElement).getDuration() / 100.0);

                constraintSet.constrainPercentWidth(arrayList_GameEventRectangles.get(currentElement).getId(), widthConstrainPercentage_element1);
                constraintSet.connect(arrayList_GameEventRectangles.get(currentElement).getId(),ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID,ConstraintSet.BOTTOM,0);
                constraintSet.connect(arrayList_GameEventRectangles.get(currentElement).getId(),ConstraintSet.TOP,ConstraintSet.PARENT_ID ,ConstraintSet.TOP,0);
                constraintSet.connect(arrayList_GameEventRectangles.get(currentElement).getId(),ConstraintSet.LEFT,ConstraintSet.PARENT_ID ,ConstraintSet.LEFT,0);
                constraintSet.connect(arrayList_GameEventRectangles.get(currentElement).getId(),ConstraintSet.RIGHT,ConstraintSet.PARENT_ID ,ConstraintSet.RIGHT,0);

                float horizontalBias = 1.0f ;
                constraintSet.setHorizontalBias(arrayList_GameEventRectangles.get(currentElement).getId(), horizontalBias);
                float verticalBiasOfEventElementToBeInTheLine = 0.049f;
                constraintSet.setVerticalBias(arrayList_GameEventRectangles.get(currentElement).getId(), verticalBiasOfEventElementToBeInTheLine);
                constraintSet.applyTo(constraintLayout);

            }


            //Shift the view to the right border of the display. This is done before the view is being displayed to the user such that it can flow from right to left in the game
            if (currentTimeSlot == arrayList_GameEventRectangles.get(currentElement).getStartingTimeSlot() - 10) {
                arrayList_GameEventRectangles.get(currentElement).setTranslationX(arrayList_GameEventRectangles.get(currentElement).getWidth());
            }


            //Animate view element
            if (currentTimeSlot == arrayList_GameEventRectangles.get(currentElement).getStartingTimeSlot()) {
                arrayList_GameEventRectangles.get(currentElement).getBackground().setAlpha(255);


                View rectangle = arrayList_GameEventRectangles.get(currentElement);
                int rectangleWidth = rectangle.getWidth();

                float distanceToCover_current = widthDisplay + rectangleWidth;
                float distanceToCover_normalizedObject = widthDisplay + 20;
                double ratioDistanceDifference = distanceToCover_current /distanceToCover_normalizedObject;

                int numberOfMillisecondsUntilTheMiddleOfTheScreen_Level1 = 8000;
                long durationForTheAnimation = (long)(numberOfMillisecondsUntilTheMiddleOfTheScreen_Level1 * speedMultiplicatorLevel [currentLevel - 1] * ratioDistanceDifference);


                arrayList_GameEventRectangles.get(currentElement).animate().setDuration(durationForTheAnimation).translationX(widthDisplay*(-1)).setInterpolator(new LinearInterpolator()).start();

            }

            helpCounterUpdateScreen++;


            //Check if the view is still running
            if (arrayList_GameEventRectangles.get(currentElement).isActive() && currentElement==0) {

                if (arrayList_GameEventRectangles.get(currentElement).getX() < arrayList_GameEventRectangles.get(currentElement).getWidth() * (-0.8)) {
                    arrayList_GameEventRectangles.get(currentElement).incrementNumberOfTimeSlotsAfterFinishing();
                }
                if (arrayList_GameEventRectangles.get(currentElement).getNumberOfTimeSlotsAfterFinishing()> 100) {
                    arrayList_GameEventRectangles.get(currentElement).setActive(false);
                }
            }
        }


        //Check if the level is finished

        if (currentTimeSlot >= totalTimeSlotsForTheLevel) {
            levelFinished();

        }


    }





    /*
    This method counts down the time left in the game by using a CountDownTimer. It is called every time step of the game.
    It also reduces the water and temperature level constantly and in an intensified way if the corresponding flying buttons are pressed.
    */
    private void countDownTime(){

        cdt = new CountDownTimer(DURATION, DELAY_COUNT_DOWN_TIMER) {
            boolean delay = true;
            public void onTick(long millisUntilFinished) {


                if(delay) {
                    delay = false;
                } else {
                    currentTimeLeftInTheLevel_MILLIS = currentTimeLeftInTheLevel_MILLIS - DELAY_IN_MILLIS;
                    helpCounterCountDownTime++;
                    currentTimeSlot++;
                    if (isFlyingButtonGeneratedAir) {
                        helpCounterTimeSlotsFlyingButtonAirGenerated++;
                    }
                    if (helpCounterTimeSlotsFlyingButtonAirGenerated >= limitTimeSlotsFlyingButtonAction) {
                        helpCounterTimeSlotsFlyingButtonAirGenerated =0;
                        isFlyingButtonGeneratedAir = false;
                    }

                    if (isFlyingButtonGeneratedShower) {
                        helpCounterTimeSlotsFlyingButtonShowerGenerated++;
                    }
                    if (helpCounterTimeSlotsFlyingButtonShowerGenerated >= limitTimeSlotsFlyingButtonAction) {
                        helpCounterTimeSlotsFlyingButtonShowerGenerated =0;
                        isFlyingButtonGeneratedShower = false;
                    }

                    thermometer.changeTemperature(-0.35);

                    if (isFlyingButtonPressedAir) {
                        thermometer.changeTemperature(-0.35 * 5.5);
                        helpCounterFlyingButtonAirPressed++;
                        if (helpCounterFlyingButtonAirPressed > limitTimeSlotsFlyingButtonAction) {
                            isFlyingButtonPressedAir = false;
                        }
                    }
                    thermometer.invalidate();

                    hotWaterTank.changeVolumeBar(-0.15);
                    if (isFlyingButtonPressedShower) {
                        hotWaterTank.changeVolumeBar(-0.15 * 15);
                        helpCounterFlyingButtonShowerPressed++;
                        if (helpCounterFlyingButtonShowerPressed> limitTimeSlotsFlyingButtonAction) {
                            isFlyingButtonPressedShower = false;
                        }
                    }
                    hotWaterTank.invalidate();

                    updateScreen();
                    delay = true;
                }

            }
            public void onFinish() {
                updateScreen();
            }
        }.start();

    }




    @Override
    public void onResume() {
        super.onResume();

        if (currentTimeSlot >= totalTimeSlotsForTheLevel && DialogFR_LevelEnd.isDialogShown==false) {
            levelFinished();
        }
    }



    /*
    This method is called when the time of a level is finished. It stops the count down timer and navigates to the DialogFragment.
     */

    private void levelFinished() {
        // Stop the CountDownTimer
        if (cdt != null) {
            cdt.cancel();
        }

        //Navigate to DialogFragment
        NavController navController = Navigation.findNavController(requireActivity(), R.id.navHostfragment);
        NavDirections action = FR_GameDirections.actionFRGameToDialogFRLevelEnd(
                currentCO2SavingsThisLevel,
                neededCO2SavingsInTheLevel,
                currentLevel,
                currentComfort
        );
        navController.navigate(action);

    }


    /*
    Method for generating the flying button depending on the 2 types: shower and air
     */
    private void generateFlyingButton(String typeOfFlyingButton) {
        final ImageView flyingButton = new ImageView(getContext());
        if (typeOfFlyingButton.equals(FLYING_BUTTON_SHOWER)) {
            flyingButton.setImageResource(R.drawable.flying_button_shower_1);
        }
        if (typeOfFlyingButton.equals(FLYING_BUTTON_AIR)) {
            flyingButton.setImageResource(R.drawable.flying_button_air_1);
        }

        flyingButton.setId(View.generateViewId());

        // Add the ImageView to the ConstraintLayout
        constraintLayout.addView(flyingButton);


        // Set layout parameters for the ImageView
        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        flyingButton.setLayoutParams(layoutParams);

        // Position the ImageView using constraints
        constraintSet.clone(constraintLayout);
        constraintSet.applyTo(constraintLayout);

        // Set starting positions and animate the view Animate the ImageView
        float startingPositionX = (float) (0.15 *widthDisplay) + (float)(0.7 * Math.random() * widthDisplay);
        float startingPositionY = (float) (0.15 *heightDisplay)+ (float)(0.7 * Math.random() * heightDisplay);
        flyingButton.setTranslationX(startingPositionX);
        flyingButton.setTranslationY(startingPositionY);
        animateFlyingButton(flyingButton);
        flyingButton.bringToFront();
        flyingButton.setElevation(100);

        // Set an OnClickListener for the ImageView after the user has clicked on it
        flyingButton.setOnClickListener(v -> {
            if (typeOfFlyingButton.equals(FLYING_BUTTON_SHOWER)) {
                isFlyingButtonPressedShower = true;
                helpCounterFlyingButtonShowerPressed = 0;
                playAudioFile(R.raw.app_game_shower_1);

                // Create a blinking animation
                binding.imageViewHotWaterShower.setVisibility(View.VISIBLE);
                AlphaAnimation blinkAnimation = new AlphaAnimation(1, 0); // From fully visible to fully transparent
                blinkAnimation.setDuration(300);
                blinkAnimation.setRepeatCount(7);
                blinkAnimation.setRepeatMode(Animation.REVERSE); // Reverse the animation to make it visible again
                binding.imageViewHotWaterShower.startAnimation(blinkAnimation);
            }

            if (typeOfFlyingButton.equals(FLYING_BUTTON_AIR)) {
                isFlyingButtonPressedAir = true;
                helpCounterFlyingButtonAirPressed = 0;
                playAudioFile(R.raw.app_game_window_1);

                // Create a blinking animation
                binding.imageViewOpenWindow.setVisibility(View.VISIBLE);
                AlphaAnimation blinkAnimation = new AlphaAnimation(1, 0); // From fully visible to fully transparent
                blinkAnimation.setDuration(300);
                blinkAnimation.setRepeatCount(7);
                blinkAnimation.setRepeatMode(Animation.REVERSE); // Reverse the animation to make it visible again
                binding.imageViewOpenWindow.startAnimation(blinkAnimation);
            }
            ViewGroup parentView = (ViewGroup) flyingButton.getParent();
            parentView.removeView(flyingButton);


        });
    }


    /*
    Method for defining the animation of the flying button
     */

    private void animateFlyingButton(final ImageView flyingButton) {
        final int screenWidth = widthDisplay - flyingButton.getWidth();
        final int screenHeight = heightDisplay - flyingButton.getHeight();
        final int animationDuration = 5000;
        float startingPositionX = flyingButton.getX();
        float startingPositionY = flyingButton.getY();

        float translationXDestination;
        float translationYDestination;

        //Determine direction
        boolean animateFromRightToLeft = false;
        boolean animateFromBottomToTop = false;
        if (startingPositionX > (double)screenWidth/2) {
            animateFromRightToLeft = true;
        }
        if (startingPositionY > (double)screenHeight/2) {
            animateFromBottomToTop = true;
        }
        float freeSpaceForAnimationX = screenWidth - startingPositionX;
        float freeSpaceForAnimationY = screenHeight- startingPositionY;

        double speedFactor = 0.1;

        if (animateFromRightToLeft) {
            translationXDestination = startingPositionX  - (float)((Math.random()+speedFactor) * speedFactor * 10 *  freeSpaceForAnimationX);
        }
        else {
            translationXDestination = startingPositionX  + (float)((Math.random()+speedFactor) * speedFactor * 10 *  freeSpaceForAnimationX);
        }

        if (animateFromBottomToTop) {
            translationYDestination = startingPositionY - (float)((Math.random()+speedFactor) * speedFactor * 10 *  freeSpaceForAnimationY);
        }
        else {
            translationYDestination = startingPositionY + (float)((Math.random()+speedFactor) * speedFactor * 10 *  freeSpaceForAnimationY);
        }


        flyingButton.animate()
                .translationX(translationXDestination)
                .translationY(translationYDestination)
                .setDuration(animationDuration)
                .setInterpolator(new LinearInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(@NonNull Animator animation) {}

                    @Override
                    public void onAnimationEnd(@NonNull Animator animation) {

                    }

                    @Override
                    public void onAnimationCancel(@NonNull Animator animation) {}

                    @Override
                    public void onAnimationRepeat(@NonNull Animator animation) {}
                })
                .start();

        // Schedule the flying button to disappear after a certain time
        new Handler().postDelayed(() -> flyingButton.setVisibility(View.INVISIBLE), animationDuration); // Adjust the time in milliseconds as needed
    }




    /*
    Method for playing a single audio file. If the audio file has been played during the last PLAY_INTERVAL_AUDIO_MILLISECONDS, it won't be repeated
     */
    private void playAudioFile(int resId) {
        if (!FR_Settings.getSoundOn(requireContext())) return;

        long now = System.currentTimeMillis();
        long lastTime = lastPlayTimesSounds.getOrDefault(resId, 0L);

        if (now - lastTime < PLAY_INTERVAL_AUDIO_MILLISECONDS) return;

        Integer soundId = soundMap.get(resId);
        if (soundId != null) {
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f);
            lastPlayTimesSounds.put(Integer.valueOf(String.valueOf(resId)), now);
        }
    }





    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Add a callback to handle the back button press
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) { // Enabled callback
                    @Override
                    public void handleOnBackPressed() {
                        // Navigate to the Menu fragment
                        NavController navController = NavHostFragment.findNavController(FR_Game.this);
                        navController.navigate(R.id.FR_Menu);

                        // Stop the current game logic
                        onDestroyView();
                    }
                });
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // And clean up any postDelayed callbacks that are waiting to fire
        cdt.cancel();
        handler.removeCallbacksAndMessages(null);

        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }


    }
}