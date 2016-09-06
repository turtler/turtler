package turtler.voyageur.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.util.ArrayList;

/**
 * Created by carolinewong on 9/3/16.
 */
@ParseClassName("AppUser")
public class User extends ParseObject {

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public ArrayList<User> getFriends() {
        return friends;
    }

    public ArrayList<Trip> getTrips() {
        return trips;
    }

    public String email;
    public String name;
    public String pictureUrl;
    public ArrayList<User> friends;
    public ArrayList<Trip> trips;

    public User() {}

    public User(String email, String name, String pictureUrl, ArrayList<User> friends, ArrayList<Trip> trips) {
        this.email = email;
        this.name = name;
        this.pictureUrl = pictureUrl;
        this.friends = friends;
        this.trips = trips;
    }
}