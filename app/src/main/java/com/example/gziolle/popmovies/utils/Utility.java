package com.example.gziolle.popmovies.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.example.gziolle.popmovies.R;
import com.example.gziolle.popmovies.data.FavoritesContract;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by gziolle on 12/15/2016.
 */

public class Utility {

    public static final String TMDB_AUTHORITY = "api.themoviedb.org";
    public static final String POSTER_PATH_AUTHORITY = "http://image.tmdb.org/t/p/w342";
    public static final String POSTER_PATH_BIG_AUTHORITY = "http://image.tmdb.org/t/p/w500";
    public static final String TMDB_API_VERSION = "3";
    public static final String TMDB_MOVIE_DIR = "movie";
    public static final String TMDB_MOVIE_VIDEOS = "videos";
    public static final String TMDB_MOVIE_REVIEWS = "reviews";
    public static final String TMDB_API_KEY = "api_key";
    public static final String TMDB_LANGUAGE = "language";
    public static final String TMDB_PAGE = "page";

    public static final String TMDB_ID = "id";
    public static final String TMDB_AUTHOR = "author";
    public static final String TMDB_CONTENT = "content";
    public static final String TMDB_URL = "url";

    public static final String VOTE_AVERAGE = "vote_average";

    public static final int STATUS_SERVER_NEEDS_API_KEY = 7;
    public static final int STATUS_RESOURCE_NOT_FOUND = 34;
    public static final int STATUS_SERVER_DOWN = 100;

    public static String savePosterIntoStorage(Bundle bundle, Context context, Bitmap bitmap) {

        String fileName = bundle.getString(FavoritesContract.FavoritesEntry.COLUMN_POSTER_PATH);
        ContextWrapper cw = new ContextWrapper(context);

        //path to /data/data/com.example.gziolle.popmovies/app/data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);

        //Create the file
        File filePath = new File(directory, fileName);

        FileOutputStream fileOutputStream = null;

        try {
            fileOutputStream = new FileOutputStream(filePath);

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } finally {
            try {
                fileOutputStream.close();
            } catch (IOException | NullPointerException ex) {
                ex.printStackTrace();
            }
        }
        return filePath.getAbsolutePath();
    }


    public static Bitmap getImageFromUrl(Context context, String source) {
        Bitmap bitmapPoster = null;
        try {
            if (isConnected(context)) {
                URL url = new URL(source);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();

                InputStream is = connection.getInputStream();
                bitmapPoster = BitmapFactory.decodeStream(is);
            }
            return bitmapPoster;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

    }

    public static boolean insertMovieIntoDB(Bundle bundle, Context context) {
        ContentValues values = new ContentValues();
        values.put(FavoritesContract.FavoritesEntry.COLUMN_MOVIE_ID, String.valueOf(bundle.getLong(FavoritesContract.FavoritesEntry.COLUMN_MOVIE_ID)));
        values.put(FavoritesContract.FavoritesEntry.COLUMN_TITLE, bundle.getString(FavoritesContract.FavoritesEntry.COLUMN_TITLE));
        values.put(FavoritesContract.FavoritesEntry.COLUMN_ORIGINAL_MOVIE_TITLE, bundle.getString(FavoritesContract.FavoritesEntry.COLUMN_ORIGINAL_MOVIE_TITLE));
        values.put(FavoritesContract.FavoritesEntry.COLUMN_POSTER_PATH, bundle.getString(FavoritesContract.FavoritesEntry.COLUMN_POSTER_PATH));
        values.put(FavoritesContract.FavoritesEntry.COLUMN_OVERVIEW, bundle.getString(FavoritesContract.FavoritesEntry.COLUMN_OVERVIEW));
        values.put(FavoritesContract.FavoritesEntry.COLUMN_AVERAGE, String.valueOf(bundle.getDouble(FavoritesContract.FavoritesEntry.COLUMN_AVERAGE)));
        values.put(FavoritesContract.FavoritesEntry.COLUMN_RELEASE_DATE, bundle.getString(FavoritesContract.FavoritesEntry.COLUMN_RELEASE_DATE));

        Uri rowUri = context.getContentResolver().insert(FavoritesContract.FavoritesEntry.CONTENT_URI, values);

        return rowUri != null;

    }

    public static boolean deleteMovieFromDB(Bundle bundle, Context context) {
        String selection = FavoritesContract.FavoritesEntry.TABLE_NAME + "." + FavoritesContract.FavoritesEntry.COLUMN_MOVIE_ID + " = ?";
        String[] selectionArgs = {String.valueOf(bundle.getLong(FavoritesContract.FavoritesEntry.COLUMN_MOVIE_ID))};

        int rowsCount = context.getContentResolver().delete(FavoritesContract.FavoritesEntry.CONTENT_URI, selection, selectionArgs);

        return (rowsCount != -1);
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
    }

    public static void storeStatusToSharedPreferences(Context context, int status) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putInt(context.getString(R.string.pref_status), status);
        editor.commit();
    }

    public static String formatDate(Context context, String date) {

        String[] values = date.split("-");
        int[] intValues = new int[3];

        for (int i = 0; i < intValues.length; i++) {
            intValues[i] = Integer.valueOf(values[i]);
        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(intValues[0], intValues[1], intValues[2]);

        SimpleDateFormat formatter = new SimpleDateFormat(context.getString(R.string.formatted_date));
        return formatter.format(calendar.getTime());

    }
}
