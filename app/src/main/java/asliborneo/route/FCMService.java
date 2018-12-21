package asliborneo.route;

import asliborneo.route.Model.FCMResponse;
import asliborneo.route.Model.Sender;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;


public interface FCMService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAaCv3G_M:APA91bGc1_PbWz8_jf7N4oY6pX4f8vSfiegfXUd5gfmUi5sPnPjjLjk7xlQGcRsLiFHx2k-q6D9HHizTAyL9jIqMXQRGePtLPiQA3LZBp8UyVLnPQgF7EAsPEXJqBxBMoLaVgy0qTijS"
    })
    @POST("fcm/send")
    Call<FCMResponse> sendMessage(@Body Sender body);
}