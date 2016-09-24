package turtler.voyageur.fragments;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStrip;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

import turtler.voyageur.R;
import turtler.voyageur.adapters.TripFragmentPageAdapter;
import turtler.voyageur.models.Trip;
import turtler.voyageur.models.User;

/**
 * Created by skulkarni on 9/14/16.
 */
public class ViewPagerContainerFragment extends Fragment {
    ImageView ivCover;
    PagerSlidingTabStrip tabsStrip;
    public ViewPager viewPager;
    TripFragmentPageAdapter pagerAdapter;
    String tripImage;
    Toolbar toolbar;
    TextView toolbarTitle;
    AppCompatActivity parentActivity;
    CollapsingToolbarLayout collapsingToolbarLayout;
    HashMap<MenuItem, String> menuItemStringHashMap = new HashMap<>();
    Trip trip;
    User user;

    public ViewPagerContainerFragment() {   }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        tripImage = getArguments().getString("tripImage");
        View root = inflater.inflate(R.layout.fragment_view_pager_container, container, false);
        toolbar = (Toolbar) root.findViewById(R.id.toolbarViewPager);
        toolbarTitle = (TextView) root.findViewById(R.id.tvToolbarTitle);
        collapsingToolbarLayout = (CollapsingToolbarLayout) root.findViewById(R.id.collapsing_toolbar);
        parentActivity = (AppCompatActivity) getActivity();
        parentActivity.setSupportActionBar(toolbar);
        setHasOptionsMenu(true);
        parentActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        parentActivity.getSupportActionBar().setDisplayShowHomeEnabled(false);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    parentActivity.onBackPressed();
                }
        });
        pagerAdapter = new TripFragmentPageAdapter(getChildFragmentManager());
        viewPager = (ViewPager) root.findViewById(R.id.viewPager);
        ivCover = (ImageView) root.findViewById(R.id.ivCoverViewPager);
        tabsStrip = (PagerSlidingTabStrip) root.findViewById(R.id.tabStrip);
        if (tripImage != null) {
            ivCover.setTag(1);
            Picasso.with(getContext()).load(tripImage).into(ivCover);
        }
        else {
            ivCover.setBackgroundColor(Color.parseColor("#a1d0ff"));
        }

        viewPager.setAdapter(pagerAdapter);
        tabsStrip.setViewPager(viewPager);

        populateTripUI();
        return root;
    }

    private void populateTripUI() {
        String tripId = getArguments().getString("tripId");
        ParseQuery<Trip> query = ParseQuery.getQuery("Trip");
        query.orderByDescending("date").getInBackground(tripId, new GetCallback<Trip>() {
            @Override
            public void done(Trip object, ParseException e) {
                trip = object;
                collapsingToolbarLayout.setTitle(trip.getName().toString());

                trip.getTripFriendsRelation().getQuery().findInBackground(new FindCallback<User>() {
                    @Override
                    public void done(List<User> users, ParseException e) {
                        if (users != null) {
                            user = users.get(0);
                            addUserItems(users);
                            View b = parentActivity.findViewById(R.id.fabAddEvent);
                            for (int i = 0; i < users.size(); i++) {
                                User f = users.get(i);
                                if (f.getEmail().equals(ParseUser.getCurrentUser().getEmail())) {
                                    b.setVisibility(View.VISIBLE);
                                    break;
                                } else {
                                    b.setVisibility(View.GONE);
                                }
                            }
                        }
                    }
                });
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
        final int num = 0;
        final Menu menu = toolbar.getMenu();
        for (final User friend : tripFriends) {
            final String initials = getInitials(friend.getName());
            Glide.with(this).load(friend.getPictureUrl()).asBitmap().centerCrop().into(new SimpleTarget<Bitmap>(80, 80) {
                @Override
                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                    RoundedBitmapDrawable d =
                            RoundedBitmapDrawableFactory.create(parentActivity.getResources(), resource);
                    d.setCircular(true);
                    final MenuItem menuItem = menu.add(Menu.NONE, num, Menu.NONE, "");
                    menuItem.setIcon(d);
                    menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                    menuItemStringHashMap.put(menuItem, friend.getEmail());
                    menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            String email = menuItemStringHashMap.get(item).toString();
                            Fragment pf = ProfileFragment.newInstance();
                            Bundle b = new Bundle();
                            b.putString("email", email);
                            pf.setArguments(b);
                            FragmentTransaction ftProfile = parentActivity.getSupportFragmentManager().beginTransaction();
                            ftProfile.replace(R.id.flTrip, pf);
                            ftProfile.addToBackStack("profile");
                            ftProfile.commit();
                            return true;
                        }
                    });


                }
            });
        }

    }

    public TripFragmentPageAdapter getAdapter() {
        return pagerAdapter;
    }
}

