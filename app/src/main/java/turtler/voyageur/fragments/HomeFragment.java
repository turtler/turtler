package turtler.voyageur.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.parse.ParseException;
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
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, view);
        gridAdapter = new ImageGridAdapter(getContext(), getImages());
        gridView.setAdapter(gridAdapter);
        gridView.setLayoutManager(new StaggeredGridLayoutManager(3, 1));
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
