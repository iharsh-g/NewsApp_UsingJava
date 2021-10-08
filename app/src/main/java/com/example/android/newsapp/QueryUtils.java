package com.example.android.newsapp;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class QueryUtils {
    private static final String LOG_TAG = QueryUtils.class.getSimpleName();

    public static List<News> fetchNewsData(String requestUrl) {
        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            //jsonResponse = makeHttpRequest(url);
            jsonResponse = getResponseFromHttpUrl(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        // Extract relevant fields from the JSON response and create a list of {@link Earthquake}s
        List<News> news = extractFeatureFromJson(jsonResponse);

        // Return the list of {@link Earthquake}s
        return news;
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;
    }

    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

    private static List<News> extractFeatureFromJson(String NewsJSON) {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(NewsJSON)) {
            return null;
        }

        List<News> news = new ArrayList<>();

        try {

            // Create a JSONObject from the JSON response string
            JSONObject reader = new JSONObject(NewsJSON);
            JSONObject response = reader.getJSONObject("response");
            JSONArray newsArray = response.getJSONArray("results");

            for (int i = 0; i < newsArray.length(); i++) {

                JSONObject currentNews = newsArray.getJSONObject(i);

                String sectionName = currentNews.getString("sectionName");

                String newsTitle = currentNews.getString("webTitle");
                String newsPublish = currentNews.getString("webPublicationDate");
                String newsURL;
                if(currentNews.has("webUrl")){
                    newsURL = currentNews.getString("webUrl");
                }
                else{
                    newsURL = null;
                }

                String thumbnail;
                if(currentNews.has("fields")){
                    JSONObject fields = currentNews.getJSONObject("fields");
                    thumbnail = fields.getString("thumbnail");
                }
                else{
                    thumbnail = null;
                }


                news.add(new News("newsName", sectionName, newsTitle, newsURL, thumbnail, newsPublish));
            }

        } catch (JSONException e) {
            Log.e("QueryUtils", "Problem parsing the News JSON results", e);
        }

        return news;
    }

}

