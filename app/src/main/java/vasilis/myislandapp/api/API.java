package vasilis.myislandapp.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;
import vasilis.myislandapp.api.callbacks.CallbackListPlace;
import vasilis.myislandapp.api.callbacks.CallbackPlaceDetails;

public interface API {

    String CACHE = "Cache-Control: max-age=0";
    String AGENT = "User-Agent: Place";

    @Headers({CACHE, AGENT})
    @GET("app/services/listPlaces")
    Call<CallbackListPlace> getPlacesByPage(
            @Query("page") int page,
            @Query("count") int count,
            @Query("draft") int draft
    );

    @Headers({CACHE, AGENT})
    @GET("app/services/getPlaceDetails")
    Call<CallbackPlaceDetails> getPlaceDetails(
            @Query("place_id") int place_id
    );


}
