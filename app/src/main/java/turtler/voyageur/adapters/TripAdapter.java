package turtler.voyageur.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import turtler.voyageur.R;
import turtler.voyageur.activities.TripActivity;
import turtler.voyageur.models.Event;
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
        //@BindView(R.id.tvStartDate) TextView tvStartDate;
        //@BindView(R.id.tvEndDate) TextView tvEndDate;


        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getLayoutPosition();

            ParseObject trip = mTrips.get(position);
            Intent i = new Intent(mContext, TripActivity.class);
            i.putExtra("tripId", trip.getObjectId());
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
        /*if (t.getStartDate() != null) {
            String startDate = TimeFormatUtils.dateToString(t.getStartDate());
            viewHolder.tvStartDate.setText(startDate);
        }
        if (t.getEndDate() != null) {
            String endDate = TimeFormatUtils.dateToString(t.getEndDate());
            viewHolder.tvEndDate.setText(endDate);
        }*/
        if (t.getCoverPhotoURL() != null) {
            Picasso.with(mContext).load(t.getCoverPhotoURL().getPictureUrl()).into(viewHolder.ivCoverImage);
        }
        else {
            Picasso.with(mContext).load(coverPhotoURL).into(viewHolder.ivCoverImage);
        }

    }

    @Override
    public int getItemCount() {
        return mTrips.size();
    }
}