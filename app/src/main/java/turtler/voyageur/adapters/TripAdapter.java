package turtler.voyageur.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import turtler.voyageur.R;
import turtler.voyageur.activities.TripActivity;
import turtler.voyageur.models.Image;
import turtler.voyageur.models.Trip;
import turtler.voyageur.utils.TimeFormatUtils;

/**
 * Created by carolinewong on 9/9/16.
 */
public class TripAdapter extends RecyclerView.Adapter<TripAdapter.ViewHolder> {
    private ArrayList<Trip> mTrips;
    private Context mContext;
    String coverPhotoURL = "http://coverphotosite.com/thumbs/the_great_salt_lake-t1.jpg";

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.ivCoverImage)
        ImageView ivCoverImage;
        @BindView(R.id.tvTripName) TextView tvTripName;
        @BindView(R.id.tvTripDate) TextView tvTripDate;


        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getLayoutPosition();

            Trip trip = mTrips.get(position);
            Intent i = new Intent(mContext, TripActivity.class);
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) mContext, (View)(ivCoverImage), "cover");

            Bundle b = options.toBundle();
            b.putString("tripId", trip.getObjectId());
            Image coverPhoto = trip.getCoverPhoto();
            if (coverPhoto != null) {
                b.putString("tripImage", coverPhoto.getPictureUrl());
            }
            i.putExtras(b);
            mContext.startActivity(i);
        }
    }

    public TripAdapter(Context context, ArrayList<Trip> trips) {
        mContext = context;
        mTrips = trips;
    }

    @Override
    public TripAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View itemTripView = inflater.inflate(R.layout.item_trip, parent, false);
        ViewHolder viewHolder = new ViewHolder(itemTripView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(TripAdapter.ViewHolder viewHolder, int position) {
        Trip t = mTrips.get(position);

        viewHolder.tvTripName.setText(t.getName());
        if (t.getStartDate() != null && t.getEndDate() != null) {
            String startDate = TimeFormatUtils.dateToString(t.getStartDate());
            String endDate = TimeFormatUtils.dateToString(t.getEndDate());
            viewHolder.tvTripDate.setText(startDate + " - " + endDate);
        }


        if (t.getCoverPhoto() != null) {
            coverPhotoURL = t.getCoverPhoto().getPictureUrl();
            Glide.with(mContext).load(t.getCoverPhoto().getPictureUrl()).into(viewHolder.ivCoverImage);
        }
        else {

            Glide.with(mContext).load(coverPhotoURL).into(viewHolder.ivCoverImage);

        }

    }

    @Override
    public int getItemCount() {
        return mTrips.size();
    }
}