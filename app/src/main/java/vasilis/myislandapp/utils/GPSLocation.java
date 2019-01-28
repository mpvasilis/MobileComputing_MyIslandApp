package vasilis.myislandapp.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import vasilis.myislandapp.R;
import vasilis.myislandapp.data.SharedPref;
import vasilis.myislandapp.data.ThisApplication;
import vasilis.myislandapp.model.Place;

public class GPSLocation {

    public static boolean needRequestPermission() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    public static boolean isLolipopOrHigher() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
    }

    public static void systemBarLolipop(Activity act) {
        if (isLolipopOrHigher()) {
            Window window = act.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //window.setStatusBarColor(GPSLocation.colorDarker(new SharedPref(act).getThemeColorInt()));
        }
    }

    public static boolean cekConnection(Context context) {
        ConnectionDetector conn = new ConnectionDetector(context);
        return conn.isConnectingToInternet();
    }

    public static void initImageLoader(Context context) {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .defaultDisplayImageOptions(options)
                .threadPoolSize(3)
                .memoryCache(new WeakMemoryCache())
                .build();
        ImageLoader.getInstance().init(config);
    }

    public static DisplayImageOptions getGridOption() {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();

        return options;
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return model;
        } else {
            return manufacturer + " " + model;
        }
    }

    public static String getAndroidVersion() {
        return Build.VERSION.RELEASE + "";
    }

    public static int getGridSpanCount(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        float screenWidth = displayMetrics.widthPixels;
        float cellWidth = activity.getResources().getDimension(R.dimen.item_place_width);
        return Math.round(screenWidth / cellWidth);
    }

    public static GoogleMap configStaticMap(Activity act, GoogleMap googleMap, Place place) {
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.getUiSettings().setCompassEnabled(false);
        googleMap.getUiSettings().setRotateGesturesEnabled(false);
        googleMap.getUiSettings().setZoomGesturesEnabled(false);
        googleMap.isTrafficEnabled();
        googleMap.setTrafficEnabled(false);
        googleMap.getUiSettings().setScrollGesturesEnabled(false);
        googleMap.getUiSettings().setMapToolbarEnabled(false);

        LayoutInflater inflater = (LayoutInflater) act.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View marker_view = inflater.inflate(R.layout.maps_marker, null);
        ((ImageView) marker_view.findViewById(R.id.marker_bg)).setColorFilter(act.getResources().getColor(R.color.marker_secondary));

        CameraPosition cameraPosition = new CameraPosition.Builder().target(place.getPosition()).zoom(12).build();
        MarkerOptions markerOptions = new MarkerOptions().position(place.getPosition());
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(GPSLocation.createBitmapFromView(act, marker_view)));
        googleMap.addMarker(markerOptions);
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        return googleMap;
    }

    public static GoogleMap configActivityMaps(GoogleMap googleMap) {
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setRotateGesturesEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);
        googleMap.getUiSettings().setScrollGesturesEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(true);

        return googleMap;
    }


    public static void aboutAction(Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Σχετικά");
        builder.setMessage(activity.getString(R.string.about_text));
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    public static void dialNumber(Context ctx, String phone) {
        try {
            Intent i = new Intent(Intent.ACTION_DIAL);
            i.setData(Uri.parse("tel:" + phone));
            ctx.startActivity(i);
        } catch (Exception e) {
            Toast.makeText(ctx, "Cannot dial number", Toast.LENGTH_SHORT);
        }
    }

    public static void directUrl(Context ctx, String website) {
        String url = website;
        if (!url.startsWith("https://") && !url.startsWith("http://")) {
            url = "http://" + url;
        }
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        ctx.startActivity(i);
    }


    public static Bitmap createBitmapFromView(Activity act, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        act.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }

    public static void setActionBarColor(Context ctx, ActionBar actionbar) {
        ColorDrawable colordrw = new ColorDrawable(new SharedPref(ctx).getThemeColorInt());
        actionbar.setBackgroundDrawable(colordrw);
    }

    public static int colorDarker(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f;
        return Color.HSVToColor(hsv);
    }

    public static int colorBrighter(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] /= 0.8f;
        return Color.HSVToColor(hsv);
    }

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
        LatLng curLoc = GPSLocation.getCurLocation(act);
        if (curLoc != null) {
            return GPSLocation.getSortedDistanceList(items, curLoc);
        }

        return items;
    }

    public static List<Place> itemsWithDistance(Context ctx, List<Place> items) {
        LatLng curLoc = GPSLocation.getCurLocation(ctx);
        if (curLoc != null) {
            return GPSLocation.getDistanceList(items, curLoc);
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
        LocationListener locationListener = GPSLocation.requestLocationUpdate(mLocationManager);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                bestLocation = l;
            }
        }
        mLocationManager.removeUpdates(locationListener);
        return bestLocation;
    }

    private static LocationListener requestLocationUpdate(LocationManager manager) {
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

        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        return locationListener;
    }

    public static String getFormatedDistance(float distance) {
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(1);
        return df.format(distance) + " km";
    }

    public static int dpToPx(Context c, int dp) {
        Resources r = c.getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

}
