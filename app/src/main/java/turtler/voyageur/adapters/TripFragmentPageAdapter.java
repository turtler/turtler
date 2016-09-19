package turtler.voyageur.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;

import turtler.voyageur.fragments.TripMapFragment;
import turtler.voyageur.fragments.TripTimelineFragment;

/**
 * Created by cwong on 9/11/16.
 */

public class TripFragmentPageAdapter extends FragmentPagerAdapter {
    private String tabTitles[] = { "Timeline", "Map" };

    public TripFragmentPageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return TripTimelineFragment.newInstance();
        } else if (position == 1) {
            return TripMapFragment.newInstance();
        } else {
            return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }

    @Override
    public int getCount() {
        return tabTitles.length;
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }

}