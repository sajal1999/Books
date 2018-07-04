package com.example.android.books;

public class BookListingQ {


    private String mAuthor;
    private String mTitle;


    public BookListingQ(String author, String title) {
        mAuthor = author;
        mTitle = title;

    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getTitle() {
        return mTitle;


    }
}