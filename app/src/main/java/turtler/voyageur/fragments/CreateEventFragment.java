package turtler.voyageur.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import turtler.voyageur.R;

/**
 * Created by carolinewong on 9/3/16.
 */
public class CreateEventFragment extends DialogFragment {
    @BindView(R.id.ivPreview) ImageView ivPreview;
    private Unbinder unbinder;

    public CreateEventFragment() {}

    public static CreateEventFragment newInstance() {
        CreateEventFragment frag = new CreateEventFragment();
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_event, container, false);
        getDialog().setCanceledOnTouchOutside(true);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        byte[] byteArray = getArguments().getByteArray("data");
        Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        ivPreview.setImageBitmap(bmp);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
