package turtler.voyageur.activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import turtler.voyageur.R;
import turtler.voyageur.adapters.TripAdapter;
import turtler.voyageur.fragments.CreateTripFragment;
import turtler.voyageur.models.Trip;
import turtler.voyageur.models.User;

/**
 * Created by carolinewong on 9/3/16.
 */
public class ProfileActivity extends AppCompatActivity implements CreateTripFragment.CreateTripFragmentListener {
    @BindView(R.id.rvTrips) RecyclerView rvTrips;
    @BindView(R.id.fabAddTrip) FloatingActionButton fabAddTrip;
    ArrayList<Trip> trips;
    TripAdapter tripAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);
        trips = new ArrayList<>();
        tripAdapter = new TripAdapter(this, trips);
        rvTrips.setAdapter(tripAdapter);
        rvTrips.setLayoutManager(new LinearLayoutManager(this));

        fabAddTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCreateTripFragment();
            }
        });
        populateTrips();
    }

    public void populateTrips() {
        User currentUser = (User) ParseUser.getCurrentUser();
        ParseRelation<ParseObject> tripRelation = currentUser.getRelation("trips");
        try {
            int curSize = tripAdapter.getItemCount();
            ParseQuery query = tripRelation.getQuery();
            query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);

            List<Trip> queriedTrips = query.find();
            trips.addAll(queriedTrips);
            tripAdapter.notifyItemRangeInserted(curSize, queriedTrips.size());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void showCreateTripFragment() {
        FragmentManager fm = getSupportFragmentManager();
        CreateTripFragment alertDialog = CreateTripFragment.newInstance();
        alertDialog.show(fm, "fragment_create_trip");
    }

    @Override
    public void onFinishCreateTripDialog(Trip newTrip) {
        int curSize = tripAdapter.getItemCount();
        trips.add(curSize, newTrip);
        tripAdapter.notifyItemInserted(curSize);
    }
}
