package turtler.voyageur.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import turtler.voyageur.R;
import turtler.voyageur.fragments.ProfileFragment;
import turtler.voyageur.fragments.ViewPagerContainerFragment;
import turtler.voyageur.models.Image;
import turtler.voyageur.models.Trip;
import turtler.voyageur.models.User;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by cwong on 9/11/16.
 */
public class TripActivity extends AppCompatActivity {
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.tvToolbarTitle) TextView toolbarTitle;
    Trip trip;
    User user;
    HashMap<MenuItem, String> menuItemStringHashMap = new HashMap<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);
        String tripId = getIntent().getStringExtra("tripId");
        Trip tripObj = ParseObject.createWithoutData(Trip.class, tripId);
        tripObj.getTripCreatorRelation().getQuery().findInBackground(new FindCallback<User>() {
            @Override
            public void done(List<User> users, ParseException e) {
            }
        });

        ViewPagerContainerFragment vpcf = new ViewPagerContainerFragment();
        String tripImg = getIntent().getExtras().getString("tripImage");
        Bundle b = new Bundle();
        b.putString("tripImage", tripImg);
        vpcf.setArguments(b);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.flTrip, vpcf, "view_pager")
                .commit();

        populateTripUI();
    }

    private void populateTripUI() {
        String tripId = getIntent().getStringExtra("tripId");
        ParseQuery<Trip> query = ParseQuery.getQuery("Trip");
        query.orderByDescending("date").getInBackground(tripId, new GetCallback<Trip>() {
            @Override
            public void done(Trip object, ParseException e) {
                trip = object;
                Bundle b = new Bundle();
                try {
                    List<Image> i = trip.getImageRelation().getQuery().find();
                    if (i != null && i.size() > 0) {
                        b.putString("tripImage", i.get(0).getPictureUrl());
                    }
                } catch (ParseException e1) {
                    e1.printStackTrace();
                }

                trip.getTripCreatorRelation().getQuery().findInBackground(new FindCallback<User>() {
                    @Override
                    public void done(List<User> users, ParseException e) {
                        if (users != null) {
                            user = users.get(0);
                            View b = findViewById(R.id.fabAddEvent);
                            if (user.getEmail().equals(ParseUser.getCurrentUser().getEmail())) {
                                b.setVisibility(View.VISIBLE);
                            }
                            else {
                                b.setVisibility(View.GONE);
                            }
                        }
                    }
                });

                toolbarTitle.setText(trip.getName().toString());
                try {
                    List<User> tripFriends = trip.getTripFriendsRelation().getQuery().find();
                    if (tripFriends.size() > 0) {
                        addUserItems(tripFriends);
                    }
                } catch (ParseException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    public String getInitials(String fullName) {
        String[] nameArray = fullName.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String name : nameArray) {
            sb.append(name.charAt(0));
        }
        return sb.toString();
    }

    public void addUserItems(List<User> tripFriends) {
        int num = 0;
        Menu menu = toolbar.getMenu();
        for (User friend : tripFriends) {
            String initials = getInitials(friend.getName());
            final MenuItem menuItem = menu.add(Menu.NONE, num, Menu.NONE, initials);
            menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menuItemStringHashMap.put(menuItem, friend.getEmail());
            menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    String email = menuItemStringHashMap.get(item).toString();
                    Fragment pf = ProfileFragment.newInstance();
                    Bundle b = new Bundle();
                    b.putString("email", email);
                    pf.setArguments(b);
                    FragmentTransaction ftProfile = getSupportFragmentManager().beginTransaction();
                    ftProfile.replace(R.id.flTrip, pf);
                    ftProfile.addToBackStack(null);
                    ftProfile.commit();
                    return true;
                }
            });
        }
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
