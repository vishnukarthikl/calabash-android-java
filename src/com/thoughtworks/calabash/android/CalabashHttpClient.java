package com.thoughtworks.calabash.android;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static com.thoughtworks.calabash.android.CalabashLogger.error;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;

public class CalabashHttpClient {
    private static final String TEST_SERVER_DUMP_URL = "http://localhost:%s/dump";
    private URL url;

    public CalabashHttpClient(CalabashWrapper calabashWrapper) {
        try {
            final int serverPort = parseInt(calabashWrapper.getTestServerPort());
            url = new URL(format(TEST_SERVER_DUMP_URL, serverPort));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (CalabashException e) {
            throw new RuntimeException(e);
        }
    }

    public String getViewDump() {
        String dump = "{}";
        try {
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            final InputStream stream = connection.getInputStream();
            dump = Utils.toString(stream);
        } catch (IOException e) {
            CalabashLogger.error("Could not fetch view dump", e);
        } catch (CalabashException e) {
            error("Could not fetch view dump", e);
        }
        return dump;
    }
}
