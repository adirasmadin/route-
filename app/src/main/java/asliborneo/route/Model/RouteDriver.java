package asliborneo.route.Model;

public class RouteDriver {
    private String name;

    private String phone;
    private String rates;
    private String avatarUrl;
    private String carType;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRates() {
        return rates;
    }

    public void setRates(String rates) {
        this.rates = rates;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getCarType() {
        return carType;
    }

    public void setCarType(String carType) {
        this.carType = carType;
    }

    public RouteDriver(String name, String phone, String rates, String avatarUrl, String carType) {

        this.name = name;
        this.phone = phone;
        this.rates = rates;
        this.avatarUrl = avatarUrl;
        this.carType = carType;
    }

    public RouteDriver() {

    }
}