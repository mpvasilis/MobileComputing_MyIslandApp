package vasilis.myislandapp.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;

import vasilis.myislandapp.R;

public class SharedPref {

    public static final String REFRESH_PLACES = ".REFRESH_PLACES";
    private static final String THEME_COLOR_KEY = ".THEME_COLOR_KEY";
    private static final String LAST_PLACE_PAGE = "LAST_PLACE_PAGE_KEY";

    private Context context;
    private SharedPreferences sharedPreferences;
    private SharedPreferences prefs;


    public SharedPref(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("MAIN_PREF", Context.MODE_PRIVATE);
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean isRefreshPlaces() {
        return sharedPreferences.getBoolean(REFRESH_PLACES, false);
    }

    public void setRefreshPlaces(boolean need_refresh) {
        sharedPreferences.edit().putBoolean(REFRESH_PLACES, need_refresh).apply();
    }

    public String getThemeColor() {
        return sharedPreferences.getString(THEME_COLOR_KEY, "");
    }

    public int getThemeColorInt() {
        if (getThemeColor().equals("")) {
            return context.getResources().getColor(R.color.colorPrimary);
        }
        return Color.parseColor(getThemeColor());
    }

    public int getLastPlacePage() {
        return sharedPreferences.getInt(LAST_PLACE_PAGE, 1);
    }

    public void setLastPlacePage(int page) {
        sharedPreferences.edit().putInt(LAST_PLACE_PAGE, page).apply();
    }


}
