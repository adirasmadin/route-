package asliborneo.route;

import asliborneo.route.Model.DataMessage;
import asliborneo.route.Model.FCMResponse;
import asliborneo.route.Model.Sender;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;


public interface FCMService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAaCv3G_M:APA91bHjxWwRtJmQg_zWVTrD9RWrzR0JTnpnoiZlRsEhrWiGtXP_tESjdlirLODnA_dMR5ny_zZhVP9Ix6XTg68MhvRCzJWfdBRwT54GL7hqb2x8q8gX-q96zpL6Wq2xmrElU6iYhhQo"
    })
    @POST("fcm/send")
    Call<FCMResponse> sendMessage(@Body DataMessage body);
}