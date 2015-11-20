package com.jacob.whereiam;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity  implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    public static RecyclerView recList;
    public static ImageAdapter image;
    protected GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation = null;
    protected String mLatitudeLabel;
    protected String mLongitudeLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        buildGoogleApiClient();
        mGoogleApiClient.connect();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recList = (RecyclerView) findViewById(R.id.cardview);
        image = new ImageAdapter(new ArrayList<Image>());

        DrawerFragment drawer = (DrawerFragment) getSupportFragmentManager().findFragmentById(R.id.drawer_fragment);
        drawer.setup(R.id.drawer_fragment, (DrawerLayout) findViewById(R.id.drawer_layout), (Toolbar) toolbar);

        while(mLastLocation == null) {
            try {
            wait();
            } catch (Exception e){}
        }
        setImages();
    }

    public void setImages() {

        FetchImagesTask task = new FetchImagesTask();
        task.execute(String.valueOf(this.mLastLocation.getLatitude()), String.valueOf(this.mLastLocation.getLongitude()));
        this.recList.setHasFixedSize(true);
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
        } else {
            Toast.makeText(this, "No Location", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(LOG_TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(LOG_TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }
}
