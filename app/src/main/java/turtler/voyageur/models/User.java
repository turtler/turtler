package turtler.voyageur.models;

import java.util.ArrayList;

/**
 * Created by carolinewong on 9/3/16.
 */
public class User {

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getPicture_url() {
        return picture_url;
    }

    public ArrayList<User> getFriends() {
        return friends;
    }

    public ArrayList<Trip> getTrips() {
        return trips;
    }

    public String email;
    public String name;
    public String picture_url;
    public ArrayList<User> friends;
    public ArrayList<Trip> trips;

    public User() {}

    public User(String email, String name, String picture_url, ArrayList<User> friends, ArrayList<Trip> trips) {
        this.email = email;
        this.name = name;
        this.picture_url = picture_url;
        this.friends = friends;
        this.trips = trips;
    }

}
