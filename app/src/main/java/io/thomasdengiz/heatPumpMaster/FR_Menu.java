package io.thomasdengiz.heatPumpMaster;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import io.thomasdengiz.heatPumpMaster.databinding.FragmentMenuBinding;


/*
This class is for the "Menu" fragment. It handles the navigation to other fragments.
 */

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
        io.thomasdengiz.heatPumpMaster.databinding.FragmentMenuBinding binding = FragmentMenuBinding.inflate(inflater, container, false);

        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);


        binding.imagebuttonGame.setOnClickListener(this);
        binding.imageButtonOptions.setOnClickListener(this);
        binding.imageButtonInstructions.setOnClickListener(this);
        binding.imagebuttonFacts.setOnClickListener(this);

        binding.imageButtonLevelSelection.setOnClickListener(this);


        //Exit the game what happens when back is pressed
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        requireActivity().finish();
                    }
                }
        );


        return binding.getRoot();
    }



    /*
    This method handles the navigation to other fragments.
     */
    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.imagebutton_game) {
            Navigation.findNavController(requireView()).navigate(FR_MenuDirections.actionFRMenuToFRGame());

        }

        if(view.getId() == R.id.imageButton_instructions) {
            Navigation.findNavController(requireView()).navigate(FR_MenuDirections.actionFRMenuToFRHowToPlay());
        }

        if(view.getId() == R.id.imagebutton_facts) {
            Navigation.findNavController(requireView()).navigate(FR_MenuDirections.actionFRMenuToFRInterestingFacts());
        }

        if(view.getId() == R.id.imageButton_options) {
            Navigation.findNavController(requireView()).navigate(FR_MenuDirections.actionFRMenuToFROptions());
        }

        if(view.getId() == R.id.imageButton_level_selection) {
            Navigation.findNavController(requireView()).navigate(FR_MenuDirections.actionFRMenuToFRRVLevelSelectionMenu());
        }


    }
}