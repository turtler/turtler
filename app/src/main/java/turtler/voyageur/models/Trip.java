package turtler.voyageur.models;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseRelation;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by carolinewong on 9/3/16.
 */
@ParseClassName("Trip")
public class Trip extends ParseObject {
    public String name;
    public Date startDate;
    public Date endDate;
    public ArrayList<User> tripFriends;
    public ArrayList<Event> events;
    public User tripCreator;

    public Trip() {
        super();
    }

    public Trip(String name) {
        super();
        setName(name);
    }

    public String getName() {
        return getString("name");
    }

    public void setName(String name) {
        put("name", name);
    }

    public Date getStartDate() {
        return getDate("startDate");
    }

    public void setStartDate(Date startDate) {
        put("startDate", startDate);
    }

    public Date getEndDate() {
        return getDate("endDate");
    }

    public void setEndDate(Date endDate) {
        put("endDate", endDate);
    }

    public ArrayList<Event> getEvents() {
        return events;
    }

    public Image getCoverPhotoURL() {
        try {
            return getImageRelation().getQuery().getFirst();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void addImage(Image image) {
        getImageRelation().add(image);
        saveInBackground();
    }

    public ParseRelation<Image> getImageRelation() {
        return getRelation("image");
    }


    public ParseRelation<User> getTripFriendsRelation() {
        return getRelation("tripFriends");
    }

    public void addTripFriend(User friend) {
        getTripFriendsRelation().add(friend);
        saveInBackground();
    }

    public void removeTripFriend(User friend) {
        getTripFriendsRelation().remove(friend);
        saveInBackground();
    }

    public ParseRelation<Event> getEventsRelation() {
        return getRelation("events");
    }

    public void addEvent(Event event) {
        getEventsRelation().add(event);
        saveInBackground();
    }

    public void removeEvent(Event event) {
        getEventsRelation().remove(event);
        saveInBackground();
    }

    public ParseRelation<User> getTripCreatorRelation() {
        return getRelation("tripCreator");
    }

    public void addTripCreator(User tripCreator) {
        getTripCreatorRelation().add(tripCreator);
        saveInBackground();
    }

    public void removeTripCreator(User tripCreator) {
        getTripCreatorRelation().remove(tripCreator);
        saveInBackground();
    }
}
