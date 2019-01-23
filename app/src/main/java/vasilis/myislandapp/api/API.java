package vasilis.myislandapp.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;
import vasilis.myislandapp.api.callbacks.CallbackListPlace;
import vasilis.myislandapp.api.callbacks.CallbackPlaceDetails;

public interface API {

    String CACHE = "Cache-Control: max-age=0";
    String AGENT = "User-Agent: Place";

    @Headers({CACHE, AGENT})
    @GET("places")
    Call<CallbackListPlace> getPlacesByPage(
            @Query("page") int page,
            @Query("count") int count
    );

    @Headers({CACHE, AGENT})
    @GET("places")
    Call<CallbackPlaceDetails> getPlaceDetails(
            @Query("id") int place_id
    );

    @Headers({CACHE, AGENT})
    @GET("loadMore")
    Call<CallbackPlaceDetails> loadMore(
            @Query("id") int place_id
    );

    @Headers({CACHE, AGENT})
    @GET("loadMoreImages")
    Call<CallbackPlaceDetails> loadMoreImages(
            @Query("id") int place_id
    );

    @Headers({CACHE, AGENT})
    @GET("getBeachOverallRating")
    Call<CallbackPlaceDetails> getBeachOverallRating(
            @Query("id") int place_id
    );

    @Headers({CACHE, AGENT})
    @POST("rateBeach")
    Call<CallbackPlaceDetails> rateBeach(
            @Query("id") int place_id,
            @Query("rating") int rating,
            @Query("deviceID") String deviceID

    );



}
