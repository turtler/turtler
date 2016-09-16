package turtler.voyageur.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.astuetz.PagerSlidingTabStrip;

import butterknife.BindView;
import turtler.voyageur.R;
import turtler.voyageur.adapters.TripFragmentPageAdapter;

/**
 * Created by skulkarni on 9/14/16.
 */
public class ViewPagerContainerFragment extends Fragment {
    PagerSlidingTabStrip tabsStrip;
    public ViewPager viewPager;
    TripFragmentPageAdapter pagerAdapter;
    public ViewPagerContainerFragment() {   }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_view_pager_container, container, false);

        pagerAdapter = new TripFragmentPageAdapter(getActivity().getSupportFragmentManager());
        viewPager = (ViewPager) root.findViewById(R.id.viewPager);
        tabsStrip = (PagerSlidingTabStrip) root.findViewById(R.id.tabStrip);

        viewPager.setAdapter(pagerAdapter);
        tabsStrip.setViewPager(viewPager);

        return root;
    }

    public TripFragmentPageAdapter getAdapter() {
        return pagerAdapter;
    }
}

