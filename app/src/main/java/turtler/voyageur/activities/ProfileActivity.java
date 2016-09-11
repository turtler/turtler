package turtler.voyageur.activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import turtler.voyageur.R;
import turtler.voyageur.adapters.TripAdapter;
import turtler.voyageur.fragments.CreateTripFragment;
import turtler.voyageur.models.Trip;

/**
 * Created by carolinewong on 9/3/16.
 */
public class ProfileActivity extends AppCompatActivity implements CreateTripFragment.CreateTripFragmentListener {
    @BindView(R.id.lvTrips) ListView lvTrips;
    @BindView(R.id.fabAddTrip) FloatingActionButton fabAddTrip;
    ArrayList<ParseObject> trips;
    TripAdapter tripAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);
        trips = new ArrayList<>();
        tripAdapter = new TripAdapter(this, trips);
        lvTrips.setAdapter(tripAdapter);

        fabAddTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCreateTripFragment();
            }
        });
        populateTrips();
    }

    public void populateTrips() {
        ParseQuery<Trip> query = ParseQuery.getQuery("Trip");
        query.whereEqualTo("userId", ParseUser.getCurrentUser().getObjectId());
        query.findInBackground(new FindCallback<Trip>() {
            @Override
            public void done(List<Trip> objects, ParseException e) {
                trips.addAll(objects);
                tripAdapter.notifyDataSetChanged();
            }
        });
    }

    private void showCreateTripFragment() {
        FragmentManager fm = getSupportFragmentManager();
        CreateTripFragment alertDialog = CreateTripFragment.newInstance();
        alertDialog.show(fm, "fragment_create_trip");
    }

    @Override
    public void onFinishCreateTripDialog(ParseObject newTrip) {
        trips.add(newTrip);
        tripAdapter.notifyDataSetChanged();
    }
}
