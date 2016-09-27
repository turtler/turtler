package turtler.voyageur.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import jp.wasabeef.recyclerview.animators.FadeInAnimator;
import turtler.voyageur.R;
import turtler.voyageur.adapters.EventAdapter;
import turtler.voyageur.models.Event;
import turtler.voyageur.models.Trip;

/**
 * Created by cwong on 9/11/16.
 */
public class TripTimelineFragment extends android.support.v4.app.Fragment implements CreateEventFragment.CreateEventFragmentListener {
    @BindView(R.id.rvEvents) RecyclerView rvEvents;
    Unbinder unbinder;
    Trip trip;
    EventAdapter eventAdapter;
    ArrayList<Event> events;

    public static TripTimelineFragment newInstance() {
        TripTimelineFragment fragment = new TripTimelineFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trip_timeline, container, false);
        unbinder = ButterKnife.bind(this, view);

        events = new ArrayList<>();
        populateEvents();

        FloatingActionButton fabAddEvent = (FloatingActionButton) getActivity().findViewById(R.id.fabAddEvent);
        fabAddEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCreateEventFragment();
            }
        });
        return view;
    }

    public void populateEvents() {
        String tripId = getActivity().getIntent().getStringExtra("tripId");
        ParseQuery query = ParseQuery.getQuery(Trip.class);
        query.include("events");
        query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
        try {
            trip = (Trip) query.get(tripId);
            events.addAll(trip.getEventsRelation().getQuery().orderByDescending("date").find());
            eventAdapter = new EventAdapter(getContext(), events);
            rvEvents.setAdapter(eventAdapter);
            rvEvents.setLayoutManager(new LinearLayoutManager(getContext()));
            rvEvents.setItemAnimator(new FadeInAnimator());
            rvEvents.getItemAnimator().setAddDuration(1100);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void showCreateEventFragment() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        CreateEventFragment eventDialogFragment = CreateEventFragment.newInstance(trip.getObjectId());
        eventDialogFragment.setTargetFragment(TripTimelineFragment.this, 300);
        eventDialogFragment.show(fm, "fragment_create_event");
    }

    @Override
    public void onFinishCreateEventDialog(Event event) {
        events.add(0, event);
        eventAdapter.notifyItemInserted(0);
        rvEvents.scrollToPosition(0);
        ImageView ivCover = (ImageView) getActivity().findViewById(R.id.ivCoverViewPager);
        if (ivCover.getTag() == null || !ivCover.getTag().equals(1)) {
            Glide.with(getContext()).load(event.getImageURL()).into(ivCover);
            ivCover.setTag(1);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
