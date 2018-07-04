package com.example.android.books;

import android.text.TextUtils;


import android.util.Log;





import org.json.JSONArray;


import org.json.JSONException;


import org.json.JSONObject;





import java.io.BufferedReader;


import java.io.IOException;


import java.io.InputStream;


import java.io.InputStreamReader;


import java.net.HttpURLConnection;


import java.net.MalformedURLException;


import java.net.URL;


import java.nio.charset.Charset;


import java.util.ArrayList;


import java.util.List;
public final class QueryUtils {


    private QueryUtils() {


    }


    public static String formatListOfAuthors(JSONArray authorsList) throws JSONException {

        String authorsListInString = null;

        if (authorsList.length() == 0) {
            return null;
        }

        for (int i = 0; i < authorsList.length(); i++){
            if (i == 0) {
                authorsListInString = authorsList.getString(0);
            } else {
                authorsListInString += ", " + authorsList.getString(i);
            }
        }

        return authorsListInString;
    }


    public static List<BookListingQ> extractBooks(String json) {

        List<BookListingQ> books = new ArrayList<>();

        try {
            JSONObject jsonResponse = new JSONObject(json);

            if (jsonResponse.getInt("totalItems") == 0) {
                return books;
            }
            JSONArray jsonArray = jsonResponse.getJSONArray("items");

            for (int i = 0; i < jsonArray.length(); i++){
                JSONObject bookObject = jsonArray.getJSONObject(i);

                JSONObject bookInfo = bookObject.getJSONObject("volumeInfo");

                String title = bookInfo.getString("title");
                JSONArray authorsArray = bookInfo.getJSONArray("authors");
                String authors = formatListOfAuthors(authorsArray);

                BookListingQ book = new BookListingQ(authors, title);
                books.add(book);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return books;
    }
}
