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
import java.util.Set;
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
import org.eclipse.jetty.http.PreEncodedHttpField;
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
import org.openhab.core.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * This class handles HTTP and SSE connections to/from a Hue Bridge running CLIP 2.
 *
 * It uses the following connection mechanisms..
 *
 * <li>The primary communication uses HTTP 2 streams over a shared permanent HTTP 2 session.</li>
 * <li>The 'putResource()' method uses HTTP/1.1 over the OH common Jetty client if the HPACK jar is missing.</li>
 * <li>The 'registerApplicationKey()' method uses HTTP/1.1 over the OH common Jetty client.</li>
 * <li>The 'isClip2Supported()' static method uses HTTP/1.1 over the OH common Jetty client via 'HttpUtil'.</li>
 *
 * @author Andrew Fiddian-Green - Initial Contribution
 */
@NonNullByDefault
public class Clip2Bridge implements Closeable {

    /**
     * Interface for processing adapter errors. It handles fatal errors by implementing the fatalError() method.
     *
     * @author Andrew Fiddian-Green - Initial Contribution
     */
    private static interface AdapterErrorHandler {

        /**
         * Enum of potential fatal HTTP 2 session/stream errors.
         *
         * @author Andrew Fiddian-Green - Initial Contribution
         */
        public static enum Error {
            CLOSED,
            ERROR,
            FAILURE,
            TIMEOUT,
            RESET,
            IDLE,
            GO_AWAY,
            UNAUTHORIZED;
        }

        public static final Set<Error> SOFT_ERROR = Set.of(Error.GO_AWAY, Error.RESET);

        public void fatalError(Error error);
    }

    /**
     * Base (abstract) adapter for HTTP 2 stream events.
     *
     * It implements a CompletableFuture by means of which the caller can wait for the response data to come in. And
     * which, in the case of fatal errors, gets completed exceptionally.
     *
     * It handles the following fatal error events by notifying the owner..
     *
     * <li>onHeaders() HTTP unauthorized codes</li>
     *
     * @author Andrew Fiddian-Green - Initial Contribution
     */
    private abstract static class BaseAdapter extends Stream.Listener.Adapter implements AdapterErrorHandler {

        protected final Clip2Bridge owner;
        protected final List<String> strings = new ArrayList<>();
        protected final CompletableFuture<String> completable = new CompletableFuture<>();

        protected BaseAdapter(Clip2Bridge owner) {
            this.owner = owner;
        }

        @Override
        public void fatalError(Error error) {
            Exception e;
            if (Error.UNAUTHORIZED.equals(error)) {
                e = new HttpUnauthorizedException("HTTP 2 request not authorized");
            } else {
                e = new ApiException("HTTP 2 stream " + error.toString().toLowerCase());
            }
            completable.completeExceptionally(e);
            owner.fatalError(this, error);
        }

        /**
         * Check the reply headers to see whether the request was authorised.
         */
        @Override
        public void onHeaders(@Nullable Stream stream, @Nullable HeadersFrame frame) {
            Objects.requireNonNull(frame);
            MetaData metaData = frame.getMetaData();
            if (metaData.isResponse()) {
                int httpStatus = ((Response) metaData).getStatus();
                switch (httpStatus) {
                    case HttpStatus.UNAUTHORIZED_401:
                    case HttpStatus.FORBIDDEN_403:
                        fatalError(Error.UNAUTHORIZED);
                    default:
                }
            }
        }
    }

    /**
     * Adapter for regular HTTP GET/PUT request stream events.
     *
     * It assembles the incoming text data into an HTTP 'content' entity. And when the last data frame arrives, it
     * returns the full content by completing the CompletableFuture with that data.
     *
     * In addition to those handled by the parent, it handles the following fatal error events by notifying the owner..
     *
     * <li>onIdleTimeout()</li>
     * <li>onTimeout()</li>
     *
     * @author Andrew Fiddian-Green - Initial Contribution
     */
    private static class ContentAdapter extends BaseAdapter {

        protected ContentAdapter(Clip2Bridge owner) {
            super(owner);
        }

        @Override
        public void onData(@Nullable Stream stream, @Nullable DataFrame frame, @Nullable Callback callback) {
            Objects.requireNonNull(frame);
            Objects.requireNonNull(callback);
            synchronized (this) {
                strings.add(StandardCharsets.UTF_8.decode(frame.getData()).toString());
                if (frame.isEndStream() && !completable.isDone()) {
                    completable.complete(String.join("", strings));
                }
            }
            callback.succeeded();
        }

        @Override
        public boolean onIdleTimeout(@Nullable Stream stream, @Nullable Throwable x) {
            fatalError(Error.IDLE);
            return true;
        }

        @Override
        public void onTimeout(@Nullable Stream stream, @Nullable Throwable x) {
            fatalError(Error.TIMEOUT);
        }
    }

    /**
     * Adapter for SSE stream events.
     *
     * It receives the incoming text lines. Receipt of the first data line causes the CompletableFuture to complete. It
     * then parses subsequent data according to the SSE specification. If the line starts with a 'data:' message, it
     * adds the data to the list of strings. And if the line is empty (i.e. the last line of an event), it passes the
     * full set of strings to the owner via a call-back method.
     *
     * The stream must be permanently connected, so it ignores onIdleTimeout() events.
     *
     * The parent class handles most fatal errors, but since the event stream is supposed to be permanently connected,
     * the following events are also considered as fatal..
     *
     * <li>OnClosed()</li>
     * <li>onReset()</li>
     *
     * @author Andrew Fiddian-Green - Initial Contribution
     */
    private static class EventAdapter extends BaseAdapter {

        protected EventAdapter(Clip2Bridge owner) {
            super(owner);
        }

        @Override
        public void onClosed(@Nullable Stream stream) {
            fatalError(Error.CLOSED);
        }

        @Override
        public void onData(@Nullable Stream stream, @Nullable DataFrame frame, @Nullable Callback callback) {
            Objects.requireNonNull(frame);
            Objects.requireNonNull(callback);
            if (!completable.isDone()) {
                completable.complete(Boolean.toString(true));
            }
            synchronized (this) {
                for (String line : StandardCharsets.UTF_8.decode(frame.getData()).toString().split("\\R")) {
                    if (line.startsWith("data: ")) {
                        strings.add(line.substring(6));
                    }
                }
                if (!strings.isEmpty()) {
                    owner.onEventData(String.join("", strings));
                    strings.clear();
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
            fatalError(Error.RESET);
        }
    }

    /**
     * Adapter for HTTP 2 session status events.
     *
     * The session must be permanently connected, so it ignores onIdleTimeout() events.
     * It also handles the following fatal events by notifying the owner..
     *
     * <li>onClose()</li>
     * <li>onFailure()</li>
     * <li>onGoAway()</li>
     * <li>onReset()</li>
     *
     * @author Andrew Fiddian-Green - Initial Contribution
     */
    private static class SessionAdapter extends Session.Listener.Adapter implements AdapterErrorHandler {

        private final Clip2Bridge owner;

        protected SessionAdapter(Clip2Bridge owner) {
            this.owner = owner;
        }

        @Override
        public void fatalError(Error error) {
            owner.fatalError(this, error);
        }

        @Override
        public void onClose(@Nullable Session session, @Nullable GoAwayFrame frame) {
            fatalError(Error.CLOSED);
        }

        @Override
        public void onFailure(@Nullable Session session, @Nullable Throwable failure) {
            fatalError(Error.FAILURE);
        }

        @Override
        public void onGoAway(@Nullable Session session, @Nullable GoAwayFrame frame) {
            fatalError(Error.GO_AWAY);
        }

        @Override
        public boolean onIdleTimeout(@Nullable Session session) {
            return false;
        }

        @Override
        public void onPing(@Nullable Session session, @Nullable PingFrame frame) {
            owner.checkAliveOk();
            if (Objects.nonNull(session) && Objects.nonNull(frame) && !frame.isReply()) {
                session.ping(new PingFrame(true), Callback.NOOP);
            }
        }

        @Override
        public void onReset(@Nullable Session session, @Nullable ResetFrame frame) {
            fatalError(Error.RESET);
        }
    }

    /**
     * Enum showing the online state of the session connection.
     *
     * @author Andrew Fiddian-Green - Initial Contribution
     */
    private static enum State {
        CLOSED, // session closed
        PASSIVE, // session open for HTTP calls only
        ACTIVE; // session open for HTTP calls, and actively sending call-backs
    }

    private static final String APPLICATION_ID = "org-openhab-binding-hue-clip2";
    private static final String APPLICATION_KEY = "hue-application-key";

    private static final String EVENT_STREAM_ID = "eventStream";
    private static final String FORMAT_URL_CONFIG = "http://%s/api/0/config";
    private static final String FORMAT_URL_RESOURCE = "https://%s/clip/v2/resource/";
    private static final String FORMAT_URL_REGISTER = "http://%s/api";
    private static final String FORMAT_URL_EVENTS = "https://%s/eventstream/clip/v2";

    private static final int CLIP2_MINIMUM_VERSION = 1948086000;

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
                if (Integer.parseInt(swVersion) >= CLIP2_MINIMUM_VERSION) {
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
    private final boolean useHttp1;
    private final Object restartLock = new Object();

    private State onlineState = State.CLOSED;
    private Instant lastRequestTime = Instant.MIN;
    private Instant sessionExpireTime = Instant.MAX;
    private @Nullable Session http2Session;
    private @Nullable ScheduledFuture<?> checkAliveTask;

    /**
     * Constructor.
     *
     * @param httpClient the OH common HTTP client.
     * @param http2Client2
     * @param bridgeHandler the bridge handler.
     * @param hostName the host name (ip address) of the Hue bridge
     * @param applicationKey the application key.
     */
    public Clip2Bridge(HttpClient httpClient, HTTP2Client http2Client, Clip2BridgeHandler bridgeHandler,
            String hostName, String applicationKey) {
        logger.debug("Clip2Bridge()");
        this.httpClient = httpClient;
        this.http2Client = http2Client;
        this.applicationKey = applicationKey;
        this.bridgeHandler = bridgeHandler;
        this.hostName = hostName;
        baseUrl = String.format(FORMAT_URL_RESOURCE, hostName);
        eventUrl = String.format(FORMAT_URL_EVENTS, hostName);
        registrationUrl = String.format(FORMAT_URL_REGISTER, hostName);
        boolean http2HpackLoaded = false;
        try {
            PreEncodedHttpField field = new PreEncodedHttpField(HttpHeader.C_METHOD, "PUT");
            ByteBuffer bytes = ByteBuffer.allocate(32);
            field.putTo(bytes, HttpVersion.HTTP_2);
            http2HpackLoaded = true;
        } catch (Exception e) {
            logger.warn("Clip2Bridge() HTTP/2 hpack module not yet loaded; falling back to HTTP/1.1");
        }
        useHttp1 = !http2HpackLoaded;
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
            fatalError(this, AdapterErrorHandler.Error.TIMEOUT);
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
            boolean wasActive = onlineState == State.ACTIVE;
            onlineState = State.CLOSED;
            closeCheckAliveTask();
            closeSession();
            if (wasActive) {
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
     * @param cause the entity that caused this method to be called.
     * @param error the type of error.
     */
    private void fatalError(Object cause, BaseAdapter.Error error) {
        if (onlineState == State.CLOSED) {
            return;
        }
        String causeId = cause.getClass().getSimpleName();
        if (cause instanceof ContentAdapter) {
            logger.debug("fatalError() {} {} ignoring", causeId, error);
        } else if (AdapterErrorHandler.SOFT_ERROR.contains(error)) {
            synchronized (restartLock) {
                State priorState = onlineState;
                try {
                    logger.debug("fatalError() {} {} reconnecting", causeId, error);
                    onlineState = State.PASSIVE; // suppress handler notification
                    close();
                    openPassive();
                    if (priorState == State.ACTIVE) {
                        openActive();
                    }
                } catch (ApiException | HttpUnauthorizedException e) {
                    logger.warn("fatalError() {} {} reconnect failed {}", causeId, error, e.getMessage(), e);
                    onlineState = priorState; // re-enable handler notification
                    close();
                }
            }
        } else {
            logger.warn("fatalError() {} {} closing", causeId, error);
            close();
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
     */
    public Resources getResources(ResourceReference reference) throws ApiException {
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
        throttle();
        Session session = http2Session;
        if (Objects.isNull(session) || session.isClosed()) {
            throw new ApiException("HTTP 2 session is null or closed");
        }
        String url = getUrl(reference);
        HttpFields fields = new HttpFields();
        fields.put(HttpHeader.ACCEPT, MediaType.APPLICATION_JSON);
        fields.put(HttpHeader.USER_AGENT, APPLICATION_ID);
        fields.put(APPLICATION_KEY, applicationKey);
        HeadersFrame headers = new HeadersFrame(
                new MetaData.Request("GET", new HttpURI(url), HttpVersion.HTTP_2, fields), null, true);
        ContentAdapter adapter = new ContentAdapter(this);
        Completable<@Nullable Stream> completable = new Completable<>();
        logger.trace("GET {} HTTP/2", url);
        Stream stream = null;
        try {
            session.newStream(headers, completable, adapter);
            stream = Objects.requireNonNull(completable.get(TIMEOUT_SECONDS, TimeUnit.SECONDS));
            String json = adapter.completable.get(TIMEOUT_SECONDS, TimeUnit.SECONDS).trim();
            logger.trace("HTTP/2 200 OK << {}", json);
            Resources resources = Objects.requireNonNull(jsonParser.fromJson(json, Resources.class));
            if (logger.isDebugEnabled()) {
                resources.getErrors().forEach(error -> logger.debug("Resources error:{}", error));
            }
            return resources;
        } catch (ExecutionException e) {
            Throwable e2 = e.getCause();
            if (e2 instanceof HttpUnauthorizedException) {
                throw (HttpUnauthorizedException) e2;
            }
            throw new ApiException("Error sending request", e);
        } catch (InterruptedException | TimeoutException e) {
            throw new ApiException("Error sending request", e);
        } catch (JsonParseException e) {
            throw new ApiException("Parsing error", e);
        } finally {
            if (Objects.nonNull(stream)) {
                stream.reset(new ResetFrame(stream.getId(), 0), Callback.NOOP);
            }
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
    private void onEventData(String data) {
        if (onlineState != State.ACTIVE) {
            return;
        }
        logger.trace("onEventData() << {}", data);
        JsonElement jsonElement = JsonParser.parseString(data);
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
        HttpFields fields = new HttpFields();
        fields.put(HttpHeader.ACCEPT, MediaType.SERVER_SENT_EVENTS);
        fields.put(HttpHeader.USER_AGENT, APPLICATION_ID);
        fields.put(APPLICATION_KEY, applicationKey);
        MetaData.Request request = new MetaData.Request("GET", new HttpURI(eventUrl), HttpVersion.HTTP_2, fields);
        HeadersFrame headers = new HeadersFrame(request, null, true);
        EventAdapter adapter = new EventAdapter(this);
        Completable<@Nullable Stream> completable = new Completable<>();
        logger.trace("GET {} HTTP/2", eventUrl);
        Stream stream = null;
        try {
            session.newStream(headers, completable, adapter);
            stream = Objects.requireNonNull(completable.get(TIMEOUT_SECONDS, TimeUnit.SECONDS));
            stream.setIdleTimeout(0);
            stream.setAttribute(EVENT_STREAM_ID, session);
            adapter.completable.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
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
        SessionAdapter adapter = new SessionAdapter(this);
        Completable<@Nullable Session> completable = new Completable<>();
        try {
            http2Client.connect(httpClient.getSslContextFactory(), address, adapter, completable);
            http2Session = Objects.requireNonNull(completable.get(TIMEOUT_SECONDS, TimeUnit.SECONDS));
            checkAliveOk(); // initialise the session timeout window
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new ApiException("Error opening HTTP 2 session", e);
        }
    }

    /**
     * Use an HTTP/1.1 or HTTP/2 PUT command to send a resource to the server.
     *
     * <p>
     * <b>Developer Note:</b>
     * For best performance this method should use a new stream on the existing HTTP/2 session. However the Jetty HTTP2
     * library version currently used by OH [v9.4.50.v20221201] fails with an 'hpack' (header compression) error
     * <a href="https://github.com/eclipse/jetty.project/issues/9168">(Jetty Issue 9168)</a> when attempting to encode
     * the PUT method. This may be fixed when the OH Jetty library is increased to a higher version, or when Jetty
     * HTTP/2 is included in the OH core, but in the meantime we can fall back to using a regular HTTP/1.1 call.
     * </p>
     *
     * @param resource the resource to put.
     * @throws ApiException if something fails.
     */
    public void putResource(Resource resource) throws ApiException {
        if (onlineState == State.CLOSED) {
            return;
        }
        if (useHttp1) {
            putResourceHttp1Impl(resource);
        } else {
            putResourceHttp2Impl(resource);
        }
    }

    /**
     * Use an HTTP/1.1 PUT to send a Resource to the server.
     *
     * @param resource the resource to put.
     * @throws ApiException if something fails.
     */
    private void putResourceHttp1Impl(Resource resource) throws ApiException {
        throttle();
        String url = getUrl(new ResourceReference().setId(resource.getId()).setType(resource.getType()));
        String json = jsonParser.toJson(resource);
        logger.trace("PUT {} HTTP/1.1 >> {}", url, json);
        try {
            json = httpClient.newRequest(url).method(HttpMethod.PUT).header(APPLICATION_KEY, applicationKey)
                    .header(HttpHeader.USER_AGENT, APPLICATION_ID).accept(MediaType.APPLICATION_JSON)
                    .content(new StringContentProvider(MediaType.APPLICATION_JSON, json, StandardCharsets.UTF_8))
                    .timeout(TIMEOUT_SECONDS, TimeUnit.SECONDS).send().getContentAsString().trim();
            logger.trace("HTTP/1.1 200 OK << {}", json);
            Resources result = Objects.requireNonNull(jsonParser.fromJson(json, Resources.class));
            if (logger.isDebugEnabled()) {
                result.getErrors().forEach(error -> logger.debug("Resources error:{}", error));
            }
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            throw new ApiException("Error sending request", e);
        } catch (JsonParseException e) {
            throw new ApiException("Parsing error", e);
        }
    }

    /**
     * Use an HTTP/2 PUT to send a Resource to the server.
     *
     * @param resource the resource to put.
     * @throws ApiException if something fails.
     */
    private void putResourceHttp2Impl(Resource resource) throws ApiException {
        throttle();
        Session session = http2Session;
        if (Objects.isNull(session) || session.isClosed()) {
            throw new ApiException("HTTP 2 session is null or closed");
        }
        String jsonSend = jsonParser.toJson(resource);
        ByteBuffer jsonBytes = ByteBuffer.wrap(jsonSend.getBytes(StandardCharsets.UTF_8));
        String url = getUrl(new ResourceReference().setId(resource.getId()).setType(resource.getType()));
        HttpFields fields = new HttpFields();
        fields.put(HttpHeader.ACCEPT, MediaType.APPLICATION_JSON);
        fields.put(HttpHeader.USER_AGENT, APPLICATION_ID);
        fields.put(HttpHeader.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        fields.putLongField(HttpHeader.CONTENT_LENGTH, jsonBytes.capacity());
        fields.put(APPLICATION_KEY, applicationKey);
        HeadersFrame headers = new HeadersFrame(
                new MetaData.Request("PUT", new HttpURI(url), HttpVersion.HTTP_2, fields), null, false);
        ContentAdapter adapter = new ContentAdapter(this);
        Completable<@Nullable Stream> completable = new Completable<>();
        logger.trace("PUT {} HTTP/2 >> {}", url, jsonSend);
        Stream stream = null;
        try {
            session.newStream(headers, completable, adapter);
            stream = Objects.requireNonNull(completable.get(TIMEOUT_SECONDS, TimeUnit.SECONDS));
            stream.data(new DataFrame(stream.getId(), jsonBytes, true), Callback.NOOP);
            String jsonRead = adapter.completable.get(TIMEOUT_SECONDS, TimeUnit.SECONDS).trim();
            logger.trace("HTTP/2 200 OK << {}", jsonRead);
            Resources resources = Objects.requireNonNull(jsonParser.fromJson(jsonRead, Resources.class));
            if (logger.isDebugEnabled()) {
                resources.getErrors().forEach(error -> logger.debug("Resources error:{}", error));
            }
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            throw new ApiException("Error sending request", e);
        } catch (JsonParseException e) {
            throw new ApiException("Parsing error", e);
        } finally {
            if (Objects.nonNull(stream)) {
                stream.reset(new ResetFrame(stream.getId(), 0), Callback.NOOP);
            }
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
