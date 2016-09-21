package turtler.voyageur.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.FindCallback;
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
import turtler.voyageur.models.Trip;
import turtler.voyageur.models.User;

/**
 * Created by cwong on 9/11/16.
 */
public class ProfileFragment extends Fragment implements CreateTripFragment.CreateTripFragmentListener {
    @BindView(R.id.rvTrips) RecyclerView rvTrips;
    @BindView(R.id.fabAddTrip) FloatingActionButton fabAddTrip;
    ArrayList<Trip> trips;
    TripAdapter tripAdapter;

    public static ProfileFragment newInstance() {
        Bundle args = new Bundle();
        ProfileFragment fragment = new ProfileFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, view);
        trips = new ArrayList<>();
        tripAdapter = new TripAdapter(getContext(), trips);
        rvTrips.setAdapter(tripAdapter);
        rvTrips.setLayoutManager(new LinearLayoutManager(getContext()));

        fabAddTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCreateTripFragment();
            }
        });
        populateTrips();
        return view;
    }

    public void populateTrips() {
        User currentUser;
        if (getArguments() != null && getArguments().containsKey("email")) {
            fabAddTrip.setVisibility(View.GONE);
            String email = getArguments().getString("email");
            ParseQuery<User> pq = new ParseQuery("_User");
            pq.whereEqualTo("email", email);
            pq.findInBackground(new FindCallback<User>() {
                @Override
                public void done(List<User> users, ParseException e) {
                    if (users != null && users.size() > 0) {
                        User user = users.get(0);
                        ParseRelation<ParseObject> tripRelation = user.getRelation("trips");
                        loadTrip(tripRelation);
                    }
                }
            });
        } else {
            fabAddTrip.setVisibility(View.VISIBLE);
            currentUser = (User) ParseUser.getCurrentUser();
            ParseRelation<ParseObject> tripRelation = currentUser.getRelation("trips");
            loadTrip(tripRelation);
        }
    }

    public void loadTrip(ParseRelation<ParseObject> tripRelation) {
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
        FragmentManager fm = getActivity().getSupportFragmentManager();
        CreateTripFragment tripFragment = CreateTripFragment.newInstance();
        tripFragment.setTargetFragment(ProfileFragment.this, 300);
        tripFragment.show(fm, "fragment_create_trip");
    }

    @Override
    public void onFinishCreateTripDialog(Trip newTrip) {
        trips.add(0, newTrip);
        tripAdapter.notifyItemInserted(0);
    }
}