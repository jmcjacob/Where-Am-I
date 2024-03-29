package jacob.com.whereami;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

// Class for getting and inserting an returned IDs and Titles to an SQL database in a new thread and features code from
// Tamada, R. (2013) Android JSON Parsing Tutorial. [online] Available from: http://www.androidhive.info/2012/01/android-json-parsing-tutorial/ [Accessed on 7 November 2015].
public class FetchImages extends AsyncTask<String, Integer, Void> {
    private final String LOG_TAG = FetchImages.class.getSimpleName();
    public boolean finished = false;
    public Integer images = 10;

    private Void getImageDataFromJson(String JsonStr) throws Exception {
        JsonStr = JsonStr.replace("jsonFlickrApi(", "");
        JsonStr = JsonStr.replace(")", "");

        JSONObject topobj = new JSONObject(JsonStr);
        JSONObject innerObj = topobj.getJSONObject("photos");
        JSONArray jsonArray = innerObj.getJSONArray("photo");

        Integer search = jsonArray.length();
        if (images <= jsonArray.length())
            search = images;

        try {
            for (int i = 0; i < search; i++) {
                JSONObject photo = jsonArray.getJSONObject(i);
                String[] temp = {photo.getString("title"), photo.getString("id")};
                MainActivity.database.execSQL("INSERT INTO IMAGES (TITLE, ID) VALUES (\"" + temp[0] + "\",\"" + temp[1] + "\");");
            }
            return null;
        }
        catch (Exception e) {
            Log.e(LOG_TAG, "FetchImages ERROR: " + e);
            return null;
        }
    }

    @Override
    protected Void doInBackground(String... Parameters) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String imageJsonStr = null;

        try {
            final String Flickr_BASE_URL = "https://api.flickr.com/services/rest/?method=flickr.photos.search";
            final String API_PARAM = "api_key";
            final String SORT_PARAM = "sort";
            final String LAT_PARAM = "lat";
            final String LON_PARAM= "lon";
            final String FORMAT_PARAM = "format";

            Uri builtUri = Uri.parse(Flickr_BASE_URL).buildUpon()
                    .appendQueryParameter(API_PARAM, "5e4604aaf0ff1d97a4a621f9b0d06e17")
                    .appendQueryParameter(SORT_PARAM, MainActivity.sharedpreferences.getString("searchType", "relevance"))
                            .appendQueryParameter(LAT_PARAM, Parameters[0])
                    .appendQueryParameter(LON_PARAM, Parameters[1])
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
                return null;
            }
            imageJsonStr = buffer.toString();
        }
        catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            return null;
        }
        finally {
            if (urlConnection != null)
                urlConnection.disconnect();
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        try {
            return getImageDataFromJson(imageJsonStr);
        }
        catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void V) {
        FetchImage task = new FetchImage();
        task.images = images;
        task.execute();
        finished = true;
    }
}