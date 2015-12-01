package jacob.com.whereami;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.jacob.whereiam.R;

public class MainActivity extends AppCompatActivity {

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    GoogleApiClient mGoogleApiClient;
    public static RecyclerView recList;
    public static SQLiteDatabase database;
    public Location dive = null;
    public static ImageAdapter image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        database = openOrCreateDatabase("Image_Database", MODE_PRIVATE, null);
        recList = (RecyclerView) findViewById(R.id.card_view);
        recList.setHasFixedSize(false);
        GridLayoutManager glm = new GridLayoutManager(this, 2);
        glm.setOrientation(GridLayoutManager.VERTICAL);
        this.recList.setLayoutManager(glm);
        refresh();
    }

    private boolean refresh() {
        if (isNetworkAvailable()) {
            database.execSQL("CREATE TABLE IF NOT EXISTS IMAGES(TITLE VARCHAR,ID VARCHAR, THUMBNAIL VARCHAR, SOURCE VARCHAR);");
            database.execSQL("DELETE FROM IMAGES;");
            FetchImages task = new FetchImages();
            task.images = 30;
            task.execute("0","0");
            return true;
        }
        else {
            TextView textView = (TextView)findViewById(R.id.network);
            textView.setVisibility(View.VISIBLE);
            return false;
        }
    }

    /*
    private void enableGPS(){
        LocationManager locationManager=(LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener=new LocationListener(){
            public void onLocationChanged(    Location location){
                dive.setLongitude(location.getLongitude());
                dive.setLatitude(location.getLatitude());
            }
            public void onStatusChanged(    String provider,    int status,    Bundle extras){
            }
            public void onProviderEnabled(    String provider){
            }
            public void onProviderDisabled(    String provider){
            }
        };
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }
    */

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
