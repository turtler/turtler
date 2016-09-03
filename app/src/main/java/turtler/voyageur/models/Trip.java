package turtler.voyageur.models;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by carolinewong on 9/3/16.
 */
public class Trip {
    public String getName() {
        return name;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public ArrayList<User> getTripFriends() {
        return tripFriends;
    }

    public ArrayList<Event> getEvents() {
        return events;
    }

    public String name;
    public Date startDate;
    public Date endDate;
    public ArrayList<User> tripFriends;
    public ArrayList<Event> events;

    public Trip() {}

    public Trip(String name, Date startDate, Date endDate, ArrayList<User> tripFriends, ArrayList<Event> events) {
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.tripFriends = tripFriends;
        this.events = events;
    }
}
