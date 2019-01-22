package asliborneo.route.Model;

public class RouteDriver {
    private String name;

    private String phone;
    private String rates;
    private String avatarUrl;
    private String carType;

    public String getMakePayment() {
        return makePayment;
    }

    public void setMakePayment(String makePayment) {
        this.makePayment = makePayment;
    }

    private String makePayment;
    private String email;
    private String password;
    private String wallet;
    private String gender;

    public RouteDriver(String gender) {
        this.gender = gender;
    }

    public String getGender() {

        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getWallet() {
        return wallet;
    }

    public void setWallet(String wallet) {
        this.wallet = wallet;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public RouteDriver(String email, String password) {

        this.email = email;
        this.password = password;
    }

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

    public RouteDriver(String name, String phone, String rates, String avatarUrl, String carType, String wallet) {

        this.name = name;
        this.phone = phone;
        this.rates = rates;
        this.avatarUrl = avatarUrl;
        this.carType = carType;
       this.wallet =wallet;

    }

    public RouteDriver() {

    }
}