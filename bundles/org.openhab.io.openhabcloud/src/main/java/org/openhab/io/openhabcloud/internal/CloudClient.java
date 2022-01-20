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
package org.openhab.io.openhabcloud.internal;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.BufferUtil;
import org.eclipse.jetty.util.URIUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.openhab.core.OpenHAB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.socket.backo.Backoff;
import io.socket.client.IO;
import io.socket.client.Manager;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.Transport;
import io.socket.thread.EventThread;

/**
 * This class provides communication between openHAB and the openHAB Cloud service.
 * It also implements async http proxy for serving requests from user to
 * openHAB through the openHAB Cloud. It uses Socket.IO connection to connect to
 * the openHAB Cloud service and Jetty Http client to send local http requests to
 * openHAB.
 *
 * @author Victor Belov - Initial contribution
 * @author Kai Kreuzer - migrated code to new Jetty client and ESH APIs
 */
public class CloudClient {
    /*
     * Logger for this class
     */
    private final Logger logger = LoggerFactory.getLogger(CloudClient.class);

    /*
     * This variable holds base URL for the openHAB Cloud connections
     */
    private final String baseURL;

    /*
     * This variable holds openHAB's UUID for authenticating and connecting to the openHAB Cloud
     */
    private final String uuid;

    /*
     * This variable holds openHAB's secret for authenticating and connecting to the openHAB Cloud
     */
    private final String secret;

    /*
     * This variable holds local openHAB's base URL for connecting to the local openHAB instance
     */
    private final String localBaseUrl;

    /*
     * This variable holds instance of Jetty HTTP client to make requests to local openHAB
     */
    private final HttpClient jettyClient;

    /*
     * This map holds HTTP requests to local openHAB which are currently running
     */
    private final Map<Integer, Request> runningRequests = new ConcurrentHashMap<>();

    /*
     * This variable indicates if connection to the openHAB Cloud is currently in an established state
     */
    private boolean isConnected;

    /*
     * This variable holds version of local openHAB
     */
    private String openHABVersion;

    /*
     * This variable holds instance of Socket.IO client class which provides communication
     * with the openHAB Cloud
     */
    private Socket socket;

    /*
     * The protocol of the openHAB-cloud URL.
     */
    private String protocol = "https";

    /*
     * This variable holds instance of CloudClientListener which provides callbacks to communicate
     * certain events from the openHAB Cloud back to openHAB
     */
    private CloudClientListener listener;
    private boolean remoteAccessEnabled;
    private Set<String> exposedItems;

    /**
     * Back-off strategy for reconnecting when manual reconnection is needed
     */
    private final Backoff reconnectBackoff = new Backoff();

    /**
     * Constructor of CloudClient
     *
     * @param uuid openHAB's UUID to connect to the openHAB Cloud
     * @param secret openHAB's Secret to connect to the openHAB Cloud
     * @param remoteAccessEnabled Allow the openHAB Cloud to be used as a remote proxy
     * @param exposedItems Items that are made available to apps connected to the openHAB Cloud
     */
    public CloudClient(HttpClient httpClient, String uuid, String secret, String baseURL, String localBaseUrl,
            boolean remoteAccessEnabled, Set<String> exposedItems) {
        this.uuid = uuid;
        this.secret = secret;
        this.baseURL = baseURL;
        this.localBaseUrl = localBaseUrl;
        this.remoteAccessEnabled = remoteAccessEnabled;
        this.exposedItems = exposedItems;
        this.jettyClient = httpClient;
        reconnectBackoff.setMin(1000);
        reconnectBackoff.setMax(30_000);
        reconnectBackoff.setJitter(0.5);
    }

    /**
     * Connect to the openHAB Cloud
     */

    public void connect() {
        try {
            socket = IO.socket(baseURL);
            URL parsed = new URL(baseURL);
            protocol = parsed.getProtocol();
        } catch (URISyntaxException e) {
            logger.error("Error creating Socket.IO: {}", e.getMessage());
        } catch (MalformedURLException e) {
            logger.error("Error parsing baseURL to get protocol, assuming https. Error: {}", e.getMessage());
        }
        socket.io().on(Manager.EVENT_TRANSPORT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                logger.trace("Manager.EVENT_TRANSPORT");
                Transport transport = (Transport) args[0];
                transport.on(Transport.EVENT_REQUEST_HEADERS, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        logger.trace("Transport.EVENT_REQUEST_HEADERS");
                        @SuppressWarnings("unchecked")
                        Map<String, List<String>> headers = (Map<String, List<String>>) args[0];
                        headers.put("uuid", List.of(uuid));
                        headers.put("secret", List.of(secret));
                        headers.put("openhabversion", List.of(OpenHAB.getVersion()));
                        headers.put("clientversion", List.of(CloudService.clientVersion));
                        headers.put("remoteaccess", List.of(((Boolean) remoteAccessEnabled).toString()));
                    }
                });
            }
        }).on(Manager.EVENT_CONNECT_ERROR, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                if (args.length > 0) {
                    if (args[0] instanceof Exception) {
                        Exception e = (Exception) args[0];
                        logger.debug(
                                "Error connecting to the openHAB Cloud instance: {} {}. Should reconnect automatically.",
                                e.getClass().getSimpleName(), e.getMessage());
                    } else {
                        logger.debug(
                                "Error connecting to the openHAB Cloud instance: {}. Should reconnect automatically.",
                                args[0]);
                    }
                } else {
                    logger.debug("Error connecting to the openHAB Cloud instance. Should reconnect automatically.");
                }
            }
        });
        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                logger.debug("Socket.IO connected");
                isConnected = true;
                onConnect();
            }
        }).on(Socket.EVENT_CONNECTING, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                logger.debug("Socket.IO connecting");
            }
        }).on(Socket.EVENT_RECONNECTING, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                logger.debug("Socket.IO re-connecting (attempt {})", args[0]);
            }
        }).on(Socket.EVENT_RECONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                logger.debug("Socket.IO re-connected successfully (attempt {})", args[0]);
            }
        }).on(Socket.EVENT_RECONNECT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if (args[0] instanceof Exception) {
                    Exception e = (Exception) args[0];
                    logger.debug("Socket.IO re-connect attempt error: {} {}", e.getClass().getSimpleName(),
                            e.getMessage());
                } else {
                    logger.debug("Socket.IO re-connect attempt error: {}", args[0]);
                }
            }
        }).on(Socket.EVENT_RECONNECT_FAILED, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                logger.debug("Socket.IO re-connect attempts failed. Stopping reconnection.");
            }
        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if (args.length > 0) {
                    logger.debug("Socket.IO disconnected: {}", args[0]);
                } else {
                    logger.debug("Socket.IO disconnected");
                }
                isConnected = false;
                onDisconnect();
            }
        }).on(Socket.EVENT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if (CloudClient.this.socket.connected()) {
                    if (logger.isDebugEnabled() && args.length > 0) {
                        logger.error("Error during communication: {}", args[0]);
                    } else {
                        logger.error("Error during communication");
                    }
                } else {
                    // We are not connected currently, manual reconnection is needed to keep trying to (re-)establish
                    // connection.
                    //
                    // Socket.IO 1.x java client: 'error' event is emitted from Socket on connection errors that are not
                    // retried, but also with error that are automatically retried. If we
                    //
                    // Note how this is different in Socket.IO 2.x java client, Socket emits 'connect_error' event.
                    // OBS: Don't get confused with Socket IO 2.x docs online, in 1.x connect_error is emitted also on
                    // errors that are retried by the library automatically!
                    long delay = reconnectBackoff.duration();
                    // Try reconnecting on connection errors
                    if (logger.isDebugEnabled() && args.length > 0) {
                        if (args[0] instanceof Exception) {
                            Exception e = (Exception) args[0];
                            logger.error(
                                    "Error connecting to the openHAB Cloud instance: {} {}. Reconnecting after {} ms.",
                                    e.getClass().getSimpleName(), e.getMessage(), delay);
                        } else {
                            logger.error(
                                    "Error connecting to the openHAB Cloud instance: {}. Reconnecting after {} ms.",
                                    args[0], delay);
                        }
                    } else {
                        logger.error("Error connecting to the openHAB Cloud instance. Reconnecting.");
                    }
                    socket.close();
                    sleepSocketIO(delay);
                    socket.connect();
                }
            }
        }).on("request", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                onEvent("request", (JSONObject) args[0]);
            }
        }).on("cancel", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                onEvent("cancel", (JSONObject) args[0]);
            }
        }).on("command", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                onEvent("command", (JSONObject) args[0]);
            }
        });
        socket.connect();
    }

    /**
     * Callback method for socket.io client which is called when connection is established
     */

    public void onConnect() {
        logger.info("Connected to the openHAB Cloud service (UUID = {}, base URL = {})", this.uuid, this.localBaseUrl);
        reconnectBackoff.reset();
        isConnected = true;
    }

    /**
     * Callback method for socket.io client which is called when disconnect occurs
     */

    public void onDisconnect() {
        logger.info("Disconnected from the openHAB Cloud service (UUID = {}, base URL = {})", this.uuid,
                this.localBaseUrl);
        isConnected = false;
        // And clean up the list of running requests
        runningRequests.clear();
    }

    /**
     * Callback method for socket.io client which is called when an error occurs
     */

    public void onError(IOException error) {
        logger.debug("{}", error.getMessage());
    }

    /**
     * Callback method for socket.io client which is called when a message is received
     */

    public void onEvent(String event, JSONObject data) {
        logger.debug("on(): {}", event);
        if ("command".equals(event)) {
            handleCommandEvent(data);
            return;
        }
        if (remoteAccessEnabled) {
            if ("request".equals(event)) {
                handleRequestEvent(data);
            } else if ("cancel".equals(event)) {
                handleCancelEvent(data);
            } else {
                logger.warn("Unsupported event from openHAB Cloud: {}", event);
            }
        }
    }

    private void handleRequestEvent(JSONObject data) {
        try {
            // Get unique request Id
            int requestId = data.getInt("id");
            logger.debug("Got request {}", requestId);
            // Get request path
            String requestPath = data.getString("path");
            logger.debug("Path {}", requestPath);
            // Get request method
            String requestMethod = data.getString("method");
            logger.debug("Method {}", requestMethod);
            // Get JSONObject for request headers
            JSONObject requestHeadersJson = data.getJSONObject("headers");
            logger.debug("Headers: {}", requestHeadersJson.toString());
            // Get request body
            String requestBody = data.getString("body");
            logger.trace("Body {}", requestBody);
            // Get JSONObject for request query parameters
            JSONObject requestQueryJson = data.getJSONObject("query");
            logger.debug("Query {}", requestQueryJson.toString());
            // Create URI builder with base request URI of openHAB and path from request
            String newPath = URIUtil.addPaths(localBaseUrl, requestPath);
            Iterator<String> queryIterator = requestQueryJson.keys();
            // Add query parameters to URI builder, if any
            newPath += "?";
            while (queryIterator.hasNext()) {
                String queryName = queryIterator.next();
                newPath += queryName;
                newPath += "=";
                newPath += URLEncoder.encode(requestQueryJson.getString(queryName), "UTF-8");
                if (queryIterator.hasNext()) {
                    newPath += "&";
                }
            }
            // Finally get the future request URI
            URI requestUri = new URI(newPath);
            // All preparations which are common for different methods are done
            // Now perform the request to openHAB
            // If method is GET
            logger.debug("Request method is {}", requestMethod);
            Request request = jettyClient.newRequest(requestUri);
            setRequestHeaders(request, requestHeadersJson);
            String proto = protocol;
            if (data.has("protocol")) {
                proto = data.getString("protocol");
            }
            request.header("X-Forwarded-Proto", proto);
            HttpMethod method = HttpMethod.fromString(requestMethod);
            if (method == null) {
                logger.debug("Unsupported request method {}", requestMethod);
                return;
            }
            request.method(method);
            if (!requestBody.isEmpty()) {
                request.content(new BytesContentProvider(requestBody.getBytes()));
            }

            request.onResponseHeaders(response -> {
                logger.debug("onHeaders {}", requestId);
                JSONObject responseJson = new JSONObject();
                try {
                    responseJson.put("id", requestId);
                    responseJson.put("headers", getJSONHeaders(response.getHeaders()));
                    responseJson.put("responseStatusCode", response.getStatus());
                    responseJson.put("responseStatusText", "OK");
                    socket.emit("responseHeader", responseJson);
                    logger.trace("Sent headers to request {}", requestId);
                    logger.trace("{}", responseJson.toString());
                } catch (JSONException e) {
                    logger.debug("{}", e.getMessage());
                }
            }).onResponseContent((theResponse, content) -> {
                logger.debug("onResponseContent: {}, content size {}", requestId, String.valueOf(content.remaining()));
                JSONObject responseJson = new JSONObject();
                try {
                    responseJson.put("id", requestId);
                    responseJson.put("body", BufferUtil.toArray(content));
                    if (logger.isTraceEnabled()) {
                        logger.trace("{}", StandardCharsets.UTF_8.decode(content).toString());
                    }
                    socket.emit("responseContentBinary", responseJson);
                    logger.trace("Sent content to request {}", requestId);
                } catch (JSONException e) {
                    logger.debug("{}", e.getMessage());
                }
            }).onRequestFailure((origRequest, failure) -> {
                logger.debug("onRequestFailure: {},  {}", requestId, failure.getMessage());
                JSONObject responseJson = new JSONObject();
                try {
                    responseJson.put("id", requestId);
                    responseJson.put("responseStatusText", "openHAB connection error: " + failure.getMessage());
                    socket.emit("responseError", responseJson);
                } catch (JSONException e) {
                    logger.debug("{}", e.getMessage());
                }
            }).send(result -> {
                logger.debug("onComplete: {}", requestId);
                // Remove this request from list of running requests
                runningRequests.remove(requestId);
                if ((result != null && result.isFailed())
                        && (result.getResponse() != null && result.getResponse().getStatus() != HttpStatus.OK_200)) {
                    if (result.getFailure() != null) {
                        logger.debug("Jetty request {} failed: {}", requestId, result.getFailure().getMessage());
                    }
                    if (result.getRequestFailure() != null) {
                        logger.debug("Request Failure: {}", result.getRequestFailure().getMessage());
                    }
                    if (result.getResponseFailure() != null) {
                        logger.debug("Response Failure: {}", result.getResponseFailure().getMessage());
                    }
                }
                JSONObject responseJson = new JSONObject();
                try {
                    responseJson.put("id", requestId);
                    socket.emit("responseFinished", responseJson);
                    logger.debug("Finished responding to request {}", requestId);
                } catch (JSONException e) {
                    logger.debug("{}", e.getMessage());
                }
            });

            // If successfully submitted request to http client, add it to the list of currently
            // running requests to be able to cancel it if needed
            runningRequests.put(requestId, request);
        } catch (JSONException | IOException | URISyntaxException e) {
            logger.debug("{}", e.getMessage());
        }
    }

    private void setRequestHeaders(Request request, JSONObject requestHeadersJson) {
        Iterator<String> headersIterator = requestHeadersJson.keys();
        // Convert JSONObject of headers into Header ArrayList
        while (headersIterator.hasNext()) {
            String headerName = headersIterator.next();
            String headerValue;
            try {
                headerValue = requestHeadersJson.getString(headerName);
                logger.debug("Jetty set header {} = {}", headerName, headerValue);
                if (!headerName.equalsIgnoreCase("Content-Length")) {
                    request.header(headerName, headerValue);
                }
            } catch (JSONException e) {
                logger.warn("Error processing request headers: {}", e.getMessage());
            }
        }
    }

    private void handleCancelEvent(JSONObject data) {
        try {
            int requestId = data.getInt("id");
            logger.debug("Received cancel for request {}", requestId);
            // Find and abort running request
            Request request = runningRequests.get(requestId);
            if (request != null) {
                request.abort(new InterruptedException());
                runningRequests.remove(requestId);
            }
        } catch (JSONException e) {
            logger.debug("{}", e.getMessage());
        }
    }

    private void handleCommandEvent(JSONObject data) {
        String itemName = data.getString("item");
        if (exposedItems.contains(itemName)) {
            try {
                logger.debug("Received command {} for item {}.", data.getString("command"), itemName);
                if (this.listener != null) {
                    this.listener.sendCommand(itemName, data.getString("command"));
                }
            } catch (JSONException e) {
                logger.debug("{}", e.getMessage());
            }
        } else {
            logger.warn("Received command from openHAB Cloud for item '{}', which is not exposed.", itemName);
        }
    }

    /**
     * This method sends notification to the openHAB Cloud
     *
     * @param userId openHAB Cloud user id
     * @param message notification message text
     * @param icon name of the icon for this notification
     * @param severity severity name for this notification
     */
    public void sendNotification(String userId, String message, @Nullable String icon, @Nullable String severity) {
        if (isConnected()) {
            JSONObject notificationMessage = new JSONObject();
            try {
                notificationMessage.put("userId", userId);
                notificationMessage.put("message", message);
                notificationMessage.put("icon", icon);
                notificationMessage.put("severity", severity);
                socket.emit("notification", notificationMessage);
            } catch (JSONException e) {
                logger.debug("{}", e.getMessage());
            }
        } else {
            logger.debug("No connection, notification is not sent");
        }
    }

    /**
     * This method sends log notification to the openHAB Cloud
     *
     * @param message notification message text
     * @param icon name of the icon for this notification
     * @param severity severity name for this notification
     */
    public void sendLogNotification(String message, @Nullable String icon, @Nullable String severity) {
        if (isConnected()) {
            JSONObject notificationMessage = new JSONObject();
            try {
                notificationMessage.put("message", message);
                notificationMessage.put("icon", icon);
                notificationMessage.put("severity", severity);
                socket.emit("lognotification", notificationMessage);
            } catch (JSONException e) {
                logger.debug("{}", e.getMessage());
            }
        } else {
            logger.debug("No connection, notification is not sent");
        }
    }

    /**
     * This method sends broadcast notification to the openHAB Cloud
     *
     * @param message notification message text
     * @param icon name of the icon for this notification
     * @param severity severity name for this notification
     */
    public void sendBroadcastNotification(String message, @Nullable String icon, @Nullable String severity) {
        if (isConnected()) {
            JSONObject notificationMessage = new JSONObject();
            try {
                notificationMessage.put("message", message);
                notificationMessage.put("icon", icon);
                notificationMessage.put("severity", severity);
                socket.emit("broadcastnotification", notificationMessage);
            } catch (JSONException e) {
                logger.debug("{}", e.getMessage());
            }
        } else {
            logger.debug("No connection, notification is not sent");
        }
    }

    /**
     * Send item update to openHAB Cloud
     *
     * @param itemName the name of the item
     * @param itemState updated item state
     *
     */
    public void sendItemUpdate(String itemName, String itemState) {
        if (isConnected()) {
            logger.debug("Sending update '{}' for item '{}'", itemState, itemName);
            JSONObject itemUpdateMessage = new JSONObject();
            try {
                itemUpdateMessage.put("itemName", itemName);
                itemUpdateMessage.put("itemStatus", itemState);
                socket.emit("itemupdate", itemUpdateMessage);
            } catch (JSONException e) {
                logger.debug("{}", e.getMessage());
            }
        } else {
            logger.debug("No connection, Item update is not sent");
        }
    }

    /**
     * Returns true if openHAB Cloud connection is active
     */
    public boolean isConnected() {
        return isConnected;
    }

    /**
     * Disconnect from openHAB Cloud
     */
    public void shutdown() {
        logger.info("Shutting down openHAB Cloud service connection");
        socket.disconnect();
    }

    public String getOpenHABVersion() {
        return openHABVersion;
    }

    public void setOpenHABVersion(String openHABVersion) {
        this.openHABVersion = openHABVersion;
    }

    public void setListener(CloudClientListener listener) {
        this.listener = listener;
    }

    private JSONObject getJSONHeaders(HttpFields httpFields) {
        JSONObject headersJSON = new JSONObject();
        try {
            for (HttpField field : httpFields) {
                headersJSON.put(field.getName(), field.getValue());
            }
        } catch (JSONException e) {
            logger.warn("Error forming response headers: {}", e.getMessage());
        }
        return headersJSON;
    }

    private void sleepSocketIO(long delay) {
        EventThread.exec(() -> {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {

            }
        });
    }
}
