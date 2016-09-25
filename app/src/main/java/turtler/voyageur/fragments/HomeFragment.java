package turtler.voyageur.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import turtler.voyageur.R;
import turtler.voyageur.adapters.ImageGridAdapter;
import turtler.voyageur.models.Image;

/**
 * Created by cwong on 9/13/16.
 */
public class HomeFragment extends Fragment {
    @BindView(R.id.rvImageGrid) RecyclerView gridView;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    private static View view;

    private ImageGridAdapter gridAdapter;

    public static HomeFragment newInstance() {
        Bundle args = new Bundle();
        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);

            ButterKnife.bind(this, view);
        }
        try {
            view = inflater.inflate(R.layout.fragment_home, container, false);
            ButterKnife.bind(this, view);
            AppCompatActivity parentActivity = (AppCompatActivity) getActivity();
            parentActivity.setSupportActionBar(mToolbar);
            mToolbar.setNavigationIcon(R.mipmap.ic_whitelogo);
            parentActivity.getSupportActionBar().setDisplayShowTitleEnabled(false);
        } catch (InflateException e) {
        /* map is already there, just return view as it is */
        }

        gridAdapter = new ImageGridAdapter(getContext(), getImages());
        gridView.setAdapter(gridAdapter);
        gridView.setLayoutManager(new StaggeredGridLayoutManager(3, 1));
        gridView.setForegroundGravity(Gravity.CENTER_VERTICAL);

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment) getActivity().getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                LatLng searchLtLng = place.getLatLng();
                ParseGeoPoint gp = new ParseGeoPoint(searchLtLng.latitude, searchLtLng.longitude);
                ParseQuery pq = new ParseQuery("Image");
                pq.whereWithinMiles("geoPoint", gp, 100.0);
                pq.findInBackground(new FindCallback() {
                    @Override
                    public void done(List objects, ParseException e) {
                        if (e != null) {
                            Log.e("ERROR", e.toString());
                        }
                    }

                    @Override
                    public void done(Object images, Throwable throwable) {
                        ArrayList<Image> imageItems = (ArrayList<Image>) images;
                        gridAdapter.updateImageList(imageItems);
                        Log.d("locations", images.toString());

                    }
                });
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("bleh", "An error occurred: " + status);
            }
        });

        return view;
    }

    public ArrayList<Image> getImages() {
        final ArrayList<Image> imageItems = new ArrayList<Image>();
        final ParseQuery<Image> parseImageQuery = new ParseQuery("Image");
        try {
            List<Image> parseImages = parseImageQuery.find();
            for (int i = 0; i < parseImages.size(); i++) {
                Image parseImg = parseImages.get(i);
                imageItems.add(parseImg);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return imageItems;
    }
}
