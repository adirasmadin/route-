package asliborneo.route.Model;

public class Notification{
    public String title;
    public String detail;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return detail;
    }

    public void setBody(String body) {
        this.detail = body;
    }

    public Notification() {
    }

    public Notification(String title, String body) {
        this.title = title;
        this.detail = body;
    }
}