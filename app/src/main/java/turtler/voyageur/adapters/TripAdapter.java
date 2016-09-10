package turtler.voyageur.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.parse.ParseObject;

import java.util.ArrayList;

import turtler.voyageur.R;

/**
 * Created by carolinewong on 9/9/16.
 */
public class TripAdapter extends ArrayAdapter<ParseObject> {
    public TripAdapter(Context c, ArrayList<ParseObject> trips) {
        super(c, 0, trips);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ParseObject t = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_trip, parent, false);
        }
        TextView tvTripName = (TextView) convertView.findViewById(R.id.tvTripName);
        tvTripName.setText(t.get("name").toString());
        return convertView;
    }
}
