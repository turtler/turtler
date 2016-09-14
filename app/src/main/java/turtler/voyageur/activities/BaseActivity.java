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
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.PermissionUtils;
import turtler.voyageur.R;
import turtler.voyageur.VoyageurApplication;
import turtler.voyageur.adapters.ImageGridAdapter;
import turtler.voyageur.fragments.ProfileFragment;
import turtler.voyageur.models.Image;
import turtler.voyageur.models.User;
import turtler.voyageur.utils.AmazonUtils;
import turtler.voyageur.utils.BitmapScaler;
import turtler.voyageur.utils.ImageUtils;


public class BaseActivity extends AppCompatActivity {
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.gvImageGrid) GridView gridView;
    @BindView(R.id.bottom_toolbar) ActionMenuView mBottomBar;

    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    public final static int PICK_PHOTO_CODE = 1046;
    public String photoFileName = "photo";
    private final int LOGIN_REQUEST_CODE = 20;
    private String userEmail;
    private TransferUtility transferUtility;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private ImageGridAdapter gridAdapter;
    private long UPDATE_INTERVAL = 60000;  /* 60 secs */
    private long FASTEST_INTERVAL = 5000; /* 5 secs */
    private static final String[] PERMISSION_GETMYLOCATION = new String[] {"android.permission.ACCESS_FINE_LOCATION","android.permission.ACCESS_COARSE_LOCATION"};
    private static final int REQUEST_GETMYLOCATION = 0;
    protected LocationManager locationManager;

    @Override
    @SuppressWarnings("all")
    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        gridAdapter = new ImageGridAdapter(this, R.layout.item_grid, getImages());
        gridView.setAdapter(gridAdapter);

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
    }

    public ArrayList<Image> getImages() {
        final ArrayList<Image> imageItems = new ArrayList<Image>();
        final ParseQuery<Image> parseImageQuery = new ParseQuery("Image");
        try {
            List<Image> parseImages = parseImageQuery.find();
            for (int i = 0; i < parseImages.size(); i++) {
                Image parseImg = parseImages.get(i);
                imageItems.add(parseImg);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return imageItems;
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
        ActionMenuView bottomBar = (ActionMenuView)findViewById(R.id.bottom_toolbar);
        Menu bottomMenu = bottomBar.getMenu();
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
                Intent homeIntent = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(homeIntent);
                return true;
            case R.id.item_menu_camera:
                showCameraOptions();
                return true;
            case R.id.item_menu_profile:
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.frame_layout, ProfileFragment.newInstance());
                ft.commit();
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
        View v = findViewById(R.id.item_menu_camera);
        PopupMenu popup = new PopupMenu(this, v);
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
                //resize bitmap or else may hit OutOfMemoryError
                Bitmap resizedBitmap = BitmapScaler.scaleToFitWidth(takenImage, 200);
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

                    Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    if (mLastLocation != null) {
                        ImageUtils.saveImageToParse(this, transferUtility, mLastLocation, resizedFile);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "No picture taken!", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PICK_PHOTO_CODE) {
            if (data != null) {
                Uri photoUri = data.getData();
                // Do something with the photo based on Uri
                Bitmap selectedImage = null;
                try {
                    selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
                    File resizedFile = new File(photoUri.getPath());
                    resizedFile.createNewFile();
                    Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    if (mLastLocation != null) {
                        ImageUtils.saveImageToParse(this, transferUtility, mLastLocation, resizedFile);
                    }
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
}
