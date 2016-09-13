package turtler.voyageur.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import turtler.voyageur.R;
import turtler.voyageur.VoyageurApplication;
import turtler.voyageur.activities.BaseActivity;
import turtler.voyageur.models.Event;
import turtler.voyageur.models.Image;
import turtler.voyageur.models.Trip;
import turtler.voyageur.models.User;
import turtler.voyageur.utils.BitmapScaler;
import turtler.voyageur.utils.Constants;

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
    GoogleApiClient mGoogleApiClient;

    Trip currentTrip;
    String tripId;
    private Unbinder unbinder;
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    public final static int PICK_PHOTO_CODE = 1046;
    public String photoFileName = "photo";
    public final String APP_TAG = "VoyageurApp";
    public final String AMAZON_S3_FILE_URL = "https://voyaging.s3.amazonaws.com/";
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
        VoyageurApplication.getGoogleApiHelper();

        if(VoyageurApplication.getGoogleApiHelper().isConnected())
        {
            mGoogleApiClient = VoyageurApplication.getGoogleApiHelper().getGoogleApiClient();
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
        ivPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCameraOptions();
            }
        });
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
        intent.putExtra(MediaStore.EXTRA_OUTPUT, getPhotoFileUri(photoFileName)); // set the image file name
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

    // Returns uri for photo stored on disk with fileName
    public Uri getPhotoFileUri(String fileName) {
        if (isExternalStorageAvailable()) {
            File mediaStorageDir = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES), APP_TAG);
            if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
                Log.d(APP_TAG, "failed to create directory");
            }
            return Uri.fromFile(new File(mediaStorageDir.getPath() + File.separator + fileName));
        }
        return null;
    }

    private boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        return state.equals(Environment.MEDIA_MOUNTED);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        final Context self = getContext();

        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == getActivity().RESULT_OK) {
                Uri takenPhotoUri = getPhotoFileUri(photoFileName);
                Bitmap takenImage = BitmapFactory.decodeFile(takenPhotoUri.getPath());
                //resize bitmap or else may hit OutOfMemoryError
                Bitmap resizedBitmap = BitmapScaler.scaleToFitWidth(takenImage, 200);
                // ImageView ivPreview = (ImageView) findViewById(R.id.ivPreview);
                ivPreview.setImageBitmap(resizedBitmap);
                // save file
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
                // new file for the resized bitmap
                Uri resizedUri = getPhotoFileUri(photoFileName + UUID.randomUUID());
                File resizedFile = new File(resizedUri.getPath());
                try {
                    resizedFile.createNewFile();
                    FileOutputStream fos = new FileOutputStream(resizedFile);
                    fos.write(bytes.toByteArray());
                    fos.close();

                    Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    if (mLastLocation != null) {
                        saveImageToParse(mLastLocation, resizedFile);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(self, "No picture taken!", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PICK_PHOTO_CODE) {
            if (data != null) {
                Uri photoUri = data.getData();
                // Do something with the photo based on Uri
                Bitmap selectedImage = null;
                try {
                    selectedImage = MediaStore.Images.Media.getBitmap(self.getContentResolver(), photoUri);
                    // ImageView ivPreview = (ImageView) findViewById(R.id.ivPreview);
                    ivPreview.setImageBitmap(selectedImage);
                    File resizedFile = new File(photoUri.getPath());
                    resizedFile.createNewFile();

                    Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    if (mLastLocation != null) {
                        saveImageToParse(mLastLocation, resizedFile);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                // Load the selected image into a preview
                ivPreview.setImageBitmap(selectedImage);
            } else {
                Toast.makeText(self, "No picture chosen!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void saveImageToParse(Location mLastLocation, File resizedFile) {
        String lat = Double.toString(mLastLocation.getLatitude());
        String lon = Double.toString(mLastLocation.getLongitude());
        Image parseImage = new Image();
        parseImage.setLatitude(mLastLocation.getLatitude());
        parseImage.setLongitude(mLastLocation.getLongitude());
        parseImage.setPictureUrl(AMAZON_S3_FILE_URL + resizedFile.getName());
        parseImage.setUser((User) ParseUser.getCurrentUser());

        final Context self = getContext();

        parseImage.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Toast.makeText(getActivity(), "Successfully saved image on Parse",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("ERROR", "Failed to save marker", e);
                }

            }
        });

        Toast.makeText(getContext(), lat + "," + lon, Toast.LENGTH_LONG).show();

        TransferObserver observer = transferUtility.upload(Constants.BUCKET_NAME, resizedFile.getName(),
                resizedFile);

        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int i, TransferState transferState) {
                if (transferState.toString().equals("COMPLETED")) {
                    Toast.makeText(self, "Image uploaded successfully to Amazon S3!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onProgressChanged(int i, long l, long l1) {

            }

            @Override
            public void onError(int i, Exception e) {
                Toast.makeText(self, e.getMessage(), Toast.LENGTH_LONG).show();
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
