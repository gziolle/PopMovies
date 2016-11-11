package com.example.gziolle.popmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by gziolle on 11/9/2016.
 */

public class FavoritesDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "movies.db";
    private static final int DATABASE_VERSION = 1;


    private static final String CREATE_FAVORITES_TABLE = "CREATE TABLE " + FavoritesContract.FavoritesEntry.TABLE_NAME + " (" +
            FavoritesContract.FavoritesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            FavoritesContract.FavoritesEntry.COLUMN_MOVIE_ID + " TEXT NOT NULL," +
            FavoritesContract.FavoritesEntry.COLUMN_TITLE + " TEXT NOT NULL," +
            FavoritesContract.FavoritesEntry.COLUMN_POSTER_PATH + " TEXT NOT NULL," +
            FavoritesContract.FavoritesEntry.COLUMN_OVERVIEW + " TEXT NOT NULL," +
            FavoritesContract.FavoritesEntry.COLUMN_AVERAGE + " REAL NOT NULL," +
            FavoritesContract.FavoritesEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL);";

    public FavoritesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_FAVORITES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + FavoritesContract.FavoritesEntry.TABLE_NAME + ";");
        onCreate(sqLiteDatabase);
    }
}