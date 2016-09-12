package turtler.voyageur.activities;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.astuetz.PagerSlidingTabStrip;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import butterknife.BindView;
import butterknife.ButterKnife;
import turtler.voyageur.R;
import turtler.voyageur.adapters.TripFragmentPageAdapter;
import turtler.voyageur.models.Trip;

/**
 * Created by cwong on 9/11/16.
 */
public class TripActivity extends AppCompatActivity {
    @BindView(R.id.viewPager) ViewPager viewPager;
    @BindView(R.id.tabStrip) PagerSlidingTabStrip tabsStrip;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip);
        ButterKnife.bind(this);

        TripFragmentPageAdapter pagerAdapter = new TripFragmentPageAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        tabsStrip.setViewPager(viewPager);

        String tripId = getIntent().getStringExtra("tripId");
        ParseQuery<Trip> query = ParseQuery.getQuery(Trip.class);
        query.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
        query.getInBackground(tripId, new GetCallback<Trip>() {
            @Override
            public void done(Trip trip, ParseException e) {
                if (e == null) {

                }
            }
        });
    }
}
