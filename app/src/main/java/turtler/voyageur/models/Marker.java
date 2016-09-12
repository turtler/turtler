package turtler.voyageur.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * Created by skulkarni on 9/5/16.
 */
@ParseClassName("Marker")
public class Marker extends ParseObject {
    public static final String USER_KEY = "user";
    public static final String TRIP_KEY = "trip";
    public static final String LATITUDE_KEY = "latitude";
    public static final String LONGITUDE_KEY = "longitude";
    public Trip trip;
    public double latitude;
    public double longitude;
    public ParseUser user;

    public Marker() {
        // A default constructor is required.
    }

    public double getLatitudeKey() {
        return getDouble(LATITUDE_KEY);
    }

    public double getLongitudeKey() {
        return getDouble(LONGITUDE_KEY);
    }

    public Trip getTrip() {
        return (Trip) getParseObject(TRIP_KEY);
    }

    public ParseUser getUser() {
        return getParseUser(USER_KEY);
    }

    public void setUser(ParseUser u) {
        put(USER_KEY, u);
    }

    public void setTrip(String tripId) {
        put(TRIP_KEY, ParseObject.createWithoutData(Trip.class, tripId));
    }
    public void setLatitudeKey(double latitude) {
        put(LATITUDE_KEY, latitude);
    }

    public void setLongitudeKey(double longitude) {
        put(LONGITUDE_KEY, longitude);
    }
}
