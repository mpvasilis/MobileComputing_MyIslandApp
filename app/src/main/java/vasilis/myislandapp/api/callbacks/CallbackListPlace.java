package vasilis.myislandapp.api.callbacks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import vasilis.myislandapp.model.Place;

public class CallbackListPlace implements Serializable {

    public int count_total = -1;
    public List<Place> places = new ArrayList<>();

}
