package io.thomasdengiz.heatPumpMaster;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import io.thomasdengiz.heatPumpMaster.databinding.FragmentHowToPlayBinding;

/*
This class is for the "How to play" fragment. It does not contain any logic
 */

public class FR_HowToPlay extends Fragment {


    public FR_HowToPlay() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        io.thomasdengiz.heatPumpMaster.databinding.FragmentHowToPlayBinding binding = FragmentHowToPlayBinding.inflate(inflater, container, false);
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);


        // One-time hint using Toast
        SharedPreferences prefs = requireContext().getSharedPreferences("scroll_hint_how_to_play", Context.MODE_PRIVATE);
        boolean shown = prefs.getBoolean("scroll_hint_how_to_play", false);

        if (!shown) {
            Toast.makeText(getContext(), getString(R.string.hint_scroll_down), Toast.LENGTH_LONG).show();

            // Show it again after the first one disappears (approx. 3.5 seconds)
            new android.os.Handler().postDelayed(() ->
                            Toast.makeText(getContext(), getString(R.string.hint_scroll_down), Toast.LENGTH_LONG).show(),
                    3500
            );

            prefs.edit().putBoolean("scroll_hint_how_to_play", true).apply();
        }

        return binding.getRoot();
    }
}