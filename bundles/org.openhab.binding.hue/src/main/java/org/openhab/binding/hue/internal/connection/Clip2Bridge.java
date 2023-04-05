/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.hue.internal.connection;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.core.MediaType;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http.MetaData;
import org.eclipse.jetty.http.MetaData.Response;
import org.eclipse.jetty.http2.api.Session;
import org.eclipse.jetty.http2.api.Stream;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.frames.DataFrame;
import org.eclipse.jetty.http2.frames.GoAwayFrame;
import org.eclipse.jetty.http2.frames.HeadersFrame;
import org.eclipse.jetty.http2.frames.PingFrame;
import org.eclipse.jetty.http2.frames.ResetFrame;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.Promise.Completable;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.hue.internal.dto.CreateUserRequest;
import org.openhab.binding.hue.internal.dto.SuccessResponse;
import org.openhab.binding.hue.internal.dto.clip2.BridgeConfig;
import org.openhab.binding.hue.internal.dto.clip2.Event;
import org.openhab.binding.hue.internal.dto.clip2.Resource;
import org.openhab.binding.hue.internal.dto.clip2.ResourceReference;
import org.openhab.binding.hue.internal.dto.clip2.Resources;
import org.openhab.binding.hue.internal.dto.clip2.enums.ResourceType;
import org.openhab.binding.hue.internal.exceptions.ApiException;
import org.openhab.binding.hue.internal.exceptions.HttpUnauthorizedException;
import org.openhab.binding.hue.internal.handler.Clip2BridgeHandler;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * This class handles HTTP and SSE connections to/from a Hue Bridge running CLIP 2.
 *
 * It uses the following connection mechanisms:
 *
 * <li>The primary communication uses HTTP 2 streams over a shared permanent HTTP 2 session.</li>
 * <li>The 'registerApplicationKey()' method uses HTTP/1.1 over the OH common Jetty client.</li>
 * <li>The 'isClip2Supported()' static method uses HTTP/1.1 over the OH common Jetty client via 'HttpUtil'.</li>
 *
 * @author Andrew Fiddian-Green - Initial Contribution
 */
@NonNullByDefault
public class Clip2Bridge implements Closeable {

    /**
     * Base (abstract) adapter for listening to HTTP 2 stream events.
     *
     * It implements a CompletableFuture by means of which the caller can wait for the response data to come in. And
     * which, in the case of fatal errors, gets completed exceptionally.
     *
     * It handles the following fatal error events by notifying the containing class:
     *
     * <li>onHeaders() HTTP unauthorized codes</li>
     */
    private abstract class BaseStreamListenerAdapter<T> extends Stream.Listener.Adapter {
        protected final CompletableFuture<T> completable = new CompletableFuture<T>();
        protected byte[] cachedBytes = new byte[0];
        private String contentType = "UNDEFINED";

        protected T awaitResult() throws ExecutionException, InterruptedException, TimeoutException {
            return completable.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        }

        /**
         * Return the concatenation of the bytes from the given byte array and the given ByteBuffer.
         *
         * @param firstPart the array that will become the first part of the result.
         * @param secondPart the buffer whose bytes will become the second part of the result.
         * @return a byte array containing the concatenation of the first and second parts.
         */
        protected byte[] combineBytes(byte[] firstPart, ByteBuffer secondPart) {
            byte[] result = new byte[firstPart.length + secondPart.capacity()];
            System.arraycopy(firstPart, 0, result, 0, firstPart.length);
            secondPart.position(0).get(result, firstPart.length, secondPart.capacity());
            return result;
        }

        /**
         * Return the HTTP content type.
         *
         * @return content type e.g. 'application/json'
         */
        protected String getContentType() {
            return contentType;
        }

        protected void handleHttp2Error(Http2Error error) {
            Http2Exception e = new Http2Exception(error);
            if (Http2Error.UNAUTHORIZED.equals(error)) {
                // for external error handling, abstract authorization errors into a separate exception
                completable.completeExceptionally(new HttpUnauthorizedException("HTTP 2 request not authorized"));
            } else {
                completable.completeExceptionally(e);
            }
            fatalErrorDelayed(this, e);
        }

        /**
         * Check the reply headers to see whether the request was authorised.
         */
        @Override
        public void onHeaders(@Nullable Stream stream, @Nullable HeadersFrame frame) {
            Objects.requireNonNull(frame);
            MetaData metaData = frame.getMetaData();
            if (metaData.isResponse()) {
                Response responseMetaData = (Response) metaData;
                int httpStatus = responseMetaData.getStatus();
                switch (httpStatus) {
                    case HttpStatus.UNAUTHORIZED_401:
                    case HttpStatus.FORBIDDEN_403:
                        handleHttp2Error(Http2Error.UNAUTHORIZED);
                    default:
                }
                contentType = responseMetaData.getFields().get(HttpHeader.CONTENT_TYPE).toLowerCase();
            }
        }
    }

    /**
     * Adapter for listening to regular HTTP 2 GET/PUT request stream events.
     *
     * It assembles the incoming text data into an HTTP 'content' entity. And when the last data frame arrives, it
     * returns the full content by completing the CompletableFuture with that data.
     *
     * In addition to those handled by the parent, it handles the following fatal error events by notifying the
     * containing class:
     *
     * <li>onIdleTimeout()</li>
     * <li>onTimeout()</li>
     */
    private class ContentStreamListenerAdapter extends BaseStreamListenerAdapter<String> {

        @Override
        public void onData(@Nullable Stream stream, @Nullable DataFrame frame, @Nullable Callback callback) {
            Objects.requireNonNull(frame);
            Objects.requireNonNull(callback);
            synchronized (this) {
                byte[] receivedBytes = combineBytes(cachedBytes, frame.getData());
                if (frame.isEndStream() && !completable.isDone()) {
                    completable.complete(new String(receivedBytes, StandardCharsets.UTF_8).trim());
                    if (cachedBytes.length > 0) {
                        cachedBytes = new byte[0];
                    }
                } else {
                    cachedBytes = receivedBytes;
                }
            }
            callback.succeeded();
        }

        @Override
        public boolean onIdleTimeout(@Nullable Stream stream, @Nullable Throwable x) {
            handleHttp2Error(Http2Error.IDLE);
            return true;
        }

        @Override
        public void onTimeout(@Nullable Stream stream, @Nullable Throwable x) {
            handleHttp2Error(Http2Error.TIMEOUT);
        }
    }

    /**
     * Adapter for listening to SSE event stream events.
     *
     * It receives the incoming text lines. Receipt of the first data line causes the CompletableFuture to complete. It
     * then parses subsequent data according to the SSE specification. If the line starts with a 'data:' message, it
     * adds the data to the list of strings. And if the line is empty (i.e. the last line of an event), it passes the
     * full set of strings to the owner via a call-back method.
     *
     * The stream must be permanently connected, so it ignores onIdleTimeout() events.
     *
     * The parent class handles most fatal errors, but since the event stream is supposed to be permanently connected,
     * the following events are also considered as fatal:
     *
     * <li>onClosed()</li>
     * <li>onReset()</li>
     */
    private class EventStreamListenerAdapter extends BaseStreamListenerAdapter<Boolean> {
        @Override
        public void onClosed(@Nullable Stream stream) {
            handleHttp2Error(Http2Error.CLOSED);
        }

        @Override
        public void onData(@Nullable Stream stream, @Nullable DataFrame frame, @Nullable Callback callback) {
            Objects.requireNonNull(frame);
            Objects.requireNonNull(callback);
            if (!completable.isDone()) {
                completable.complete(Boolean.TRUE);
            }
            synchronized (this) {
                byte[] receivedBytes = combineBytes(cachedBytes, frame.getData());
                String receivedString = new String(receivedBytes, StandardCharsets.UTF_8);
                String[] receivedLines = receivedString.split("\\R", -1);
                int receivedLineCount = receivedLines.length;

                // two blank lines mark the end of an SSE packet
                boolean endOfPacket = (receivedLineCount > 1) && receivedLines[receivedLineCount - 2].isBlank()
                        && receivedLines[receivedLineCount - 1].isBlank();

                if (endOfPacket) {
                    if (cachedBytes.length > 0) {
                        cachedBytes = new byte[0];
                    }
                    // append any 'data' field values to the event message
                    StringBuilder eventMessage = new StringBuilder();
                    for (String receivedLine : receivedLines) {
                        if (receivedLine.startsWith("data:")) {
                            String dataFieldValue = receivedLine.substring(5).trim();
                            if (!eventMessage.isEmpty()) {
                                eventMessage.append("\n");
                            }
                            eventMessage.append(dataFieldValue);
                        }
                    }
                    if (!eventMessage.isEmpty()) {
                        onEventData(eventMessage.toString());
                    }
                } else {
                    cachedBytes = receivedBytes;
                }
            }
            callback.succeeded();
        }

        @Override
        public boolean onIdleTimeout(@Nullable Stream stream, @Nullable Throwable x) {
            return false;
        }

        @Override
        public void onReset(@Nullable Stream stream, @Nullable ResetFrame frame) {
            handleHttp2Error(Http2Error.RESET);
        }
    }

    /**
     * Enum of potential fatal HTTP 2 session/stream errors.
     */
    private enum Http2Error {
        CLOSED,
        FAILURE,
        TIMEOUT,
        RESET,
        IDLE,
        GO_AWAY,
        UNAUTHORIZED;
    }

    /**
     * Private exception for handling HTTP 2 stream and session errors.
     */
    @SuppressWarnings("serial")
    private static class Http2Exception extends ApiException {
        public final Http2Error error;

        public Http2Exception(Http2Error error) {
            this(error, null);
        }

        public Http2Exception(Http2Error error, @Nullable Throwable cause) {
            super("HTTP 2 stream " + error.toString().toLowerCase(), cause);
            this.error = error;
        }
    }

    /**
     * Adapter for listening to HTTP 2 session status events.
     *
     * The session must be permanently connected, so it ignores onIdleTimeout() events.
     * It also handles the following fatal events by notifying the containing class:
     *
     * <li>onClose()</li>
     * <li>onFailure()</li>
     * <li>onGoAway()</li>
     * <li>onReset()</li>
     */
    private class SessionListenerAdapter extends Session.Listener.Adapter {

        @Override
        public void onClose(@Nullable Session session, @Nullable GoAwayFrame frame) {
            fatalErrorDelayed(this, new Http2Exception(Http2Error.CLOSED));
        }

        @Override
        public void onFailure(@Nullable Session session, @Nullable Throwable failure) {
            fatalErrorDelayed(this, new Http2Exception(Http2Error.FAILURE));
        }

        @Override
        public void onGoAway(@Nullable Session session, @Nullable GoAwayFrame frame) {
            fatalErrorDelayed(this, new Http2Exception(Http2Error.GO_AWAY));
        }

        @Override
        public boolean onIdleTimeout(@Nullable Session session) {
            return false;
        }

        @Override
        public void onPing(@Nullable Session session, @Nullable PingFrame frame) {
            checkAliveOk();
            if (Objects.nonNull(session) && Objects.nonNull(frame) && !frame.isReply()) {
                session.ping(new PingFrame(true), Callback.NOOP);
            }
        }

        @Override
        public void onReset(@Nullable Session session, @Nullable ResetFrame frame) {
            fatalErrorDelayed(this, new Http2Exception(Http2Error.RESET));
        }
    }

    /**
     * Enum showing the online state of the session connection.
     */
    private static enum State {
        CLOSED, // session closed
        PASSIVE, // session open for HTTP calls only
        ACTIVE; // session open for HTTP calls and actively receiving SSE events
    }

    private static final String APPLICATION_ID = "org-openhab-binding-hue-clip2";
    private static final String APPLICATION_KEY = "hue-application-key";

    private static final String EVENT_STREAM_ID = "eventStream";
    private static final String FORMAT_URL_CONFIG = "http://%s/api/0/config";
    private static final String FORMAT_URL_RESOURCE = "https://%s/clip/v2/resource/";
    private static final String FORMAT_URL_REGISTER = "http://%s/api";
    private static final String FORMAT_URL_EVENTS = "https://%s/eventstream/clip/v2";

    private static final long CLIP2_MINIMUM_VERSION = 1948086000L;

    public static final int TIMEOUT_SECONDS = 10;
    private static final int CHECK_ALIVE_SECONDS = 300;
    private static final int REQUEST_INTERVAL_MILLISECS = 100;

    private static final ResourceReference BRIDGE = new ResourceReference().setType(ResourceType.BRIDGE);

    /**
     * Static method to attempt to connect to a Hue Bridge, get its software version, and check if it is high enough to
     * support the CLIP 2 API.
     *
     * @param hostName the bridge IP address.
     * @return true if bridge is online and it supports CLIP 2, or false if it is online and does not support CLIP 2.
     * @throws IOException if unable to communicate with the bridge.
     * @throws NumberFormatException if the bridge firmware version is invalid.
     */
    public static boolean isClip2Supported(String hostName) throws IOException {
        String response;
        Properties headers = new Properties();
        headers.put(HttpHeader.ACCEPT, MediaType.APPLICATION_JSON);
        response = HttpUtil.executeUrl("GET", String.format(FORMAT_URL_CONFIG, hostName), headers, null, null,
                TIMEOUT_SECONDS * 1000);
        BridgeConfig config = new Gson().fromJson(response, BridgeConfig.class);
        if (Objects.nonNull(config)) {
            String swVersion = config.swversion;
            if (Objects.nonNull(swVersion)) {
                if (Long.parseLong(swVersion) >= CLIP2_MINIMUM_VERSION) {
                    return true;
                }
            }
        }
        return false;
    }

    private final Logger logger = LoggerFactory.getLogger(Clip2Bridge.class);

    private final HttpClient httpClient;
    private final HTTP2Client http2Client;
    private final String hostName;
    private final String baseUrl;
    private final String eventUrl;
    private final String registrationUrl;
    private final String applicationKey;
    private final Clip2BridgeHandler bridgeHandler;
    private final Gson jsonParser = new Gson();

    private boolean restarting = false;
    private State onlineState = State.CLOSED;
    private Instant lastRequestTime = Instant.MIN;
    private Instant sessionExpireTime = Instant.MAX;
    private @Nullable Session http2Session;
    private @Nullable ScheduledFuture<?> checkAliveTask;

    /**
     * Constructor.
     *
     * @param httpClientFactory the OH core HttpClientFactory.
     * @param bridgeHandler the bridge handler.
     * @param hostName the host name (ip address) of the Hue bridge
     * @param applicationKey the application key.
     */
    public Clip2Bridge(HttpClientFactory httpClientFactory, Clip2BridgeHandler bridgeHandler, String hostName,
            String applicationKey) {
        logger.debug("Clip2Bridge()");
        httpClient = httpClientFactory.getCommonHttpClient();
        http2Client = httpClientFactory.createHttp2Client("hue-clip2", httpClient.getSslContextFactory());
        http2Client.setConnectTimeout(Clip2Bridge.TIMEOUT_SECONDS * 1000);
        http2Client.setIdleTimeout(-1);
        this.bridgeHandler = bridgeHandler;
        this.hostName = hostName;
        this.applicationKey = applicationKey;
        baseUrl = String.format(FORMAT_URL_RESOURCE, hostName);
        eventUrl = String.format(FORMAT_URL_EVENTS, hostName);
        registrationUrl = String.format(FORMAT_URL_REGISTER, hostName);
    }

    /**
     * Send a ping to the Hue bridge to check that the session is still alive.
     */
    private void checkAlive() {
        if (onlineState == State.CLOSED) {
            return;
        }
        logger.debug("checkAlive()");
        Session session = http2Session;
        if (Objects.nonNull(session)) {
            session.ping(new PingFrame(false), Callback.NOOP);
        }
        if (Instant.now().isAfter(sessionExpireTime)) {
            fatalError(this, new Http2Exception(Http2Error.TIMEOUT));
        }
    }

    /**
     * Connection is ok, so reschedule the session check alive expire time. Called in response to incoming ping frames
     * from the bridge.
     */
    protected void checkAliveOk() {
        logger.debug("checkAliveOk()");
        sessionExpireTime = Instant.now().plusSeconds(CHECK_ALIVE_SECONDS * 2);
    }

    /**
     * Close the connection.
     */
    @Override
    public void close() {
        synchronized (this) {
            logger.debug("close()");
            boolean notifyHandler = (onlineState == State.ACTIVE) && !restarting;
            onlineState = State.CLOSED;
            closeCheckAliveTask();
            closeSession();
            try {
                http2Client.stop();
            } catch (Exception e) {
                // ignore
            }
            if (notifyHandler) {
                bridgeHandler.onConnectionOffline();
            }
        }
    }

    /**
     * Close the check alive task if necessary.
     */
    private void closeCheckAliveTask() {
        logger.debug("closeCheckAliveTask()");
        ScheduledFuture<?> task = checkAliveTask;
        if (Objects.nonNull(task)) {
            task.cancel(true);
        }
        checkAliveTask = null;
    }

    /**
     * Close the HTTP 2 session if necessary.
     */
    private void closeSession() {
        logger.debug("closeSession()");
        Session session = http2Session;
        if (Objects.nonNull(session)) {
            session.close(0, null, Callback.NOOP);
        }
        http2Session = null;
    }

    /**
     * Method that is called back in case of fatal stream or session events. Note: under normal operation, the Hue
     * Bridge sends a 'soft' GO_AWAY command every nine or ten hours, so we handle such soft errors by attempting to
     * silently close and re-open the connection without notifying the handler of an actual 'hard' error.
     *
     * @param listener the entity that caused this method to be called.
     * @param cause the exception that caused the error.
     */
    private synchronized void fatalError(Object listener, Http2Exception cause) {
        if (restarting || (onlineState == State.CLOSED)) {
            return;
        }
        String causeId = listener.getClass().getSimpleName();
        if (listener instanceof ContentStreamListenerAdapter) {
            // on GET / PUT requests the caller handles errors and closes the stream; the session is still OK
            logger.debug("fatalError() {} {} ignoring", causeId, cause.error);
        } else if (cause.error == Http2Error.GO_AWAY) {
            logger.debug("fatalError() {} {} reconnecting", causeId, cause.error);

            // close
            restarting = true;
            boolean active = onlineState == State.ACTIVE;
            close();

            // schedule task to open again
            bridgeHandler.getScheduler().schedule(() -> {
                try {
                    openPassive();
                    if (active) {
                        openActive();
                    }
                } catch (ApiException | HttpUnauthorizedException e) {
                    logger.warn("fatalError() {} {} reconnect failed {}", causeId, cause.error, e.getMessage(), e);
                    restarting = false;
                    close();
                }
                restarting = false;
            }, 5, TimeUnit.SECONDS);
        } else {
            logger.warn("fatalError() {} {} closing", causeId, cause.error, cause);
            close();
        }
    }

    /**
     * Method that is called back in case of fatal stream or session events. Schedules fatalError() to be called after a
     * delay in order to prevent sequencing issues.
     *
     * @param listener the entity that caused this method to be called.
     * @param cause the exception that caused the error.
     */
    protected void fatalErrorDelayed(Object listener, Http2Exception cause) {
        bridgeHandler.getScheduler().schedule(() -> fatalError(listener, cause), 1, TimeUnit.SECONDS);
    }

    /**
     * HTTP GET a Resources object, for a given resource Reference, from the Hue Bridge. The reference is a class
     * comprising a resource type and an id. If the id is a specific resource id then only the one specific resource
     * is returned, whereas if it is null then all resources of the given resource type are returned.
     *
     * It wraps the getResourcesImpl() method in a try/catch block, and transposes any HttpUnAuthorizedException into an
     * ApiException. Such transposition should never be required in reality since by the time this method is called, the
     * connection will surely already have been authorised.
     *
     * @param reference the Reference class to get.
     * @return a Resource object containing either a list of Resources or a list of Errors.
     * @throws ApiException if anything fails.
     */
    public Resources getResources(ResourceReference reference) throws ApiException {
        sleepDuringRestart();
        if (onlineState == State.CLOSED) {
            throw new ApiException("getResources() offline");
        }
        try {
            return getResourcesImpl(reference);
        } catch (HttpUnauthorizedException e) {
            throw new ApiException("getResources() unauthorized error", e);
        }
    }

    /**
     * Internal method to send an HTTP 2 GET request to the Hue Bridge and process its response.
     *
     * @param reference the Reference class to get.
     * @return a Resource object containing either a list of Resources or a list of Errors.
     * @throws ApiException if the communication failed, or an unexpected result occurred.
     * @throws HttpUnauthorizedException if the request was refused as not authorised or forbidden.
     */
    private Resources getResourcesImpl(ResourceReference reference) throws ApiException, HttpUnauthorizedException {
        Session session = http2Session;
        if (Objects.isNull(session) || session.isClosed()) {
            throw new ApiException("HTTP 2 session is null or closed");
        }
        throttle();
        String url = getUrl(reference);
        HeadersFrame headers = prepareHeaders(url, MediaType.APPLICATION_JSON);
        logger.trace("GET {} HTTP/2", url);
        try {
            Completable<@Nullable Stream> streamPromise = new Completable<>();
            ContentStreamListenerAdapter contentStreamListener = new ContentStreamListenerAdapter();
            session.newStream(headers, streamPromise, contentStreamListener);
            // wait for stream to be opened
            Objects.requireNonNull(streamPromise.get(TIMEOUT_SECONDS, TimeUnit.SECONDS));
            // wait for HTTP response contents
            String contentJson = contentStreamListener.awaitResult();
            String contentType = contentStreamListener.getContentType();
            logger.trace("HTTP/2 200 OK (Content-Type: {}) << {}", contentType, contentJson);
            if (!MediaType.APPLICATION_JSON.equals(contentType)) {
                throw new ApiException("Unexpected Content-Type: " + contentType);
            }
            try {
                Resources resources = Objects.requireNonNull(jsonParser.fromJson(contentJson, Resources.class));
                if (logger.isDebugEnabled()) {
                    resources.getErrors().forEach(error -> logger.debug("Resources error:{}", error));
                }
                return resources;
            } catch (JsonParseException e) {
                throw new ApiException("Parsing error", e);
            }
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof HttpUnauthorizedException) {
                throw (HttpUnauthorizedException) cause;
            }
            throw new ApiException("Error sending request", e);
        } catch (InterruptedException | TimeoutException e) {
            throw new ApiException("Error sending request", e);
        }
    }

    /**
     * Build a full path to a server end point, based on a Reference class instance. If the reference contains only
     * a resource type, the method returns the end point url to get all resources of the given resource type. Whereas if
     * it also contains an id, the method returns the end point url to get the specific single resource with that type
     * and id.
     *
     * @param reference a Reference class instance.
     * @return the complete end point url.
     */
    private String getUrl(ResourceReference reference) {
        String url = baseUrl + reference.getType().name().toLowerCase();
        String id = reference.getId();
        return Objects.isNull(id) || id.isEmpty() ? url : url + "/" + id;
    }

    /**
     * The event stream calls this method when it has received text data. It parses the text as JSON into a list of
     * Event entries, converts the list of events to a list of resources, and forwards that list to the bridge
     * handler.
     *
     * @param data the incoming (presumed to be JSON) text.
     */
    protected void onEventData(String data) {
        if (onlineState != State.ACTIVE) {
            return;
        }
        logger.trace("onEventData() << {}", data);
        JsonElement jsonElement;
        try {
            jsonElement = JsonParser.parseString(data);
        } catch (JsonSyntaxException e) {
            logger.debug("onEventData() invalid data '{}'", data, e);
            return;
        }
        if (!(jsonElement instanceof JsonArray)) {
            logger.debug("onEventData() data is not a JsonArray {}", data);
            return;
        }
        List<Event> events;
        try {
            events = jsonParser.fromJson(jsonElement, Event.EVENT_LIST_TYPE);
        } catch (JsonParseException e) {
            logger.debug("onEventData() {}", e.getMessage(), e);
            return;
        }
        if (Objects.isNull(events) || events.isEmpty()) {
            logger.debug("onEventData() event list is null or empty");
            return;
        }
        List<Resource> resources = new ArrayList<>();
        events.forEach(event -> resources.addAll(event.getData()));
        if (resources.isEmpty()) {
            logger.debug("onEventData() resource list is empty");
            return;
        }
        resources.forEach(resource -> resource.markAsSparse());
        bridgeHandler.onResourcesEvent(resources);
    }

    /**
     * Open the HTTP 2 session and the event stream.
     *
     * @throws ApiException if there was a communication error.
     * @throws HttpUnauthorizedException if the application key is not authenticated
     */
    public void open() throws ApiException, HttpUnauthorizedException {
        logger.debug("open()");
        openPassive();
        openActive();
        bridgeHandler.onConnectionOnline();
    }

    /**
     * Make the session active, by opening an HTTP 2 SSE event stream (if necessary).
     *
     * @throws ApiException if an error was encountered.
     * @throws HttpUnauthorizedException if the application key is not authenticated.
     */
    private void openActive() throws ApiException, HttpUnauthorizedException {
        synchronized (this) {
            openEventStream();
            onlineState = State.ACTIVE;
        }
    }

    /**
     * Open the check alive task if necessary.
     */
    private void openCheckAliveTask() {
        ScheduledFuture<?> task = checkAliveTask;
        if (Objects.isNull(task) || task.isCancelled() || task.isDone()) {
            logger.debug("openCheckAliveTask()");
            checkAliveTask = bridgeHandler.getScheduler().scheduleWithFixedDelay(() -> checkAlive(),
                    CHECK_ALIVE_SECONDS, CHECK_ALIVE_SECONDS, TimeUnit.SECONDS);
        }
    }

    /**
     * Implementation to open an HTTP 2 SSE event stream if necessary.
     *
     * @throws ApiException if an error was encountered.
     * @throws HttpUnauthorizedException if the application key is not authenticated.
     */
    private void openEventStream() throws ApiException, HttpUnauthorizedException {
        throttle();
        Session session = http2Session;
        if (Objects.isNull(session) || session.isClosed()) {
            throw new ApiException("HTTP 2 session is null or in an illegal state");
        }
        if (session.getStreams().stream().anyMatch(stream -> Objects.nonNull(stream.getAttribute(EVENT_STREAM_ID)))) {
            return;
        }
        logger.debug("openEventStream()");
        HeadersFrame headers = prepareHeaders(eventUrl, MediaType.SERVER_SENT_EVENTS);
        logger.trace("GET {} HTTP/2", eventUrl);
        Stream stream = null;
        try {
            Completable<@Nullable Stream> streamPromise = new Completable<>();
            EventStreamListenerAdapter eventStreamListener = new EventStreamListenerAdapter();
            session.newStream(headers, streamPromise, eventStreamListener);
            // wait for stream to be opened
            stream = Objects.requireNonNull(streamPromise.get(TIMEOUT_SECONDS, TimeUnit.SECONDS));
            stream.setIdleTimeout(0);
            stream.setAttribute(EVENT_STREAM_ID, session);
            // wait for "hi" from the bridge
            eventStreamListener.awaitResult();
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            if (Objects.nonNull(stream)) {
                stream.reset(new ResetFrame(stream.getId(), 0), Callback.NOOP);
            }
            throw new ApiException("Error opening event stream", e);
        }
    }

    /**
     * Private method to open the HTTP 2 session in passive mode.
     *
     * @throws ApiException if there was a communication error.
     * @throws HttpUnauthorizedException if the application key is not authenticated.
     */
    private void openPassive() throws ApiException, HttpUnauthorizedException {
        synchronized (this) {
            logger.debug("openPassive()");
            onlineState = State.CLOSED;
            try {
                http2Client.start();
            } catch (Exception e) {
                throw new ApiException("Error starting HTTP/2 client", e);
            }
            openSession();
            openCheckAliveTask();
            onlineState = State.PASSIVE;
        }
    }

    /**
     * Open the HTTP 2 session if necessary.
     *
     * @throws ApiException if it was not possible to create and connect the session.
     */
    private void openSession() throws ApiException {
        Session session = http2Session;
        if (Objects.nonNull(session) && !session.isClosed()) {
            return;
        }
        logger.debug("openSession()");
        InetSocketAddress address = new InetSocketAddress(hostName, 443);
        try {
            SessionListenerAdapter sessionListener = new SessionListenerAdapter();
            Completable<@Nullable Session> sessionPromise = new Completable<>();
            http2Client.connect(http2Client.getBean(SslContextFactory.class), address, sessionListener, sessionPromise);
            // wait for the (SSL) session to be opened
            http2Session = Objects.requireNonNull(sessionPromise.get(TIMEOUT_SECONDS, TimeUnit.SECONDS));
            checkAliveOk(); // initialise the session timeout window
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new ApiException("Error opening HTTP 2 session", e);
        }
    }

    /**
     * Helper class to create a HeadersFrame for a standard HTTP GET request.
     *
     * @param url the server url.
     * @param acceptContentType the accepted content type for the response.
     * @return the HeadersFrame.
     */
    private HeadersFrame prepareHeaders(String url, String acceptContentType) {
        return prepareHeaders(url, acceptContentType, "GET", -1, null);
    }

    /**
     * Helper class to create a HeadersFrame for a more exotic HTTP request.
     *
     * @param url the server url.
     * @param acceptContentType the accepted content type for the response.
     * @param method the HTTP request method.
     * @param contentLength the length of the content e.g. for a PUT call.
     * @param contentType the respective content type.
     * @return the HeadersFrame.
     */
    private HeadersFrame prepareHeaders(String url, String acceptContentType, String method, long contentLength,
            @Nullable String contentType) {
        HttpFields fields = new HttpFields();
        fields.put(HttpHeader.ACCEPT, acceptContentType);
        if (contentType != null) {
            fields.put(HttpHeader.CONTENT_TYPE, contentType);
        }
        if (contentLength >= 0) {
            fields.putLongField(HttpHeader.CONTENT_LENGTH, contentLength);
        }
        fields.put(HttpHeader.USER_AGENT, APPLICATION_ID);
        fields.put(APPLICATION_KEY, applicationKey);
        return new HeadersFrame(new MetaData.Request(method, new HttpURI(url), HttpVersion.HTTP_2, fields), null,
                contentLength <= 0);
    }

    /**
     * Use an HTTP/2 PUT command to send a resource to the server.
     *
     * @param resource the resource to put.
     * @throws ApiException if something fails.
     */
    public void putResource(Resource resource) throws ApiException {
        sleepDuringRestart();
        if (onlineState == State.CLOSED) {
            return;
        }
        Session session = http2Session;
        if (Objects.isNull(session) || session.isClosed()) {
            throw new ApiException("HTTP 2 session is null or closed");
        }
        throttle();
        String requestJson = jsonParser.toJson(resource);
        ByteBuffer requestBytes = ByteBuffer.wrap(requestJson.getBytes(StandardCharsets.UTF_8));
        String url = getUrl(new ResourceReference().setId(resource.getId()).setType(resource.getType()));
        HeadersFrame headers = prepareHeaders(url, MediaType.APPLICATION_JSON, "PUT", requestBytes.capacity(),
                MediaType.APPLICATION_JSON);
        logger.trace("PUT {} HTTP/2 >> {}", url, requestJson);
        try {
            Completable<@Nullable Stream> streamPromise = new Completable<>();
            ContentStreamListenerAdapter contentStreamListener = new ContentStreamListenerAdapter();
            session.newStream(headers, streamPromise, contentStreamListener);
            // wait for stream to be opened
            Stream stream = Objects.requireNonNull(streamPromise.get(TIMEOUT_SECONDS, TimeUnit.SECONDS));
            stream.data(new DataFrame(stream.getId(), requestBytes, true), Callback.NOOP);
            // wait for HTTP response
            String contentJson = contentStreamListener.awaitResult();
            String contentType = contentStreamListener.getContentType();
            logger.trace("HTTP/2 200 OK (Content-Type: {}) << {}", contentType, contentJson);
            if (!MediaType.APPLICATION_JSON.equals(contentType)) {
                throw new ApiException("Unexpected Content-Type: " + contentType);
            }
            try {
                Resources resources = Objects.requireNonNull(jsonParser.fromJson(contentJson, Resources.class));
                if (logger.isDebugEnabled()) {
                    resources.getErrors().forEach(error -> logger.debug("Resources error:{}", error));
                }
            } catch (JsonParseException e) {
                logger.warn("putResource() error parsing JSON response:{}", contentJson);
                throw new ApiException("Parsing error", e);
            }
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            throw new ApiException("Error sending request", e);
        }
    }

    /**
     * Try to register the application key with the hub. Use the given application key if one is provided; otherwise the
     * hub will create a new one. Note: this requires an HTTP 1.1 client call.
     *
     * @param oldApplicationKey existing application key if any i.e. may be empty.
     * @return the existing or a newly created application key.
     * @throws ApiException if there was a communications error.
     * @throws HttpUnauthorizedException if the registration failed.
     */
    public String registerApplicationKey(@Nullable String oldApplicationKey)
            throws ApiException, HttpUnauthorizedException {
        logger.debug("registerApplicationKey()");
        String json = jsonParser.toJson((Objects.isNull(oldApplicationKey) || oldApplicationKey.isEmpty())
                ? new CreateUserRequest(APPLICATION_ID)
                : new CreateUserRequest(oldApplicationKey, APPLICATION_ID));
        Request httpRequest = httpClient.newRequest(registrationUrl).method(HttpMethod.POST)
                .timeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .content(new StringContentProvider(json), MediaType.APPLICATION_JSON);
        ContentResponse contentResponse;
        try {
            logger.trace("POST {} HTTP/1.1 >> {}", registrationUrl, json);
            contentResponse = httpRequest.send();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new ApiException("HTTP processing error", e);
        }
        int httpStatus = contentResponse.getStatus();
        json = contentResponse.getContentAsString().trim();
        logger.trace("HTTP/1.1 {} {} << {}", httpStatus, contentResponse.getReason(), json);
        if (httpStatus != HttpStatus.OK_200) {
            throw new ApiException("HTTP bad response");
        }
        try {
            List<SuccessResponse> entries = jsonParser.fromJson(json, SuccessResponse.GSON_TYPE);
            if (Objects.nonNull(entries) && !entries.isEmpty()) {
                SuccessResponse response = entries.get(0);
                Map<String, Object> responseSuccess = response.success;
                if (Objects.nonNull(responseSuccess)) {
                    String newApplicationKey = (String) responseSuccess.get("username");
                    if (Objects.nonNull(newApplicationKey)) {
                        return newApplicationKey;
                    }
                }
            }
        } catch (JsonParseException e) {
            // fall through
        }
        throw new HttpUnauthorizedException("Application key registration failed");
    }

    /**
     * Sleep the caller during any period when the connection is marked as restarting.
     * Force time out after 10 seconds.
     */
    private void sleepDuringRestart() {
        int iteration = 0;
        while (restarting) {
            try {
                Thread.sleep(500);
                if (++iteration > 20) {
                    break;
                }
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    /**
     * Test the Hue Bridge connection state by attempting to connect and trying to execute a basic command that requires
     * authentication.
     *
     * @throws ApiException if it was not possible to connect.
     * @throws HttpUnauthorizedException if it was possible to connect but not to authenticate.
     */
    public void testConnectionState() throws HttpUnauthorizedException, ApiException {
        logger.debug("testConnectionState()");
        try {
            openPassive();
            getResourcesImpl(BRIDGE);
        } catch (HttpUnauthorizedException | ApiException e) {
            close();
            throw e;
        }
    }

    /**
     * Hue Bridges get confused if they receive too many HTTP requests in a short period of time (e.g. on start up), so
     * this method throttles the requests so that the minimum interval is REQUEST_INTERVAL_MILLISECS.
     */
    private synchronized void throttle() {
        try {
            long delay = Duration.between(Instant.now(), lastRequestTime).toMillis() + REQUEST_INTERVAL_MILLISECS;
            if (delay > 0) {
                Thread.sleep(delay);
            }
        } catch (InterruptedException | ArithmeticException e) {
            // fall through
        }
        lastRequestTime = Instant.now();
    }
}
