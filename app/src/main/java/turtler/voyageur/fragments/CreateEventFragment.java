package turtler.voyageur.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import turtler.voyageur.R;
import turtler.voyageur.models.Event;
import turtler.voyageur.models.Trip;
import turtler.voyageur.models.User;

/**
 * Created by carolinewong on 9/3/16.
 */
public class CreateEventFragment extends DialogFragment {
    @BindView(R.id.ivPreview) ImageView ivPreview;
    @BindView(R.id.tvLocationLabel) TextView tvLocationLabel;
    @BindView(R.id.tvLocation) TextView tvLocation;
    @BindView(R.id.tvCaptionLabel) TextView tvCaptionLabel;
    @BindView(R.id.etCaption) EditText etCaption;
    @BindView(R.id.btnSaveEvent) Button btnSaveEvent;

    Trip currentTrip;
    String tripId;
    private Unbinder unbinder;

    public CreateEventFragment() {}

    public interface CreateEventFragmentListener {
        void onFinishCreateEventDialog(Event event);
    }

    public static CreateEventFragment newInstance(String tripId) {
        CreateEventFragment frag = new CreateEventFragment();
        Bundle args = new Bundle();
        args.putString("tripId", tripId);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tripId = getArguments().getString("tripId", "");
        if (tripId != "") {
            ParseQuery<Trip> query = ParseQuery.getQuery(Trip.class);
            query.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK); // or CACHE_ONLY
            query.getInBackground(tripId, new GetCallback<Trip>() {
                public void done(Trip item, ParseException e) {
                    if (e == null) {
                        currentTrip = item;
                    }
                }
            });
        } else {
            /**TODO: set trip to be get current trip **/
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_event, container, false);
        getDialog().setCanceledOnTouchOutside(true);
        unbinder = ButterKnife.bind(this, view);
        setupListeners();
        return view;
    }

    public void setupListeners() {
        btnSaveEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Event newEvent = new Event();

                //set attributes for event
                String caption = etCaption.getText().toString();
                newEvent.setCaption(caption);

                final User user = (User) User.getCurrentUser();
                newEvent.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        newEvent.setCreator(user);
                        currentTrip.addEvent(newEvent);
                        CreateEventFragmentListener listener = (CreateEventFragmentListener) getTargetFragment();
                        listener.onFinishCreateEventDialog(newEvent);
                        dismiss();
                    }
                });
            }
        });
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        byte[] byteArray = getArguments().getByteArray("data");
//        Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
//        ivPreview.setImageBitmap(bmp);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
