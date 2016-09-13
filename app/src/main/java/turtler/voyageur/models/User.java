package turtler.voyageur.models;

import com.parse.ParseClassName;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;

/**
 * Created by carolinewong on 9/3/16.
 */
@ParseClassName("_User")
public class User extends ParseUser {
    public User() {
        super();
    }

    public String getEmail() {
        return getString("email");
    }

    public String getName() {
        return getString("name");
    }

    public String getfID() {
        return getString("fID");
    }

    public String getPictureUrl() {
        return getString("pictureUrl");
    }

    public ParseRelation<User> getFriends() {
        return getRelation("friends");
    }

    public ArrayList<Trip> getTrips() {
        return trips;
    }

    public String email;
    public String name;
    public String pictureUrl;
    public ArrayList<User> friends;
    public ArrayList<Trip> trips;
    public String fID;

    public void setName(String name) {
        put("name", name);
    }

    public void setfID(String fID) {
        put("fID", fID);
    }

    public ParseRelation<Trip> getTripsRelation() {
        return getRelation("trips");
    }

    public void addTrip(Trip t) {
        getTripsRelation().add(t);
        saveInBackground();
    }

    public void removeTrip(Trip t) {
        getTripsRelation().remove(t);
        saveInBackground();
    }

    public ParseRelation<User> getFriendsRelation() {
        return getRelation("friends");
    }

    public void addFriend(User u) {
        getFriendsRelation().add(u);
        saveInBackground();
    }

    public void removeFriend(User u) {
        getFriendsRelation().remove(u);
        saveInBackground();
    }
}