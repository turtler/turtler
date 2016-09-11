package turtler.voyageur.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by carolinewong on 9/3/16.
 */
@ParseClassName("Image")
public class Image extends ParseObject {
    public String getPicture_url() {
        return picture_url;
    }

    public User getUser() {
        return user;
    }

    public Event getEvent() {
        return event;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String picture_url;
    public User user;
    public Event event;
    public double latitude;
    public double longitude;

    public Image() {}

    public Image(String picture_url, User user, Event event, double latitude, double longitude) {
        this.picture_url = picture_url;
        this.user = user;
        this.event = event;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
