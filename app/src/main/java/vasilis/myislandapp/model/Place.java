package vasilis.myislandapp.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Place implements Serializable, ClusterItem {
    public int id;
    public String name;
    public String image;
    public String address;
    public String phone;
    public String website;
    public String description;
    public double lng;
    public double lat;
    public long last_update;
    public float distance = -1;
    public int category;
    public float OverallRating = -1;

    public List<Images> images = new ArrayList<>();

    @Override
    public LatLng getPosition() {
        return new LatLng(lat, lng);
    }

    public boolean isEmpty() {
        return (address == null && phone == null && website == null && description == null);
    }

    public float getOverallRating() {
        return OverallRating;
    }

}
