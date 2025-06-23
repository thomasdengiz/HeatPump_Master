package io.thomasdengiz.heatPumpMaster;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.thomasdengiz.heatPumpMaster.databinding.FragmentLevelSelectionMenuBinding;

import java.util.ArrayList;

/*
This class defines the fragment for level selection. It contains a Recyclerview (RV) with all levels and statistics about the levels
 */

public class FR_RV_Level_Selection_Menu extends Fragment implements View.OnClickListener {

    private RV_Adapter_Level_Selection adapter;
    private ArrayList<RV_Item_Level_Selection_Menu> levelList;

    private FragmentLevelSelectionMenuBinding binding;


    public FR_RV_Level_Selection_Menu() {
        // Required empty public constructor
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLevelSelectionMenuBinding.inflate(inflater, container, false);
        buildRecyclerView();
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        binding.buttonPlay.setOnClickListener(this);
        return binding.getRoot();

    }

    private void buildRecyclerView() {

        RecyclerView recyclerView = binding.recyclerView;
        recyclerView.setHasFixedSize(true);

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2, GridLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);


        levelList = new ArrayList<>();

        //Shared preferences query and processing to get the level information data;
        SharedPreferences prefs = requireContext().getSharedPreferences("user_level_progress", Context.MODE_PRIVATE);
        int totalLevels = MainActivity.sqLite_DB.getNumberOfLevels();

        levelList.clear();
        for (int levelNumber = 1; levelNumber <= totalLevels; levelNumber++) {
            String keyBase = "level_" + levelNumber;

            float bestResultThisLevel = (float) (Math.round(prefs.getFloat(keyBase + "_percentage", 0f) * 10.0) / 10.0);
            int totalCO2SavingsThisLevel = prefs.getInt(keyBase + "_co2", 0);
            long gasLongBits = prefs.getLong(keyBase + "_gas", Double.doubleToRawLongBits(0.0));
            double gasSavingsTotalThisLevel = Double.longBitsToDouble(gasLongBits);
            boolean levelUnlocked;
            if (levelNumber == 1) {
                levelUnlocked = prefs.getBoolean(keyBase + "_unlocked", true);  // Level 1 defaults to unlocked
            } else {
                levelUnlocked = prefs.getBoolean(keyBase + "_unlocked", false); // Other levels default to locked
            }


            RV_Item_Level_Selection_Menu levelItem = new RV_Item_Level_Selection_Menu(
                    levelNumber,
                    bestResultThisLevel,
                    totalCO2SavingsThisLevel,
                    (int) Math.round(gasSavingsTotalThisLevel),
                    levelUnlocked,
                    false // or your logic here
            );

            levelList.add(levelItem);
        }


        adapter = new RV_Adapter_Level_Selection(levelList, getContext());


        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);



        adapter.setOnItemClickListener(new RV_Adapter_Level_Selection.OnItemClickListener() {

            /*
            Define what happens when clicking on an item in the RecyclerView
            */

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onItemClick(int position) {
                if(!levelList.get(position).isSelected()) {
                    //Select the selected Level and unselect all other levels
                    if (levelList.get(position).isLevelUnlocked()) {
                        for (int i =0; i<levelList.size();i++) {
                            levelList.get(i).setSelected(false);
                        }
                        levelList.get(position).setSelected(true);
                    }

                }
                // Notify the adapter that the data has changed
                adapter.notifyDataSetChanged();

             }

        });

    }//end method buildRecyclerView


    @Override
    public void onClick(View view) {
        if (view.getId()==  R.id.buttonPlay) {

            //get the currently selected level item
            int selectedLevel = -1;
            for (int i =0; i< levelList.size(); i++) {
                if (levelList.get(i).isSelected()) {
                    selectedLevel = levelList.get(i).getLevelNumber();
                }
            }
            //Navigate to the FR_Game and set the level
            if (selectedLevel !=-1) {
                FR_Game.currentLevel = selectedLevel;
                NavController navController = Navigation.findNavController(requireActivity(), R.id.navHostfragment);
                navController.navigate(FR_RV_Level_Selection_MenuDirections.actionFRRVLevelSelectionMenuToFRGame());
            }

        }
    }
}
