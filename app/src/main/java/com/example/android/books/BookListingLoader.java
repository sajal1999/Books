package com.example.android.books;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.List;

public class BookListingLoader
        extends AsyncTaskLoader<List<BookListingQ>>

{

    private String mUrl;

    public BookListingLoader(Context context, String url) {
        super(context);
        mUrl = url;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    /**
     * This is on a background thread.
     */
    @Override
    public List<BookListingQ> loadInBackground() {
        if (mUrl == null) {
            return null;
        }

        // Perform the network request, parse the response, and extract a list of earthquakes.
        List<BookListingQ> books = QueryUtils.extractBooks(mUrl);
        return books;
    }
}
