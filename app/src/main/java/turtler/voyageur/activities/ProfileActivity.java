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
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import turtler.voyageur.R;
import turtler.voyageur.adapters.TripAdapter;
import turtler.voyageur.fragments.CreateTripFragment;

/**
 * Created by carolinewong on 9/3/16.
 */
public class ProfileActivity extends AppCompatActivity implements CreateTripFragment.CreateTripFragmentListener {
    @BindView(R.id.rvTrips) RecyclerView rvTrips;
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
        ParseUser currentUser = ParseUser.getCurrentUser();
        ParseRelation tripRelation = (ParseRelation) currentUser.get("trips");
        try {
            int curSize = tripAdapter.getItemCount();
            List<ParseObject> queriedTrips = tripRelation.getQuery().find();
            trips.addAll(tripRelation.getQuery().find());
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
    public void onFinishCreateTripDialog(ParseObject newTrip) {
        int curSize = tripAdapter.getItemCount();
        trips.add(curSize, newTrip);
        tripAdapter.notifyItemInserted(curSize);
    }
}
