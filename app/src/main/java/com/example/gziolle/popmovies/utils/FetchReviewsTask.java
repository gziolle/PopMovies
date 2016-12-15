package com.example.gziolle.popmovies.utils;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.example.gziolle.popmovies.BuildConfig;
import com.example.gziolle.popmovies.ReviewItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by gziolle on 12/15/2016.
 */

public class FetchReviewsTask extends AsyncTask<String, Void, ArrayList<ReviewItem>> {

    private static final String RESULTS = "results";
    private final String LOG_TAG = FetchReviewsTask.class.getSimpleName();

    public AsyncResponse response = null;

    public FetchReviewsTask(AsyncResponse response) {
        this.response = response;
    }

    @Override
    protected ArrayList<ReviewItem> doInBackground(String... params) {

        HttpURLConnection conn = null;
        InputStream is;
        BufferedReader reader = null;
        String jResult;
        ArrayList<ReviewItem> reviewList = new ArrayList<>();

        if (params[0] == null) {
            return null;
        }

        try {
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http");
            builder.authority(Utility.TMDB_AUTHORITY);
            builder.appendPath(Utility.TMDB_API_VERSION).appendPath(Utility.TMDB_MOVIE_DIR).appendPath(params[0]).appendPath(Utility.TMDB_MOVIE_REVIEWS);
            builder.appendQueryParameter(Utility.TMDB_API_KEY, BuildConfig.THE_MOVIE_DB_KEY);
            builder.appendQueryParameter(Utility.TMDB_LANGUAGE, "en-us");
            builder.appendQueryParameter(Utility.TMDB_PAGE, "1");

            URL queryUrl = new URL(builder.build().toString());

            conn = (HttpURLConnection) queryUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            is = conn.getInputStream();

            //Error handling
            if (is == null) {
                return null;
            }

            reader = new BufferedReader(new InputStreamReader(is));

            String line;
            StringBuffer buffer = new StringBuffer();

            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            //Error handling
            if (buffer.length() == 0) {
                return null;
            }

            jResult = buffer.toString();

            reviewList = getDataFromJSON(jResult);
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());

        } finally {
            if (conn != null) {
                conn.disconnect();
            }

            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    Log.e(LOG_TAG, e.getMessage());
                }
            }
        }
        return reviewList;
    }

    @Override
    protected void onPostExecute(ArrayList<ReviewItem> reviewList) {
        super.onPostExecute(reviewList);
        if (reviewList != null) {
            response.updateReviews(reviewList);
        }
    }

    private ArrayList<ReviewItem> getDataFromJSON(String jString) throws JSONException {
        ArrayList<ReviewItem> reviewList = new ArrayList<>();

        JSONObject mainObject = new JSONObject(jString);
        JSONArray reviewArray = mainObject.getJSONArray(RESULTS);

        for (int i = 0; i < reviewArray.length(); i++) {
            JSONObject trailer = reviewArray.getJSONObject(i);
            reviewList.add(new ReviewItem(trailer.getString(Utility.TMDB_ID), trailer.getString(
                    Utility.TMDB_AUTHOR), trailer.getString(Utility.TMDB_CONTENT), trailer.getString(Utility.TMDB_URL)));
        }
        return reviewList;
    }

    public interface AsyncResponse {
        void updateReviews(ArrayList<ReviewItem> reviews);
    }
}