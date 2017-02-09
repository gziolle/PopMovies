package com.example.gziolle.popmovies;

/**
 * Created by gziolle on 10/18/2016.
 */

public class MovieItem {

    private long _id;
    private String title;
    private String originalTitle;
    private String posterPath;
    private String overview;
    private double average;
    private String releaseDate;


    public MovieItem(long _id, String title, String originalTitle, String posterPath, String overview, double vote_average, String releaseDate) {
        this._id = _id;
        this.title = title;
        this.originalTitle = originalTitle;
        this.posterPath = posterPath;
        this.overview = overview;
        this.average = vote_average;
        this.releaseDate = releaseDate;
    }

    public long get_id() {
        return _id;
    }

    public String getTitle() {
        return title;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public String getOverview() {
        return overview;
    }

    public double getAverage() {
        return average;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }
}
