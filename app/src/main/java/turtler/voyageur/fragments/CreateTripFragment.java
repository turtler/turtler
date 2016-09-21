package turtler.voyageur.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import turtler.voyageur.R;
import turtler.voyageur.adapters.UserItemArrayAdapter;
import turtler.voyageur.models.Trip;
import turtler.voyageur.models.User;
import turtler.voyageur.models.UserEntry;
import turtler.voyageur.utils.TimeFormatUtils;

/**
 * Created by cwong on 9/9/16.
 */
public class CreateTripFragment extends DialogFragment {
    @BindView(R.id.etTripName) EditText etTripName;
    @BindView(R.id.etStartDate) EditText etStartDate;
    @BindView(R.id.etEndDate) EditText etEndDate;
    Date startDate;
    Date endDate;
    ArrayList<String> friendsListIds = new ArrayList<String>();

    private Unbinder unbinder;

    public CreateTripFragment(){}

    public interface CreateTripFragmentListener {
        void onFinishCreateTripDialog(Trip newTrip);
    }

    public static CreateTripFragment newInstance() {
        CreateTripFragment tripFragment = new CreateTripFragment();
        return tripFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        final View view = inflater.inflate(R.layout.fragment_create_trip, null);
        unbinder = ButterKnife.bind(this, view);
        User u = (User) ParseUser.getCurrentUser();
        try {
            List<User> friends = u.getFriends().getQuery().find();
            List<UserEntry> userEntries = new ArrayList<UserEntry>();
            for (int i = 0; i < friends.size(); i++) {
                UserEntry newUserEntry = new UserEntry();
                newUserEntry.name = friends.get(i).getName();
                newUserEntry.imgUrl = friends.get(i).getPictureUrl();
                newUserEntry.objectID = friends.get(i).getObjectId();
                userEntries.add(newUserEntry);
            }
            final AutoCompleteTextView textView = (AutoCompleteTextView)
                    view.findViewById(R.id.etFriendsTrip);

            UserItemArrayAdapter.UserRowListener listener = new UserItemArrayAdapter.UserRowListener() {
                @Override
                public void onClick(UserEntry u) {
                    friendsListIds.add(u.objectID);
                    TextView tvFriendsList = (TextView) view.findViewById(R.id.tvFriendsList);
                    if (tvFriendsList != null) {
                        String currText = "";
                        if (tvFriendsList.getText() != "") {
                            currText = tvFriendsList.getText() + ", ";
                        }

                        tvFriendsList.setText(currText + u.name);
                    }
                    textView.setText("");
                    textView.dismissDropDown();
                }
            };

            UserItemArrayAdapter adapter = new UserItemArrayAdapter(getContext(),
                    R.layout.item_dropdown_user, userEntries, listener);

            textView.setAdapter(adapter);

        } catch (ParseException e) {
            e.printStackTrace();
        }


        setupListeners();
        return new AlertDialog.Builder(getActivity()).setTitle("Create New Trip").setView(view)
                .setPositiveButton("Save",  new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveTrip();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
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
    }

    public void saveTrip() {
        String tripName = etTripName.getText().toString();
        final Trip newTrip = new Trip(tripName);
        if (startDate != null) {
            newTrip.setStartDate(startDate);
        }
        if (endDate != null) {
            newTrip.setEndDate(endDate);
        }
        if (friendsListIds.size() > 0) {
            for (int i = 0; i < friendsListIds.size(); i++) {
                String friendId = friendsListIds.get(i);
                newTrip.addTripFriend(ParseUser.createWithoutData(User.class, friendId));
            }
        }
        final User user = (User) User.getCurrentUser();
        newTrip.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                newTrip.addTripCreator(user);
                user.addTrip(newTrip);
                CreateTripFragmentListener listener = (CreateTripFragmentListener) getTargetFragment();
                listener.onFinishCreateTripDialog(newTrip);
                dismiss();
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
