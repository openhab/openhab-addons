/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.webthing.internal.client;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;

import com.pgssoft.httpclient.HttpClientMock;

/**
 *
 *
 * @author Gregor Roth - Initial contribution
 */
public class WebthingTest {

    @Test
    public void testWebthingDescription() throws Exception {
        HttpClientMock httpClientMock = new HttpClientMock();
        httpClientMock.onGet("http://example.org:8090").doReturn(load("/windsensor_response.json"));
        httpClientMock.onGet("http://example.org:8090/properties/windspeed")
                .doReturn(load("/windsensor_property.json"));

        var webthing = createTestWebthing("http://example.org:8090", httpClientMock);
        var metadata = webthing.getThingDescription();
        assertEquals("Wind", metadata.title);
    }

    @Test
    public void testReadOnlyTest() throws Exception {
        HttpClientMock httpClientMock = new HttpClientMock();
        httpClientMock.onGet("http://example.org:8090").doReturn(load("/windsensor_response.json"));
        httpClientMock.onGet("http://example.org:8090/properties/windspeed")
                .doReturn(load("/windsensor_property.json"));

        var webthing = createTestWebthing("http://example.org:8090", httpClientMock);

        assertEquals(34.0, webthing.readProperty("windspeed"));
        try {
            webthing.writeProperty("windspeed", 23.0);
            Assert.fail();
        } catch (RuntimeException e) {
            assertEquals(
                    "could not write windspeed (http://example.org:8090/properties/windspeed) with 23.0 windspeed is readOnly",
                    e.getMessage());
        }
    }

    @Test
    public void testReadWriteTest() throws Exception {
        HttpClientMock httpClientMock = new HttpClientMock();
        httpClientMock.onGet("http://example.org:8090/0").doReturn(load("/awning_response.json"));
        httpClientMock.onGet("http://example.org:8090/0/properties/target_position")
                .doReturn(load("/awning_property.json"));
        httpClientMock.onPut("http://example.org:8090/0/properties/target_position")
                .withBody(is("{\"target_position\":10}")).doReturnStatus(200);

        var webthing = createTestWebthing("http://example.org:8090/0", httpClientMock);

        assertEquals(85.0, webthing.readProperty("target_position"));
        webthing.writeProperty("target_position", 10);
    }

    @Test
    public void testWriteError() throws Exception {
        HttpClientMock httpClientMock = new HttpClientMock();
        httpClientMock.onGet("http://example.org:8090/0").doReturn(load("/awning_response.json"));
        httpClientMock.onGet("http://example.org:8090/0/properties/target_position")
                .doReturn(load("/awning_property.json"));
        httpClientMock.onPut("http://example.org:8090/0/properties/target_position")
                .withBody(is("{\"target_position\":10}")).doReturnStatus(400);

        var webthing = createTestWebthing("http://example.org:8090/0", httpClientMock);
        try {
            webthing.writeProperty("target_position", 10);
            Assert.fail();
        } catch (RuntimeException e) {
            assertEquals(
                    "could not write target_position (http://example.org:8090/0/properties/target_position) with 10 Got error response: ",
                    e.getMessage());
        }
    }

    @Test
    public void testReadError() throws Exception {
        HttpClientMock httpClientMock = new HttpClientMock();
        httpClientMock.onGet("http://example.org:8090").doReturn(load("/windsensor_response.json"));
        httpClientMock.onGet("http://example.org:8090/properties/windspeed").doReturnStatus(500);

        var webthing = createTestWebthing("http://example.org:8090", httpClientMock);
        try {
            webthing.readProperty("windspeed");
            Assert.fail();
        } catch (RuntimeException e) {
            assertEquals(
                    "could not read windspeed (http://example.org:8090/properties/windspeed). Got error response: ",
                    e.getMessage());
        }
    }

    public static String load(String name) throws Exception {
        return new String(Files.readAllBytes(Paths.get(WebthingTest.class.getResource(name).toURI())));
    }

    public static ConsumedThingImpl createTestWebthing(String uri, HttpClient httpClient) throws IOException {
        return createTestWebthing(uri, httpClient, new TestWebsocketConnectionFactory());
    }

    public static ConsumedThingImpl createTestWebthing(String uri, HttpClient httpClient,
            WebSocketConnectionFactory websocketConnectionFactory) throws IOException {
        return new ConsumedThingImpl(URI.create(uri), ConnectionListener.EMPTY, httpClient, websocketConnectionFactory);
    }

    public static class TestWebsocketConnectionFactory implements WebSocketConnectionFactory {
        public final Map<String, PropertyChangedListener> listeners = new ConcurrentHashMap<>();

        @Override
        public WebSocketConnection create(@NotNull ConsumedThing webthing, @NotNull URI webSocketURI,
                @NotNull ConnectionListener connectionListener, @NotNull Duration pingPeriod) {
            return new WebSocketConnection() {
                @Override
                public void observeProperty(String propertyName, PropertyChangedListener listener) {
                    listeners.put(propertyName, listener);
                }

                @Override
                public void close() {
                }
            };
        }
    }
}
