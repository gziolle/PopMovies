package com.example.gziolle.popmovies;

/**
 * Created by gziolle on 10/26/2016.
 */

public class TrailerItem {
    private static final String BROWSER_URL = "http://www.youtube.com/watch?v=";
    private static final String APP_URL = "vnd.youtube:";

    public String movieKey;
    public String trailerName;
    public String browserUrl;
    public String appUrl;

    public TrailerItem(String movieKey, String trailerName) {
        this.movieKey = movieKey;
        this.trailerName = trailerName;

        this.browserUrl = BROWSER_URL + this.movieKey;
        this.appUrl = APP_URL + this.movieKey;
    }
}
