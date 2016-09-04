package turtler.voyageur.models;

import java.util.ArrayList;

/**
 * Created by carolinewong on 9/3/16.
 */
public class Event {
    public int getTripId() {
        return tripId;
    }

    public ArrayList<Image> getImages() {
        return images;
    }

    public ArrayList<User> getPeopleAtEvent() {
        return peopleAtEvent;
    }

    public String getCaption() {
        return caption;
    }

    public int tripId;
    public ArrayList<Image> images;
    public ArrayList<User> peopleAtEvent;
    public String caption;

    public Event() {}

    public Event(int tripId, ArrayList<Image> images, ArrayList<User> peopleAtEvent, String caption) {
        this.tripId = tripId;
        this.images = images;
        this.peopleAtEvent = peopleAtEvent;
        this.caption = caption;
    }
}

