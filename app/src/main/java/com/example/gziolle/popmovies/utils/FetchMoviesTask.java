package com.example.gziolle.popmovies.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.example.gziolle.popmovies.BuildConfig;
import com.example.gziolle.popmovies.MovieItem;
import com.example.gziolle.popmovies.MovieListFragment;
import com.example.gziolle.popmovies.R;
import com.example.gziolle.popmovies.data.FavoritesContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by gziolle on 1/17/2017.
 */

public class FetchMoviesTask extends AsyncTask<String, Void, ArrayList<MovieItem>> {

    private static final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

    private static final String RESULTS = "results";
    private static final String RESULT_CODE = "status_code";

    public static int ROW_ID = 0;
    public static int MOVIE_ID = 1;
    public static int MOVIE_TITLE = 2;
    public static int MOVIE_ORIGINAL_TITLE = 3;
    public static int MOVIE_POSTER_PATH = 4;
    public static int MOVIE_OVERVIEW = 5;
    public static int MOVIE_AVERAGE = 6;
    public static int MOVIE_RELEASE_DATE = 7;

    ProgressDialog mProgressDialog;
    private Context mContext = null;
    private AsyncResponse response = null;

    public FetchMoviesTask(Context context, AsyncResponse response) {
        this.mContext = context;
        this.response = response;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setMessage(mContext.getString(R.string.progress_message));
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    @Override
    protected ArrayList<MovieItem> doInBackground(String... params) {
        MovieListFragment.setIsFetching(true);
        //https://api.themoviedb.org/3/movie/popular?api_key=35b914867b56190b284ba099bce2cb35&language=en-US

        HttpURLConnection conn = null;
        InputStream is;
        BufferedReader reader = null;
        String moviesJSONString;
        ArrayList<MovieItem> movieList = new ArrayList<>();

        if (params[0] == null) {
            return null;
        }

        String queryMode = params[0];
        String currentPage = params[1];

        if (mContext.getString(R.string.query_mode_favorites).equals(queryMode)) {
            movieList = getDataFromDatabase();
        }
        //Make a web query to retrieve date for the other options.
        else {
            try {
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("http");
                builder.authority(Utility.TMDB_AUTHORITY);
                builder.appendPath(Utility.TMDB_API_VERSION).appendPath(Utility.TMDB_MOVIE_DIR).appendPath(queryMode);
                builder.appendQueryParameter(Utility.TMDB_API_KEY, BuildConfig.THE_MOVIE_DB_KEY);
                builder.appendQueryParameter(Utility.TMDB_LANGUAGE, Locale.getDefault().getLanguage() + "-" + Locale.getDefault().getCountry());
                builder.appendQueryParameter(Utility.TMDB_PAGE, currentPage);

                URL queryUrl = new URL(builder.build().toString());

                Log.d("Ziolle", queryUrl.toString());

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

                moviesJSONString = buffer.toString();

                movieList = getDataFromJSON(moviesJSONString);

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
        }
        return movieList;
    }

    @Override
    protected void onPostExecute(ArrayList<MovieItem> result) {
        super.onPostExecute(result);
        mProgressDialog.dismiss();
        response.updateMovieList(result);
        MovieListFragment.setIsFetching(false);
    }

    private ArrayList<MovieItem> getDataFromJSON(String JSONString) throws JSONException {
        ArrayList<MovieItem> movieItems = new ArrayList<>();
        JSONObject mainObject = new JSONObject(JSONString);

        if (mainObject.has(RESULT_CODE)) {
            int errorCode = mainObject.getInt(RESULT_CODE);

            switch (errorCode) {
                case Utility.STATUS_SERVER_NEEDS_API_KEY:
                    Utility.storeStatusToSharedPreferences(mContext, Utility.STATUS_SERVER_NEEDS_API_KEY);
                    return null;
                case Utility.STATUS_RESOURCE_NOT_FOUND:
                    Utility.storeStatusToSharedPreferences(mContext, Utility.STATUS_RESOURCE_NOT_FOUND);
                    return null;
                default:
                    Utility.storeStatusToSharedPreferences(mContext, Utility.STATUS_SERVER_DOWN);
                    return null;
            }
        }

        JSONArray moviesArray = mainObject.getJSONArray(RESULTS);

        for (int i = 0; i < moviesArray.length(); i++) {
            JSONObject movie = moviesArray.getJSONObject(i);
            //long _id, String original_title, String posterPath, String overview, double vote_average, String releaseDate
            MovieItem item = new MovieItem(movie.getLong(Utility.TMDB_ID),
                    movie.getString(FavoritesContract.FavoritesEntry.COLUMN_TITLE),
                    movie.getString(FavoritesContract.FavoritesEntry.COLUMN_ORIGINAL_MOVIE_TITLE),
                    movie.getString(FavoritesContract.FavoritesEntry.COLUMN_POSTER_PATH),
                    movie.getString(FavoritesContract.FavoritesEntry.COLUMN_OVERVIEW),
                    movie.getDouble(Utility.VOTE_AVERAGE),
                    movie.getString(FavoritesContract.FavoritesEntry.COLUMN_RELEASE_DATE));
            movieItems.add(item);
        }
        return movieItems;
    }

    private ArrayList<MovieItem> getDataFromDatabase() {

        ArrayList<MovieItem> movieList = new ArrayList<>();
        String[] projection = {FavoritesContract.FavoritesEntry._ID,
                FavoritesContract.FavoritesEntry.COLUMN_MOVIE_ID,
                FavoritesContract.FavoritesEntry.COLUMN_TITLE,
                FavoritesContract.FavoritesEntry.COLUMN_ORIGINAL_MOVIE_TITLE,
                FavoritesContract.FavoritesEntry.COLUMN_POSTER_PATH,
                FavoritesContract.FavoritesEntry.COLUMN_OVERVIEW,
                FavoritesContract.FavoritesEntry.COLUMN_AVERAGE,
                FavoritesContract.FavoritesEntry.COLUMN_RELEASE_DATE};


        Cursor favoriteMovies = mContext.getContentResolver().query(FavoritesContract.FavoritesEntry.CONTENT_URI, projection, null, null, null);

        if (favoriteMovies != null) {
            while (favoriteMovies.moveToNext()) {
                MovieItem item = new MovieItem(favoriteMovies.getInt(MOVIE_ID),
                        favoriteMovies.getString(MOVIE_TITLE),
                        favoriteMovies.getString(MOVIE_ORIGINAL_TITLE),
                        favoriteMovies.getString(MOVIE_POSTER_PATH),
                        favoriteMovies.getString(MOVIE_OVERVIEW),
                        favoriteMovies.getDouble(MOVIE_AVERAGE),
                        favoriteMovies.getString(MOVIE_RELEASE_DATE));
                movieList.add(item);
            }
        }

        if (favoriteMovies != null && !favoriteMovies.isClosed()) {
            favoriteMovies.close();
        }
        return movieList;
    }

    public interface AsyncResponse {
        void updateMovieList(ArrayList<MovieItem> movieList);
    }
}
