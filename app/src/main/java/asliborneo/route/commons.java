package asliborneo.route;

import asliborneo.route.Model.User;

public class commons {
    public static String Current_Token;
    public static double base_fare=2.55;
    private static double time_rate=0.35;
    private static double distance_rate=1.75;
    public static User current_user=null;
    public static final java.lang.String user_field="usr";
    public static final String password_field="pwd";

    public static double price_formula(double km,double min){
        return base_fare+(distance_rate*km)+(time_rate*min);
    }
}