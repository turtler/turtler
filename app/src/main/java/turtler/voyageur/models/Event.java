package turtler.voyageur.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseRelation;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by carolinewong on 9/3/16.
 */
@ParseClassName("Event")
public class Event extends ParseObject {
    public Trip trip;
    public ArrayList<Image> images;
    public ArrayList<User> peopleAtEvent;
    public String caption;
    public User creator;
    public String title;
    public Marker marker;
    public Date date;
    public Integer eventDay;

    public void setEventDay(int day) {
        eventDay = day;
    }
    public Integer getEventDay() {
        return eventDay;
    }
    public Marker getMarker() {
        return (Marker) getParseObject("marker");
    }

    public Event() {
        super();
    }

    public Trip getTrip()  {
        return (Trip) getParseObject("trip");
    }

    public void setTrip(String tripId) {
        put("trip", ParseObject.createWithoutData(Trip.class, tripId));
    }

    public ArrayList<Image> getImages() {
        return images;
    }

    public ArrayList<User> getPeopleAtEvent() {
        return peopleAtEvent;
    }

    public ParseRelation<User> getFriendsRelation() {
        return getRelation("friends");
    }

    public void addFriend(User f) {
        getFriendsRelation().add(f);
        saveInBackground();
    }

    public void removeFriend(User f) {
        getFriendsRelation().remove(f);
        saveInBackground();
    }

    public void setImageURL(String imgURL) {
        put("imageUrl", imgURL);
    }

    public String getImageURL() {
        return getString("imageUrl");
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

    public String getTitle() {
        return getString("title");
    }

    public void setTitle(String title) {
        put("title", title);
    }

    public Date getDate() {
        return getDate("date");
    }

    public void setDate(Date date) {
        put("date", date);
    }

    public ParseRelation<Image> imagesRelation() {
        return getRelation("image");
    }

    public void addImage(Image image) {
        imagesRelation().add(image);
        saveInBackground();
    }

    public void removeImage(Image image) {
        imagesRelation().remove(image);
        saveInBackground();
    }

    public ParseRelation<Marker> getMarkerParseRelation() {
        return getRelation("marker");
    }

    public void addMarker(Marker m) {
        getMarkerParseRelation().add(m);
        saveInBackground();
    }

    public void removeMarker(Marker m) {
        getMarkerParseRelation().remove(m);
        saveInBackground();
    }

    public Event(Trip t, ArrayList<Image> images, ArrayList<User> peopleAtEvent, String caption) {
        this.trip = t;
        this.images = images;
        this.peopleAtEvent = peopleAtEvent;
        this.caption = caption;
    }
}

