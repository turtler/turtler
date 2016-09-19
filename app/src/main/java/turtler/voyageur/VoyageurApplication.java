package turtler.voyageur;

import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.facebook.FacebookSdk;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.interceptors.ParseLogInterceptor;

import permissions.dispatcher.NeedsPermission;
import turtler.voyageur.models.Event;
import turtler.voyageur.models.Image;
import turtler.voyageur.models.Marker;
import turtler.voyageur.models.Trip;
import turtler.voyageur.models.User;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by cwong on 9/11/16.
 */
public class VoyageurApplication extends Application {
    protected LocationManager locationManager;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private long UPDATE_INTERVAL = 60000;  /* 60 secs */
    private long FASTEST_INTERVAL = 5000; /* 5 secs */
    private static final String[] PERMISSION_GETMYLOCATION = new String[] {"android.permission.ACCESS_FINE_LOCATION","android.permission.ACCESS_COARSE_LOCATION"};

    private GoogleApiClientHelper googleApiHelper;
    private static VoyageurApplication mInstance;

    public GoogleApiClientHelper getGoogleApiHelperInstance() {
        return this.googleApiHelper;
    }
    public static GoogleApiClientHelper getGoogleApiHelper() {
        return getInstance().getGoogleApiHelperInstance();
    }

    public static synchronized VoyageurApplication getInstance() {
        return mInstance;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        googleApiHelper = new GoogleApiClientHelper(this);

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

        if (ContextCompat.checkSelfPermission(VoyageurApplication.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == 1) {
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
                        @Override
                        public void onStatusChanged(String provider, int status, Bundle extras) {
                        }
                        @Override
                        public void onProviderEnabled(String provider) {
                        }
                        @Override
                        public void onProviderDisabled(String provider) {
                        }
                        @Override
                        public void onLocationChanged(final Location location) {
                        }
                    });
        }
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/UniSansRegular.otf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
    }

    @SuppressWarnings("all")
    @NeedsPermission({android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION})
    void getMyLocation() {
        locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
    }
}
