package octii.app.taxiapp.locale;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.preference.PreferenceManager;

import java.util.Locale;
import java.util.Objects;

public class LocaleUtils {

    public static final String ENGLISH = "en";
    public static final String RUSSIAN = "ru";
    public static final String SERBIAN = "sr";


    public static void initialize(Context context, String defaultLanguage) {
        setLocale(context, defaultLanguage);
    }

    public static boolean setLocale(Context context, String language) {
        return updateResources(context, language);
    }

    static boolean updateResources(Context context, String language) {
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            setSystemLocale(configuration, locale);
        } else {
            setSystemLocaleLegacy(configuration, locale);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context = context.createConfigurationContext(configuration);
        } else {
            context.getResources().updateConfiguration(configuration, context.getResources().getDisplayMetrics());
        }
        return true;
    }

    @SuppressWarnings("deprecation")
    public static Locale getSystemLocaleLegacy(Configuration config){
        return config.locale;
    }

    @TargetApi(Build.VERSION_CODES.N)
    public static Locale getSystemLocale(Configuration config){
        return config.getLocales().get(0);
    }

    @SuppressWarnings("deprecation")
    public static void setSystemLocaleLegacy(Configuration config, Locale locale){
        config.locale = locale;
    }

    @TargetApi(Build.VERSION_CODES.N)
    public static void setSystemLocale(Configuration config, Locale locale){
        config.setLocale(locale);
    }

    public static void setSelectedLanguageId(String id){
        final SharedPreferences prefs = getDefaultSharedPreference(Application.getInstance().getApplicationContext());
        assert prefs != null;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("app_language_id", id);
        editor.apply();
    }

    public static String getSelectedLanguageId(){
        return Objects.requireNonNull(getDefaultSharedPreference(Application.getInstance()
                .getApplicationContext())).getString("app_language_id", SERBIAN);
    }


    private static SharedPreferences getDefaultSharedPreference(Context context) {
        if (PreferenceManager.getDefaultSharedPreferences(Application.getInstance().getApplicationContext()) != null)
            return PreferenceManager.getDefaultSharedPreferences(context);
        else
            return null;
    }

}
