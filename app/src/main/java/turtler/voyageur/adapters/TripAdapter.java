package turtler.voyageur.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import turtler.voyageur.R;
import turtler.voyageur.activities.TripActivity;
import turtler.voyageur.utils.TimeFormatUtils;

/**
 * Created by carolinewong on 9/9/16.
 */
public class TripAdapter extends RecyclerView.Adapter<TripAdapter.ViewHolder> {
    private ArrayList<ParseObject> mTrips;
    private Context mContext;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.tvTripName) TextView tvTripName;
        @BindView(R.id.tvStartDate) TextView tvStartDate;
        @BindView(R.id.tvEndDate) TextView tvEndDate;

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
            i.putExtra("tripName", trip.get("name").toString());
            mContext.startActivity(i);
        }
    }

    public TripAdapter(Context context, ArrayList<ParseObject> trips) {
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
        ParseObject t = mTrips.get(position);

        viewHolder.tvTripName.setText(t.get("name").toString());
        if (t.get("startDate") != null) {
            String startDate = TimeFormatUtils.dateToString((Date) t.get("startDate"));
            viewHolder.tvStartDate.setText(startDate);
        }
        if (t.get("endDate") != null) {
            String endDate = TimeFormatUtils.dateToString((Date) t.get("endDate"));
            viewHolder.tvEndDate.setText(endDate);
        }
    }

    @Override
    public int getItemCount() {
        return mTrips.size();
    }
}