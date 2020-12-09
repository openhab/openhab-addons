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
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.openhab.binding.webthing.internal.client.dto.PropertyStatusMessage;

import com.google.gson.Gson;
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
    public void testReadReadOnlyProperty() throws Exception {
        HttpClientMock httpClientMock = new HttpClientMock();
        httpClientMock.onGet("http://example.org:8090").doReturn(load("/windsensor_response.json"));
        httpClientMock.onGet("http://example.org:8090/properties/windspeed")
                .doReturn(load("/windsensor_property.json"));

        var webthing = createTestWebthing("http://example.org:8090", httpClientMock);

        assertEquals(34.0, webthing.readProperty("windspeed"));
        try {
            webthing.writeProperty("windspeed", 23.0);
            fail();
        } catch (RuntimeException e) {
            assertEquals(
                    "could not write windspeed (http://example.org:8090/properties/windspeed) with 23.0 windspeed is readOnly",
                    e.getMessage());
        }
    }

    @Test
    public void testReadPropertyTest() throws Exception {
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
    public void testWritePropertyError() throws Exception {
        HttpClientMock httpClientMock = new HttpClientMock();
        httpClientMock.onGet("http://example.org:8090/0").doReturn(load("/awning_response.json"));
        httpClientMock.onGet("http://example.org:8090/0/properties/target_position")
                .doReturn(load("/awning_property.json"));
        httpClientMock.onPut("http://example.org:8090/0/properties/target_position")
                .withBody(is("{\"target_position\":10}")).doReturnStatus(400);

        var webthing = createTestWebthing("http://example.org:8090/0", httpClientMock);
        try {
            webthing.writeProperty("target_position", 10);
            fail();
        } catch (RuntimeException e) {
            assertEquals(
                    "could not write target_position (http://example.org:8090/0/properties/target_position) with 10 Got error response: ",
                    e.getMessage());
        }
    }

    @Test
    public void testReadPropertyError() throws Exception {
        HttpClientMock httpClientMock = new HttpClientMock();
        httpClientMock.onGet("http://example.org:8090").doReturn(load("/windsensor_response.json"));
        httpClientMock.onGet("http://example.org:8090/properties/windspeed").doReturnStatus(500);

        var webthing = createTestWebthing("http://example.org:8090", httpClientMock);
        try {
            webthing.readProperty("windspeed");
            fail();
        } catch (RuntimeException e) {
            assertEquals(
                    "could not read windspeed (http://example.org:8090/properties/windspeed). Got error response: ",
                    e.getMessage());
        }
    }

    @Test
    public void testWebSocket() throws Exception {
        HttpClientMock httpClientMock = new HttpClientMock();
        httpClientMock.onGet("http://example.org:8090/0").doReturn(load("/awning_response.json"));
        httpClientMock.onGet("http://example.org:8090/0/properties/target_position")
                .doReturn(load("/awning_property.json"));

        var errorHandler = new ErrorHandler();
        var webSocketFactory = new TestWebsocketConnectionFactory();
        var webthing = createTestWebthing("http://example.org:8090/0", httpClientMock, errorHandler, webSocketFactory);

        var propertyChangedListenerImpl = new PropertyChangedListenerImpl();
        webthing.observeProperty("target_position", propertyChangedListenerImpl);

        var webSocketServerSide = webSocketFactory.webSocketRef.get();
        var message = new PropertyStatusMessage();
        message.messageType = "propertyStatus";
        message.data = Map.of("target_position", 33);
        webSocketServerSide.sendToClient(message);

        while (propertyChangedListenerImpl.valueRef.get() == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignore) {
            }
        }
        assertEquals(33.0, propertyChangedListenerImpl.valueRef.get());

        propertyChangedListenerImpl.valueRef.set(null);
        message = new PropertyStatusMessage();
        message.messageType = "propertyStatus";
        message.data = Map.of("target_position", 55);
        webSocketServerSide.sendShuffledMessageToClient(message);

        try {
            Thread.sleep(300);
        } catch (InterruptedException ignore) {
        }

        assertNull(propertyChangedListenerImpl.valueRef.get());

        webSocketServerSide.sendCloseToClient();
        assertEquals("websocket closed by peer. ", errorHandler.errorRef.get());
    }

    public static String load(String name) throws Exception {
        return new String(Files.readAllBytes(Paths.get(WebthingTest.class.getResource(name).toURI())));
    }

    public static ConsumedThingImpl createTestWebthing(String uri, HttpClient httpClient) throws IOException {
        return createTestWebthing(uri, httpClient, (String) -> {
        }, new TestWebsocketConnectionFactory());
    }

    public static ConsumedThingImpl createTestWebthing(String uri, HttpClient httpClient, Consumer<String> errorHandler,
            WebSocketConnectionFactory websocketConnectionFactory) throws IOException {
        return new ConsumedThingImpl(URI.create(uri), errorHandler, httpClient, websocketConnectionFactory,
                Duration.ofMillis(100));
    }

    public static class TestWebsocketConnectionFactory implements WebSocketConnectionFactory {
        public final AtomicReference<WebSocketImpl> webSocketRef = new AtomicReference<>();

        @Override
        public WebSocketConnection create(@NotNull URI webSocketURI, @NotNull Consumer<String> errorHandler,
                @NotNull Duration pingPeriod) {
            var webSocketConnection = new WebSocketConnectionImpl(errorHandler, pingPeriod);
            var webSocket = new WebSocketImpl(webSocketConnection);
            webSocketRef.set(webSocket);
            webSocketConnection.onOpen(webSocket);
            return webSocketConnection;
        }
    }

    public static final class WebSocketImpl implements WebSocket {
        private final WebSocket.Listener listener;

        WebSocketImpl(WebSocket.Listener listener) {
            this.listener = listener;
        }

        public void sendToClient(PropertyStatusMessage message) {
            var data = new Gson().toJson(message);
            listener.onText(this, data.substring(0, 11), false);
            listener.onText(this, data.substring(11, 17), false);
            listener.onText(this, data.substring(17), true);
        }

        public void sendShuffledMessageToClient(PropertyStatusMessage message) {
            var data = new Gson().toJson(message);
            listener.onText(this, data.substring(0, 6), false);
            listener.onText(this, data.substring(17), true);
        }

        public void sendCloseToClient() {
            listener.onClose(this, 200, "");
        }

        public CompletableFuture<WebSocket> sendText(CharSequence data, boolean last) {
            return null;
        }

        public CompletableFuture<WebSocket> sendBinary(ByteBuffer data, boolean last) {
            return null;
        }

        public CompletableFuture<WebSocket> sendPing(ByteBuffer message) {
            listener.onPong(this, message);
            return null;
        }

        public CompletableFuture<WebSocket> sendPong(ByteBuffer message) {
            return null;
        }

        public CompletableFuture<WebSocket> sendClose(int statusCode, String reason) {
            return null;
        }

        public void request(long n) {
        }

        public String getSubprotocol() {
            return null;
        }

        public boolean isOutputClosed() {
            return false;
        }

        public boolean isInputClosed() {
            return false;
        }

        public void abort() {
        }
    }

    private static final class PropertyChangedListenerImpl implements PropertyChangedListener {
        public final AtomicReference<String> propertyNameRef = new AtomicReference<>();
        public final AtomicReference<Object> valueRef = new AtomicReference<>();

        public void onPropertyValueChanged(String propertyName, Object value) {
            propertyNameRef.set(propertyName);
            valueRef.set(value);
        }
    }

    public static class ErrorHandler implements Consumer<String> {
        public final AtomicReference<String> errorRef = new AtomicReference<>();

        @Override
        public void accept(String error) {
            errorRef.set(error);
        }
    }
}
