package turtler.voyageur.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by skulkarni on 9/5/16.
 */
@ParseClassName("Marker")
public class Marker extends ParseObject {
    public static final String LATITUDE_KEY = "latitude";
    public static final String LONGITUDE_KEY = "longitude";
    public Marker() {
        // A default constructor is required.
    }

    public double getLatitudeKey() {
        return getDouble(LATITUDE_KEY);
    }

    public double getLongitudeKey() {
        return getDouble(LONGITUDE_KEY);
    }

    public void setLatitudeKey(double latitude) {
        put(LATITUDE_KEY, latitude);
    }

    public void setLongitudeKey(double longitude) {
        put(LONGITUDE_KEY, longitude);
    }
}
