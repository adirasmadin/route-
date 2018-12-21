package asliborneo.route;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class FCMClient {
    private static Retrofit retrofit=null;
    private static Retrofit fcm_retrofit=null;
    public static Retrofit getClient(String fcmURL){
        if (fcm_retrofit==null){
            fcm_retrofit=new Retrofit.Builder()
                    .baseUrl(fcmURL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return fcm_retrofit;
    }

}
