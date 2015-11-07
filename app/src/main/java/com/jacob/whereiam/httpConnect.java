package com.jacob.whereiam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import android.util.Log;

public class httpConnect
{
    final String TAG = "JasonParser.java";
    static String json = "";

    public String getJSONFromUrl(String url)
    {
        try
        {
            URL u = new URL(url);
            HttpURLConnection restConnection = (HttpURLConnection) u.openConnection();
            restConnection.setRequestMethod("Get");
            restConnection.setRequestProperty("Content-length", "0");
            restConnection.setUseCaches(false);
            restConnection.setAllowUserInteraction(false);
            restConnection.setConnectTimeout(1000);
            restConnection.setReadTimeout(10000);
            restConnection.connect();
            int status = restConnection.getResponseCode();

            switch (status)
            {
                case 200:
                case 201:
                    BufferedReader br = new BufferedReader(new InputStreamReader(restConnection.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;

                    while ((line = br.readLine()) != null)
                    {
                        sb.append(line+"\n");
                    }
                    br.close();

                    try
                    {
                        json = sb.toString();
                    }
                    catch (Exception e)
                    {
                        Log.e(TAG, "Error: " + e.toString());
                    }
                    return json;
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, "Error: " + e.toString());
        }
        return null;
    }
}