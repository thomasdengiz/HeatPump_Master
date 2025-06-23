package io.thomasdengiz.heatPumpMaster;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import androidx.core.text.HtmlCompat;

import com.bumptech.glide.request.target.Target;
import io.thomasdengiz.heatPumpMaster.databinding.FragmentInterestingFactsBinding;
import java.util.Locale;


/*
This class is for the "Interesting Facts" fragment. It does not contain any logic. It uses the library Glide to load images and further displays hyperlinks.
 */

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


        // Load images with Glide based on the locale
        loadImage(binding.imageEfficiency, isGerman ? "facts_efficiency_german" : "facts_efficiency_english");
        loadImage(binding.imageHeating, isGerman ? "facts_heatingstats_german" : "facts_heatingstats_english");
        loadImage(binding.imageEmissions, isGerman ? "facts_emissions_german" : "facts_emissions_english");
        loadImage(binding.imageGrid, "facts_grid"); // No language variation


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


        // One-time hint using Toast
        SharedPreferences prefs = requireContext().getSharedPreferences("scroll_hint_interesting_facts", Context.MODE_PRIVATE);
        boolean shown = prefs.getBoolean("scroll_hint_interesting_facts", false);

        if (!shown) {
            Toast.makeText(getContext(), getString(R.string.hint_scroll_right), Toast.LENGTH_LONG).show();

            new android.os.Handler().postDelayed(() ->
                            Toast.makeText(getContext(), getString(R.string.hint_scroll_right), Toast.LENGTH_LONG).show(),
                    3500
            );

            prefs.edit().putBoolean("scroll_hint_interesting_facts", true).apply();
        }



        return binding.getRoot();
    }

    /**
     * Load images dynamically with Glide, respecting the correct dimensions from XML.
     */


    private void loadImage(ImageView imageView, String drawableName) {
        int drawableId = getDrawableId(drawableName);
        if (drawableId != 0) {
            Glide.with(this)
                    .load(drawableId)
                    .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .dontTransform()
                    .into(imageView);
        }
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
