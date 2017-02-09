package com.example.gziolle.popmovies;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.transition.Transition;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.gziolle.popmovies.data.FavoritesContract;
import com.example.gziolle.popmovies.utils.FetchReviewsTask;
import com.example.gziolle.popmovies.utils.FetchTrailersTask;
import com.example.gziolle.popmovies.utils.Utility;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

import android.support.v7.widget.RecyclerView;

/**
 * Created by gziolle on 10/19/2016.
 */

public class DetailFragment extends Fragment implements TrailerAdapter.RecyclerViewClickListener, FetchReviewsTask.AsyncResponse, FetchTrailersTask.AsyncResponse {

    public static final String LOG_TAG = DetailFragment.class.getSimpleName();
    static final String DETAIL_FRAGMENT_TAG = "DFTAG";

    public static ArrayList<TrailerItem> mMovieTrailers = new ArrayList<>();
    ViewGroup mReviewLayout;
    Toolbar mToolbar;
    private RecyclerView.Adapter mTrailerAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(mToolbar);

        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mToolbar.setPadding(0, getStatusBarHeight(), 0, 0);
        }

        ActionBar bar = activity.getSupportActionBar();

        if (bar != null) {
            bar.setDisplayShowTitleEnabled(false);
            bar.setDisplayHomeAsUpEnabled(true);
        }

        RecyclerView trailerRecyclerView = (RecyclerView) rootView.findViewById(R.id.trailer_list);
        RecyclerView.LayoutManager trailerLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        trailerRecyclerView.setLayoutManager(trailerLayoutManager);
        mTrailerAdapter = new TrailerAdapter(getActivity(), mMovieTrailers, this);
        trailerRecyclerView.setAdapter(mTrailerAdapter);

        trailerRecyclerView.setNestedScrollingEnabled(false);



        mReviewLayout = (LinearLayout) rootView.findViewById(R.id.review_list);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            getActivity().getWindow().getSharedElementEnterTransition().addListener(new Transition.TransitionListener() {
                @Override
                public void onTransitionStart(Transition transition) {

                }

                @Override
                public void onTransitionEnd(Transition transition) {
                    if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                        final View gradientView = rootView.findViewById(R.id.gradient_view);

                        ObjectAnimator gradientViewAnim = ObjectAnimator.ofFloat(gradientView, "alpha", 0f, 1f);
                        gradientViewAnim.setInterpolator(new DecelerateInterpolator());
                        gradientView.setVisibility(View.VISIBLE);

                        ObjectAnimator toolbarAnim = ObjectAnimator.ofFloat(mToolbar, "alpha", 0f, 1f);
                        toolbarAnim.setInterpolator(new DecelerateInterpolator());
                        mToolbar.setVisibility(View.VISIBLE);

                        AnimatorSet animatorSet = new AnimatorSet();
                        animatorSet.playTogether(gradientViewAnim, toolbarAnim);
                        animatorSet.start();
                    }
                }

                @Override
                public void onTransitionCancel(Transition transition) {

                }

                @Override
                public void onTransitionPause(Transition transition) {

                }

                @Override
                public void onTransitionResume(Transition transition) {

                }
            });

        }

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            bindView(bundle);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void updateTrailerAndReviewList(Long id) {
        ConnectivityManager manager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        if (networkInfo.isAvailable() && networkInfo.isConnected()) {
            new FetchTrailersTask(getActivity(), this).execute(String.valueOf(id));
            new FetchReviewsTask(this).execute(String.valueOf(id));
        }
    }

    public void bindView(Bundle bundle) {

        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mToolbar.setTitle(bundle.getString(FavoritesContract.FavoritesEntry.COLUMN_TITLE));
        }

        ImageView moviePoster = (ImageView) getActivity().findViewById(R.id.movie_image);
        Bitmap bitmap = bundle.getParcelable("bitmap");

        moviePoster.setImageDrawable(new BitmapDrawable(getResources(), bitmap));
        /*String moviePosterPath = bundle.getString(FavoritesContract.FavoritesEntry.COLUMN_POSTER_PATH);

        if (moviePosterPath != null) {
            if (!moviePosterPath.startsWith("/data")) {
                moviePosterPath = Utility.POSTER_PATH_BIG_AUTHORITY + moviePosterPath;
                Log.d("Ziolle", "url = " + moviePosterPath);
                //Download the image using Picasso API
                Picasso.with(getActivity()).load(moviePosterPath)
                        .error(R.mipmap.ic_launcher).resize(500, 750).centerCrop().into(moviePoster);
            } else {
                File posterFile = new File(moviePosterPath);
                Picasso.with(getActivity()).load(posterFile)
                        .error(R.mipmap.ic_launcher).resize(500, 750).centerCrop().into(moviePoster);
            }
        }*/

        TextView originalTitle = (TextView) getActivity().findViewById(R.id.original_title);
        originalTitle.setText(bundle.getString(FavoritesContract.FavoritesEntry.COLUMN_ORIGINAL_MOVIE_TITLE));

        TextView releaseDate = (TextView) getActivity().findViewById(R.id.release_date);
        String formattedDate = Utility.formatDate(getActivity(), bundle.getString(FavoritesContract.FavoritesEntry.COLUMN_RELEASE_DATE));
        releaseDate.setText(formattedDate);

        TextView voteAverage = (TextView) getActivity().findViewById(R.id.average);
        String average = String.format(getActivity().getResources().getString(R.string.average_note), bundle.getDouble(FavoritesContract.FavoritesEntry.COLUMN_AVERAGE));
        voteAverage.setText(average);

        TextView overview = (TextView) getActivity().findViewById(R.id.overview);
        overview.setText(bundle.getString(FavoritesContract.FavoritesEntry.COLUMN_OVERVIEW));

        try {
            updateTrailerAndReviewList(bundle.getLong(FavoritesContract.FavoritesEntry.COLUMN_MOVIE_ID));
        } catch (NullPointerException nex) {
            Log.e(LOG_TAG, "NullPointerException " + nex.getMessage());
        }
    }

    @Override
    public void recyclerViewListClicked(View v, int position) {
        TrailerItem trailer = mMovieTrailers.get(position);
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(trailer.appUrl));
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(trailer.browserUrl));

        try {
            startActivity(appIntent);
        } catch (ActivityNotFoundException ex) {
            startActivity(browserIntent);
        }
    }

    @Override
    public void updateTrailers(ArrayList<TrailerItem> trailers) {
        if (trailers.size() != 0) {
            TextView trailerListTitle = (TextView) getActivity().findViewById(R.id.trailers_title);
            trailerListTitle.setVisibility(View.VISIBLE);
        }

        if (mMovieTrailers.size() != 0) {
            mMovieTrailers.clear();
        }
        mMovieTrailers.addAll(trailers);
        mTrailerAdapter.notifyDataSetChanged();
    }

    @Override
    public void updateReviews(ArrayList<ReviewItem> reviews) {

        if (reviews.size() != 0) {
            TextView reviewListTitle = (TextView) mReviewLayout.findViewById(R.id.review_title);
            reviewListTitle.setVisibility(View.VISIBLE);
        }

        for (ReviewItem item : reviews) {
            View layout = LayoutInflater.from(getActivity()).inflate(R.layout.review_item, mReviewLayout, false);

            TextView reviewContent = (TextView) layout.findViewById(R.id.review_content);
            reviewContent.setText(item.getContent());

            TextView reviewAuthor = (TextView) layout.findViewById(R.id.review_author);
            String author = String.format(getActivity().getResources().getString(R.string.by_author), item.getAuthor());
            reviewAuthor.setText(author);

            if (mReviewLayout != null) {
                mReviewLayout.addView(layout);
            }
        }
    }

    // A method to find height of the status bar
    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        Log.d("Ziolle", "result = " + result);
        return result;
    }


}

