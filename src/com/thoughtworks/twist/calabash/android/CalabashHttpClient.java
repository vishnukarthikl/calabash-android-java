package com.thoughtworks.twist.calabash.android;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static com.thoughtworks.twist.calabash.android.CalabashLogger.error;

public class CalabashHttpClient {
    private static final String TEST_SERVER_DUMP_URL = "http://localhost:34782/dump";
    private URL url;

    public CalabashHttpClient() {
        try {
            url = new URL(TEST_SERVER_DUMP_URL);
        } catch (MalformedURLException e) {
            //should never happen
        }
    }

    public String getViewDump() {
        String dump = "{}";
        try {
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            final InputStream stream = connection.getInputStream();
            dump = IOUtils.toString(stream, "UTF-8");
        } catch (IOException e) {
            error("Could not fetch view dump", e);
        }
        return dump;
    }
}
