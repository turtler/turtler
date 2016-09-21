package turtler.voyageur.models;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

/**
 * Created by carolinewong on 9/3/16.
 */
@ParseClassName("Image")
public class Image extends ParseObject {
    public static final String LATITUDE_KEY = "latitude";
    public static final String LONGITUDE_KEY = "longitude";
    public static final String USER_KEY = "user";
    public static final String EVENT_KEY = "event";
    public static final String PICTURE_URL_KEY = "pictureUrl";


    public String getPictureUrl() {
        return getString(PICTURE_URL_KEY);
    }

    public User getUser() {
        return (User) getParseUser(USER_KEY);
    }

    public Event getEvent() {
        return (Event) get("event");
    }

    public double getLatitude() {
        return getDouble(LATITUDE_KEY);
    }

    public double getLongitude() {
        return getDouble(LONGITUDE_KEY);
    }

    public String pictureUrl;
    public User user;
    public Event event;
    public double latitude;
    public double longitude;

    public Image() {}

    public void setGeoPoint(ParseGeoPoint p ) {
        put("geoPoint", p);
    }
    public void setPictureUrl(String pictureUrl) {
        put(PICTURE_URL_KEY, pictureUrl);
    }

    public void setUser(User user) {
        put(USER_KEY, user);
    }

    public void setEvent(Event event) {
        put(EVENT_KEY, event);
    }

    public void setLatitude(double latitude) {
        put(LATITUDE_KEY, latitude);
    }

    public void setLongitude(double longitude) {
        put(LONGITUDE_KEY, longitude);

    }

    public Image(String pictureUrl, User user, Event event, double latitude, double longitude) {
        this.pictureUrl = pictureUrl;
        this.user = user;
        this.event = event;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
