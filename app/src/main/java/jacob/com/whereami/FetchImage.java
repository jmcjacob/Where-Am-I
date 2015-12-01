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

public class FetchImage extends AsyncTask<String[], Void, Void> {

    private final String LOG_TAG = FetchImage.class.getSimpleName();
    public boolean finished = false;

    protected Void getImageFromJson(String JsonStr, String ID) throws Exception {
        JsonStr = JsonStr.replace("jsonFlickrApi(", "");
        JsonStr = JsonStr.replace(")", "");

        JSONObject topobj = new JSONObject(JsonStr);
        JSONObject innerObj = topobj.getJSONObject("sizes");
        JSONArray jsonArray = innerObj.getJSONArray("size");

        for (int i = 1; i < jsonArray.length(); i = i + 1) {
            JSONObject size = jsonArray.getJSONObject(i);
            if (size.getString("label").equals("Thumbnail")) {
                String update = "UPDATE IMAGES SET THUMBNAIL = \"" + size.getString("source") + "\" WHERE ID = \"" + ID + "\";";
                MainActivity.database.execSQL(update);
            }
            if (size.getString("label").equals("Original")) {
                String update = "UPDATE IMAGES SET SOURCE = \"" + size.getString("source") + "\" WHERE ID = \"" + ID + "\";";
                MainActivity.database.execSQL(update);
            }
        }
        return null;
    }

    protected Void doInBackground(String[]... params) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String imageJsonStr = null;
        try {
            final String Flickr_BASE_URL =
                    "https://api.flickr.com/services/rest/?method=flickr.photos.getSizes";
            final String API_PARAM = "api_key";
            final String TEXT_PARAM = "photo_id";
            final String FORMAT_PARAM = "format";

            Uri builtUri = Uri.parse(Flickr_BASE_URL).buildUpon()
                    .appendQueryParameter(API_PARAM, "5e4604aaf0ff1d97a4a621f9b0d06e17")
                    .appendQueryParameter(TEXT_PARAM, params[0][1])
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
            getImageFromJson(imageJsonStr, params[0][1]);
            return null;
        }
        catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(Void v){
        finished = true;
        Log.v(LOG_TAG, "Finished an image");
    }
}