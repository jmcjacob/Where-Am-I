package com.jacob.whereiam;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Jacob on 20/11/2015.
 */
public class BitmapImage extends AsyncTask<Image, Void, Void> {

    private final String LOG_TAG = BitmapImage.class.getSimpleName();
    Bitmap bitmap = null;

    protected Void doInBackground(Image... image)
    {
        Bitmap myBitmap = null;
        try {
            URL url = new URL(image[0].SRC);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            myBitmap = BitmapFactory.decodeStream(input);
        } catch (Exception e) {
            Log.e(LOG_TAG, "ERROR: " + e);
        }
        if (myBitmap != null) {
            bitmap = myBitmap;
            return null;
        }
        return null;
    }

    protected Boolean isReady()
    {
        if (bitmap != null)
            return true;
        else
            return false;
    }
}
