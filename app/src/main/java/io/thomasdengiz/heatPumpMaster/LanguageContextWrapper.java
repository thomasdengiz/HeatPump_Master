package io.thomasdengiz.heatPumpMaster;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;

import java.util.Locale;


/*
This class is used to change the language of the App.
 */
public class LanguageContextWrapper extends ContextWrapper {

    public LanguageContextWrapper(Context base) {
        super(base);
    }

    public static ContextWrapper wrap(Context context, String language) {
        if (language == null || language.isEmpty()) {
            return new LanguageContextWrapper(context); // No changes if language is empty
        }

        Configuration config = context.getResources().getConfiguration();
        Locale currentLocale = getCurrentLocale(config);

        if (!currentLocale.getLanguage().equals(language)) {
            Locale newLocale = new Locale(language);
            Locale.setDefault(newLocale);
            setLocale(config, newLocale);
        }

        context = context.createConfigurationContext(config);
        return new LanguageContextWrapper(context);
    }

    private static Locale getCurrentLocale(Configuration config) {
        return config.getLocales().get(0);
    }

    private static void setLocale(Configuration config, Locale locale) {
        config.setLocale(locale);
    }
}
