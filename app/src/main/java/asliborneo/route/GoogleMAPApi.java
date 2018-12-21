package asliborneo.route;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class GoogleMAPApi {
    private  static Retrofit retrofit = null;

    public static Retrofit getClient(String googleAPIUrl) {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder().baseUrl(googleAPIUrl).addConverterFactory(ScalarsConverterFactory.create()).build();


        }
        return retrofit;
    }

    public static Retrofit getDirectionClient(String baseURL){
        if (retrofit==null){
            retrofit=new Retrofit.Builder()
                    .baseUrl(baseURL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

}
