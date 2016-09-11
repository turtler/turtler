package turtler.voyageur.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.Date;

import turtler.voyageur.R;
import turtler.voyageur.utils.TimeFormatUtils;

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
        TextView tvStartDate = (TextView) convertView.findViewById(R.id.tvStartDate);
        TextView tvEndDate = (TextView) convertView.findViewById(R.id.tvEndDate);
        tvTripName.setText(t.get("name").toString());
        if (t.get("startDate") != null) {
            String startDate = TimeFormatUtils.dateToString((Date) t.get("startDate"));
            tvStartDate.setText(startDate);
        }
        if (t.get("endDate") != null) {
            String endDate = TimeFormatUtils.dateToString((Date) t.get("endDate"));
            tvEndDate.setText(endDate);
        }
        return convertView;
    }
}
