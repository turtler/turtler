package turtler.voyageur.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;

import butterknife.ButterKnife;
import turtler.voyageur.R;
import turtler.voyageur.fragments.ViewPagerContainerFragment;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by cwong on 9/11/16.
 */
public class TripActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fade_in_center, R.anim.fade_out_center);
        setContentView(R.layout.activity_trip);
        ButterKnife.bind(this);
        String tripId = getIntent().getStringExtra("tripId");

        ViewPagerContainerFragment vpcf = new ViewPagerContainerFragment();
        String tripImg = getIntent().getExtras().getString("tripImage");
        Bundle b = new Bundle();
        b.putString("tripImage", tripImg);
        b.putString("tripId", tripId);
        vpcf.setArguments(b);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.flTrip, vpcf, "view_pager")
                .commit();

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
