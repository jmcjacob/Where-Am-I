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

public class FetchImagesTask extends AsyncTask<String, Integer, List> {

    private final String LOG_TAG = FetchImagesTask.class.getSimpleName();

    private List getImageDataFromJson(String JsonStr)
            throws Exception {

        JsonStr = JsonStr.replace("jsonFlickrApi(", "");
        JsonStr = JsonStr.replace(")", "");
        InputStream in = new ByteArrayInputStream(JsonStr.getBytes(StandardCharsets.UTF_8));
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));

        try {
            return readIDArray(reader);
        }
        finally {
            reader.close();
        }
    }

    public List readIDArray(JsonReader reader) throws IOException {
        List IDs = new ArrayList();
        reader.beginArray();
        while(reader.hasNext())
        {
            IDs.add(readID(reader));
        }
        reader.endArray();
        return IDs;
    }

    public ID readID(JsonReader reader) throws IOException {
        String image_id = null;
        String image_title = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("id")) {
                image_id = reader.nextString();
            } else if (name.equals(("title"))) {
                image_title = reader.nextString();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return new ID(image_id, image_title);
    }

    @Override
    protected List doInBackground(String... Parameters)
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

            Log.v(LOG_TAG, "galley string: " + imageJsonStr);
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

    public class ID{
        public String ID;
        public String Title;

        public ID (String id, String title)
        {
            this.ID = id;
            this.Title = title;
        }
    }
}