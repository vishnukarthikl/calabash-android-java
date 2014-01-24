package com.thoughtworks.twist.calabash.android.unit;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.thoughtworks.twist.calabash.android.CalabashHttpClient;
import org.junit.Rule;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;

public class CalabashHttpClientTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(34782);

    @Test
    public void shouldFetchViewDump() {
        final String expectedBody = "{\"foo\":\"bar\"}";
        stubFor(get(urlEqualTo("/dump"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json;charset=utf-8")
                        .withBody(expectedBody)));

        final CalabashHttpClient calabashHttpClient = new CalabashHttpClient();
        final String actualBody = calabashHttpClient.getViewDump();

        assertEquals(expectedBody, actualBody);

    }

    @Test
    public void shouldFetchEmptyIfNotSuccess() {
        final String expectedBody = "{}";
        stubFor(get(urlEqualTo("/dump"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json;charset=utf-8")
                        ));

        final CalabashHttpClient calabashHttpClient = new CalabashHttpClient();
        final String actualBody = calabashHttpClient.getViewDump();

        assertEquals(expectedBody, actualBody);

    }

}
