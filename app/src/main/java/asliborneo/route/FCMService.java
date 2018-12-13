package asliborneo.route;

import asliborneo.route.Model.fcm_response;
import asliborneo.route.Model.sender;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;


public interface FCMService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAaCv3G_M:APA91bHH98v_2jJ20xJC_-kyEShHYDooik6A4JeigmHMcNFUePHcmgXYvZ-HHn4kB9c265Xunwda4WrOqgbd-lv0MduF5td5VqsfPtJarUF3YOOlSuH5Ff1ZQ0TE9huLMBBV4yJkPwv_"
    })
    @POST("fcm/send")
    Call<fcm_response> send_message(@Body sender body);
}