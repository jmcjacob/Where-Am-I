package com.jacob.whereiam;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.Policy;
import java.util.List;

/**
 * Created by Jacob on 18/11/2015.
 */
public class FetchImage extends AsyncTask<ID, Integer, Void> {

    private final String LOG_TAG = FetchImagesTask.class.getSimpleName();
    public Image image = null;

    protected String getImageFromJson(String JsonStr)
        throws Exception {

            JsonStr = JsonStr.replace("jsonFlickrApi(", "");
            JsonStr = JsonStr.replace(")", "");
            InputStream in = new ByteArrayInputStream(JsonStr.getBytes(StandardCharsets.UTF_8));
            JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));

            try {
                return readImageSRCs(reader);
            } finally {
                reader.close();
            }
        }

    protected String readImageSRCs(JsonReader reader) throws Exception
    {
        String src = null;
        Boolean original = false;
        reader.beginArray();
        while (reader.hasNext()) {
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if (name.equals("lable")){
                    if (reader.nextString().equals("Original")) {
                        original = true;
                    }
                }
                if (name.equals("source")){
                    if (original) {
                        src = reader.nextString();
                    }
                }
            }
        }
        reader.endArray();
        return src;
    }

    protected Void doInBackground(ID... id)
    {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String imageJsonStr = null;

        try
        {
            final String Flickr_BASE_URL =
                    "https://api.flickr.com/services/rest/?method=flickr.photos.getSizes&api_key=5e4604aaf0ff1d97a4a621f9b0d06e17";
            final String TEXT_PARAM = "photo_id";
            final String FORMAT_PARAM = "format";

            Uri builtUri = Uri.parse(Flickr_BASE_URL).buildUpon()
                    .appendQueryParameter(TEXT_PARAM, id[0].ID)
                    .appendQueryParameter(FORMAT_PARAM, "json")
                    .build();

            URL url = new URL(builtUri.toString());

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

            Log.v(LOG_TAG, "image string: " + imageJsonStr);
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
            this.image = new Image(id[0].Title, getImageFromJson(imageJsonStr));
            return null;
        }
        catch (Exception e)
        {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
            return null;
        }
    }
}
