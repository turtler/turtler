package turtler.voyageur.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import turtler.voyageur.R;
import turtler.voyageur.models.Image;

/**
 * Created by skulkarni on 9/13/16.
 */
public class ImageGridAdapter extends ArrayAdapter {
    private List<Image> mItems = new ArrayList<Image>();
    private Context context;
    private int layoutResourceId;


    public ImageGridAdapter(Context context, int layoutResourceId, ArrayList items) {
        super(context, layoutResourceId, items);
        this.mItems = items;
        this.context = context;
        this.layoutResourceId = layoutResourceId;
    }
    static class ViewHolder {
        ImageView image;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.image = (ImageView) row.findViewById(R.id.image);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        Image item = mItems.get(position);
        Picasso.with(getContext()).load(item.getPictureUrl()).into(holder.image);
        return row;
    }
}
