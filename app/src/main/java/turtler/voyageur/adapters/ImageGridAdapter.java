package turtler.voyageur.adapters;

import android.support.v7.widget.RecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import turtler.voyageur.R;
import turtler.voyageur.models.Image;

/**
 * Created by skulkarni on 9/13/16.
 */
public class ImageGridAdapter extends RecyclerView.Adapter<ImageGridAdapter.ViewHolder> {
    private List<Image> mItems = new ArrayList<Image>();
    private Context mContext;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.image) ImageView image;

        public ViewHolder(View imgView) {
            super(imgView);
            ButterKnife.bind(this, imgView);
            imgView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

        }
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View imageGridView = inflater.inflate(R.layout.item_grid, parent, false);
        ViewHolder viewHolder = new ViewHolder(imageGridView);
        return viewHolder;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public void onBindViewHolder(ImageGridAdapter.ViewHolder holder, int position) {
        Image i = mItems.get(position);
        Glide.with(mContext).load(i.getPictureUrl()).into(holder.image);
    }
    public ImageGridAdapter(Context context, ArrayList<Image> items) {
        mItems = items;
        mContext = context;
    }
    
}
