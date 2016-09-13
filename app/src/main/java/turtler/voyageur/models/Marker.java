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
    public static final String EVENT_KEY = "event";

    public static final String LATITUDE_KEY = "latitude";
    public static final String LONGITUDE_KEY = "longitude";
    public Trip trip;
    public Event event;
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

    public Event getEvent() {
        return (Event) getParseObject(EVENT_KEY);
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
    public void setLatitude(double latitude) {
        put(LATITUDE_KEY, latitude);
    }

    public void setLongitude(double longitude) {
        put(LONGITUDE_KEY, longitude);
    }

    public void setUser(User u) {
        put(USER_KEY, u);
    }
    public void setEvent(Event e) {
        put(EVENT_KEY, e);
    }
}
