package jacob.com.whereami;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
import android.widget.Toast;

import com.google.android.gms.location.LocationRequest;
import com.jacob.whereiam.R;

public class MainActivity extends AppCompatActivity {

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    public static RecyclerView recList;
    public static SQLiteDatabase database;
    public Location dive = null;
    //public static ImageAdapter image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        database = openOrCreateDatabase("Image_Database", MODE_PRIVATE, null);
        database.execSQL("CREATE TABLE IF NOT EXISTS IMAGES(TITLE VARCHAR,ID VARCHAR, THUMBNAIL VARCHAR, SOURCE VARCHAR);");
        FetchImages task = new FetchImages();
        task.execute("0", "1");
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
}
