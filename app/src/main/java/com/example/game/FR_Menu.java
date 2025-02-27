package com.example.game;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.game.databinding.FragmentMenuBinding;


public class FR_Menu extends Fragment implements View.OnClickListener{


    public FR_Menu() {

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        com.example.game.databinding.FragmentMenuBinding binding = FragmentMenuBinding.inflate(inflater, container, false);
        // Check if the device width is 720dp or larger (tablet size) to set orientation
        if (isTablet()) {
            requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }

        binding.buttonGame.setOnClickListener(this);
        binding.buttonOptions.setOnClickListener(this);
        binding.buttonHowToPlay.setOnClickListener(this);
        binding.buttonFacts.setOnClickListener(this);
        binding.buttonExit.setOnClickListener(this);
        binding.buttonLevelSelection.setOnClickListener(this);


        return binding.getRoot();
    }

    private boolean isTablet() {
        // Get display metrics
        DisplayMetrics metrics = getResources().getDisplayMetrics();

        // Calculate screen width in dp
        float widthDp = metrics.widthPixels / metrics.density;

        // Return true if width is 720dp or larger
        return widthDp >= 720;
    }






    @Override
    public void onClick(View view) {
        Log.d("FR_Menu", "Button clicked with ID: " + view.getId());
        if(view.getId() == R.id.button_game) {
            Navigation.findNavController(requireView()).navigate(FR_MenuDirections.actionFRMenuToFRGame());

        }

        if(view.getId() == R.id.button_how_to_play) {
            Navigation.findNavController(requireView()).navigate(FR_MenuDirections.actionFRMenuToFRHowToPlay());
        }

        if(view.getId() == R.id.button_facts) {
            Navigation.findNavController(requireView()).navigate(FR_MenuDirections.actionFRMenuToFRInterestingFacts());
        }

        if(view.getId() == R.id.button_options) {
            Navigation.findNavController(requireView()).navigate(FR_MenuDirections.actionFRMenuToFROptions());
        }

        if(view.getId() == R.id.button_level_selection) {
            Navigation.findNavController(requireView()).navigate(FR_MenuDirections.actionFRMenuToFRRVLevelSelectionMenu());
        }




        if(view.getId() == R.id.button_exit) {
            requireActivity().finishAndRemoveTask();
        }

    }
}