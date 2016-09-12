package turtler.voyageur.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import turtler.voyageur.R;
import turtler.voyageur.models.Event;
import turtler.voyageur.models.Trip;

/**
 * Created by cwong on 9/11/16.
 */
public class TripTimelineFragment extends android.support.v4.app.Fragment implements CreateEventFragment.CreateEventFragmentListener {
    @BindView(R.id.rvEvents) RecyclerView rvEvents;
    @BindView(R.id.fabAddEvent) FloatingActionButton fabAddEvent;
    Unbinder unbinder;
    Trip trip;
    ArrayList<Event> events;

    public static TripTimelineFragment newInstance() {
        TripTimelineFragment fragment = new TripTimelineFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String tripId = getActivity().getIntent().getStringExtra("tripId");
        ParseQuery query = ParseQuery.getQuery(Trip.class);
        query.include("events");
        query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
        try {
            trip = (Trip) query.get(tripId);
            events = (ArrayList<Event>) trip.getEventsRelation().getQuery().find();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trip_timeline, container, false);
        unbinder = ButterKnife.bind(this, view);
        fabAddEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCreateEventFragment();
            }
        });
        return view;
    }

    private void showCreateEventFragment() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        CreateEventFragment eventDialogFragment = CreateEventFragment.newInstance(trip.getObjectId());
        eventDialogFragment.setTargetFragment(TripTimelineFragment.this, 300);
        eventDialogFragment.show(fm, "fragment_create_event");
    }

    @Override
    public void onFinishCreateEventDialog(Event event) {
        Log.d("event", event.toString());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
