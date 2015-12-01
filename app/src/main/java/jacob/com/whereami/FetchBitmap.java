package jacob.com.whereami;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class FetchBitmap extends AsyncTask<String, Void, Bitmap> {
    private final String LOG_TAG = FetchBitmap.class.getSimpleName();

    protected Bitmap doInBackground(String... params) {
        Bitmap bitmap = null;
        try {
            URL url = new URL(params[0]);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            bitmap = BitmapFactory.decodeStream(input);
        }
        catch (Exception e) {
            Log.e(LOG_TAG, "Fetch Bitmap: " + e);
        }
        if (bitmap != null) {
            return bitmap;
        }
        return null;
    }
}
