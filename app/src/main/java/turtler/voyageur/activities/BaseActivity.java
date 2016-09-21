package turtler.voyageur.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.GridHolder;
import com.orhanobut.dialogplus.OnItemClickListener;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.PermissionUtils;
import turtler.voyageur.R;
import turtler.voyageur.VoyageurApplication;
import turtler.voyageur.fragments.CreateEventFragment;
import turtler.voyageur.fragments.HomeFragment;
import turtler.voyageur.fragments.ProfileFragment;
import turtler.voyageur.models.Event;
import turtler.voyageur.models.Image;
import turtler.voyageur.models.Trip;
import turtler.voyageur.models.User;
import turtler.voyageur.utils.AmazonUtils;
import turtler.voyageur.utils.BitmapScaler;
import turtler.voyageur.utils.ImageUtils;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class BaseActivity extends AppCompatActivity implements CreateEventFragment.CreateEventFragmentListener{
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.bottom_toolbar) ActionMenuView mBottomBar;

    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    public final static int PICK_PHOTO_CODE = 1046;
    public String photoFileName = "photo";
    private final int LOGIN_REQUEST_CODE = 20;
    private String userEmail;
    private TransferUtility transferUtility;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private long UPDATE_INTERVAL = 60000;  /* 60 secs */
    private long FASTEST_INTERVAL = 5000; /* 5 secs */
    private static final String[] PERMISSION_GETMYLOCATION = new String[] {"android.permission.ACCESS_FINE_LOCATION","android.permission.ACCESS_COARSE_LOCATION"};
    private static final int REQUEST_GETMYLOCATION = 0;
    protected LocationManager locationManager;
    private Trip currentTrip;
    private boolean toCreateEventFragment;
    private String chosenImageId;
    private Double currentLat;
    private Double currentLong;
    private Bitmap imageBitmap;


    @Override
    @SuppressWarnings("all")
    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        toCreateEventFragment = false;
        mGoogleApiClient = VoyageurApplication.getGoogleApiHelper().getGoogleApiClient();
        transferUtility = AmazonUtils.getTransferUtility(this);
        if (PermissionUtils.hasSelfPermissions(BaseActivity.this, PERMISSION_GETMYLOCATION)) {
            getMyLocation();
        }
        else {
            ActivityCompat.requestPermissions(this, PERMISSION_GETMYLOCATION, REQUEST_GETMYLOCATION);
        }

        startLocationUpdates();

        if (ContextCompat.checkSelfPermission(BaseActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == 1) {
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
                        @Override
                        public void onStatusChanged(String provider, int status, Bundle extras) {
                        }
                        @Override
                        public void onProviderEnabled(String provider) {
                        }
                        @Override
                        public void onProviderDisabled(String provider) {
                        }
                        @Override
                        public void onLocationChanged(final Location location) {
                        }
                    });
        }

        final User currentUser = (User) User.getCurrentUser();
        if (currentUser != null) {
            // do stuff with the user
            userEmail = currentUser.getEmail();
        } else {
            Intent i = new Intent(BaseActivity.this, LoginActivity.class);
            startActivityForResult(i, LOGIN_REQUEST_CODE);
        }
        //set default fragment
        FragmentTransaction ftHome = getSupportFragmentManager().beginTransaction();
        ftHome.replace(R.id.frame_layout, HomeFragment.newInstance());
        ftHome.commit();

        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        Date today = cal.getTime();

        ParseQuery<Trip> tripParseQuery = ParseQuery.getQuery("Trip");
        tripParseQuery.selectKeys(new ArrayList<>(Arrays.asList("objectId")));
        tripParseQuery.whereLessThan("startDate", today);
        tripParseQuery.whereGreaterThanOrEqualTo("endDate", today);
        tripParseQuery.getFirstInBackground(new GetCallback<Trip>() {
            @Override
            public void done(Trip object, ParseException e) {
                currentTrip = object;
            }
        });
    }

    @SuppressWarnings("all")
    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    void getMyLocation() {
        locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // getMenuInflater().inflate(R.menu.top_nav_bar, menu); //TODO: Make top nav bar layout
        Menu bottomMenu = mBottomBar.getMenu();
        getMenuInflater().inflate(R.menu.bottom_nav_bar, bottomMenu);
        for (int i = 0; i < bottomMenu.size(); i++) {
            bottomMenu.getItem(i).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    return onOptionsItemSelected(item);
                }
            });
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_menu_home:
                FragmentTransaction ftHome = getSupportFragmentManager().beginTransaction();
                ftHome.replace(R.id.frame_layout, HomeFragment.newInstance());
                ftHome.commit();
                return true;
            case R.id.item_menu_camera:
                showCameraOptions();
                return true;
            case R.id.item_menu_profile:
                FragmentTransaction ftProf = getSupportFragmentManager().beginTransaction();
                ftProf.replace(R.id.frame_layout, ProfileFragment.newInstance());
                ftProf.commit();
                return true;
            case R.id.item_logout:
                Intent logoutActivity = new Intent(getApplicationContext(), LoginActivity.class);
                startActivityForResult(logoutActivity, LOGIN_REQUEST_CODE);
                return true;
            default:
                return false;
        }
    }

    public void showCameraOptions() {
        DialogPlus dialog = DialogPlus.newDialog(this)
                .setContentHolder(new GridHolder(2))
                .setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new String[]{getString(R.string.take_photo), getString(R.string.choose_library)}))
                .setExpanded(true, 200)
                .setGravity(Gravity.CENTER)
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

    protected void startLocationUpdates() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
    }

    public void showCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, ImageUtils.getPhotoFileUri(this, photoFileName)); // set the image file name
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }
    public void showPhotoLibrary() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, PICK_PHOTO_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Uri takenPhotoUri = ImageUtils.getPhotoFileUri(this, photoFileName);
                Bitmap takenImage = BitmapFactory.decodeFile(takenPhotoUri.getPath());
                resizeAndUploadPhoto(takenImage);
            } else {
                Toast.makeText(this, "No picture taken!", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PICK_PHOTO_CODE) {
            if (data != null) {
                Uri photoUri = data.getData();
                // Do something with the photo based on Uri
                Bitmap selectedImageBitmap = null;
                try {
                    selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
                    resizeAndUploadPhoto(selectedImageBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "No picture chosen!", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == LOGIN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                userEmail = data.getExtras().getString("user_email");
            }
        }
    }

    public void resizeAndUploadPhoto(Bitmap imgBitmap) {
        Bitmap resizedBitmap = BitmapScaler.scaleToFitWidth(imgBitmap, 200);
        // save file
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
        // new file for the resized bitmap
        Uri resizedUri = ImageUtils.getPhotoFileUri(this, photoFileName + UUID.randomUUID());
        File resizedFile = new File(resizedUri.getPath());

        try {
            resizedFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(resizedFile);
            fos.write(bytes.toByteArray());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            Image image = ImageUtils.saveImageToParse(this, transferUtility, mLastLocation, resizedFile);
            currentLat = mLastLocation.getLatitude();
            currentLong = mLastLocation.getLongitude();
            toCreateEventFragment = true;
            chosenImageId = image.getObjectId();
            imageBitmap = imgBitmap;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (toCreateEventFragment) {
            this.showCreateEventFragment(currentLat, currentLong, chosenImageId);
            toCreateEventFragment = false;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    public void showCreateEventFragment(Double lat, Double lon, String imageId) {
        FragmentManager fm = this.getSupportFragmentManager();
        CreateEventFragment eventDialogFragment = CreateEventFragment.newInstance(currentTrip.getObjectId(), lat, lon, imageId, imageBitmap, true);
        eventDialogFragment.show(fm, "fragment_create_event");
    }

    @Override
    public void onFinishCreateEventDialog(Event event) {
        Snackbar.make(this.getCurrentFocus(), "Created Event", Snackbar.LENGTH_LONG)
                .show();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
