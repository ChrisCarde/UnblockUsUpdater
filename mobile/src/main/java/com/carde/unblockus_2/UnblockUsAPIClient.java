package com.carde.unblockus_2;

/**
 * Created by ccarde on 11/9/16.
 */

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by ccarde on 8/22/16.
 */

public class UnblockUsAPIClient {


    public static final int ACCOUNT_STATE_UNINITIALIZED = 0;
    public static final int ACCOUNT_STATE_BAD_PASSWORD = 1;
    public static final int ACCOUNT_STATE_BAD_EMAIL = 2;
    public static final int ACCOUNT_STATE_ACTIVE = 3;
    public static final int ACCOUNT_STATE_TRIAL = 4;
    public static final int ACCOUNT_STATE_NETWORK_ERROR = 5;

    private static final String API_BASE_URL = "https://api.unblock-us.com/login";
    private String accountEmail;
    private String accountPassword;
    private int accountState = ACCOUNT_STATE_UNINITIALIZED;

    private final String DEBUG_TAG = UnblockUsAPIClient.class.getCanonicalName();

    public UnblockUsAPIClient(String accountEmail, String accountPassword) {
        this.accountEmail = accountEmail;
        this.accountPassword = accountPassword;
    }

    public void update(UpdateResultListener listener) {
        Log.d("UnblockUsAPIClient","update()");
        String urlString = API_BASE_URL + "?" + accountEmail + ":" + accountPassword;
        try {
            URL apiCallUrl = new URL(urlString);
            new APICaller(listener).execute(apiCallUrl);
        } catch (MalformedURLException e) {
            Log.e(DEBUG_TAG,"Bad API url: " + urlString);
        }

    }

    private String downloadUrl(URL url) throws IOException {
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 500;

        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d(DEBUG_TAG, "The response is: " + response);
            is = conn.getInputStream();

            // Convert the InputStream into a string
            String contentAsString = readIt(is, len);
            return contentAsString;

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    // Reads an InputStream and converts it to a String.
    public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }

    class APICaller extends AsyncTask<URL, Void, Void> {
        UpdateResultListener listener;
        String errorMessage = null;

        APICaller(UpdateResultListener listener) {
            this.listener = listener;
        }

        protected Void doInBackground(URL... urls) {
            if (urls.length != 1)
                throw new IllegalArgumentException("Incorrect number of URLs in APICaller: " + urls.length);

            String response;
            try {
                Log.d(DEBUG_TAG,"making network call");
                response = downloadUrl(urls[0]);
                Log.d(DEBUG_TAG,"got reply: " + response);
                if (response.startsWith("active")) {
                    accountState = ACCOUNT_STATE_ACTIVE;
                } else if (response.startsWith("in_trial")) {
                    accountState = ACCOUNT_STATE_TRIAL;
                } else if (response.startsWith("not_found")) {
                    accountState = ACCOUNT_STATE_BAD_EMAIL;
                } else if (response.startsWith("bad_password")) {
                    accountState = ACCOUNT_STATE_BAD_PASSWORD;
                }
            } catch (IOException e) {
                Log.e(DEBUG_TAG,"Error during network requets: " + e.getMessage());
                accountState = ACCOUNT_STATE_UNINITIALIZED;
                errorMessage = e.getMessage();
            }
            return null;
        }

        protected void onPostExecute(Void v) {
            Log.d(DEBUG_TAG,"in onPostExecute()");
            listener.handleUpdateResults(accountState, errorMessage);
        }

    }

    interface UpdateResultListener {
        void handleUpdateResults(int state, String errorMessage);
        void setView(View view);
    }


}
