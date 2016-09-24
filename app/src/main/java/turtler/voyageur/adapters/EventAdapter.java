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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.parse.FindCallback;
import com.parse.ParseException;

import org.joda.time.Days;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.CropCircleTransformation;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import turtler.voyageur.R;
import turtler.voyageur.fragments.DetailEvent;
import turtler.voyageur.fragments.TripMapFragment;
import turtler.voyageur.fragments.ViewPagerContainerFragment;
import turtler.voyageur.models.Event;
import turtler.voyageur.models.Marker;
import turtler.voyageur.models.Trip;
import turtler.voyageur.models.User;
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
        @BindView(R.id.ivImage1) ImageView ivImage1;
        @BindView(R.id.ivImage2) ImageView ivImage2;
        @BindView(R.id.ivImage3) ImageView ivImage3;
        @BindView(R.id.ivImage4) ImageView ivImage4;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getLayoutPosition();
            Event event = mEvents.get(position);

            FragmentActivity activity = (FragmentActivity)(mContext);
            FragmentManager fm = activity.getSupportFragmentManager();
            DetailEvent alertDialog = DetailEvent.newInstance(event.getObjectId());
            alertDialog.show(fm, "fragment_detail_event");

            /*
            ViewPagerContainerFragment vpFrag = (ViewPagerContainerFragment) fm.findFragmentByTag("view_pager");
            try {
                List<Marker> markers = event.getMarkerParseRelation().getQuery().find();
                if (markers != null) {
                    Marker m = markers.get(0);
                    vpFrag.viewPager.setCurrentItem(1);
                    TripMapFragment mapFrag = (TripMapFragment) vpFrag.viewPager.getAdapter().instantiateItem(vpFrag.viewPager, 1);
                    GoogleMap map = mapFrag.getMap();
                    CameraUpdate c = CameraUpdateFactory.newLatLng(new LatLng(m.getLatitudeKey(), m.getLongitudeKey()));
                    CameraUpdate zoom = CameraUpdateFactory.zoomTo(14);
                    map.moveCamera(c);
                    map.animateCamera(zoom);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }*/
        }
    }

    public EventAdapter(Context context, ArrayList<Event> events) {
        mContext = context;
        mEvents = events;
        if (startDate == null) {
            if (mEvents != null && mEvents.size() > 0) {
                Trip t = null;
                try {
                    t = mEvents.get(0).getTrip().fetchIfNeeded();
                    t.getTripFriendsRelation().getQuery().findInBackground(new FindCallback<User>() {
                        @Override
                        public void done(List<User> objects, ParseException e) {
                            if (objects != null && objects.size() > 0) {
                                for (int i = 0; i < objects.size(); i++) {
                                    String picUrl = objects.get(i).getPictureUrl();
                                    Glide.with(mContext).load(picUrl).diskCacheStrategy(DiskCacheStrategy.SOURCE).preload();
                                }
                            }
                        }
                    });
                    startDate = LocalDate.fromDateFields(t.getStartDate());
                } catch (ParseException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
        int prevDay = 0;
        for (Event event: mEvents) {
            if (event.getDate() != null) {
                int days = Days.daysBetween(startDate, LocalDate.fromDateFields(event.getDate())).getDays() + 1;
                if (days != prevDay) {
                    event.setEventDay(days);
                    prevDay = days;
                }
            }
        }
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
        Event ev = mEvents.get(position);
        if (ev.getImageURL() != null) {
            Glide.with(mContext).load(ev.getImageURL()).bitmapTransform(new RoundedCornersTransformation(mContext, 3, 3)).into(viewHolder.ivfirstImage);
        }

        if (ev.getEventDay() != null) {
            viewHolder.tvDayLabel.setText("Day " + ev.getEventDay());
            viewHolder.tvDayLabel.setVisibility(View.VISIBLE);
            viewHolder.tvDayLabel.setPadding(0, 0, 0, 10);
        } else {
            viewHolder.tvDayLabel.setText("");
        }
        if (ev.getDate() != null) {
            viewHolder.tvDate.setText(TimeFormatUtils.dateTimeToString(ev.getDate()));
        }
        if (ev.getCaption() != null) {
            viewHolder.tvCaption.setText(ev.getCaption());
        }
        if (ev.getTitle() != null) {
            viewHolder.tvTitle.setText(ev.getTitle());
        }
        final ArrayList<ImageView> imageViews = new ArrayList<>(Arrays.asList(viewHolder.ivImage1, viewHolder.ivImage2, viewHolder.ivImage3, viewHolder.ivImage4));
        ev.getFriendsRelation().getQuery().selectKeys(new ArrayList<>(Arrays.asList("pictureUrl"))).findInBackground(new FindCallback<User>() {
            @Override
            public void done(List<User> objects, ParseException e) {
                if (objects.size() == 0) {
                    return;
                }
                for (int i = 0; i < objects.size(); i++) {
                    if (i >= 4) {
                        break;
                    }
                    ImageView currentImageView = imageViews.get(i);
                    currentImageView.setMinimumHeight(80);
                    currentImageView.setMinimumWidth(80);
                    Glide.with(mContext).load(objects.get(i).getPictureUrl()).bitmapTransform(new CropCircleTransformation(mContext)).into(imageViews.get(i));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mEvents.size();
    }
}