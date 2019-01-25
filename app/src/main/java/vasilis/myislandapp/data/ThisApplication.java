package vasilis.myislandapp.data;

import android.app.Application;
import android.location.Location;
import android.provider.Settings;
import android.util.Log;

import vasilis.myislandapp.utils.GPSLocation;

public class ThisApplication extends Application {

    private static ThisApplication mInstance;
    private Location location = null;
    public static synchronized ThisApplication getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("MyIslandApp", "onCreate : ThisApplication");
        mInstance = this;
        GPSLocation.initImageLoader(getApplicationContext());
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getDeviceID() {
        return Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    }

}
