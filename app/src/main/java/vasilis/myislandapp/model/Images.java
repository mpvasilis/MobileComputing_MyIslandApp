package vasilis.myislandapp.model;

import java.io.Serializable;

public class Images implements Serializable {
    public int place_id;
    public String name;

    public Images() {
    }

    public Images(int place_id, String name) {
        this.place_id = place_id;
        this.name = name;
    }

    public String getImageUrl() {
        return name;
    }
}
