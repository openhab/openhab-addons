/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.api.BatchMode;
import org.eclipse.jetty.websocket.api.CloseStatus;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.SuspendToken;
import org.eclipse.jetty.websocket.api.UpgradeRequest;
import org.eclipse.jetty.websocket.api.UpgradeResponse;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.api.WebSocketPingPongListener;
import org.eclipse.jetty.websocket.api.WebSocketPolicy;
import org.eclipse.jetty.websocket.api.WriteCallback;
import org.junit.jupiter.api.Test;
import org.openhab.binding.webthing.internal.client.dto.PropertyStatusMessage;

import com.google.gson.Gson;

/**
 *
 *
 * @author Gregor Roth - Initial contribution
 */
@NonNullByDefault
public class WebthingTest {
    private static final Gson GSON = new Gson();

    @Test
    public void testWebthingDescription() throws Exception {
        var httpClient = mock(org.eclipse.jetty.client.HttpClient.class);
        var request = Mocks.mockRequest(null, load("/windsensor_response.json"));
        when(httpClient.newRequest(URI.create("http://example.org:8090"))).thenReturn(request);

        var request2 = Mocks.mockRequest(null, load("/windsensor_property.json"));
        when(httpClient.newRequest(URI.create("http://example.org:8090/properties/windspeed"))).thenReturn(request2);

        var webthing = createTestWebthing("http://example.org:8090", httpClient);
        var metadata = webthing.getThingDescription();
        assertEquals("Wind", metadata.title);
    }

    @Test
    public void testWebthingDescriptionUnsetSchema() throws Exception {
        var httpClient = mock(org.eclipse.jetty.client.HttpClient.class);
        var request = Mocks.mockRequest(null, load("/unsetschema_response.json"));
        when(httpClient.newRequest(URI.create("http://example.org:8090"))).thenReturn(request);

        var request2 = Mocks.mockRequest(null, load("/windsensor_property.json"));
        when(httpClient.newRequest(URI.create("http://example.org:8090/properties/windspeed"))).thenReturn(request2);

        var webthing = createTestWebthing("http://example.org:8090", httpClient);
        var metadata = webthing.getThingDescription();
        assertEquals("Wind", metadata.title);
    }

    @Test
    public void testWebthingDescriptionUNsupportedSchema() throws Exception {
        var httpClient = mock(org.eclipse.jetty.client.HttpClient.class);
        var request = Mocks.mockRequest(null, load("/unknownschema_response.json"));
        when(httpClient.newRequest(URI.create("http://example.org:8090"))).thenReturn(request);

        try {
            createTestWebthing("http://example.org:8090", httpClient);
            fail();
        } catch (IOException e) {
            assertEquals(
                    "unsupported schema (@context parameter) https://www.w3.org/2019/wot/td/v1 (Supported schemas are https://webthings.io/schemas and https://iot.mozilla.org/schemas)",
                    e.getMessage());
        }
    }

    @Test
    public void testReadReadOnlyProperty() throws Exception {
        var httpClient = mock(org.eclipse.jetty.client.HttpClient.class);
        var request = Mocks.mockRequest(null, load("/windsensor_response.json"));
        when(httpClient.newRequest(URI.create("http://example.org:8090"))).thenReturn(request);

        var request2 = Mocks.mockRequest(null, load("/windsensor_property.json"));
        when(httpClient.newRequest(URI.create("http://example.org:8090/properties/windspeed"))).thenReturn(request2);

        var webthing = createTestWebthing("http://example.org:8090", httpClient);

        assertEquals(34.0, webthing.readProperty("windspeed"));
        try {
            webthing.writeProperty("windspeed", 23.0);
            fail();
        } catch (PropertyAccessException e) {
            assertEquals(
                    "could not write windspeed (http://example.org:8090/properties/windspeed) with 23.0. Property is readOnly",
                    e.getMessage());
        }
    }

    @Test
    public void testReadPropertyTest() throws Exception {
        var httpClient = mock(org.eclipse.jetty.client.HttpClient.class);
        var request = Mocks.mockRequest(null, load("/awning_response.json"));
        when(httpClient.newRequest(URI.create("http://example.org:8090/0"))).thenReturn(request);

        var request2 = Mocks.mockRequest(null, load("/awning_property.json"));
        when(httpClient.newRequest(URI.create("http://example.org:8090/0/properties/target_position")))
                .thenReturn(request2);

        var webthing = createTestWebthing("http://example.org:8090/0", httpClient);

        assertEquals(85.0, webthing.readProperty("target_position"));
    }

    @Test
    public void testWriteProperty() throws Exception {
        var httpClient = mock(org.eclipse.jetty.client.HttpClient.class);
        var request = Mocks.mockRequest(null, load("/awning_response.json"));
        when(httpClient.newRequest(URI.create("http://example.org:8090/0"))).thenReturn(request);

        var request2 = Mocks.mockRequest("{\"target_position\":10}", load("/awning_property.json"));
        when(httpClient.newRequest(URI.create("http://example.org:8090/0/properties/target_position")))
                .thenReturn(request2);

        var webthing = createTestWebthing("http://example.org:8090/0", httpClient);
        webthing.writeProperty("target_position", 10);
    }

    @Test
    public void testWritePropertyError() throws Exception {
        var httpClient = mock(org.eclipse.jetty.client.HttpClient.class);
        var request = Mocks.mockRequest(null, load("/awning_response.json"));
        when(httpClient.newRequest(URI.create("http://example.org:8090/0"))).thenReturn(request);

        var request2 = Mocks.mockRequest("{\"target_position\":10}", load("/awning_property.json"), 200, 400);
        when(httpClient.newRequest(URI.create("http://example.org:8090/0/properties/target_position")))
                .thenReturn(request2);

        var webthing = createTestWebthing("http://example.org:8090/0", httpClient);
        try {
            webthing.writeProperty("target_position", 10);
            fail();
        } catch (PropertyAccessException e) {
            assertEquals(
                    "could not write target_position (http://example.org:8090/0/properties/target_position) with 10",
                    e.getMessage());
        }
    }

    @Test
    public void testReadPropertyError() throws Exception {
        var httpClient = mock(org.eclipse.jetty.client.HttpClient.class);
        var request = Mocks.mockRequest(null, load("/windsensor_response.json"));
        when(httpClient.newRequest(URI.create("http://example.org:8090"))).thenReturn(request);

        var request2 = Mocks.mockRequest(null, load("/windsensor_response.json"), 500, 200);
        when(httpClient.newRequest(URI.create("http://example.org:8090/properties/windspeed"))).thenReturn(request2);

        var webthing = createTestWebthing("http://example.org:8090", httpClient);
        try {
            webthing.readProperty("windspeed");
            fail();
        } catch (PropertyAccessException e) {
            assertEquals("could not read windspeed (http://example.org:8090/properties/windspeed)", e.getMessage());
        }
    }

    @Test
    public void testWebSocket() throws Exception {
        var httpClient = mock(org.eclipse.jetty.client.HttpClient.class);
        var request = Mocks.mockRequest(null, load("/awning_response.json"));
        when(httpClient.newRequest(URI.create("http://example.org:8090/0"))).thenReturn(request);

        var request2 = Mocks.mockRequest(null, load("/awning_property.json"));
        when(httpClient.newRequest(URI.create("http://example.org:8090/0/properties/target_position")))
                .thenReturn(request2);

        var errorHandler = new ErrorHandler();
        var webSocketFactory = new TestWebsocketConnectionFactory();
        var webthing = createTestWebthing("http://example.org:8090/0", httpClient, errorHandler, webSocketFactory);

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

        webSocketServerSide.sendCloseToClient();
        assertEquals("websocket closed by peer. ", errorHandler.errorRef.get());
    }

    @Test
    public void testWebSocketReceiveTimout() throws Exception {
        var httpClient = mock(org.eclipse.jetty.client.HttpClient.class);
        var request = Mocks.mockRequest(null, load("/awning_response.json"));
        when(httpClient.newRequest(URI.create("http://example.org:8090/0"))).thenReturn(request);

        var request2 = Mocks.mockRequest(null, load("/awning_property.json"));
        when(httpClient.newRequest(URI.create("http://example.org:8090/0/properties/target_position")))
                .thenReturn(request2);

        var errorHandler = new ErrorHandler();
        var webSocketFactory = new TestWebsocketConnectionFactory();
        var pingPeriod = Duration.ofMillis(300);
        var webthing = createTestWebthing("http://example.org:8090/0", httpClient, errorHandler, webSocketFactory,
                pingPeriod);

        var propertyChangedListenerImpl = new PropertyChangedListenerImpl();
        webthing.observeProperty("target_position", propertyChangedListenerImpl);
        webSocketFactory.webSocketRef.get().ignorePing.set(true);

        try {
            Thread.sleep(pingPeriod.dividedBy(2).toMillis());
        } catch (InterruptedException ignore) {
        }
        assertNull(errorHandler.errorRef.get());

        try {
            Thread.sleep(pingPeriod.multipliedBy(4).toMillis());
        } catch (InterruptedException ignore) {
        }
        assertTrue(errorHandler.errorRef.get().startsWith("connection seems to be broken (last message received at"));
    }

    public static String load(String name) throws Exception {
        return new String(Files.readAllBytes(Paths.get(WebthingTest.class.getResource(name).toURI())));
    }

    public static ConsumedThingImpl createTestWebthing(String uri, HttpClient httpClient) throws IOException {
        return createTestWebthing(uri, httpClient, (String) -> {
        }, new TestWebsocketConnectionFactory());
    }

    public static ConsumedThingImpl createTestWebthing(String uri, HttpClient httpClient, Consumer<String> errorHandler,
            WebSocketConnectionFactory websocketConnectionFactory, Duration pingPeriod) throws IOException {
        return new ConsumedThingImpl(httpClient, URI.create(uri), Executors.newSingleThreadScheduledExecutor(),
                errorHandler, websocketConnectionFactory, pingPeriod);
    }

    public static ConsumedThingImpl createTestWebthing(String uri, HttpClient httpClient, Consumer<String> errorHandler,
            WebSocketConnectionFactory websocketConnectionFactory) throws IOException {
        return createTestWebthing(uri, httpClient, errorHandler, websocketConnectionFactory, Duration.ofSeconds(100));
    }

    public static class TestWebsocketConnectionFactory implements WebSocketConnectionFactory {
        public final AtomicReference<WebSocketImpl> webSocketRef = new AtomicReference<>();

        @Override
        public WebSocketConnection create(URI webSocketURI, ScheduledExecutorService executor,
                Consumer<String> errorHandler, Duration pingPeriod) {
            var webSocketConnection = new WebSocketConnectionImpl(executor, errorHandler, pingPeriod);
            var webSocket = new WebSocketImpl(webSocketConnection);
            webSocketRef.set(webSocket);
            webSocketConnection.onWebSocketConnect(webSocket);
            return webSocketConnection;
        }
    }

    public static final class WebSocketImpl implements Session {
        private final WebSocketListener listener;
        private final WebSocketPingPongListener pongListener;
        public AtomicBoolean ignorePing = new AtomicBoolean(false);

        WebSocketImpl(WebSocketConnectionImpl connection) {
            this.listener = connection;
            this.pongListener = connection;
        }

        @Override
        public void close() {
        }

        @Override
        public void close(@Nullable CloseStatus closeStatus) {
        }

        @Override
        public void close(int statusCode, @Nullable String reason) {
        }

        @Override
        public void disconnect() throws IOException {
        }

        @Override
        public long getIdleTimeout() {
            return 0;
        }

        @Override
        public InetSocketAddress getLocalAddress() {
            return InetSocketAddress.createUnresolved("test", 23);
        }

        @Override
        public WebSocketPolicy getPolicy() {
            return WebSocketPolicy.newClientPolicy();
        }

        @Override
        public String getProtocolVersion() {
            return "1";
        }

        @Override
        public RemoteEndpoint getRemote() {
            return new RemoteEndpoint() {
                @Override
                public void sendBytes(@Nullable ByteBuffer data) throws IOException {
                }

                @Override
                public Future sendBytesByFuture(@Nullable ByteBuffer data) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void sendBytes(@Nullable ByteBuffer data, @Nullable WriteCallback callback) {
                }

                @Override
                public void sendPartialBytes(@Nullable ByteBuffer fragment, boolean isLast) throws IOException {
                }

                @Override
                public void sendPartialString(@Nullable String fragment, boolean isLast) throws IOException {
                }

                @Override
                public void sendPing(@Nullable ByteBuffer applicationData) throws IOException {
                    if (!ignorePing.get()) {
                        pongListener.onWebSocketPong(applicationData);
                    }
                }

                @Override
                public void sendPong(@Nullable ByteBuffer applicationData) throws IOException {
                }

                @Override
                public void sendString(@Nullable String text) throws IOException {
                }

                @Override
                public Future sendStringByFuture(@Nullable String text) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void sendString(@Nullable String text, @Nullable WriteCallback callback) {
                }

                @Override
                public BatchMode getBatchMode() {
                    return BatchMode.AUTO;
                }

                @Override
                public void setBatchMode(@Nullable BatchMode mode) {
                }

                @Override
                public InetSocketAddress getInetSocketAddress() {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void flush() throws IOException {
                }

                @Override
                public int getMaxOutgoingFrames() {
                    return 0;
                }

                @Override
                public void setMaxOutgoingFrames(int maxOutgoingFrames) {
                }
            };
        }

        @Override
        public InetSocketAddress getRemoteAddress() {
            return InetSocketAddress.createUnresolved("test", 12);
        }

        @Override
        public UpgradeRequest getUpgradeRequest() {
            throw new UnsupportedOperationException();
        }

        @Override
        public UpgradeResponse getUpgradeResponse() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isOpen() {
            return false;
        }

        @Override
        public boolean isSecure() {
            return false;
        }

        @Override
        public void setIdleTimeout(long ms) {
        }

        @Override
        public SuspendToken suspend() {
            return new SuspendToken() {

                @Override
                public void resume() {
                }
            };
        }

        public void sendToClient(PropertyStatusMessage message) {
            var data = GSON.toJson(message);
            listener.onWebSocketText(data);
        }

        public void sendCloseToClient() {
            listener.onWebSocketClose(200, "");
        }

        public CompletableFuture<WebSocket> sendPing(String message) {
            if (!ignorePing.get()) {
                var bytes = message.getBytes(StandardCharsets.UTF_8);
                listener.onWebSocketBinary(bytes, 0, bytes.length);
            }
            return CompletableFuture.completedFuture(null);
        }
    }

    private static final class PropertyChangedListenerImpl implements BiConsumer<String, Object> {
        public final AtomicReference<String> propertyNameRef = new AtomicReference<>();
        public final AtomicReference<Object> valueRef = new AtomicReference<>();

        @Override
        public void accept(String propertyName, Object value) {
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
