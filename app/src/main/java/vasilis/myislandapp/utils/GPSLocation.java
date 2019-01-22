package vasilis.myislandapp.utils;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import vasilis.myislandapp.data.ThisApplication;
import vasilis.myislandapp.model.Place;

public class GPSLocation {
    private static float calculateDistance(LatLng from, LatLng to) {
        Location start = new Location("");
        start.setLatitude(from.latitude);
        start.setLongitude(from.longitude);

        Location end = new Location("");
        end.setLatitude(to.latitude);
        end.setLongitude(to.longitude);

        float distInMeters = start.distanceTo(end);
        float resultDist = distInMeters / 1000;
        return resultDist;
    }

    public static List<Place> filterItemsWithDistance(Activity act, List<Place> items) {
        LatLng curLoc = Tools.getCurLocation(act);
        if (curLoc != null) {
            return Tools.getSortedDistanceList(items, curLoc);
        }

        return items;
    }

    public static List<Place> itemsWithDistance(Context ctx, List<Place> items) {
        LatLng curLoc = Tools.getCurLocation(ctx);
        if (curLoc != null) {
            return Tools.getDistanceList(items, curLoc);
        }
        return items;
    }

    public static List<Place> getDistanceList(List<Place> places, LatLng curLoc) {
        if (places.size() > 0) {
            for (Place p : places) {
                p.distance = calculateDistance(curLoc, p.getPosition());
            }
        }
        return places;
    }

    public static List<Place> getSortedDistanceList(List<Place> places, LatLng curLoc) {
        List<Place> result = new ArrayList<>();
        if (places.size() > 0) {
            for (int i = 0; i < places.size(); i++) {
                Place p = places.get(i);
                p.distance = calculateDistance(curLoc, p.getPosition());
                result.add(p);
            }
            Collections.sort(result, new Comparator<Place>() {
                @Override
                public int compare(final Place p1, final Place p2) {
                    return Float.compare(p1.distance, p2.distance);
                }
            });
        } else {
            return places;
        }
        return result;
    }

    public static LatLng getCurLocation(Context ctx) {
        if (PermissionUtil.isLocationGranted(ctx)) {
            LocationManager manager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
            if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Location loc = ThisApplication.getInstance().getLocation();
                if (loc == null) {
                    loc = getLastKnownLocation(ctx);
                    ThisApplication.getInstance().setLocation(loc);
                }
                if (loc != null) {
                    return new LatLng(loc.getLatitude(), loc.getLongitude());
                }
            }
        }
        return null;
    }

    public static Location getLastKnownLocation(Context ctx) {
        LocationManager mLocationManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = Tools.requestLocationUpdate(mLocationManager);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        mLocationManager.removeUpdates(locationListener);
        return bestLocation;
    }

    private static LocationListener requestLocationUpdate(LocationManager manager) {
        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        // Register the listener with the Location Manager to receive location updates
        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        return locationListener;
    }

    public static String getFormatedDistance(float distance) {
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(1);
        return df.format(distance) + " km";
    }
}
