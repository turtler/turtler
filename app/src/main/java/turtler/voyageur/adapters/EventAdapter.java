package turtler.voyageur.adapters;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.squareup.picasso.Picasso;

import org.joda.time.Days;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import turtler.voyageur.R;
import turtler.voyageur.fragments.DetailEvent;
import turtler.voyageur.models.Event;
import turtler.voyageur.models.Image;
import turtler.voyageur.models.Trip;
import turtler.voyageur.utils.TimeFormatUtils;

/**
 * Created by cwong on 9/11/16.
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {
    private ArrayList<Event> mEvents;
    private Context mContext;
    private int currDay = 0;
    LocalDate startDate;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.ivFirstImage) ImageView ivfirstImage;
        @BindView(R.id.tvTitle) TextView tvTitle;
        @BindView(R.id.tvCaption) TextView tvCaption;
        @BindView(R.id.tvDate) TextView tvDate;
        @BindView(R.id.tvDayLabel) TextView tvDayLabel;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getLayoutPosition();
            ParseObject event = mEvents.get(position);
            FragmentActivity activity = (FragmentActivity)(mContext);
            FragmentManager fm = activity.getSupportFragmentManager();
            DetailEvent alertDialog = DetailEvent.newInstance(event.getObjectId());
            alertDialog.show(fm, "fragment_detail_event");
        }
    }

    public EventAdapter(Context context, ArrayList<Event> events) {
        mContext = context;
        mEvents = events;
    }

    @Override
    public EventAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View itemEventView = inflater.inflate(R.layout.item_event, parent, false);
        ViewHolder viewHolder = new ViewHolder(itemEventView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(EventAdapter.ViewHolder viewHolder, int position) {
        if (startDate == null) {
            if (mEvents != null && mEvents.size() > 0) {
                Trip t = null;
                try {
                    t = mEvents.get(0).getTrip().fetchIfNeeded();
                    startDate = LocalDate.fromDateFields(t.getStartDate());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                startDate = LocalDate.fromDateFields(t.getStartDate());
            }
        }
        Event ev = mEvents.get(position);
        ArrayList<Image> images = new ArrayList<>();
        try {
            images = (ArrayList<Image>) ev.imagesRelation().getQuery().find();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (images.size() > 0) {
            Picasso.with(mContext).load(images.get(0).getPictureUrl()).into(viewHolder.ivfirstImage);
        }
        viewHolder.tvCaption.setText(ev.getCaption());
        viewHolder.tvTitle.setText(ev.getTitle());
        if (ev.getDate() != null) {
            int days = Days.daysBetween(startDate, LocalDate.fromDateFields(ev.getDate())).getDays() + 1;
            if (days != currDay) {
                viewHolder.tvDayLabel.setText("Day " + days);
                currDay = days;
            }
            viewHolder.tvDate.setText(TimeFormatUtils.dateTimeToString(ev.getDate()));
        }
    }

    @Override
    public int getItemCount() {
        return mEvents.size();
    }
}