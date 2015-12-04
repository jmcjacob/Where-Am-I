package jacob.com.whereami;

import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Chuck extends AsyncTask<Void, Void, String> {

    private final String LOG_TAG = CatFact.class.getSimpleName();

    private String getImageDataFromJson(String JsonStr) throws Exception {

        JSONObject topobj = new JSONObject(JsonStr);
        JSONObject inobj = topobj.getJSONObject("value");
        return inobj.getString("joke");
    }

    protected String doInBackground(Void... v){
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String imageJsonStr = null;

        try {
            final String CAT_BASE_URL = "http://api.icndb.com/jokes/random";

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

