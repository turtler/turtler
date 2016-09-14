package turtler.voyageur.fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import turtler.voyageur.R;
import turtler.voyageur.models.Event;
import turtler.voyageur.models.Image;
import turtler.voyageur.utils.TimeFormatUtils;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * Use the {@link DetailEvent#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetailEvent extends DialogFragment {
    @BindView(R.id.ivFirstImage) ImageView ivfirstImage;
    @BindView(R.id.tvTitle) TextView tvTitle;
    @BindView(R.id.tvCaption) TextView tvCaption;
    @BindView(R.id.tvDate) TextView tvDate;
    Unbinder unbinder;

    private static final String ARG_EVENT_ID = "eventId";

    // TODO: Rename and change types of parameters
    private String eventId;


    public DetailEvent() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param eventId Parameter 1.
     * @return A new instance of fragment DetailEvent.
     */
    // TODO: Rename and change types and number of parameters
    public static DetailEvent newInstance(String eventId) {
        DetailEvent fragment = new DetailEvent();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventId = getArguments().getString(ARG_EVENT_ID);
            ParseQuery<Event> query = ParseQuery.getQuery("Event");
            query.getInBackground(eventId, new GetCallback<Event>() {
                @Override
                public void done(Event eventObj, ParseException e) {
                    if (e == null) {
                        setEventDetailLayout(eventObj);
                    }
                }
            });
        }
    }

    public void setEventDetailLayout(Event ev) {
        ArrayList<Image> images = new ArrayList<>();
        try {
            images = (ArrayList<Image>) ev.imagesRelation().getQuery().find();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (images.size() > 0) {
            Picasso.with(getContext()).load(images.get(0).getPictureUrl()).into(ivfirstImage);
        }
        tvCaption.setText(ev.getCaption());
        tvTitle.setText(ev.getTitle());
        if (ev.getDate() != null) {
            tvDate.setText(TimeFormatUtils.dateTimeToString(ev.getDate()));
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail_event, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }
}
