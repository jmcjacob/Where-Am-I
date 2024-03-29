package jacob.com.whereami;

import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.jacob.whereiam.R;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

// Class for executing a JSON request to set a large SRC and small SRC to the database for each row with an ID and features code from
// Tamada, R. (2013) Android JSON Parsing Tutorial. [online] Available from: http://www.androidhive.info/2012/01/android-json-parsing-tutorial/ [Accessed on 7 November 2015].
public class FetchImage extends AsyncTask<Void, Void, Boolean> {

    private final String LOG_TAG = FetchImage.class.getSimpleName();
    public boolean finished = false;
    public Integer images = 10;

    protected Void getImageFromJson(String JsonStr, String ID) throws Exception {
        JsonStr = JsonStr.replace("jsonFlickrApi(", "");
        JsonStr = JsonStr.replace(")", "");
        String thumbnail = null;
        String large = null;
        String original = null;
        String update;

        JSONObject topobj = new JSONObject(JsonStr);
        JSONObject innerObj = topobj.getJSONObject("sizes");
        JSONArray jsonArray = innerObj.getJSONArray("size");

        for (int i = 1; i < jsonArray.length(); i = i + 1) {
            JSONObject size = jsonArray.getJSONObject(i);
            if (size.getString("label").equals("Small")) {
                thumbnail = size.getString("source");
            }else if (size.getString("label").equals("Large")) {
                large = size.getString("source");
            }
            else if (size.getString("label").equals("Original")) {
                original = size.getString("source");
            }
        }
        if (large!=null)
            update = "UPDATE IMAGES SET THUMBNAIL = \"" + thumbnail + "\", SOURCE = \"" + large + "\" WHERE ID = \"" + ID + "\";";
        else
            update = "UPDATE IMAGES SET THUMBNAIL = \"" + thumbnail + "\", SOURCE = \"" + original + "\" WHERE ID = \"" + ID + "\";";
        MainActivity.database.execSQL(update);
        return null;
    }

    protected Boolean doInBackground(Void... params) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String imageJsonStr = null;
        String[] IDs = new String[images];
        String query = "SELECT ID FROM IMAGES";
        Cursor c = MainActivity.database.rawQuery(query, null);
        c.moveToFirst();
        for (int i = 0; i < images; i ++) {
            IDs[i] = c.getString(c.getColumnIndex("ID"));
            if(!c.moveToNext())
                break;
        }

        for (int i = 0; i < images; i++) {
            try {
                final String Flickr_BASE_URL =
                        "https://api.flickr.com/services/rest/?method=flickr.photos.getSizes";
                final String API_PARAM = "api_key";
                final String TEXT_PARAM = "photo_id";
                final String FORMAT_PARAM = "format";

                Uri builtUri = Uri.parse(Flickr_BASE_URL).buildUpon()
                        .appendQueryParameter(API_PARAM, "5e4604aaf0ff1d97a4a621f9b0d06e17")
                        .appendQueryParameter(TEXT_PARAM, IDs[i])
                        .appendQueryParameter(FORMAT_PARAM, "json")
                        .build();

                URL url = new URL(builtUri.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null)
                    return null;
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                }
                imageJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
            } finally {
                if (urlConnection != null)
                    urlConnection.disconnect();
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            try {
                getImageFromJson(imageJsonStr, IDs[i]);
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean b){
        MainActivity.image = new ImageAdapter();
        MainActivity.recList.setAdapter(MainActivity.image);
        finished = true;
    }
}