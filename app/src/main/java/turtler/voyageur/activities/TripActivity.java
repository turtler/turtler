package turtler.voyageur.activities;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStrip;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import turtler.voyageur.R;
import turtler.voyageur.adapters.TripFragmentPageAdapter;
import turtler.voyageur.models.Trip;
import turtler.voyageur.models.User;

/**
 * Created by cwong on 9/11/16.
 */
public class TripActivity extends AppCompatActivity {
    @BindView(R.id.viewPager) ViewPager viewPager;
    @BindView(R.id.tabStrip) PagerSlidingTabStrip tabsStrip;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.tvToolbarTitle) TextView toolbarTitle;
    Trip trip;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);

        TripFragmentPageAdapter pagerAdapter = new TripFragmentPageAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        tabsStrip.setViewPager(viewPager);
        populateTripUI();
    }

    private void populateTripUI() {
        String tripId = getIntent().getStringExtra("tripId");
        ParseQuery<Trip> query = ParseQuery.getQuery("Trip");
        query.getInBackground(tripId, new GetCallback<Trip>() {
            @Override
            public void done(Trip object, ParseException e) {
                trip = object;
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
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }
}
