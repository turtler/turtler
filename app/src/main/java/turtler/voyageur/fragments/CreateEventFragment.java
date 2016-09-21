package turtler.voyageur.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnItemClickListener;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import turtler.voyageur.R;
import turtler.voyageur.VoyageurApplication;
import turtler.voyageur.adapters.UserItemArrayAdapter;
import turtler.voyageur.models.Event;
import turtler.voyageur.models.Image;
import turtler.voyageur.models.Marker;
import turtler.voyageur.models.Trip;
import turtler.voyageur.models.User;
import turtler.voyageur.models.UserEntry;
import turtler.voyageur.utils.AmazonUtils;
import turtler.voyageur.utils.BitmapScaler;
import turtler.voyageur.utils.ImageUtils;
import turtler.voyageur.utils.TimeFormatUtils;

/**
 * Created by carolinewong on 9/3/16.
 */
public class CreateEventFragment extends DialogFragment {
    @BindView(R.id.ivUploadImage) ImageView ivUploadImage;
    @BindView(R.id.etTitle) EditText etTitle;
    @BindView(R.id.etCaption) EditText etCaption;
    @BindView(R.id.etDateTime) EditText etDateTime;
    GoogleApiClient mGoogleApiClient;

    Trip currentTrip;
    String tripId;
    String imageId;
    Location mLastLocation;
    Location chosenLocation;
    Bitmap selectedImageBitmap;
    Context mContext;
    Image image;
    private Unbinder unbinder;
    Calendar calendar;
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    public final static int PICK_PHOTO_CODE = 1046;
    public String photoFileName = "photo";
    private TransferUtility transferUtility;
    ArrayList<String> friendsListIds = new ArrayList<String>();
    CreateEventFragmentListener listener;
    List<User> friends;

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

    public static CreateEventFragment newInstance(String tripId, Double lat, Double lon) {
        CreateEventFragment frag = new CreateEventFragment();
        Bundle args = new Bundle();
        args.putString("tripId", tripId);
        args.putDouble("chosenLat", lat);
        args.putDouble("chosenLong", lon);
        frag.setArguments(args);
        return frag;
    }

    public static CreateEventFragment newInstance(String tripId, Double lat, Double lon, String imageId, Bitmap bitmap, boolean fromActivity) {
        CreateEventFragment frag = new CreateEventFragment();
        Bundle args = new Bundle();
        args.putString("tripId", tripId);
        args.putDouble("chosenLat", lat);
        args.putDouble("chosenLong", lon);
        args.putString("imageId", imageId);
        args.putParcelable("bitmap", bitmap);
        args.putBoolean("fromActivity", fromActivity);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tripId = getArguments().getString("tripId", "");
        Double chosenLat = getArguments().getDouble("chosenLat");
        Double chosenLong = getArguments().getDouble("chosenLong");
        imageId = getArguments().getString("imageId", "");
        selectedImageBitmap = getArguments().getParcelable("bitmap");
        if (getArguments().getBoolean("fromActivity", false)) {
            listener = (CreateEventFragmentListener) getActivity();
        } else {
            listener = (CreateEventFragmentListener) getTargetFragment();
        }
        if (chosenLat != null && chosenLong != null) {
            chosenLocation = new Location("");
            chosenLocation.setLatitude(chosenLat);
            chosenLocation.setLongitude(chosenLong);
        }

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
        }
        if(VoyageurApplication.getGoogleApiHelper().isConnected()) {
            mGoogleApiClient = VoyageurApplication.getGoogleApiHelper().getGoogleApiClient();
        }
        transferUtility = AmazonUtils.getTransferUtility(getContext());
        calendar = Calendar.getInstance();
        mContext = getContext();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        final View view = inflater.inflate(R.layout.fragment_create_event, null);
        unbinder = ButterKnife.bind(this, view);
        etDateTime.setText("Now"); //automatically shows current time
        if (imageId != "") {
            ParseQuery<Image> query = ParseQuery.getQuery(Image.class);
            query.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK); // or CACHE_ONLY
            query.getInBackground(imageId, new GetCallback<Image>() {
                public void done(Image item, ParseException e) {
                    if (e == null) {
                        image = item;
                        Glide.with(getContext()).load(image.getPictureUrl()).into(ivUploadImage);
                    }
                }
            });
        }
        if (selectedImageBitmap != null) {
            ivUploadImage.setImageBitmap(selectedImageBitmap);
        }

        final AutoCompleteTextView textView = (AutoCompleteTextView)
                view.findViewById(R.id.etFriendsTripEvent);
        Trip t = ParseObject.createWithoutData(Trip.class, tripId);
        try {
            friends = t.getTripFriendsRelation().getQuery().find();
            List<UserEntry> userEntries = new ArrayList<UserEntry>();
            for (int i = 0; i < friends.size(); i++) {
                UserEntry newUserEntry = new UserEntry();
                newUserEntry.name = friends.get(i).getName();
                newUserEntry.imgUrl = friends.get(i).getPictureUrl();
                newUserEntry.objectID = friends.get(i).getObjectId();
                userEntries.add(newUserEntry);
            }
            UserItemArrayAdapter.UserRowListener listener = new UserItemArrayAdapter.UserRowListener() {
                @Override
                public void onClick(UserEntry u) {
                    friendsListIds.add(u.objectID);
                    TextView tvFriendsList = (TextView) view.findViewById(R.id.tvFriendsListEvent);
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

        return new AlertDialog.Builder(getActivity()).setTitle("Create New Event").setView(view)
                .setPositiveButton("Save",  new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveEvent();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
    }

    public void setupListeners() {
        etDateTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog(view);
            }
        });

        ivUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().hide();
                DialogPlus dialog = DialogPlus.newDialog(getContext())
                        .setAdapter(new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, new String[]{getString(R.string.take_photo), getString(R.string.choose_library)}))
                        .setExpanded(true, 200)
                        .setOnItemClickListener(new OnItemClickListener() {
                            @Override
                            public void onItemClick(DialogPlus dialog, Object item, View view, int position) {
                                switch (position) {
                                    case 0:
                                        showCamera();
                                        dialog.dismiss();
                                        break;
                                    case 1:
                                        showPhotoLibrary();
                                        dialog.dismiss();
                                        break;
                                    default:
                                }
                            }
                        })
                        .setExpanded(true)  // This will enable the expand feature, (similar to android L share dialog)
                        .create();
                dialog.show();
            }
        });
    }

    public void showDatePickerDialog(final View v) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                showTimePickerDialog(v);
            }
        };
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), listener, year, month, day);
        datePickerDialog.getDatePicker().setMaxDate(new Date().getTime());
        datePickerDialog.show();
    }

    public void showTimePickerDialog(View v) {
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                calendar.set(Calendar.HOUR, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                etDateTime.setText(formattedPickedStringDateTime(calendar));
            }
        };
        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), listener, hour, minute, false);
        timePickerDialog.show();
    }

    public String formattedPickedStringDateTime(Calendar c) {
        return TimeFormatUtils.dateTimeToString(c.getTime());
    }

    public void showCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, ImageUtils.getPhotoFileUri(getContext(), photoFileName)); // set the image file name
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }
    public void showPhotoLibrary() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(intent, PICK_PHOTO_CODE);
        }
    }

    @Override
    @SuppressWarnings("all")
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        final Context self = getContext();

        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == getActivity().RESULT_OK) {
                getDialog().show();
                Uri takenPhotoUri = ImageUtils.getPhotoFileUri(getContext(), photoFileName);
                Bitmap takenImage = BitmapFactory.decodeFile(takenPhotoUri.getPath());
                resizeAndUploadPhoto(takenImage);
                setFragmentUIWithEventProps();
            } else {
                getDialog().show();
                Toast.makeText(self, "No picture taken!", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PICK_PHOTO_CODE) {
            if (data != null) {
                getDialog().show();
                Uri photoUri = data.getData();
                // Do something with the photo based on Uri
                try {
                    selectedImageBitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), photoUri);
                    resizeAndUploadPhoto(selectedImageBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                setFragmentUIWithEventProps();
            } else {
                getDialog().show();
                Toast.makeText(self, "No picture chosen!", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void resizeAndUploadPhoto(Bitmap imgBitmap) {
        Bitmap resizedBitmap = BitmapScaler.scaleToFitWidth(imgBitmap, 200);
        // save file
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
        // new file for the resized bitmap
        Uri resizedUri = ImageUtils.getPhotoFileUri(getContext(), photoFileName + UUID.randomUUID());
        File resizedFile = new File(resizedUri.getPath());

        try {
            resizedFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(resizedFile);
            fos.write(bytes.toByteArray());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            image = ImageUtils.saveImageToParse(getContext(), transferUtility, mLastLocation, resizedFile);
            selectedImageBitmap = imgBitmap;
        }
    }

    public void setFragmentUIWithEventProps() {
        ivUploadImage.setPadding(0, 0, 0, 0);
        ivUploadImage.setImageBitmap(selectedImageBitmap);
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
    }

    public void saveEvent() {
        final Event newEvent = new Event();
        String caption = etCaption.getText().toString();
        newEvent.setCaption(caption);
        newEvent.setDate(calendar.getTime()); //if not set by user, defaults to now
        final User user = (User) User.getCurrentUser();
        newEvent.setCreator(user);
        newEvent.setTrip(tripId);
        newEvent.setTitle(etTitle.getText().toString());
        if (image != null) {
            newEvent.setImageURL(image.getPictureUrl());
        }
        newEvent.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                final Marker parseMarker = new Marker();
                if (chosenLocation != null) {
                    parseMarker.setLatitude(chosenLocation.getLatitude());
                    parseMarker.setLongitude(chosenLocation.getLongitude());
                } else {
                    parseMarker.setLatitude(mLastLocation.getLatitude());
                    parseMarker.setLongitude(mLastLocation.getLongitude());
                }
                for (String friendId : friendsListIds) {
                    User newFriend = ParseObject.createWithoutData(User.class, friendId);
                    newEvent.addFriend(newFriend);
                }
                parseMarker.setUser(user);
                parseMarker.setEvent(newEvent);
                parseMarker.setTrip(tripId);
                parseMarker.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        currentTrip.addEvent(newEvent);
                        newEvent.addMarker(parseMarker);
                        if (image != null) {
                            Image i = user.createWithoutData(Image.class, image.getObjectId());
                            newEvent.addImage(i);
                            if (currentTrip.getCoverPhotoURL() == null) {
                                currentTrip.addImage(i);
                            }
                        }
                        listener.onFinishCreateEventDialog(newEvent);
                        dismiss();
                    }
                });
            }
        });
    }

}
