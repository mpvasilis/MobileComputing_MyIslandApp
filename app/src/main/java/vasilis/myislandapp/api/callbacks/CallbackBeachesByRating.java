package vasilis.myislandapp.api.callbacks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import vasilis.myislandapp.model.Place;

public class CallbackBeachesByRating implements Serializable {
    public List<Place> beaches = new ArrayList<>();
}
