package asliborneo.route;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface IGoogleAPI {
    @GET("maps/api/directions/json")
    Call<Directions> getPath(@Query("mode") String mode, @Query("transit_routing_preference")String transit_routing_preference, @Query("origin")String origin, @Query("destination")String destination, @Query("key")String key );
}