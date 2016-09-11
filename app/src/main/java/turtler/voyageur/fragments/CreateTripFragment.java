package turtler.voyageur.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import turtler.voyageur.R;
import turtler.voyageur.utils.TimeFormatUtils;

/**
 * Created by cwong on 9/9/16.
 */
public class CreateTripFragment extends DialogFragment {
    @BindView(R.id.tvTripName) TextView tvTripName;
    @BindView(R.id.etTripName) EditText etTripName;
    @BindView(R.id.tvStartDate) TextView tvStartDate;
    @BindView(R.id.etStartDate) EditText etStartDate;
    @BindView(R.id.tvEndDate) TextView tvEndDate;
    @BindView(R.id.etEndDate) EditText etEndDate;
    @BindView(R.id.btnSaveTrip) Button btnSaveTrip;
    Date startDate;
    Date endDate;

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
        setupListeners();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void setupListeners() {
        etStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialogStart(view);
            }
        });

        etEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialogEnd(view);
            }
        });

        btnSaveTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ParseObject parseTrip = ParseObject.create("Trip");
                String tripName = etTripName.getText().toString();
                parseTrip.put("name", tripName);
                if (startDate != null) {
                    parseTrip.put("startDate", startDate);
                }
                if (endDate != null) {
                    parseTrip.put("endDate", endDate);
                }
                ParseUser user = ParseUser.getCurrentUser();
                ParseRelation creatorRelation = parseTrip.getRelation("tripCreator");
                creatorRelation.add(user);
                parseTrip.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        CreateTripFragmentListener listener = (CreateTripFragmentListener) getActivity();
                        listener.onFinishCreateTripDialog(parseTrip);
                        dismiss();
                    }
                });
                /* TODO: for each user on trip, save this event to their events list */
                ParseRelation relation = user.getRelation("trips");
                relation.add(parseTrip);
                user.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                    }
                });
            }
        });
    }

    public void showDatePickerDialogStart(View v) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                String startDateString = formattedPickedStringDate(year, monthOfYear, dayOfMonth);
                etStartDate.setText(startDateString);
                startDate = TimeFormatUtils.strToDate(startDateString);
            }
        };
        DatePickerDialog dgStart = new DatePickerDialog(getActivity(), listener, year, month, day);
        dgStart.show();
    }

    public void showDatePickerDialogEnd(View v) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                String endDateString = formattedPickedStringDate(year, monthOfYear, dayOfMonth);
                endDate = TimeFormatUtils.strToDate(endDateString);
                etEndDate.setText(endDateString);
            }
        };
        DatePickerDialog dgEnd = new DatePickerDialog(getActivity(), listener, year, month, day);
        dgEnd.show();
    }

    public String formattedPickedStringDate(int year, int month, int day) {
        final Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);
        return TimeFormatUtils.dateToString(c.getTime());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
