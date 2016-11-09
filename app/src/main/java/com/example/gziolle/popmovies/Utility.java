package com.example.gziolle.popmovies;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

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
 * Created by gziolle on 10/26/2016.
 */

public class Utility {

    static class FetchReviewsTask extends AsyncTask<String, Void, ArrayList<String>> {

        private static final String RESULTS = "results";
        private static final String KEY = "key";
        private final String LOG_TAG = FetchReviewsTask.class.getSimpleName();
        private String TMDB_AUTHORITY = "api.themoviedb.org";
        private String TMDB_API_VERSION = "3";
        private String TMDB_MOVIE_DIR = "movie";
        private String TMDB_MOVIE_VIDEOS = "reviews";
        private String TMDB_API_KEY = "api_key";
        private String TMDB_LANGUAGE = "language";

        @Override
        protected ArrayList<String> doInBackground(String... params) {

            HttpURLConnection conn = null;
            InputStream is;
            BufferedReader reader = null;
            String jResult;
            ArrayList<String> reviewList = new ArrayList<>();

            if (params[0] == null) {
                return null;
            }

            try {
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("http");
                builder.authority(TMDB_AUTHORITY);
                builder.appendPath(TMDB_API_VERSION).appendPath(TMDB_MOVIE_DIR).appendPath(params[1]).appendPath(TMDB_MOVIE_VIDEOS);
                builder.appendQueryParameter(TMDB_API_KEY, BuildConfig.THE_MOVIE_DB_KEY);
                builder.appendQueryParameter(TMDB_LANGUAGE, "en-us");

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
        protected void onPostExecute(ArrayList<String> reviewList) {
            super.onPostExecute(reviewList);
        }

        private ArrayList<String> getDataFromJSON(String jString) throws JSONException {
            ArrayList<String> reviewList = new ArrayList<>();

            JSONObject mainObject = new JSONObject(jString);
            JSONArray trailerArray = mainObject.getJSONArray(RESULTS);

            for (int i = 0; i < trailerArray.length(); i++) {
                JSONObject trailer = trailerArray.getJSONObject(i);
                reviewList.add(trailer.getString(KEY));
            }
            return reviewList;
        }
    }
}
