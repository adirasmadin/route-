package asliborneo.route;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static asliborneo.route.Commons.fcmURL;
import static asliborneo.route.Commons.googleAPIUrl;

public class RetrofitClient {
    private static Retrofit retrofit=null;
    private static Retrofit fcm_retrofit=null;
    public static Retrofit getClient(String fcmURL){
        if (fcm_retrofit==null){
            fcm_retrofit=new Retrofit.Builder()
                    .baseUrl(Commons.fcmURL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return fcm_retrofit;
    }
    public static Retrofit get_direction_client(){
        if (retrofit==null){
            retrofit=new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(googleAPIUrl)
                    .build();
        }
        return retrofit;
    }
}