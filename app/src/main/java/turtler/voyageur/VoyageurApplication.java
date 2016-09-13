package turtler.voyageur;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.interceptors.ParseLogInterceptor;

import turtler.voyageur.models.Event;
import turtler.voyageur.models.Image;
import turtler.voyageur.models.Marker;
import turtler.voyageur.models.Trip;
import turtler.voyageur.models.User;

/**
 * Created by cwong on 9/11/16.
 */
public class VoyageurApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ParseObject.registerSubclass(Marker.class);
        ParseObject.registerSubclass(User.class);
        ParseObject.registerSubclass(Trip.class);
        ParseObject.registerSubclass(Event.class);
        ParseObject.registerSubclass(Image.class);

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("voyaging") // should correspond to APP_ID env variable
                .clientKey("sayheyhey")  // set explicitly unless clientKey is explicitly configured on Parse server
                .addNetworkInterceptor(new ParseLogInterceptor())
                .server("https://voyaging.herokuapp.com/parse/").build());

        FacebookSdk.sdkInitialize(this);
        ParseFacebookUtils.initialize(this);
    }

}
