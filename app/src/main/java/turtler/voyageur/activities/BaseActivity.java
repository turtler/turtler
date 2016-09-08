package turtler.voyageur.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.interceptors.ParseLogInterceptor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import turtler.voyageur.R;
import turtler.voyageur.models.Marker;
import turtler.voyageur.models.User;
import turtler.voyageur.utils.BitmapScaler;

public class BaseActivity extends AppCompatActivity {
    @BindView(R.id.toolbar) Toolbar toolbar;
    public final String APP_TAG = "VoyageurApp";
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    public final static int PICK_PHOTO_CODE = 1046;
    public String photoFileName = "photo.jpg";
    private final int LOGIN_REQUEST_CODE = 20;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        ParseObject.registerSubclass(Marker.class);
        ParseObject.registerSubclass(User.class);

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("voyaging") // should correspond to APP_ID env variable
                .clientKey("sayheyhey")  // set explicitly unless clientKey is explicitly configured on Parse server
                .addNetworkInterceptor(new ParseLogInterceptor())
                .server("https://voyaging.herokuapp.com/parse/").build());

        FacebookSdk.sdkInitialize(this);
        ParseFacebookUtils.initialize(this);

        final ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            // do stuff with the user
            userEmail = currentUser.getEmail();
        } else {
            Intent i = new Intent(BaseActivity.this, LoginActivity.class);
            startActivityForResult(i, LOGIN_REQUEST_CODE);
        }

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch(item.getItemId()){
                    case R.id.item_menu_home:
                        Intent homeIntent = new Intent(getApplicationContext(), HomeActivity.class);
                        startActivity(homeIntent);
                        return true;
                    case R.id.item_menu_camera:
                        showCameraOptions();
                        return true;
                    case R.id.item_menu_profile:
                        Intent profileIntent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivityForResult(profileIntent, LOGIN_REQUEST_CODE);
                        return true;
                    case R.id.item_menu_map:
                        Intent mapIntent = new Intent(getApplicationContext(), MapActivity.class);
                        if (ParseUser.getCurrentUser() != null) {
                            mapIntent.putExtra("user_email", ParseUser.getCurrentUser().getEmail());
                            startActivity(mapIntent);
                        }

                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.bottom_nav_bar, menu);
        return true;
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

    public void showCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, getPhotoFileUri(photoFileName)); // set the image file name
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
                Uri takenPhotoUri = getPhotoFileUri(photoFileName);
                Bitmap takenImage = BitmapFactory.decodeFile(takenPhotoUri.getPath());
                //resize bitmap or else may hit OutOfMemoryError
                Bitmap resizedBitmap = BitmapScaler.scaleToFitWidth(takenImage, 200);
                ImageView ivPreview = (ImageView) findViewById(R.id.ivPreview);
                ivPreview.setImageBitmap(resizedBitmap);
                // save file
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
                // new file for the resized bitmap
                Uri resizedUri = getPhotoFileUri(photoFileName + "_resized");
                File resizedFile = new File(resizedUri.getPath());
                try {
                    resizedFile.createNewFile();
                    FileOutputStream fos = new FileOutputStream(resizedFile);
                    fos.write(bytes.toByteArray());
                    fos.close();
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
                    ImageView ivPreview = (ImageView) findViewById(R.id.ivPreview);
                    ivPreview.setImageBitmap(selectedImage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // Load the selected image into a preview
                ImageView ivPreview = (ImageView) findViewById(R.id.ivPreview);
                ivPreview.setImageBitmap(selectedImage);
            } else {
                Toast.makeText(this, "No picture chosen!", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == LOGIN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                userEmail = data.getExtras().getString("user_email");
            }
        }
    }

    // Returns uri for photo stored on disk with fileName
    public Uri getPhotoFileUri(String fileName) {
        if (isExternalStorageAvailable()) {
            File mediaStorageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), APP_TAG);
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
}
