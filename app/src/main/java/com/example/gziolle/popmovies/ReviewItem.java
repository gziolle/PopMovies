package com.example.gziolle.popmovies;

/**
 * Created by gziolle on 10/26/2016.
 */

public class ReviewItem {

    private String id;
    private String author;
    private String content;
    private String url;

    public ReviewItem(String id, String author, String content, String url) {
        this.id = id;
        this.author = author;
        this.content = content;
        this.url = url;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
