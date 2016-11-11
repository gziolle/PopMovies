package com.example.gziolle.popmovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by gziolle on 11/9/2016.
 */

public class FavoritesContract {

    public static final String CONTENT_AUTHORITY = "com.example.gziolle.popmovies.provider";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_FAVORITES = "favorites";

    public static final class FavoritesEntry implements BaseColumns {

        public static final String TABLE_NAME = "favorites";
        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_AVERAGE = "avg";
        public static final String COLUMN_RELEASE_DATE = "release_date";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVORITES).build();

        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVORITES;

        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVORITES;

        public static Uri buildFavoritesUri(long _id) {
            return ContentUris.withAppendedId(CONTENT_URI, _id);
        }

        public static Uri buildFavoritesUri() {
            return CONTENT_URI.buildUpon().build();
        }
    }
}