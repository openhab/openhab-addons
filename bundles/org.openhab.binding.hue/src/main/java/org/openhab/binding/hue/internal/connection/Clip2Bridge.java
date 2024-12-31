/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

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
import org.eclipse.jetty.http2.ErrorCode;
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
import org.openhab.binding.hue.internal.api.dto.clip1.CreateUserRequest;
import org.openhab.binding.hue.internal.api.dto.clip1.SuccessResponse;
import org.openhab.binding.hue.internal.api.dto.clip2.BridgeConfig;
import org.openhab.binding.hue.internal.api.dto.clip2.Event;
import org.openhab.binding.hue.internal.api.dto.clip2.Resource;
import org.openhab.binding.hue.internal.api.dto.clip2.ResourceReference;
import org.openhab.binding.hue.internal.api.dto.clip2.Resources;
import org.openhab.binding.hue.internal.api.dto.clip2.enums.ResourceType;
import org.openhab.binding.hue.internal.api.serialization.InstantDeserializer;
import org.openhab.binding.hue.internal.exceptions.ApiException;
import org.openhab.binding.hue.internal.exceptions.HttpUnauthorizedException;
import org.openhab.binding.hue.internal.handler.Clip2BridgeHandler;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
 * <ul>
 * <li>The primary communication uses HTTP 2 streams over a shared permanent HTTP 2 session.</li>
 * <li>The 'registerApplicationKey()' method uses HTTP/1.1 over the OH common Jetty client.</li>
 * <li>The 'isClip2Supported()' static method uses HTTP/1.1 over the OH common Jetty client via 'HttpUtil'.</li>
 * </ul>
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
        protected final CompletableFuture<T> completable = new CompletableFuture<>();
        private String contentType = "UNDEFINED";
        private int status;

        protected T awaitResult() throws ExecutionException, InterruptedException, TimeoutException {
            return completable.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        }

        /**
         * Return the HTTP content type.
         *
         * @return content type e.g. 'application/json'
         */
        protected String getContentType() {
            return contentType;
        }

        /**
         * Return the HTTP status code.
         *
         * @return status code e.g. 200
         */
        protected int getStatus() {
            return status;
        }

        /**
         * Handle an HTTP2 error.
         *
         * @param error the type of error.
         * @param session the session on which the error occurred.
         */
        protected void handleHttp2Error(Http2Error error, Session session) {
            Http2Exception e = new Http2Exception(error);
            if (Http2Error.UNAUTHORIZED.equals(error)) {
                // for external error handling, abstract authorization errors into a separate exception
                completable.completeExceptionally(new HttpUnauthorizedException("HTTP 2 request not authorized"));
            } else {
                completable.completeExceptionally(e);
            }
            fatalErrorDelayed(this, e, session);
        }

        /**
         * Check the reply headers to see whether the request was authorised.
         */
        @Override
        public void onHeaders(@Nullable Stream stream, @Nullable HeadersFrame frame) {
            Objects.requireNonNull(stream);
            Objects.requireNonNull(frame);
            MetaData metaData = frame.getMetaData();
            if (metaData.isResponse()) {
                Response responseMetaData = (Response) metaData;
                contentType = responseMetaData.getFields().get(HttpHeader.CONTENT_TYPE).toLowerCase();
                status = responseMetaData.getStatus();
                switch (status) {
                    case HttpStatus.UNAUTHORIZED_401:
                    case HttpStatus.FORBIDDEN_403:
                        handleHttp2Error(Http2Error.UNAUTHORIZED, stream.getSession());
                    default:
                }
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
        private final DataFrameCollector content = new DataFrameCollector();

        @Override
        public void onData(@Nullable Stream stream, @Nullable DataFrame frame, @Nullable Callback callback) {
            Objects.requireNonNull(frame);
            Objects.requireNonNull(callback);
            synchronized (this) {
                content.append(frame.getData());
                if (frame.isEndStream() && !completable.isDone()) {
                    completable.complete(content.contentAsString().trim());
                    content.reset();
                }
            }
            callback.succeeded();
        }

        @Override
        public boolean onIdleTimeout(@Nullable Stream stream, @Nullable Throwable x) {
            Objects.requireNonNull(stream);
            handleHttp2Error(Http2Error.IDLE, stream.getSession());
            return true;
        }

        @Override
        public void onTimeout(@Nullable Stream stream, @Nullable Throwable x) {
            Objects.requireNonNull(stream);
            handleHttp2Error(Http2Error.TIMEOUT, stream.getSession());
        }
    }

    /**
     * Class to collect incoming ByteBuffer data from HTTP 2 Data frames.
     */
    private static class DataFrameCollector {
        private byte[] buffer = new byte[512];
        private int usedSize = 0;

        public void append(ByteBuffer data) {
            int dataCapacity = data.capacity();
            int neededSize = usedSize + dataCapacity;
            if (neededSize > buffer.length) {
                int newSize = (dataCapacity < 4096) ? neededSize : Math.max(2 * buffer.length, neededSize);
                buffer = Arrays.copyOf(buffer, newSize);
            }
            data.get(buffer, usedSize, dataCapacity);
            usedSize += dataCapacity;
        }

        public String contentAsString() {
            return new String(buffer, 0, usedSize, StandardCharsets.UTF_8);
        }

        public Reader contentStreamReader() {
            return new InputStreamReader(new ByteArrayInputStream(buffer, 0, usedSize), StandardCharsets.UTF_8);
        }

        public void reset() {
            usedSize = 0;
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
        private final DataFrameCollector eventData = new DataFrameCollector();

        @Override
        public void onClosed(@Nullable Stream stream) {
            Objects.requireNonNull(stream);
            handleHttp2Error(Http2Error.CLOSED, stream.getSession());
        }

        @Override
        public void onData(@Nullable Stream stream, @Nullable DataFrame frame, @Nullable Callback callback) {
            Objects.requireNonNull(frame);
            Objects.requireNonNull(callback);
            synchronized (this) {
                eventData.append(frame.getData());
                BufferedReader reader = new BufferedReader(eventData.contentStreamReader());
                @SuppressWarnings("null")
                List<String> receivedLines = reader.lines().collect(Collectors.toList());

                // a blank line marks the end of an SSE message
                boolean endOfMessage = !receivedLines.isEmpty()
                        && receivedLines.get(receivedLines.size() - 1).isBlank();

                if (endOfMessage) {
                    eventData.reset();
                    // receipt of ANY message means the event stream is established
                    if (!completable.isDone()) {
                        completable.complete(Boolean.TRUE);
                    }
                    // append any 'data' field values to the event message
                    StringBuilder eventContent = new StringBuilder();
                    for (String receivedLine : receivedLines) {
                        if (receivedLine.startsWith("data:")) {
                            eventContent.append(receivedLine.substring(5).stripLeading());
                        }
                    }
                    if (eventContent.length() > 0) {
                        onEventData(eventContent.toString().trim());
                    }
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
            Objects.requireNonNull(stream);
            handleHttp2Error(Http2Error.RESET, stream.getSession());
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
        UNAUTHORIZED
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
            Objects.requireNonNull(session);
            fatalErrorDelayed(this, new Http2Exception(Http2Error.CLOSED), session);
        }

        @Override
        public void onFailure(@Nullable Session session, @Nullable Throwable failure) {
            Objects.requireNonNull(session);
            fatalErrorDelayed(this, new Http2Exception(Http2Error.FAILURE), session);
        }

        /**
         * The Hue bridge uses the 'nginx' web server which sends HTTP2 GO_AWAY frames after a certain number (normally
         * 999) of GET/PUT calls. This is normal behaviour so we just start a new thread to close and reopen the
         * session.
         */
        @Override
        public void onGoAway(@Nullable Session session, @Nullable GoAwayFrame frame) {
            Objects.requireNonNull(session);
            if (session.equals(http2Session)) {
                Thread recreateThread = new Thread(() -> recreateSession(),
                        "OH-binding-" + bridgeHandler.getThing().getUID() + "-RecreateSession");
                Clip2Bridge.this.recreateThread = recreateThread;
                recreateThread.start();
            }
        }

        @Override
        public boolean onIdleTimeout(@Nullable Session session) {
            return false;
        }

        @Override
        public void onPing(@Nullable Session session, @Nullable PingFrame frame) {
            Objects.requireNonNull(session);
            Objects.requireNonNull(frame);
            if (session.equals(http2Session)) {
                checkAliveOk();
                if (!frame.isReply()) {
                    session.ping(new PingFrame(true), Callback.NOOP);
                }
            }
        }

        @Override
        public void onReset(@Nullable Session session, @Nullable ResetFrame frame) {
            Objects.requireNonNull(session);
            fatalErrorDelayed(this, new Http2Exception(Http2Error.RESET), session);
        }
    }

    /**
     * Synchronizer for accessing the HTTP2 session object. This method wraps the 'sessionUseCreateLock' ReadWriteLock
     * so that GET/PUT methods can access the session on multiple concurrent threads via the 'read' access lock, yet are
     * forced to wait if the session is being created via its single thread access 'write' lock.
     */
    private class SessionSynchronizer implements AutoCloseable {
        private final Optional<Lock> lockOptional;

        SessionSynchronizer(boolean requireExclusiveAccess) throws InterruptedException {
            Lock lock = requireExclusiveAccess ? sessionUseCreateLock.writeLock() : sessionUseCreateLock.readLock();
            lockOptional = lock.tryLock(TIMEOUT_SECONDS, TimeUnit.SECONDS) ? Optional.of(lock) : Optional.empty();
        }

        @Override
        public void close() {
            lockOptional.ifPresent(lock -> lock.unlock());
        }
    }

    /**
     * Enum showing the online state of the session connection.
     */
    private enum State {
        /**
         * Session closed
         */
        CLOSED,
        /**
         * Session open for HTTP calls only
         */
        PASSIVE,
        /**
         * Session open for HTTP calls and actively receiving SSE events
         */
        ACTIVE
    }

    /**
     * Class for throttling HTTP GET and PUT requests to prevent overloading the Hue bridge.
     * <p>
     * The Hue Bridge can get confused if they receive too many HTTP requests in a short period of time (e.g. on start
     * up), or if too many HTTP sessions are opened at the same time, which cause it to respond with an HTML error page.
     * So this class a) waits to acquire permitCount (or no more than MAX_CONCURRENT_SESSIONS) stream permits, and b)
     * throttles the requests to a maximum of one per REQUEST_INTERVAL_MILLISECS.
     */
    private class Throttler implements AutoCloseable {
        private final int permitCount;

        /**
         * @param permitCount indicates how many stream permits to be acquired.
         * @throws InterruptedException
         */
        Throttler(int permitCount) throws InterruptedException {
            this.permitCount = permitCount;
            streamMutex.acquire(permitCount);
            long delay;
            synchronized (Clip2Bridge.this) {
                Instant now = Instant.now();
                delay = Objects.requireNonNull(lastRequestTime
                        .map(t -> Math.max(0, Duration.between(now, t).toMillis() + REQUEST_INTERVAL_MILLISECS))
                        .orElse(0L));
                lastRequestTime = Optional.of(now.plusMillis(delay));
            }
            Thread.sleep(delay);
        }

        @Override
        public void close() {
            streamMutex.release(permitCount);
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(Clip2Bridge.class);

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
    private static final int REQUEST_INTERVAL_MILLISECS = 50;
    private static final int MAX_CONCURRENT_STREAMS = 3;

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
                try {
                    if (Long.parseLong(swVersion) >= CLIP2_MINIMUM_VERSION) {
                        return true;
                    }
                } catch (NumberFormatException e) {
                    LOGGER.debug("isClip2Supported() swVersion '{}' is not a number", swVersion);
                }
            }
        }
        return false;
    }

    private final HttpClient httpClient;
    private final HTTP2Client http2Client;
    private final String hostName;
    private final String baseUrl;
    private final String eventUrl;
    private final String registrationUrl;
    private final String applicationKey;
    private final Clip2BridgeHandler bridgeHandler;
    private final Gson jsonParser = new GsonBuilder().registerTypeAdapter(Instant.class, new InstantDeserializer())
            .create();
    private final Semaphore streamMutex = new Semaphore(MAX_CONCURRENT_STREAMS, true); // i.e. fair
    private final ReadWriteLock sessionUseCreateLock = new ReentrantReadWriteLock(true); // i.e. fair
    private final Map<Integer, Future<?>> fatalErrorTasks = new ConcurrentHashMap<>();

    private boolean recreatingSession;
    private boolean closing;
    private State onlineState = State.CLOSED;
    private Optional<Instant> lastRequestTime = Optional.empty();
    private Instant sessionExpireTime = Instant.MAX;

    private @Nullable Session http2Session;
    private @Nullable Thread recreateThread;
    private @Nullable Future<?> checkAliveTask;

    /**
     * Constructor.
     *
     * @param httpClientFactory the OH core HttpClientFactory.
     * @param bridgeHandler the bridge handler.
     * @param hostName the host name (ip address) of the Hue bridge
     * @param applicationKey the application key.
     * @throws ApiException if unable to open Jetty HTTP/2 client.
     */
    public Clip2Bridge(HttpClientFactory httpClientFactory, Clip2BridgeHandler bridgeHandler, String hostName,
            String applicationKey) throws ApiException {
        LOGGER.debug("Clip2Bridge()");
        httpClient = httpClientFactory.getCommonHttpClient();
        http2Client = httpClientFactory.createHttp2Client("hue-clip2", httpClient.getSslContextFactory());
        http2Client.setConnectTimeout(Clip2Bridge.TIMEOUT_SECONDS * 1000);
        http2Client.setIdleTimeout(-1);
        startHttp2Client();
        this.bridgeHandler = bridgeHandler;
        this.hostName = hostName;
        this.applicationKey = applicationKey;
        baseUrl = String.format(FORMAT_URL_RESOURCE, hostName);
        eventUrl = String.format(FORMAT_URL_EVENTS, hostName);
        registrationUrl = String.format(FORMAT_URL_REGISTER, hostName);
    }

    /**
     * Cancel the given task.
     *
     * @param cancelTask the task to be cancelled (may be null)
     * @param mayInterrupt allows cancel() to interrupt the thread.
     */
    private void cancelTask(@Nullable Future<?> cancelTask, boolean mayInterrupt) {
        if (Objects.nonNull(cancelTask)) {
            cancelTask.cancel(mayInterrupt);
        }
    }

    /**
     * Send a ping to the Hue bridge to check that the session is still alive.
     */
    private void checkAlive() {
        if (onlineState == State.CLOSED) {
            return;
        }
        LOGGER.debug("checkAlive()");
        Session session = http2Session;
        if (Objects.nonNull(session)) {
            session.ping(new PingFrame(false), Callback.NOOP);
            if (Instant.now().isAfter(sessionExpireTime)) {
                fatalError(this, new Http2Exception(Http2Error.TIMEOUT), session.hashCode());
            }
        }
    }

    /**
     * Connection is ok, so reschedule the session check alive expire time. Called in response to incoming ping frames
     * from the bridge.
     */
    protected void checkAliveOk() {
        LOGGER.debug("checkAliveOk()");
        sessionExpireTime = Instant.now().plusSeconds(CHECK_ALIVE_SECONDS * 2);
    }

    /**
     * Close the connection.
     */
    @Override
    public void close() {
        closing = true;
        Thread recreateThread = this.recreateThread;
        if (Objects.nonNull(recreateThread) && recreateThread.isAlive()) {
            recreateThread.interrupt();
        }
        close2();
        try {
            stopHttp2Client();
        } catch (ApiException e) {
        }
    }

    /**
     * Private method to close the connection.
     */
    private void close2() {
        synchronized (this) {
            LOGGER.debug("close2()");
            boolean notifyHandler = onlineState == State.ACTIVE && !closing && !recreatingSession;
            onlineState = State.CLOSED;
            synchronized (fatalErrorTasks) {
                fatalErrorTasks.values().forEach(task -> cancelTask(task, true));
                fatalErrorTasks.clear();
            }
            cancelTask(checkAliveTask, true);
            checkAliveTask = null;
            closeEventStream();
            closeSession();
            if (notifyHandler) {
                bridgeHandler.onConnectionOffline();
            }
        }
    }

    /**
     * Close the event stream(s) if necessary.
     */
    private void closeEventStream() {
        Session session = http2Session;
        if (Objects.nonNull(session)) {
            final int sessionId = session.hashCode();
            session.getStreams().stream().filter(s -> Objects.nonNull(s.getAttribute(EVENT_STREAM_ID)) && !s.isReset())
                    .forEach(s -> {
                        int streamId = s.getId();
                        LOGGER.debug("closeEventStream() sessionId:{}, streamId:{}", sessionId, streamId);
                        s.reset(new ResetFrame(streamId, ErrorCode.CANCEL_STREAM_ERROR.code), Callback.NOOP);
                    });
        }
    }

    /**
     * Close the HTTP 2 session if necessary.
     */
    private void closeSession() {
        Session session = http2Session;
        if (Objects.nonNull(session)) {
            LOGGER.debug("closeSession() sessionId:{}, openStreamCount:{}", session.hashCode(),
                    session.getStreams().size());
            session.close(ErrorCode.NO_ERROR.code, "closeSession", Callback.NOOP);
        }
        http2Session = null;
    }

    /**
     * Close the given stream.
     *
     * @param stream to be closed.
     */
    private void closeStream(@Nullable Stream stream) {
        if (Objects.nonNull(stream) && !stream.isReset()) {
            stream.reset(new ResetFrame(stream.getId(), ErrorCode.NO_ERROR.code), Callback.NOOP);
        }
    }

    /**
     * Method that is called back in case of fatal stream or session events. The error is only processed if the
     * connection is online, not in process of closing, and the identities of the current session and the session that
     * caused the error are the same. In other words it ignores errors relating to expired sessions.
     *
     * @param listener the entity that caused this method to be called.
     * @param cause the type of exception that caused the error.
     * @param sessionId the identity of the session on which the error occurred.
     */
    private synchronized void fatalError(Object listener, Http2Exception cause, int sessionId) {
        if (onlineState == State.CLOSED || closing) {
            return;
        }
        Session session = http2Session;
        if (Objects.isNull(session) || session.hashCode() != sessionId) {
            return;
        }
        String listenerId = listener.getClass().getSimpleName();
        if (listener instanceof ContentStreamListenerAdapter) {
            // on GET / PUT requests the caller handles errors and closes the stream; the session is still OK
            LOGGER.debug("fatalError() listener:{}, sessionId:{}, error:{} => ignoring", listenerId, sessionId,
                    cause.error);
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("fatalError() listener:{}, sessionId:{}, error:{} => closing", listenerId, sessionId,
                        cause.error, cause);
            } else {
                LOGGER.warn("Fatal error '{}' from '{}' => closing session.", cause.error, listenerId);
            }
            close2();
        }
    }

    /**
     * Method that is called back in case of fatal stream or session events. Schedules fatalError() to be called after a
     * delay in order to prevent sequencing issues.
     *
     * @param listener the entity that caused this method to be called.
     * @param cause the type of exception that caused the error.
     * @param session the session on which the error occurred.
     */
    protected void fatalErrorDelayed(Object listener, Http2Exception cause, Session session) {
        synchronized (fatalErrorTasks) {
            final int index = fatalErrorTasks.size();
            final int sessionId = session.hashCode();
            fatalErrorTasks.put(index, bridgeHandler.getScheduler().schedule(() -> {
                fatalError(listener, cause, sessionId);
                fatalErrorTasks.remove(index);
            }, 1, TimeUnit.SECONDS));
        }
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
     * @throws InterruptedException
     */
    public Resources getResources(ResourceReference reference) throws ApiException, InterruptedException {
        if (onlineState == State.CLOSED && !recreatingSession) {
            throw new ApiException("Connection is closed");
        }
        return getResourcesImpl(reference);
    }

    /**
     * Internal method to send an HTTP 2 GET request to the Hue Bridge and process its response. Uses a Throttler to
     * prevent too many concurrent calls, and to prevent too frequent calls on the Hue bridge server. Also uses a
     * SessionSynchronizer to delay accessing the session while it is being recreated.
     *
     * @param reference the Reference class to get.
     * @return a Resource object containing either a list of Resources or a list of Errors.
     * @throws HttpUnauthorizedException if the request was refused as not authorised or forbidden.
     * @throws ApiException if the communication failed, or an unexpected result occurred.
     * @throws InterruptedException
     */
    private Resources getResourcesImpl(ResourceReference reference)
            throws HttpUnauthorizedException, ApiException, InterruptedException {
        // work around for issue #15468 (and similar)
        ResourceType resourceType = reference.getType();
        if (resourceType == ResourceType.ERROR) {
            LOGGER.debug("Resource '{}' type '{}' unknown => GET aborted", reference.getId(), resourceType);
            return new Resources();
        }
        Stream stream = null;
        try (Throttler throttler = new Throttler(1);
                SessionSynchronizer sessionSynchronizer = new SessionSynchronizer(false)) {
            Session session = getSession();
            String url = getUrl(reference);
            LOGGER.trace("GET {} HTTP/2", url);
            HeadersFrame headers = prepareHeaders(url, MediaType.APPLICATION_JSON);
            Completable<@Nullable Stream> streamPromise = new Completable<>();
            ContentStreamListenerAdapter contentStreamListener = new ContentStreamListenerAdapter();
            session.newStream(headers, streamPromise, contentStreamListener);
            // wait for stream to be opened
            stream = Objects.requireNonNull(streamPromise.get(TIMEOUT_SECONDS, TimeUnit.SECONDS));
            // wait for HTTP response contents
            String contentJson = contentStreamListener.awaitResult();
            String contentType = contentStreamListener.getContentType();
            int status = contentStreamListener.getStatus();
            LOGGER.trace("HTTP/2 {} (Content-Type: {}) << {}", status, contentType, contentJson);
            if (status != HttpStatus.OK_200) {
                throw new ApiException(String.format("Unexpected HTTP status '%d'", status));
            }
            if (!MediaType.APPLICATION_JSON.equals(contentType)) {
                throw new ApiException("Unexpected Content-Type: " + contentType);
            }
            try {
                Resources resources = Objects.requireNonNull(jsonParser.fromJson(contentJson, Resources.class));
                if (LOGGER.isDebugEnabled()) {
                    resources.getErrors().forEach(error -> LOGGER.debug("Resources error:{}", error));
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
        } catch (TimeoutException e) {
            throw new ApiException("Error sending request", e);
        } finally {
            closeStream(stream);
        }
    }

    /**
     * Safe access to the session object.
     *
     * @return the session.
     * @throws ApiException if session is null or closed.
     */
    private Session getSession() throws ApiException {
        Session session = http2Session;
        if (Objects.isNull(session) || session.isClosed()) {
            throw new ApiException("HTTP/2 session is null or closed");
        }
        return session;
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
        if (onlineState != State.ACTIVE && !recreatingSession) {
            return;
        }
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("onEventData() data:{}", data);
        } else {
            LOGGER.debug("onEventData() data length:{}", data.length());
        }
        JsonElement jsonElement;
        try {
            jsonElement = JsonParser.parseString(data);
        } catch (JsonSyntaxException e) {
            LOGGER.debug("onEventData() invalid data '{}'", data, e);
            return;
        }
        if (!(jsonElement instanceof JsonArray)) {
            LOGGER.debug("onEventData() data is not a JsonArray {}", data);
            return;
        }
        List<Event> events;
        try {
            events = jsonParser.fromJson(jsonElement, Event.EVENT_LIST_TYPE);
        } catch (JsonParseException e) {
            LOGGER.debug("onEventData() parsing error json:{}", data, e);
            return;
        }
        if (Objects.isNull(events) || events.isEmpty()) {
            LOGGER.debug("onEventData() event list is null or empty");
            return;
        }
        List<Resource> resources = new ArrayList<>();
        events.forEach(event -> {
            List<Resource> eventResources = event.getData();
            eventResources.forEach(resource -> resource.setContentType(event.getContentType()));
            resources.addAll(eventResources);
        });
        if (resources.isEmpty()) {
            LOGGER.debug("onEventData() resource list is empty");
            return;
        }
        bridgeHandler.onResourcesEvent(resources);
    }

    /**
     * Open the HTTP 2 session and the event stream.
     *
     * @throws ApiException if there was a communication error.
     * @throws InterruptedException
     */
    public void open() throws ApiException, InterruptedException {
        LOGGER.debug("open()");
        openPassive();
        openActive();
        bridgeHandler.onConnectionOnline();
    }

    /**
     * Make the session active, by opening an HTTP 2 SSE event stream (if necessary).
     *
     * @throws ApiException if an error was encountered.
     * @throws InterruptedException
     */
    private void openActive() throws ApiException, InterruptedException {
        synchronized (this) {
            openEventStream();
            onlineState = State.ACTIVE;
        }
    }

    /**
     * Open the check alive task if necessary.
     */
    private void openCheckAliveTask() {
        Future<?> task = checkAliveTask;
        if (Objects.isNull(task) || task.isCancelled() || task.isDone()) {
            LOGGER.debug("openCheckAliveTask()");
            cancelTask(checkAliveTask, false);
            checkAliveTask = bridgeHandler.getScheduler().scheduleWithFixedDelay(() -> checkAlive(),
                    CHECK_ALIVE_SECONDS, CHECK_ALIVE_SECONDS, TimeUnit.SECONDS);
        }
    }

    /**
     * Implementation to open an HTTP 2 SSE event stream if necessary.
     *
     * @throws ApiException if an error was encountered.
     * @throws InterruptedException
     */
    private void openEventStream() throws ApiException, InterruptedException {
        Session session = getSession();
        if (session.getStreams().stream().anyMatch(stream -> Objects.nonNull(stream.getAttribute(EVENT_STREAM_ID)))) {
            return;
        }
        LOGGER.trace("GET {} HTTP/2", eventUrl);
        Stream stream = null;
        try {
            HeadersFrame headers = prepareHeaders(eventUrl, MediaType.SERVER_SENT_EVENTS);
            Completable<@Nullable Stream> streamPromise = new Completable<>();
            EventStreamListenerAdapter eventStreamListener = new EventStreamListenerAdapter();
            session.newStream(headers, streamPromise, eventStreamListener);
            // wait for stream to be opened
            stream = Objects.requireNonNull(streamPromise.get(TIMEOUT_SECONDS, TimeUnit.SECONDS));
            stream.setIdleTimeout(0);
            stream.setAttribute(EVENT_STREAM_ID, session);
            // wait for "hi" from the bridge
            eventStreamListener.awaitResult();
            LOGGER.debug("openEventStream() sessionId:{} streamId:{}", session.hashCode(), stream.getId());
        } catch (ExecutionException | TimeoutException e) {
            if (Objects.nonNull(stream) && !stream.isReset()) {
                stream.reset(new ResetFrame(stream.getId(), ErrorCode.HTTP_CONNECT_ERROR.code), Callback.NOOP);
            }
            throw new ApiException("Error opening event stream", e);
        }
    }

    /**
     * Private method to open the HTTP 2 session in passive mode.
     *
     * @throws ApiException if there was a communication error.
     * @throws InterruptedException
     */
    private void openPassive() throws ApiException, InterruptedException {
        synchronized (this) {
            LOGGER.debug("openPassive()");
            onlineState = State.CLOSED;
            openSession();
            openCheckAliveTask();
            onlineState = State.PASSIVE;
        }
    }

    /**
     * Open the HTTP 2 session if necessary.
     *
     * @throws ApiException if it was not possible to create and connect the session.
     * @throws InterruptedException
     */
    private void openSession() throws ApiException, InterruptedException {
        Session session = http2Session;
        if (Objects.nonNull(session) && !session.isClosed()) {
            return;
        }
        try {
            InetSocketAddress address = new InetSocketAddress(hostName, 443);
            SessionListenerAdapter sessionListener = new SessionListenerAdapter();
            Completable<@Nullable Session> sessionPromise = new Completable<>();
            http2Client.connect(http2Client.getBean(SslContextFactory.class), address, sessionListener, sessionPromise);
            // wait for the (SSL) session to be opened
            session = Objects.requireNonNull(sessionPromise.get(TIMEOUT_SECONDS, TimeUnit.SECONDS));
            LOGGER.debug("openSession() sessionId:{}", session.hashCode());
            http2Session = session;
            checkAliveOk(); // initialise the session timeout window
        } catch (ExecutionException | TimeoutException e) {
            throw new ApiException("Error opening HTTP/2 session", e);
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
        fields.put(APPLICATION_KEY, applicationKey);
        return new HeadersFrame(new MetaData.Request(method, new HttpURI(url), HttpVersion.HTTP_2, fields), null,
                contentLength <= 0);
    }

    /**
     * Use an HTTP/2 PUT command to send a resource to the server. Uses a Throttler to prevent too many concurrent
     * calls, and to prevent too frequent calls on the Hue bridge server. Also uses a SessionSynchronizer to delay
     * accessing the session while it is being recreated.
     *
     * @param resource the resource to put.
     * @return the resource, which may contain errors.
     * @throws ApiException if something fails.
     * @throws InterruptedException
     */
    public Resources putResource(Resource resource) throws ApiException, InterruptedException {
        Stream stream = null;
        try (Throttler throttler = new Throttler(MAX_CONCURRENT_STREAMS);
                SessionSynchronizer sessionSynchronizer = new SessionSynchronizer(false)) {
            Session session = getSession();
            String requestJson = jsonParser.toJson(resource);
            ByteBuffer requestBytes = ByteBuffer.wrap(requestJson.getBytes(StandardCharsets.UTF_8));
            String url = getUrl(new ResourceReference().setId(resource.getId()).setType(resource.getType()));
            HeadersFrame headers = prepareHeaders(url, MediaType.APPLICATION_JSON, "PUT", requestBytes.capacity(),
                    MediaType.APPLICATION_JSON);
            LOGGER.trace("PUT {} HTTP/2 >> {}", url, requestJson);
            Completable<@Nullable Stream> streamPromise = new Completable<>();
            ContentStreamListenerAdapter contentStreamListener = new ContentStreamListenerAdapter();
            session.newStream(headers, streamPromise, contentStreamListener);
            // wait for stream to be opened
            stream = Objects.requireNonNull(streamPromise.get(TIMEOUT_SECONDS, TimeUnit.SECONDS));
            stream.data(new DataFrame(stream.getId(), requestBytes, true), Callback.NOOP);
            // wait for HTTP response
            String contentJson = contentStreamListener.awaitResult();
            String contentType = contentStreamListener.getContentType();
            int status = contentStreamListener.getStatus();
            LOGGER.trace("HTTP/2 {} (Content-Type: {}) << {}", status, contentType, contentJson);
            if (!HttpStatus.isSuccess(status)) {
                throw new ApiException(String.format("Unexpected HTTP status '%d'", status));
            }
            if (!MediaType.APPLICATION_JSON.equals(contentType)) {
                throw new ApiException("Unexpected Content-Type: " + contentType);
            }
            if (contentJson.isEmpty()) {
                throw new ApiException("Response payload is empty");
            }
            try {
                return Objects.requireNonNull(jsonParser.fromJson(contentJson, Resources.class));
            } catch (JsonParseException e) {
                LOGGER.debug("putResource() parsing error json:{}", contentJson, e);
                throw new ApiException("Parsing error", e);
            }
        } catch (ExecutionException | TimeoutException e) {
            throw new ApiException("Error sending PUT request", e);
        } finally {
            closeStream(stream);
        }
    }

    /**
     * Close and re-open the session. Called when the server sends a GO_AWAY message. Acquires a SessionSynchronizer
     * 'write' lock to ensure single thread access while the new session is being created. Therefore it waits for any
     * already running GET/PUT method calls, which have a 'read' lock, to complete. And also causes any new GET/PUT
     * method calls to wait until this method releases the 'write' lock again. Whereby such GET/PUT calls are postponed
     * to the new session.
     */
    private synchronized void recreateSession() {
        try (SessionSynchronizer sessionSynchronizer = new SessionSynchronizer(true)) {
            LOGGER.debug("recreateSession()");
            recreatingSession = true;
            State onlineState = this.onlineState;
            close2();
            stopHttp2Client();
            //
            startHttp2Client();
            openPassive();
            if (onlineState == State.ACTIVE) {
                openActive();
            }
        } catch (ApiException | InterruptedException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("recreateSession() exception", e);
            } else {
                LOGGER.warn("recreateSession() {}: {}", e.getClass().getSimpleName(), e.getMessage());
            }
        } finally {
            recreatingSession = false;
            LOGGER.debug("recreateSession() done");
        }
    }

    /**
     * Try to register the application key with the hub. Use the given application key if one is provided; otherwise the
     * hub will create a new one. Note: this requires an HTTP 1.1 client call.
     *
     * @param oldApplicationKey existing application key if any i.e. may be empty.
     * @return the existing or a newly created application key.
     * @throws HttpUnauthorizedException if the registration failed.
     * @throws ApiException if there was a communications error.
     * @throws InterruptedException
     */
    public String registerApplicationKey(@Nullable String oldApplicationKey)
            throws HttpUnauthorizedException, ApiException, InterruptedException {
        LOGGER.debug("registerApplicationKey()");
        String json = jsonParser.toJson((Objects.isNull(oldApplicationKey) || oldApplicationKey.isEmpty())
                ? new CreateUserRequest(APPLICATION_ID)
                : new CreateUserRequest(oldApplicationKey, APPLICATION_ID));
        Request httpRequest = httpClient.newRequest(registrationUrl).method(HttpMethod.POST)
                .timeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .content(new StringContentProvider(json), MediaType.APPLICATION_JSON);
        ContentResponse contentResponse;
        try {
            LOGGER.trace("POST {} HTTP/1.1 >> {}", registrationUrl, json);
            contentResponse = httpRequest.send();
        } catch (TimeoutException | ExecutionException e) {
            throw new ApiException("HTTP processing error", e);
        }
        int httpStatus = contentResponse.getStatus();
        json = contentResponse.getContentAsString().trim();
        LOGGER.trace("HTTP/1.1 {} {} << {}", httpStatus, contentResponse.getReason(), json);
        if (httpStatus != HttpStatus.OK_200) {
            throw new ApiException(String.format("HTTP bad response '%d'", httpStatus));
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
            LOGGER.debug("registerApplicationKey() parsing error json:{}", json, e);
        }
        throw new HttpUnauthorizedException("Application key registration failed");
    }

    private void startHttp2Client() throws ApiException {
        try {
            http2Client.start();
        } catch (Exception e) {
            throw new ApiException("Error starting HTTP/2 client", e);
        }
    }

    private void stopHttp2Client() throws ApiException {
        try {
            http2Client.stop();
        } catch (Exception e) {
            throw new ApiException("Error stopping HTTP/2 client", e);
        }
    }

    /**
     * Test the Hue Bridge connection state by attempting to connect and trying to execute a basic command that requires
     * authentication.
     *
     * @throws HttpUnauthorizedException if it was possible to connect but not to authenticate.
     * @throws ApiException if it was not possible to connect.
     * @throws InterruptedException
     */
    public void testConnectionState() throws HttpUnauthorizedException, ApiException, InterruptedException {
        LOGGER.debug("testConnectionState()");
        try {
            openPassive();
            getResourcesImpl(BRIDGE);
        } catch (ApiException e) {
            close2();
            throw e;
        }
    }
}
