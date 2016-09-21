package turtler.voyageur.utils;

import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;

import turtler.voyageur.models.Image;
import turtler.voyageur.models.User;

/**
 * Created by cwong on 9/13/16.
 */
public class ImageUtils {
    public final static String AMAZON_S3_FILE_URL = "https://voyaging.s3.amazonaws.com/";
    public final static String APP_TAG = "VoyageurApp";

    public static Image saveImageToParse(final Context context, TransferUtility transferUtility, Location mLastLocation, File resizedFile) {
        String lat = Double.toString(mLastLocation.getLatitude());
        String lon = Double.toString(mLastLocation.getLongitude());
        Image parseImage = new Image();
        ParseGeoPoint geoPoint = new ParseGeoPoint(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        parseImage.setGeoPoint(geoPoint);
        parseImage.setLatitude(mLastLocation.getLatitude());
        parseImage.setLongitude(mLastLocation.getLongitude());
        parseImage.setPictureUrl(AMAZON_S3_FILE_URL + resizedFile.getName());
        parseImage.setUser((User) ParseUser.getCurrentUser());

        parseImage.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Toast.makeText(context, "Successfully saved image on Parse",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("ERROR", "Failed to save image", e);
                }
            }
        });

        Toast.makeText(context, lat + "," + lon, Toast.LENGTH_LONG).show();

        TransferObserver observer = transferUtility.upload(Constants.BUCKET_NAME, resizedFile.getName(),
                resizedFile);

        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int i, TransferState transferState) {
                if (transferState.toString().equals("COMPLETED")) {
                    Toast.makeText(context, "Image uploaded successfully to Amazon S3!", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onProgressChanged(int i, long l, long l1) {
            }
            @Override
            public void onError(int i, Exception e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        return parseImage;
    }

    public static Image saveImageToParseNotInBackground(final Context context, TransferUtility transferUtility, Location mLastLocation, File resizedFile) {
        String lat = Double.toString(mLastLocation.getLatitude());
        String lon = Double.toString(mLastLocation.getLongitude());
        Image parseImage = new Image();
        parseImage.setLatitude(mLastLocation.getLatitude());
        parseImage.setLongitude(mLastLocation.getLongitude());
        parseImage.setPictureUrl(AMAZON_S3_FILE_URL + resizedFile.getName());
        parseImage.setUser((User) ParseUser.getCurrentUser());
        try {
            parseImage.save();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        TransferObserver observer = transferUtility.upload(Constants.BUCKET_NAME, resizedFile.getName(),
                resizedFile);

        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int i, TransferState transferState) {
                if (transferState.toString().equals("COMPLETED")) {
                    Toast.makeText(context, "Image uploaded successfully to Amazon S3!", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onProgressChanged(int i, long l, long l1) {
            }
            @Override
            public void onError(int i, Exception e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        return parseImage;
    }

    // Returns uri for photo stored on disk with fileName
    public static Uri getPhotoFileUri(Context c, String fileName) {
        if (isExternalStorageAvailable()) {
            File mediaStorageDir = new File(c.getExternalFilesDir(Environment.DIRECTORY_PICTURES), APP_TAG);
            if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
                Log.d(APP_TAG, "failed to create directory");
            }
            return Uri.fromFile(new File(mediaStorageDir.getPath() + File.separator + fileName));
        }
        return null;
    }

    private static boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        return state.equals(Environment.MEDIA_MOUNTED);
    }
}
