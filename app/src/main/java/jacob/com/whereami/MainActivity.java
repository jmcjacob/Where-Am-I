package jacob.com.whereami;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.jacob.whereiam.R;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    public final static String EXTRA_MESSAGE = "jacob.com.whereami.MESSAGE";
    public static Activity context;
    public static SharedPreferences sharedpreferences;
    public static GoogleApiClient mGoogleApiClient;
    public static SQLiteDatabase database;
    public static RecyclerView recList;
    public static ImageAdapter image;
    public static SwipeRefreshLayout swipe;
    public static Double lat = 0.0;
    public static Double lon = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mGoogleApiClient = new GoogleApiClient
                .Builder( this )
                .enableAutoManage( this, 0, this )
                .addApi( Places.GEO_DATA_API )
                .addApi( Places.PLACE_DETECTION_API )
                .addConnectionCallbacks( this )
                .addOnConnectionFailedListener( this )
                .build();

        guessCurrentPlace();

        sharedpreferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        database = openOrCreateDatabase("Image_Database", MODE_PRIVATE, null);

        recList = (RecyclerView) findViewById(R.id.card_view);
        recList.setHasFixedSize(true);
        GridLayoutManager glm = new GridLayoutManager(this, 2);
        glm.setOrientation(GridLayoutManager.VERTICAL);
        recList.setLayoutManager(glm);
        recList.setItemViewCacheSize(sharedpreferences.getInt("cacheImage", 25));

        refresh();

        DrawerFragment drawer = (DrawerFragment) getSupportFragmentManager().findFragmentById(R.id.drawer_fragment);
        drawer.setup(R.id.drawer_fragment, (DrawerLayout) findViewById(R.id.drawer_layout), toolbar);

        swipe = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                GridLayoutManager layout = (GridLayoutManager) recList.getLayoutManager();
                layout.removeAllViews();
                TextView textView = (TextView) findViewById(R.id.network);
                textView.setVisibility(View.VISIBLE);
                textView.setText("Loading...");
                refreshItems();
            }

            void refreshItems() {
                refresh();
                onItemsComplete();
            }

            void onItemsComplete() {
                swipe.setRefreshing(false);
            }
        });
    }

    public void onConnectionSuspended(int i) {

    }

    public void onConnected(Bundle b) {

    }

    public void onConnectionFailed(ConnectionResult result) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        TextView text = (TextView)findViewById(R.id.network);
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            if (text.getText() == "" || text.getText() == "No Network Connection") {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        }
        if (id == R.id.action_about) {
            if (text.getText() == "" || text.getText() == "No Network Connection") {
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if( mGoogleApiClient != null )
            mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if( mGoogleApiClient != null && mGoogleApiClient.isConnected() ) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    private void guessCurrentPlace() {
        PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi.getCurrentPlace(mGoogleApiClient, null);
        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
            @Override
            public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
                lat = likelyPlaces.get(0).getPlace().getLatLng().latitude;
                lon = likelyPlaces.get(0).getPlace().getLatLng().longitude;
                TextView text = (TextView)findViewById(R.id.locationName);
                text.setText(likelyPlaces.get(0).getPlace().getName());
                likelyPlaces.release();
            }
        });
    }

    private boolean refresh() {
        if (isNetworkAvailable()) {
            setFact();
            database.execSQL("CREATE TABLE IF NOT EXISTS IMAGES(TITLE VARCHAR,ID VARCHAR, THUMBNAIL VARCHAR, SOURCE VARCHAR);");
            database.execSQL("DELETE FROM IMAGES;");
            FetchImages task = new FetchImages();
            task.images = sharedpreferences.getInt("loadImage",50);
            task.execute(String.valueOf(lat), String.valueOf(lon));
            return true;
        }
        else {
            TextView textView = (TextView)findViewById(R.id.network);
            textView.setText("No Network Connection");
            Button butt = (Button)findViewById(R.id.refresh);
            butt.setVisibility(View.VISIBLE);
            Toast toast = Toast.makeText(getApplicationContext(), "Could Not Connect to Network", Toast.LENGTH_SHORT);
            toast.show();
            return false;
        }
    }

    public static boolean isNetworkAvailable() {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) MainActivity.context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        catch (Exception e) {
            Log.e("", "isNetworkAvailable: " + e);
            return false;
        }
    }

    public void onClickRefresh(View view) {
        GridLayoutManager layout = (GridLayoutManager) recList.getLayoutManager();
        layout.removeAllViews();
        TextView textView = (TextView) findViewById(R.id.network);
        textView.setVisibility(View.VISIBLE);
        textView.setText("Loading...");
        Button butt = (Button)findViewById(R.id.refresh);
        butt.setVisibility(View.INVISIBLE);
        refresh();
    }

    public void setFact() {
        RandomFact task = new RandomFact();
        task.execute();
        TextView textView = (TextView)findViewById(R.id.fact);
        textView.setVisibility(View.VISIBLE);
        try {
            textView.setText("Random Fact:\n" + task.get(1000, TimeUnit.MILLISECONDS));
        }
        catch (Exception e) {
            Log.e(LOG_TAG, "setFact " + e);
        }
    }
}
