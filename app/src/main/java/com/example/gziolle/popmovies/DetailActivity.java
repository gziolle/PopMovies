package com.example.gziolle.popmovies;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.gziolle.popmovies.utils.Utility;
import com.getbase.floatingactionbutton.FloatingActionButton;

import com.example.gziolle.popmovies.data.FavoritesContract;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

/**
 * Created by gziolle on 10/18/2016.
 */

public class DetailActivity extends AppCompatActivity {

    public static Bundle mBundle;
    static Context mContext;
    private static boolean isFavorite = false;
    FloatingActionButton shareFloatingActionButton;
    FloatingActionButton favoriteFloatingActionButton;
    FloatingActionsMenu actionsMenu;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();

        mBundle = new Bundle();

        if (intent != null) {
            mBundle.putLong(FavoritesContract.FavoritesEntry.COLUMN_MOVIE_ID, intent.getLongExtra(FavoritesContract.FavoritesEntry.COLUMN_MOVIE_ID, 0));
            mBundle.putString(FavoritesContract.FavoritesEntry.COLUMN_TITLE, intent.getStringExtra(FavoritesContract.FavoritesEntry.COLUMN_TITLE));
            mBundle.putString(FavoritesContract.FavoritesEntry.COLUMN_POSTER_PATH, intent.getStringExtra(FavoritesContract.FavoritesEntry.COLUMN_POSTER_PATH));
            mBundle.putString(FavoritesContract.FavoritesEntry.COLUMN_RELEASE_DATE, intent.getStringExtra(FavoritesContract.FavoritesEntry.COLUMN_RELEASE_DATE));
            mBundle.putString(FavoritesContract.FavoritesEntry.COLUMN_OVERVIEW, intent.getStringExtra(FavoritesContract.FavoritesEntry.COLUMN_OVERVIEW));
            mBundle.putDouble(FavoritesContract.FavoritesEntry.COLUMN_AVERAGE, intent.getDoubleExtra(FavoritesContract.FavoritesEntry.COLUMN_AVERAGE, 0));
        }

        if (savedInstanceState == null) {
            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(mBundle);
            getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).commit();
        }

        shareFloatingActionButton = (FloatingActionButton) findViewById(R.id.share_action_button);
        favoriteFloatingActionButton = (FloatingActionButton) findViewById(R.id.favorite_action_button);
        actionsMenu = (FloatingActionsMenu) findViewById(R.id.floating_menu);

        String[] projection = {FavoritesContract.FavoritesEntry.COLUMN_MOVIE_ID};
        String[] selectionArgs = {String.valueOf(mBundle.getLong(FavoritesContract.FavoritesEntry.COLUMN_MOVIE_ID))};
        String selection = FavoritesContract.FavoritesEntry.TABLE_NAME + "." + FavoritesContract.FavoritesEntry.COLUMN_MOVIE_ID + " = ?";
        Cursor retCursor = getContentResolver().query(FavoritesContract.FavoritesEntry.CONTENT_URI, projection, selection, selectionArgs, null);

        if (retCursor != null && retCursor.moveToFirst()) {
            isFavorite = true;
            favoriteFloatingActionButton.setTitle(getString(R.string.remove_favorite));
        } else {
            isFavorite = false;
            favoriteFloatingActionButton.setTitle(getString(R.string.add_favorite));
        }
        if (retCursor != null && !retCursor.isClosed()) {
            retCursor.close();
        }

        favoriteFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionsMenu.collapse();
                if (isFavorite) {
                    //delete movie from the database
                    if (Utility.deleteMovieFromDB(mBundle, mContext)) {
                        Toast.makeText(mContext, "Removed from favorites list", Toast.LENGTH_SHORT).show();
                        isFavorite = false;
                        favoriteFloatingActionButton.setTitle(getString(R.string.add_favorite));
                    } else {
                        Toast.makeText(mContext, "Couldn't delete this movie from the favorite's list", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    String posterUrl = mBundle.getString(FavoritesContract.FavoritesEntry.COLUMN_POSTER_PATH);
                    if (posterUrl != null) {
                        new DownloadImageTask(mContext).execute(posterUrl);
                    }
                }
            }
        });

        shareFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String test = "Share this!";
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, DetailFragment.mMovieTrailers.get(0).browserUrl);
                Intent chooserIntent = Intent.createChooser(intent, test);
                startActivity(chooserIntent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivityForResult(intent, 1);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    class DownloadImageTask extends AsyncTask<String, Void, String> {

        private Context mContext;

        DownloadImageTask(Context context) {
            this.mContext = context;
        }

        @Override
        protected String doInBackground(String... strings) {

            String imageSource = strings[0];
            String filePath = null;

            if (!imageSource.startsWith("/data")) {
                imageSource = Utility.POSTER_PATH_AUTHORITY + imageSource;
            }

            Bitmap poster = Utility.getImageFromUrl(getApplicationContext(), imageSource);
            if (poster != null) {
                filePath = Utility.savePosterIntoStorage(mBundle, mContext, poster);
                Log.d("Ziolle", "posterUrl = " + filePath);
            }
            return filePath;
        }

        @Override
        protected void onPostExecute(String filePath) {
            super.onPostExecute(filePath);
            if (filePath != null) {
                mBundle.putString(FavoritesContract.FavoritesEntry.COLUMN_POSTER_PATH, filePath);
            }
            if (Utility.insertMovieIntoDB(mBundle, mContext)) {
                Toast.makeText(mContext, "Movie added as a favorite", Toast.LENGTH_SHORT).show();
                isFavorite = true;
                favoriteFloatingActionButton.setTitle(getString(R.string.remove_favorite));
            } else {
                Toast.makeText(mContext, "Couldn't save this movie as a favorite", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
