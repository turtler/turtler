package turtler.voyageur.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import turtler.voyageur.R;

/**
 * Created by cwong on 9/9/16.
 */
public class CreateTripFragment extends DialogFragment {
    @BindView(R.id.tvTripName) TextView tvTripName;
    @BindView(R.id.etTripName) EditText etTripName;
    @BindView(R.id.btnSaveTrip) Button btnSaveTrip;
    private Unbinder unbinder;

    public CreateTripFragment(){}

    public interface CreateTripFragmentListener {
        void onFinishCreateTripDialog(ParseObject newTrip);
    }

    public static CreateTripFragment newInstance() {
        CreateTripFragment tripFragment = new CreateTripFragment();
        return tripFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_trip, container);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @OnClick(R.id.btnSaveTrip)
    public void onSaveTrip() {
        final ParseObject parseTrip = ParseObject.create("Trip");
        String tripName = etTripName.getText().toString();
        parseTrip.put("name", tripName);
        parseTrip.put("userId", ParseUser.getCurrentUser().getObjectId());
        parseTrip.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                CreateTripFragmentListener listener = (CreateTripFragmentListener) getActivity();
                listener.onFinishCreateTripDialog(parseTrip);
                dismiss();
            }
        });
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
