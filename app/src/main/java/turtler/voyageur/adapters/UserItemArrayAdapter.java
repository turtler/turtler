package turtler.voyageur.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import turtler.voyageur.R;
import turtler.voyageur.models.User;
import turtler.voyageur.models.UserEntry;

/**
 * Created by skulkarni on 9/12/16.
 */
public class UserItemArrayAdapter extends ArrayAdapter<UserEntry> {
    private UserEntry user;
    private TextView name;
    private ImageView pic;
    private List<UserEntry> users = new ArrayList<UserEntry>();
    private List<UserEntry> usersAll = new ArrayList<UserEntry>();
    private UserRowListener listener;

    public interface UserRowListener {
        public void onClick(UserEntry u);
    }

    public UserItemArrayAdapter(Context context, int tvId, List<UserEntry> u, UserRowListener l) {
        super(context, tvId, u);
        users = u;
        usersAll.addAll(u);
        listener = l;
    }


    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public UserEntry getItem(int position) {
        return users.get(position);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View row = convertView;
        LayoutInflater inflater = LayoutInflater.from(getContext());
        if (row == null) {
            row = inflater.inflate(R.layout.item_dropdown_user, parent, false);
        }

        user = users.get(position);
        String userName = user.name;
        TextView nameTextView = (TextView) row.findViewById(R.id.tvUserName);
        nameTextView.setText(userName);

        ImageView userProfImage = (ImageView) row.findViewById(R.id.ivUserProfPic);
        Picasso.with(getContext()).load(user.imgUrl).into(userProfImage);


        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usersAll.remove(user);
                listener.onClick(user);
            }
        });
        return row;
    }
    Filter nameFilter = new Filter() {

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            return ((UserEntry)resultValue).name;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if (constraint != null) {
                ArrayList<UserEntry> suggestions = new ArrayList<UserEntry>();
                for (UserEntry user : usersAll) {
                    if (user.name.toLowerCase().contains(constraint.toString().toLowerCase())) {
                        suggestions.add(user);
                    }
                }

                results.values = suggestions;
                results.count = suggestions.size();
            }

            return results;

        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            ArrayList<UserEntry> filteredList = (ArrayList<UserEntry>) results.values;
            if (results != null && results.count > 0) {
                // we have filtered results
                users.clear();
                for (UserEntry u : filteredList) {
                    users.add(u);
                }
                notifyDataSetChanged();
            }
        }
    };

    @Override
    public Filter getFilter() {
        return nameFilter;
    }

}
