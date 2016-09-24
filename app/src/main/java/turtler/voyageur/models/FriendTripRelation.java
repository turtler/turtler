package turtler.voyageur.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by cwong on 9/24/16.
 */
@ParseClassName("FriendTripRelation")
public class FriendTripRelation extends ParseObject{
    public String friendId;
    public String tripId;

    public String getTripId() {
        return getString("tripId");
    }

    public void setTripId(String tripId) {
        put("tripId", tripId);
    }

    public String getFriendId() {
        return getString("friendId");
    }

    public void setFriendId(String friendId) {
        put("friendId", friendId);
    }


    public FriendTripRelation() {
        super();
    }

    public FriendTripRelation(String friendId, String tripId) {
        super();
        setFriendId(friendId);
        setTripId(tripId);
    }
}
