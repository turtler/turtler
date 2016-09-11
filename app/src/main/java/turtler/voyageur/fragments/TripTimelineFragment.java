package turtler.voyageur.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import turtler.voyageur.R;

/**
 * Created by cwong on 9/11/16.
 */
public class TripTimelineFragment extends android.support.v4.app.Fragment {

    public static TripTimelineFragment newInstance() {
        Bundle args = new Bundle();
        TripTimelineFragment fragment = new TripTimelineFragment();
        fragment.setArguments(args);
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
        return view;
    }
}
