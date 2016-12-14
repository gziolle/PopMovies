package com.example.gziolle.popmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by gziolle on 12/14/2016.
 */

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    private ArrayList<ReviewItem> mReviewList;
    private Context mContext;

    public ReviewAdapter(Context context, ArrayList<ReviewItem> mMovieReviews) {
        mReviewList = mMovieReviews;
        mContext = context;

    }

    @Override
    public ReviewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.review_item, parent, false);
        return new ReviewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReviewAdapter.ViewHolder holder, int position) {
        ReviewItem item = mReviewList.get(position);
        holder.mContent.setText(item.getContent());

        String author = String.format(mContext.getResources().getString(R.string.by_author), item.getAuthor());
        holder.mAuthor.setText(author);
    }

    @Override
    public int getItemCount() {
        return mReviewList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView mAuthor;
        TextView mContent;

        public ViewHolder(View itemView) {
            super(itemView);
            mAuthor = (TextView) itemView.findViewById(R.id.review_author);
            mContent = (TextView) itemView.findViewById(R.id.review_content);
        }
    }
}
