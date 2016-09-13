package turtler.voyageur.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.util.TimeUtils;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import turtler.voyageur.R;
import turtler.voyageur.VoyageurApplication;
import turtler.voyageur.models.Event;
import turtler.voyageur.models.Image;
import turtler.voyageur.models.Marker;
import turtler.voyageur.models.Trip;
import turtler.voyageur.models.User;
import turtler.voyageur.utils.AmazonUtils;
import turtler.voyageur.utils.BitmapScaler;
import turtler.voyageur.utils.ImageUtils;
import turtler.voyageur.utils.TimeFormatUtils;

/**
 * Created by carolinewong on 9/3/16.
 */
public class CreateEventFragment extends DialogFragment {
    @BindView(R.id.ivPreview) ImageView ivPreview;
    @BindView(R.id.tvLocationLabel) TextView tvLocationLabel;
    @BindView(R.id.tvLocation) TextView tvLocation;
    @BindView(R.id.tvTitleLabel) TextView tvTitleLabel;
    @BindView(R.id.etTitle) EditText etTitle;
    @BindView(R.id.tvCaptionLabel) TextView tvCaptionLabel;
    @BindView(R.id.etCaption) EditText etCaption;
    @BindView(R.id.etDateTime) EditText etDateTime;
    @BindView(R.id.btnSaveEvent) Button btnSaveEvent;
    GoogleApiClient mGoogleApiClient;

    Trip currentTrip;
    String tripId;
    Location mLastLocation;
    Bitmap selectedImageBitmap;
    Image image;
    private Unbinder unbinder;
    Calendar calendar;
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    public final static int PICK_PHOTO_CODE = 1046;
    public String photoFileName = "photo";
    private TransferUtility transferUtility;

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
        if(VoyageurApplication.getGoogleApiHelper().isConnected()) {
            mGoogleApiClient = VoyageurApplication.getGoogleApiHelper().getGoogleApiClient();
        }
        transferUtility = AmazonUtils.getTransferUtility(getContext());
        calendar = Calendar.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_event, container, false);
        getDialog().setCanceledOnTouchOutside(true);
        unbinder = ButterKnife.bind(this, view);
        etDateTime.setText(TimeFormatUtils.dateTimeToString(calendar.getTime())); //automatically shows current time
        setupListeners();
        return view;
    }

    public void setupListeners() {
        etDateTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog(view);
            }
        });

        btnSaveEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Event newEvent = new Event();
                String caption = etCaption.getText().toString();
                newEvent.setCaption(caption);
                newEvent.setDate(calendar.getTime()); //if not set by user, defaults to now

                final User user = (User) User.getCurrentUser();
                newEvent.setCreator(user);
                newEvent.setTrip(tripId);
                newEvent.setTitle(etTitle.getText().toString());
                newEvent.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        final Marker parseMarker = new Marker();
                        parseMarker.setLatitude(mLastLocation.getLatitude());
                        parseMarker.setLongitude(mLastLocation.getLongitude());
                        parseMarker.setUser(user);
                        parseMarker.setEvent(newEvent);
                        parseMarker.setTrip(tripId);
                        parseMarker.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                currentTrip.addEvent(newEvent);
                                newEvent.addMarker(parseMarker);
                                newEvent.addImage(user.createWithoutData(Image.class, image.getObjectId()));
                                CreateEventFragmentListener listener = (CreateEventFragmentListener) getTargetFragment();
                                listener.onFinishCreateEventDialog(newEvent);
                                dismiss();
                            }
                        });
                    }
                });
            }
        });
        ivPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCameraOptions();
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

    public void showCameraOptions() {
        PopupMenu popup = new PopupMenu(this.getActivity(), getView());
        popup.getMenuInflater().inflate(R.menu.camera_photo_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_camera:
                        showCamera();
                        return true;
                    case R.id.menu_photos:
                        showPhotoLibrary();
                        return true;
                    default:
                        return false;
                }
            }
        });
        popup.show();
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        final Context self = getContext();

        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == getActivity().RESULT_OK) {
                Uri takenPhotoUri = ImageUtils.getPhotoFileUri(getContext(), photoFileName);
                Bitmap takenImage = BitmapFactory.decodeFile(takenPhotoUri.getPath());
                //resize bitmap or else may hit OutOfMemoryError
                selectedImageBitmap = BitmapScaler.scaleToFitWidth(takenImage, 200);
                // save file
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
                // new file for the resized bitmap
                Uri resizedUri = ImageUtils.getPhotoFileUri(getContext(), photoFileName + UUID.randomUUID());
                File resizedFile = new File(resizedUri.getPath());
                try {
                    resizedFile.createNewFile();
                    FileOutputStream fos = new FileOutputStream(resizedFile);
                    fos.write(bytes.toByteArray());
                    fos.close();

                    mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    if (mLastLocation != null) {
                        image = ImageUtils.saveImageToParse(getContext(), transferUtility, mLastLocation, resizedFile);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                setFragmentUIWithEventProps();
            } else {
                Toast.makeText(self, "No picture taken!", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PICK_PHOTO_CODE) {
            if (data != null) {
                Uri photoUri = data.getData();
                // Do something with the photo based on Uri
                selectedImageBitmap = null;
                try {
                    selectedImageBitmap = MediaStore.Images.Media.getBitmap(self.getContentResolver(), photoUri);
                    ivPreview.setImageBitmap(selectedImageBitmap);
                    File resizedFile = new File(photoUri.getPath());
                    resizedFile.createNewFile();

                    mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    if (mLastLocation != null) {
                        image = ImageUtils.saveImageToParse(getContext(), transferUtility, mLastLocation, resizedFile);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                setFragmentUIWithEventProps();
            } else {
                Toast.makeText(self, "No picture chosen!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void setFragmentUIWithEventProps() {
        ivPreview.setImageBitmap(selectedImageBitmap);
        tvLocation.setText(Double.toString(mLastLocation.getLatitude()) + ", " + Double.toString(mLastLocation.getLongitude()));
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
