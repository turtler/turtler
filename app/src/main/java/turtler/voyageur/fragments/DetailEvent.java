package turtler.voyageur.fragments;

import android.app.Dialog;
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
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.bumptech.glide.Glide;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ListHolder;
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
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import turtler.voyageur.R;
import turtler.voyageur.VoyageurApplication;
import turtler.voyageur.adapters.DialogGridItemAdapter;
import turtler.voyageur.models.Event;
import turtler.voyageur.models.Image;
import turtler.voyageur.models.Marker;
import turtler.voyageur.models.User;
import turtler.voyageur.utils.AmazonUtils;
import turtler.voyageur.utils.BitmapScaler;
import turtler.voyageur.utils.ImageUtils;
import turtler.voyageur.utils.TimeFormatUtils;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * Use the {@link DetailEvent#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetailEvent extends DialogFragment {
    @BindView(R.id.tvCaption) TextView tvCaption;
    @BindView(R.id.tvDate) TextView tvDate;
    @BindView(R.id.slider) SliderLayout sliderLayout;
    @BindView(R.id.ivUploadImage) ImageView ivUploadImage;
    Unbinder unbinder;
    Location mLastLocation;
    Image image;
    Event currentEvent;
    Dialog d;
    Bitmap selectedImageBitmap;
    GoogleApiClient mGoogleApiClient;
    private TransferUtility transferUtility;
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    public final static int PICK_PHOTO_CODE = 1046;
    public String photoFileName = "photo";

    private static final String ARG_EVENT_ID = "eventId";

    // TODO: Rename and change types of parameters
    private String eventId;


    public DetailEvent() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param eventId Parameter 1.
     * @return A new instance of fragment DetailEvent.
     */
    // TODO: Rename and change types and number of parameters
    public static DetailEvent newInstance(String eventId) {
        DetailEvent fragment = new DetailEvent();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventId = getArguments().getString(ARG_EVENT_ID);
            ParseQuery<Event> query = ParseQuery.getQuery("Event");
            query.getInBackground(eventId, new GetCallback<Event>() {
                @Override
                public void done(Event eventObj, ParseException e) {
                    if (e == null) {
                        currentEvent = eventObj;
                        d.setTitle(currentEvent.getTitle());
                        setEventDetailLayout(currentEvent);
                    }
                }
            });
        }

        transferUtility = AmazonUtils.getTransferUtility(getContext());
    }

    public void setEventDetailLayout(final Event ev) {
        ArrayList<Image> images = new ArrayList<>();
        try {
            images = (ArrayList<Image>) ev.imagesRelation().getQuery().find();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (images.size() > 0) {
            for (Image image : images) {
                TextSliderView textSliderView = new TextSliderView(getContext());
                textSliderView.image(image.getPictureUrl());
                textSliderView.setScaleType(BaseSliderView.ScaleType.FitCenterCrop);
                sliderLayout.addSlider(textSliderView);

            }
            if (images.size() == 1) {
                sliderLayout.stopAutoCycle();
            }
        }
        sliderLayout.setVisibility(View.VISIBLE);

        tvCaption.setText(ev.getCaption());
        if (ev.getDate() != null) {
            tvDate.setText(TimeFormatUtils.dateTimeToString(ev.getDate()));
        }

        ivUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().hide();
                DialogPlus dialog = DialogPlus.newDialog(getContext())
                        .setContentHolder(new ListHolder())
                        .setExpanded(true, 400)
                        .setPadding(10, 10, 10, 10)
                        .setContentHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                        .setAdapter(new DialogGridItemAdapter(getContext()))
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

        image = ImageUtils.saveImageToParse(getContext(), transferUtility, mLastLocation, resizedFile);
        selectedImageBitmap = imgBitmap;

    }

    public void setFragmentUIWithEventProps() {
        ivUploadImage.setPadding(0, 0, 0, 0);
        ivUploadImage.setImageBitmap(selectedImageBitmap);
    }

    public void showCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, ImageUtils.getPhotoFileUri(getContext(), "test")); // set the image file name
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
    public void onStop() {
        sliderLayout.stopAutoCycle();
        super.onStop();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        final View view = inflater.inflate(R.layout.fragment_detail_event, null);
        unbinder = ButterKnife.bind(this, view);
        d = new AlertDialog.Builder(getActivity()).setTitle("Event Detail").setView(view)
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

        return d;
    }

    public void saveEvent() {
        if (image != null) {
            currentEvent.addImage(image);
        }
        currentEvent.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                dismiss();
            }
        });
    }
}
