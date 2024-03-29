package jacob.com.whereami;

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

// Class for retrieving a cat fact from the catfact API in a new thread.
// Manelik (2013) Cat Facts API. [online] Available from: http://catfacts-api.appspot.com/ [Accessed on 3 December 2015].
// Tamada, R. (2013) Android JSON Parsing Tutorial. [online] Available from: http://www.androidhive.info/2012/01/android-json-parsing-tutorial/ [Accessed on 7 November 2015].
public class CatFact extends AsyncTask<Void, Void, String> {

    private final String LOG_TAG = CatFact.class.getSimpleName();

    // Method for extracting fact data from the returned JSON string.
    private String getImageDataFromJson(String JsonStr) throws Exception {
        JSONObject topobj = new JSONObject(JsonStr);
        JSONArray factArray = topobj.getJSONArray("facts");
        return factArray.getString(0);
    }

    protected String doInBackground(Void... v){
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String imageJsonStr = null;

        try {
            final String CAT_BASE_URL = "http://catfacts-api.appspot.com/api/facts";

            URL url = new URL(CAT_BASE_URL);
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
}
