package com.thoughtworks.twist.calabash.android.unit;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.thoughtworks.twist.calabash.android.CalabashHttpClient;
import com.thoughtworks.twist.calabash.android.CalabashWrapper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class CalabashHttpClientTest {

    public static final int DEFAULT_PORT = 34777;
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(DEFAULT_PORT);
    @Mock
    public CalabashWrapper calabashWrapper;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        when(calabashWrapper.getTestServerPort()).thenReturn(String.valueOf(DEFAULT_PORT));
    }

    @Test
    public void shouldFetchViewDump() {
        final String expectedBody = "{\"foo\":\"bar\"}";
        stubFor(get(urlEqualTo("/dump"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json;charset=utf-8")
                        .withBody(expectedBody)));

        final CalabashHttpClient calabashHttpClient = new CalabashHttpClient(calabashWrapper);
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

        final CalabashHttpClient calabashHttpClient = new CalabashHttpClient(calabashWrapper);
        final String actualBody = calabashHttpClient.getViewDump();

        assertEquals(expectedBody, actualBody);

    }

}
