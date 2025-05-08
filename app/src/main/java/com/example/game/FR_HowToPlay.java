package com.example.game;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.game.databinding.FragmentHowToPlayBinding;

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
        com.example.game.databinding.FragmentHowToPlayBinding binding = FragmentHowToPlayBinding.inflate(inflater, container, false);
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);


        return binding.getRoot();
    }
}