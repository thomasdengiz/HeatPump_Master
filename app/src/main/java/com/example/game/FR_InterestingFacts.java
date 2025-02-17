package com.example.game;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.core.text.HtmlCompat;
import com.example.game.databinding.FragmentInterestingFactsBinding;
import java.util.Locale;


public class FR_InterestingFacts extends Fragment {

    private FragmentInterestingFactsBinding binding;

    public FR_InterestingFacts() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the screen orientation to landscape
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentInterestingFactsBinding.inflate(inflater, container, false);

        // Determine the current locale
        boolean isGerman = Locale.getDefault().getLanguage().equals("de");

        // Set images based on the locale
        binding.imageEfficiency.setImageResource(getDrawableId(isGerman ? "facts_efficiency_german" : "facts_efficiency_english"));
        binding.imageHeating.setImageResource(getDrawableId(isGerman ? "facts_heatingstats_german" : "facts_heatingstats_english"));
        binding.imageEmissions.setImageResource(getDrawableId(isGerman ? "facts_emissions_german" : "facts_emissions_english"));

        // Format the TextViews with HTML tags and links
        String formattedTextEfficiency = getString(R.string.info_efficiency_link);
        binding.tvFactsEfficiency.setText(HtmlCompat.fromHtml(formattedTextEfficiency, HtmlCompat.FROM_HTML_MODE_LEGACY));
        binding.tvFactsEfficiency.setMovementMethod(LinkMovementMethod.getInstance());

        String formattedTextHeating = getString(R.string.info_heating_link);
        binding.tvFactsHeating.setText(HtmlCompat.fromHtml(formattedTextHeating, HtmlCompat.FROM_HTML_MODE_LEGACY));
        binding.tvFactsHeating.setMovementMethod(LinkMovementMethod.getInstance());

        String formattedTextEmission = getString(R.string.info_emissions_link);
        binding.tvFactsEmissions.setText(HtmlCompat.fromHtml(formattedTextEmission, HtmlCompat.FROM_HTML_MODE_LEGACY));
        binding.tvFactsEmissions.setMovementMethod(LinkMovementMethod.getInstance());

        String formattedTextGrid = getString(R.string.info_stabilizing_grids_link);
        binding.tvFactsGrid.setText(HtmlCompat.fromHtml(formattedTextGrid, HtmlCompat.FROM_HTML_MODE_LEGACY));
        binding.tvFactsGrid.setMovementMethod(LinkMovementMethod.getInstance());

        return binding.getRoot();
    }

    /**
     * Helper method to get drawable resource ID by name.
     */
    @SuppressLint("DiscouragedApi")
    private int getDrawableId(String name) {
        return getResources().getIdentifier(name, "drawable", requireContext().getPackageName());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
