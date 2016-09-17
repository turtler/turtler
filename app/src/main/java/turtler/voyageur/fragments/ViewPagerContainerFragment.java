package turtler.voyageur.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.astuetz.PagerSlidingTabStrip;
import com.squareup.picasso.Picasso;

import turtler.voyageur.R;
import turtler.voyageur.adapters.TripFragmentPageAdapter;

/**
 * Created by skulkarni on 9/14/16.
 */
public class ViewPagerContainerFragment extends Fragment {
    ImageView ivCover;
    PagerSlidingTabStrip tabsStrip;
    public ViewPager viewPager;
    TripFragmentPageAdapter pagerAdapter;
    String tripImage;
    public ViewPagerContainerFragment() {   }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        tripImage = getArguments().getString("tripImage");

        View root = inflater.inflate(R.layout.fragment_view_pager_container, container, false);

        pagerAdapter = new TripFragmentPageAdapter(getActivity().getSupportFragmentManager());
        viewPager = (ViewPager) root.findViewById(R.id.viewPager);
        ivCover = (ImageView) root.findViewById(R.id.ivCoverImage);
        tabsStrip = (PagerSlidingTabStrip) root.findViewById(R.id.tabStrip);
        if (tripImage != null) {
            Picasso.with(getContext()).load(tripImage).into(ivCover);
        }

        viewPager.setAdapter(pagerAdapter);
        tabsStrip.setViewPager(viewPager);

        return root;
    }

    public TripFragmentPageAdapter getAdapter() {
        return pagerAdapter;
    }
}

