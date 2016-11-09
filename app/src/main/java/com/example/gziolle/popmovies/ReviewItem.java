package com.example.gziolle.popmovies;

/**
 * Created by gziolle on 10/26/2016.
 */

public class ReviewItem {

    public String id;
    public String author;
    public String sneakPeek;
    public String url;

    public ReviewItem(String id, String author, String sneakPeek, String url) {
        this.id = id;
        this.author = author;
        this.sneakPeek = sneakPeek;
        this.url = url;
    }
}
