package vasilis.myislandapp.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;
import vasilis.myislandapp.api.callbacks.CallBackBeachOverallRating;
import vasilis.myislandapp.api.callbacks.CallBackBeachRating;
import vasilis.myislandapp.api.callbacks.CallBackListPlace;
import vasilis.myislandapp.api.callbacks.CallBackLoadMore;
import vasilis.myislandapp.api.callbacks.CallBackLoadMoreImages;
import vasilis.myislandapp.api.callbacks.CallbackPlaceDetails;

public interface API {

    String CACHE = "Cache-Control: max-age=0";
    String AGENT = "User-Agent: MyIslandApp";

    @Headers({CACHE, AGENT})
    @GET("places")
    Call<CallBackListPlace> getPlacesByPage(
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
    Call<CallBackLoadMore> loadMore(
            @Query("id") int place_id
    );

    @Headers({CACHE, AGENT})
    @GET("loadMoreImages")
    Call<CallBackLoadMoreImages> loadMoreImages(
            @Query("id") int place_id
    );

    @Headers({CACHE, AGENT})
    @GET("getBeachOverallRating")
    Call<CallBackBeachOverallRating> getBeachOverallRating(
            @Query("id") int place_id
    );

    @Headers({CACHE, AGENT})
    @POST("rateBeach")
    Call<CallBackBeachRating> rateBeach(
            @Query("id") int place_id,
            @Query("rating") int rating,
            @Query("deviceID") String deviceID

    );



}
