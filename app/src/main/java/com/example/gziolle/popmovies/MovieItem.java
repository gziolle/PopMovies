package com.example.gziolle.popmovies;

/**
 * Created by gziolle on 10/18/2016.
 */

public class MovieItem {

    private static final String AUTHORITY = "http://image.tmdb.org/t/p/w185";


    public long _id;
    public String title;
    public String posterPath;
    public String overview;
    public double average;
    public String releaseDate;


    public MovieItem(long _id, String original_title, String posterPath, String overview, double vote_average, String releaseDate) {
        this._id = _id;
        this.title = original_title;
        this.posterPath = AUTHORITY + posterPath;
        this.overview = overview;
        this.average = vote_average;
        this.releaseDate = releaseDate;
    }


}
