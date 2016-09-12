package turtler.voyageur.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseRelation;

import java.util.ArrayList;

/**
 * Created by carolinewong on 9/3/16.
 */
@ParseClassName("Event")
public class Event extends ParseObject {
    public Trip trip;
    public ArrayList<Image> images;
    public ArrayList<User> peopleAtEvent;
    public String caption;
    public User eventCreator;

    public Event() {
        super();
    }

    public Trip getTrip()  {
        return (Trip) getParseObject("trip");
    }

    public void setTrip(Trip t) {
        put("trip", t);
    }

    public ArrayList<Image> getImages() {
        return images;
    }

    public ArrayList<User> getPeopleAtEvent() {
        return peopleAtEvent;
    }

    public String getCaption() {
        return getString("caption");
    }

    public void setCaption(String caption) {
        put("caption", caption);
    }

    public User getCreator()  {
        return (User) getParseUser("user");
    }

    public void setCreator(User user) {
        put("user", user);
    }

    public ParseRelation<Image> imagesRelation() {
        return getRelation("peopleAtEvent");
    }

    public void addImage(Image image) {
        imagesRelation().add(image);
        saveInBackground();
    }

    public void removeImage(Image image) {
        imagesRelation().remove(image);
        saveInBackground();
    }

    public ParseRelation<User> peopleAtEventRelation() {
        return getRelation("peopleAtEvent");
    }

    public void addUserToEvent(User friend) {
        peopleAtEventRelation().add(friend);
        saveInBackground();
    }

    public void removeUserFromEvent(User friend) {
        peopleAtEventRelation().remove(friend);
        saveInBackground();
    }

    public Event(Trip t, ArrayList<Image> images, ArrayList<User> peopleAtEvent, String caption) {
        this.trip = t;
        this.images = images;
        this.peopleAtEvent = peopleAtEvent;
        this.caption = caption;
    }
}

