package com.example.android.books;

import android.app.LoaderManager;

import android.app.LoaderManager.LoaderCallbacks;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

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


public class MainActivity extends AppCompatActivity
        implements LoaderCallbacks<List<BookListingQ>> {
    private static final String REQUEST_URL = "https://www.googleapis.com/books/v1/volumes?q=android&maxResults=1";
    private BookListingAdapter mAdapter;
    private static final int EARTHQUAKE_LOADER_ID = 1;
    public static final String LOG_TAG = MainActivity.class.getName();
    EditText editText;
    ListView listView;
    ImageButton imageButton;

    TextView textNoDataFound;

    static final String SEARCH_RESULTS = "booksSearchResults";
    private TextView mEmptyStateTextView;

    @Override
    public void onLoadFinished(Loader<List<BookListingQ>> loader, List<BookListingQ> books) {
        // Clear the adapter of previous earthquake data
        mAdapter.clear();

        // If there is a valid list of {@link Earthquake}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (books != null && !books.isEmpty()) {
            mAdapter.addAll(books);

        }
    }


    @Override
    public Loader<List<BookListingQ>> onCreateLoader(int i, Bundle bundle) {
        // Create a new loader for the given URL
        return new BookListingLoader(this, REQUEST_URL);
    }

    @Override

    public void onLoaderReset(Loader<List<BookListingQ>> loader) {

        // TODO: Loader reset, so we can clear out our existing data.

    }


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LoaderManager loaderManager = getLoaderManager();

        // Initialize the loader. Pass in the int ID constant defined above and pass in null for
        // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
        // because this activity implements the LoaderCallbacks interface).
        loaderManager.initLoader(EARTHQUAKE_LOADER_ID, null, this);

        editText = (EditText) findViewById(R.id.editText);
        imageButton = (ImageButton) findViewById(R.id.imageButton);
        textNoDataFound = (TextView) findViewById(R.id.text_no_data_found);

        mAdapter = new BookListingAdapter(this, -1);

        listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(mAdapter);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInternetConnectionAvailable()) {
                    BookAsyncTask task = new BookAsyncTask();
                    task.execute();
                } else {
                    Toast.makeText(MainActivity.this, R.string.error_no_internet,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (savedInstanceState != null) {
            BookListingQ[] books = (BookListingQ[]) savedInstanceState.getParcelableArray(SEARCH_RESULTS);
            mAdapter.addAll(books);
        }
    }

    private boolean isInternetConnectionAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork.isConnectedOrConnecting();
    }

    private void updateUi(List<BookListingQ> books) {
        if (books.isEmpty()) {
            // if no books found, show a message
            textNoDataFound.setVisibility(View.VISIBLE);
        } else {
            textNoDataFound.setVisibility(View.GONE);
        }
        mAdapter.clear();
        mAdapter.addAll(books);
    }

    private String getUserInput() {
        return editText.getText().toString();
    }

    private String getUrlForHttpRequest() {
        final String baseUrl = "https://www.googleapis.com/books/v1/volumes?q=search+";
        String formatUserInput = getUserInput().trim().replaceAll("\\s+", "+");
        String url = baseUrl + formatUserInput;
        return url;
    }

    private class BookAsyncTask extends AsyncTask<URL, Void, List<BookListingQ>> {

        @Override
        protected List<BookListingQ> doInBackground(URL... urls) {
            URL url = createURL(getUrlForHttpRequest());
            String jsonResponse = "";

            try {
                jsonResponse = makeHttpRequest(url);
            } catch (IOException e) {
                e.printStackTrace();
            }

            List<BookListingQ> books = parseJson(jsonResponse);
            return books;
        }

        @Override
        protected void onPostExecute(List<BookListingQ> books) {
            if (books == null) {
                return;
            }
            updateUi(books);
        }

        private URL createURL(String stringUrl) {
            try {
                return new URL(stringUrl);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            }
        }

        private String makeHttpRequest(URL url) throws IOException {
            String jsonResponse = "";

            if (url == null) {
                return jsonResponse;
            }

            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;

            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(10000 /* milliseconds */);
                urlConnection.setConnectTimeout(15000 /* milliseconds */);
                urlConnection.connect();
                if (urlConnection.getResponseCode() == 200) {
                    inputStream = urlConnection.getInputStream();
                    jsonResponse = readFromStream(inputStream);
                } else {
                    Log.e("mainActivity", "Error response code: " + urlConnection.getResponseCode());
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            }
            return jsonResponse;
        }

        private String readFromStream(InputStream inputStream) throws IOException {
            StringBuilder output = new StringBuilder();
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null) {
                    output.append(line);
                    line = reader.readLine();
                }
            }
            return output.toString();
        }

        private List<BookListingQ> parseJson(String json) {

            if (json == null) {
                return null;
            }

            List<BookListingQ> books = QueryUtils.extractBooks(json);
            return books;
        }
    }
}