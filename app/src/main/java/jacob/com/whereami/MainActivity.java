package jacob.com.whereami;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.location.Criteria;
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
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.jacob.whereiam.R;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    public final static String EXTRA_MESSAGE = "jacob.com.whereami.MESSAGE";
    public static Activity context;
    public static RecyclerView recList;
    public static SQLiteDatabase database;
    public static ImageAdapter image;
    public static SwipeRefreshLayout swipe;
    public static Double lat = 53.230688;
    public static Double lon = -0.540579;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        database = openOrCreateDatabase("Image_Database", MODE_PRIVATE, null);
        recList = (RecyclerView) findViewById(R.id.card_view);
        recList.setHasFixedSize(true);
        GridLayoutManager glm = new GridLayoutManager(this, 2);
        glm.setOrientation(GridLayoutManager.VERTICAL);
        recList.setLayoutManager(glm);
        recList.setItemViewCacheSize(25);

        swipe = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.v(LOG_TAG, "Refresh");
                GridLayoutManager layout = (GridLayoutManager) recList.getLayoutManager();
                layout.removeAllViews();
                TextView textView = (TextView) findViewById(R.id.network);
                textView.setVisibility(View.VISIBLE);
                refreshItems();
            }

            void refreshItems() {
                refresh();
                onItemsComplete();
            }

            void onItemsComplete() {
                Log.v(LOG_TAG, "Finished Refresh");
                swipe.setRefreshing(false);
            }
        });
        refresh();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_about) {
            return true;
        }
        return false;
    }

    private boolean refresh() {
        if (isNetworkAvailable()) {
            database.execSQL("CREATE TABLE IF NOT EXISTS IMAGES(TITLE VARCHAR,ID VARCHAR, THUMBNAIL VARCHAR, SOURCE VARCHAR);");
            database.execSQL("DELETE FROM IMAGES;");
            FetchImages task = new FetchImages();
            task.images = 50;
            task.execute(String.valueOf(lat), String.valueOf(lon));
            return true;
        }
        else {
            TextView textView = (TextView)findViewById(R.id.network);
            textView.setText("No Network Connection");
            return false;
        }
    }

    private boolean isNetworkAvailable() {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        catch (Exception e) {
            Log.e(LOG_TAG, "isNetworkAvailable: " + e);
            return false;
        }
    }

}
