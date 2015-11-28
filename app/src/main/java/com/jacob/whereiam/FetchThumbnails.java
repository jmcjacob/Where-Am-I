package com.jacob.whereiam;

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
import java.util.ArrayList;
import java.util.List;

public class FetchThumbnails extends AsyncTask<String, Integer, List<Image>> {

    private final String LOG_TAG = FetchThumbnails.class.getSimpleName();

    private List<Image> getImageDataFromJson(String JsonStr)
            throws Exception {

        JsonStr = JsonStr.replace("jsonFlickrApi(", "");
        JsonStr = JsonStr.replace(")", "");
        List<Image> IDs = new ArrayList<>();

        JSONObject topobj = new JSONObject(JsonStr);
        JSONObject innerObj = topobj.getJSONObject("photos");
        JSONArray jsonArray = innerObj.getJSONArray("photo");

        for(int i = 0; i < 30; i++) {
            JSONObject photo = jsonArray.getJSONObject(i);
            IDs.add(new Image(photo.getString("title"),photo.getString("id")));
        }
        return IDs;
    }

    @Override
    protected List<Image> doInBackground(String... Parameters) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String imageJsonStr = null;

        try {
            final String Flickr_BASE_URL =
                    "https://api.flickr.com/services/rest/?method=flickr.photos.search&api_key=5e4604aaf0ff1d97a4a621f9b0d06e17";
            final String LAT_PARAM = "lat";
            final String LON_PARAM= "lon";
            final String FORMAT_PARAM = "format";

            Uri builtUri = Uri.parse(Flickr_BASE_URL).buildUpon()
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
    protected void onPostExecute(List<Image> IDs)
    {
        for (int i = 0; i < IDs.size(); i++) {
            FetchThumbnail image = new FetchThumbnail();
            image.execute(IDs.get(i));
        }

    }
}