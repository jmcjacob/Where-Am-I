package jacob.com.whereami;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.jacob.whereiam.R;
import java.util.concurrent.TimeUnit;

// The main Activity is where the application opens up to. In the create all the values used across multiple classes are created. This class features code found in
// Foster, D (2015) Mobile Computing. [lecture] Lincoln: University of Lincoln.
// Google (2015) Google Places APIs for Android. [software] California, USA: Google. Available from https://developers.google.com/places/android-api/ [Accessed 8 December 2015].
// Jasmin, A. (2010) Detect whether there is an Internet connection available on Android. [online] Stackoverflow. Available from http://stackoverflow.com/questions/4238921/detect-whether-there-is-an-internet-connection-available-on-android [Accessed on 1 December 2015].
// Prock, A. and Shankar, A. (2015) How to check if Location Services are enabled? [online] Stackoverflow. Available from http://stackoverflow.com/questions/10311834/how-to-check-if-location-services-are-enabled [Accessed on 1 December 2015].
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
    public static Boolean places;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .enableAutoManage(this, 0, this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(AppIndex.API).build();
        places = false;

        context = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sharedpreferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        database = openOrCreateDatabase("Image_Database", MODE_PRIVATE, null);
        database.execSQL("CREATE TABLE IF NOT EXISTS IMAGES(TITLE VARCHAR,ID VARCHAR, THUMBNAIL VARCHAR, SOURCE VARCHAR);");

        TextView textView = (TextView) findViewById(R.id.locationName);
        lat = Double.longBitsToDouble(sharedpreferences.getLong("latestLat", Double.doubleToLongBits(0.0)));
        lon = Double.longBitsToDouble(sharedpreferences.getLong("latestLon", Double.doubleToLongBits(0.0)));
        textView.setText(sharedpreferences.getString("latestLoc", "Location"));

        recList = (RecyclerView) findViewById(R.id.card_view);
        recList.setHasFixedSize(true);
        GridLayoutManager glm = new GridLayoutManager(this, 2);
        glm.setOrientation(GridLayoutManager.VERTICAL);
        recList.setLayoutManager(glm);
        recList.setItemViewCacheSize(sharedpreferences.getInt("cacheImage", 25));

        DrawerFragment drawer = (DrawerFragment) getSupportFragmentManager().findFragmentById(R.id.drawer_fragment);
        drawer.setup(R.id.drawer_fragment, (DrawerLayout) findViewById(R.id.drawer_layout), toolbar);

        swipe = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                GridLayoutManager layout = (GridLayoutManager) recList.getLayoutManager();
                layout.removeAllViews();
                Button butt = (Button) findViewById(R.id.refresh);
                butt.setVisibility(View.INVISIBLE);
                refreshItems();
            }

            void refreshItems() {
                refresh();
            }
        });

        TextView text = (TextView) findViewById(R.id.locationName);
        text.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == TextView.VISIBLE) {
                    refresh();
                }
                return false;
            }
        });

        refresh();
    }

    public void onConnectionSuspended(int i) {}

    public void onConnected(Bundle b) {}

    public void onConnectionFailed(ConnectionResult result) {}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        TextView text = (TextView)findViewById(R.id.network);
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
        }
        if (id == R.id.action_about) {
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    private void guessCurrentPlace() {
        try {
                mGoogleApiClient.connect();
                PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi.getCurrentPlace(mGoogleApiClient, null);
                result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
                    @Override
                    public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
                        lat = likelyPlaces.get(0).getPlace().getLatLng().latitude;
                        lon = likelyPlaces.get(0).getPlace().getLatLng().longitude;
                        TextView text = (TextView) findViewById(R.id.locationName);
                        text.setText(likelyPlaces.get(0).getPlace().getName());
                        text = (TextView) findViewById(R.id.address);
                        if (!likelyPlaces.get(0).getPlace().getAddress().toString().equals(null)) {
                            String address = (String) likelyPlaces.get(0).getPlace().getAddress();
                            text.setText(address.replace(", ", ",\n"));
                        }
                        text = (TextView) findViewById(R.id.number);
                        if (!likelyPlaces.get(0).getPlace().getPhoneNumber().toString().equals(null)) {
                            String number = (String) likelyPlaces.get(0).getPlace().getPhoneNumber();
                            text.setText(number.replace(" ", ""));
                        }
                        text = (TextView) findViewById(R.id.webAddress);
                        if (likelyPlaces.get(0).getPlace().getWebsiteUri() == null) {
                            TextView textView = (TextView) findViewById(R.id.webAddress);
                            textView.setText("");
                        } else
                            text.setText(likelyPlaces.get(0).getPlace().getWebsiteUri().toString());
                        likelyPlaces.release();
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putLong("latestLat", Double.doubleToRawLongBits(lat));
                        editor.putLong("latestLon", Double.doubleToRawLongBits(lon));
                        editor.putString("latestLoc", (String) text.getText());
                        editor.commit();
                    }
                });
        }
        catch (Exception e) {
            Log.e(LOG_TAG, "guessCurrentPlace: " + e);
        }
    }

    private boolean refresh() {
        if (isNetworkAvailable()) {
            if (!places)
                if (isLocationEnabled(this))
                    guessCurrentPlace();
            setFact();
            database.execSQL("DELETE FROM IMAGES;");
            FetchImages task = new FetchImages();
            task.images = sharedpreferences.getInt("loadImage",50);
            task.execute(String.valueOf(lat), String.valueOf(lon));
            return true;
        }
        else {
            GridLayoutManager layout = (GridLayoutManager) recList.getLayoutManager();
            layout.removeAllViews();
            TextView textView = (TextView) findViewById(R.id.network);
            textView.setText("Couldn't connect to network");
            Button butt = (Button) findViewById(R.id.refresh);
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

    public static boolean isLocationEnabled(Context context) {
        try {
            int locationMode = 0;
            String locationProviders;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                try {
                    locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
                } catch (Settings.SettingNotFoundException e) {
                    e.printStackTrace();
                }
                return locationMode != Settings.Secure.LOCATION_MODE_OFF;
            } else {
                locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
                return !TextUtils.isEmpty(locationProviders);
            }
        }
        catch (Exception e) {
            Log.e("", "isLocationEnabled: " + e);
            return false;
        }
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

    public void onPlacesClick(View view) {
        if (!places) {
            if (isNetworkAvailable()) {
                int PLACE_PICKER_REQUEST = 1;
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "onPlacesClick: " + e);
                }
            }
            else {
                Toast toast = Toast.makeText(getApplicationContext(), "Could Not Connect to Network", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
        else {
            places = false;
            GridLayoutManager layout = (GridLayoutManager) recList.getLayoutManager();
            layout.removeAllViews();
            TextView textView = (TextView) findViewById(R.id.network);
            textView.setVisibility(View.VISIBLE);
            textView.setText("Loading...");
            refresh();
            Button butt = (Button) findViewById(R.id.placesButton);
            butt.setText("Find Location");
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) throws NullPointerException{
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                lat = place.getLatLng().latitude;
                lon = place.getLatLng().longitude;
                TextView text = (TextView) findViewById(R.id.locationName);
                text.setText(place.getName());
                text = (TextView) findViewById(R.id.address);
                if (!place.getAddress().toString().equals(null)) {
                    String address = (String) place.getAddress();
                    text.setText(address.replace(", ", ",\n"));
                }
                text = (TextView) findViewById(R.id.number);
                if (!place.getPhoneNumber().toString().equals(null)) {
                    String number = (String)place.getPhoneNumber();
                    text.setText(number.replace(" ", ""));
                }
                text = (TextView) findViewById(R.id.webAddress);
                if (place.getWebsiteUri() == null) {
                    TextView textView = (TextView) findViewById(R.id.webAddress);
                    textView.setText("");
                }
                else
                    text.setText(place.getWebsiteUri().toString());
                places = true;
                GridLayoutManager layout = (GridLayoutManager) recList.getLayoutManager();
                layout.removeAllViews();
                TextView textView = (TextView) findViewById(R.id.network);
                textView.setVisibility(View.VISIBLE);
                textView.setText("Loading...");
                refresh();
                Button butt = (Button) findViewById(R.id.placesButton);
                butt.setText("Get Current Location");
            }
        }
    }

    public void onClickNumber(View view) {
        TextView textView = (TextView)findViewById(R.id.number);
        if (!textView.getText().equals("")) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + textView.getText()));
            startActivity(intent);
        }
    }

    public void onClickWeb(View view) {
        TextView textView = (TextView)findViewById(R.id.webAddress);
        if (!textView.getText().equals("")) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse((String) textView.getText()));
            startActivity(i);
        }
    }
}
