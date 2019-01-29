package vasilis.myislandapp.api.callbacks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import vasilis.myislandapp.model.Images;

public class CallBackLoadMoreImages implements Serializable {
    public String status;
    public List<Images> images = new ArrayList<>();

}
