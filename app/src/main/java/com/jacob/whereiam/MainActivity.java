package com.jacob.whereiam;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    public static RecyclerView recList;
    public static ImageAdapter image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recList = (RecyclerView) findViewById(R.id.cardview);
        image = new ImageAdapter(new ArrayList<Image>());

        DrawerFragment drawer = (DrawerFragment) getSupportFragmentManager().findFragmentById(R.id.drawer_fragment);
        drawer.setup(R.id.drawer_fragment, (DrawerLayout) findViewById(R.id.drawer_layout), toolbar);

        setImages();
    }

    public void setImages() {

        double[] location = getLocation();
        FetchThumbnails task = new FetchThumbnails();
        task.execute(String.valueOf(location[0]), String.valueOf(location[1]));
        this.recList.setHasFixedSize(false);
        GridLayoutManager llm = new GridLayoutManager(this, 2);
        llm.setOrientation(GridLayoutManager.VERTICAL);
        this.recList.setLayoutManager(llm);
        this.recList.setAdapter(this.image);
    }

    public static void addImage(Image _image) {
        MainActivity.image.ImageList.add(_image);
        MainActivity.recList.setAdapter(MainActivity.image);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_refresh)  {
            GridLayoutManager layout = (GridLayoutManager)this.recList.getLayoutManager();
            layout.removeAllViews();
            setImages();
        }
        return false;
    }

    public double[] getLocation() {
        double lat = 0;
        double longi = 0;
        try {
           LocationListener locationListener = new MyLocationListener();
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 35000, 10, locationListener);
            String locationProvider = lm.GPS_PROVIDER;
            Location lastKnownLocation = lm.getLastKnownLocation(locationProvider);

            lat = lastKnownLocation.getLatitude();
            longi = lastKnownLocation.getLongitude();
        }catch(SecurityException e){
            Log.e(LOG_TAG, "Error ", e);
        }
        double[] location = {lat, longi};
        return location;
    }

    private final class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location locFromGps) {}
        @Override
        public void onProviderDisabled(String provider) {}
        @Override
        public void onProviderEnabled(String provider) {}
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }
}
