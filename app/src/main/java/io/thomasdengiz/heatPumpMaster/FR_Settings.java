package io.thomasdengiz.heatPumpMaster;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import io.thomasdengiz.heatPumpMaster.databinding.FragmentOptionsBinding;


/*
This class is for the "Options" fragment. Here the user can change the language and the sound options.
 */

public class FR_Settings extends Fragment implements View.OnClickListener {


    /*
    String specifying the language of the App
     */

    public static final String LANGUAGE_GERMAN = "German";
    public static final String LANGUAGE_ENGLISH = "English";
    //Set the default language to GERMAN
    public static String currentLanguageOfTheApp = LANGUAGE_ENGLISH;

    private static final String LANGUAGE = "LANGUAGE";
    private static final String SOUND_ON = "SOUND_ON";
    private static final String SHARED_PREFS_NAME= "SHARED_PREFS_NAME";

    public FR_Settings() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    private FragmentOptionsBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentOptionsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.imageButtonGermany.setOnClickListener(this);
        binding.imageButtonUK.setOnClickListener(this);
        binding.imageButtonSoundOff.setOnClickListener(this);
        binding.imageButtonSoundOn.setOnClickListener(this);

        if(getLanguage(requireContext()).equals("en")) {
            binding.imageButtonGermany.setAlpha(0.5f);
            binding.imageButtonUK.setAlpha(1.0f);
        }
        if(getLanguage(requireContext()).equals("de")) {
            binding.imageButtonGermany.setAlpha(1.0f);
            binding.imageButtonUK.setAlpha(0.5f);
        }

        if (getSoundOn(requireContext())) {
            binding.imageButtonSoundOn.setAlpha(1.0f);
            binding.imageButtonSoundOff.setAlpha(0.5f);
        }

        if (!getSoundOn(requireContext())) {
            binding.imageButtonSoundOn.setAlpha(0.5f);
            binding.imageButtonSoundOff.setAlpha(1.0f);
        }

    }


    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onClick(View view) {


        if(view.getId() == R.id.imageButtonGermany) {

             /*
            Set the language to "German" for other fragments and database queries
             */

            currentLanguageOfTheApp = LANGUAGE_GERMAN;

            setLanguage(requireContext(), "de");

            // restart the activity
            requireActivity().finish();
            requireActivity().startActivity(new Intent(requireActivity(), MainActivity.class));


        }

        if(view.getId() == R.id.imageButtonUK) {

            /*
            Set the language to "English" for other fragments and database queries
             */

            currentLanguageOfTheApp = LANGUAGE_ENGLISH;
            setLanguage(requireContext(), "en");

            // restart the activity
            requireActivity().finish();
            requireActivity().startActivity(new Intent(requireActivity(), MainActivity.class));

        }

        if (view.getId()==R.id.imageButtonSoundOn) {
            binding.imageButtonSoundOn.setAlpha(1.0f);
            binding.imageButtonSoundOff.setAlpha(0.5f);
            setSoundOn(requireContext(), true);

        }

        if (view.getId()==R.id.imageButtonSoundOff) {
            binding.imageButtonSoundOn.setAlpha(0.5f);
            binding.imageButtonSoundOff.setAlpha(1.0f);
            setSoundOn(requireContext(), false);

        }


    }

    public static String getLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(LANGUAGE, "en");
    }

    public static void setLanguage(Context context, String language) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(LANGUAGE, language);
        editor.apply();
    }

    public static void  setSoundOn (Context context, boolean soundOn) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(SOUND_ON, Boolean.toString(soundOn));
        editor.apply();
    }

    public static boolean getSoundOn (Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        return Boolean.parseBoolean(prefs.getString(SOUND_ON, "true"));

    }

}