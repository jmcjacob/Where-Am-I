package com.jacob.whereiam;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FetchImagesTask extends AsyncTask<String, Integer, List<ID>> {

    private final String LOG_TAG = FetchImagesTask.class.getSimpleName();

    public List<Image> images = null;

    private List<ID> getImageDataFromJson(String JsonStr)
            throws Exception {

        JsonStr = JsonStr.replace("jsonFlickrApi(", "");
        JsonStr = JsonStr.replace(")", "");
        List<ID> IDs = new ArrayList<ID>();

        JSONObject topobj = new JSONObject(JsonStr);
        JSONObject innerObj = topobj.getJSONObject("photos");
        JSONArray jsonArray = innerObj.getJSONArray("photo");

        for(int i = 0; i < 100; i++)
        {
            String title = null;
            String id = null;

            JSONObject photo = jsonArray.getJSONObject(i);
            IDs.add(new ID(photo.getString("id"),photo.getString("title")));
        }

        return IDs;
    }

    @Override
    protected List<ID> doInBackground(String... Parameters)
    {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String imageJsonStr = null;

        try
        {
            final String Flickr_BASE_URL =
                    "https://api.flickr.com/services/rest/?method=flickr.photos.search&api_key=5e4604aaf0ff1d97a4a621f9b0d06e17";
            final String TEXT_PARAM = "text";
            final String FORMAT_PARAM = "format";

            Uri builtUri = Uri.parse(Flickr_BASE_URL).buildUpon()
                    .appendQueryParameter(TEXT_PARAM, Parameters[0])
                    .appendQueryParameter(FORMAT_PARAM, "json")
                    .build();

            URL url = new URL(builtUri.toString());

                //https://api.flickr.com/services/rest/?method=flickr.photos.getSizes&api_key=5e4604aaf0ff1d97a4a621f9b0d06e17&photo_id=" + Parameters[1] + "&format=json

            Log.v(LOG_TAG, "Built URI " + url.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null)
                return null;
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null)
            {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            imageJsonStr = buffer.toString();
        }
        catch (IOException e)
        {
            Log.e(LOG_TAG, "Error ", e);
            return null;
        }
        finally
        {
            if (urlConnection != null)
                urlConnection.disconnect();
            if (reader != null)
            {
                try
                {
                    reader.close();
                }
                catch (final IOException e)
                {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        try
        {
            return getImageDataFromJson(imageJsonStr);
        }
        catch (Exception e)
        {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(List<ID> IDs)
    {
        List<Image> imagelist = new ArrayList();
        Log.v(LOG_TAG, "this.images");
        for (int i = 0; i < IDs.size(); i++) {

            FetchImage image = new FetchImage();
            image.execute(IDs.get(i));
            images.add(image.image);
        }
        this.images = imagelist;

    }
}