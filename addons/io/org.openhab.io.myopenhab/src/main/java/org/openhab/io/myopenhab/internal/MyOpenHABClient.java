/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.io.myopenhab.internal;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Request.FailureListener;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Response.ContentListener;
import org.eclipse.jetty.client.api.Response.HeadersListener;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.BufferUtil;
import org.eclipse.jetty.util.URIUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.openhab.core.OpenHAB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.socket.client.IO;
import io.socket.client.Manager;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.Transport;

/**
 * This class provides communication between openHAB and my.openHAB service.
 * It also implements async http proxy for serving requests from user to
 * openHAB through my.openHAB. It uses Socket.IO connection to connect to
 * my.openHAB service and Jetty Http client to send local http requests to
 * openHAB.
 *
 * @author Victor Belov - Initial contribution
 * @author Kai Kreuzer - migrated code to new Jetty client and ESH APIs
 *
 */

public class MyOpenHABClient {
    /*
     * Logger for this class
     */
    private static Logger logger = LoggerFactory.getLogger(MyOpenHABClient.class);
    /*
     * This constant defines maximum number of HTTP connections per peer
     * address for HTTP client which performs local connections to openHAB
     */
    private static final int HTTP_CLIENT_MAX_CONNECTIONS_PER_DEST = 200;

    /*
     * This constant defines HTTP request timeout. It should be kept at about
     * 30 seconds minimum to make it work for long polling requests
     */
    private static final int HTTP_CLIENT_TIMEOUT = 30000;

    /*
     * This variable holds base URL for my.openHAB cloud connections, has a default
     * value but can be changed
     */
    private String myOpenHABBaseUrl = "https://my.openhab.org/";

    /*
     * This variable holds openHAB's UUID for authenticating and connecting to my.openHAB cloud
     */
    private String uuid;

    /*
     * This variable holds openHAB's secret for authenticating and connecting to my.openHAB cloud
     */
    private String secret;
    /*
     * This variable holds local openHAB's base URL for connecting to local openHAB instance
     */
    private String localBaseUrl = "http://localhost:"
            + Integer.parseInt(System.getProperty("org.osgi.service.http.port"));

    /*
     * This variable holds instance of Jetty HTTP client to make requests to local openHAB
     */
    private HttpClient jettyClient;
    /*
     * This hashmap holds HTTP requests to local openHAB which are currently running
     */
    private HashMap<Integer, Request> runningRequests;
    /*
     * This variable indicates if connection to my.openHAB cloud is currently in an established state
     */
    private boolean isConnected;
    /*
     * This variable holds version of local openHAB
     */
    private String openHABVersion;
    /*
     * This variable holds instance of Socket.IO client class which provides communication
     * with my.openHAB cloud
     */
    private Socket socket;
    /*
     * This variable holds instance of MyOHClientListener which provides callbacks to communicate
     * certain events from my.openHAB cloud back to openHAB
     */
    private MyOpenHABClientListener listener;

    /**
     * Constructor of MyOHClient
     *
     * @param uuid openHAB's UUID to connect to my.openHAB
     * @param secret openHAB's Secret to connect to my.openHAB
     *
     */
    public MyOpenHABClient(String uuid, String secret) {
        this.uuid = uuid;
        this.secret = secret;
        runningRequests = new HashMap<Integer, Request>();
        jettyClient = new HttpClient();
        jettyClient.setMaxConnectionsPerDestination(HTTP_CLIENT_MAX_CONNECTIONS_PER_DEST);
        jettyClient.setConnectTimeout(HTTP_CLIENT_TIMEOUT);
    }

    /**
     * Connect to my.openHAB
     */

    public void connect() {
        try {
            socket = IO.socket(myOpenHABBaseUrl);
        } catch (URISyntaxException e) {
            logger.error("Error creating Socket.IO: {}", e.getMessage());
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
                        headers.put("uuid", Arrays.asList(uuid));
                        headers.put("secret", Arrays.asList(secret));
                        headers.put("openhabversion", Arrays.asList(OpenHAB.getVersion()));
                        headers.put("myohversion", Arrays.asList(MyOpenHABService.myohVersion));
                    }
                });
            }
        });
        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                logger.debug("Socket.IO connected");
                isConnected = true;
                onConnect();
            }
        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                logger.debug("Socket.IO disconnected");
                isConnected = false;
                onDisconnect();
            }
        }).on(Socket.EVENT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                logger.error("Socket.IO error: " + args[0]);
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
        logger.info("Connected to my.openHAB service (UUID = {}, base URL = {})", this.uuid, this.localBaseUrl);
        isConnected = true;
        // On connect start jetty client to process local requests to openHAB
        if (jettyClient != null) {
            try {
                jettyClient.start();
            } catch (Exception e) {
                logger.error("Could not start Jetty client: {}", e.getMessage());
            }
        }
    }

    /**
     * Callback method for socket.io client which is called when disconnect occurs
     */

    public void onDisconnect() {
        logger.info("Disconnected from my.openHAB service (UUID = {}, base URL = {})", this.uuid, this.localBaseUrl);
        isConnected = false;
        // On disconnect stop jetty client to shutdown all ongoing requests if there were any
        if (jettyClient != null) {
            try {
                jettyClient.stop();
            } catch (Exception e) {
                logger.error("Could not stop Jetty client: {}", e.getMessage());
            }
        }
        // And clean up the list of running requests
        if (runningRequests != null) {
            runningRequests.clear();
        }
    }

    /**
     * Callback method for socket.io client which is called when an error occurs
     */

    public void onError(IOException error) {
        logger.error(error.getMessage());
    }

    /**
     * Callback method for socket.io client which is called when a message is received
     */

    public void onEvent(String event, JSONObject data) {
        logger.debug("on(): " + event);
        if ("request".equals(event)) {
            handleRequestEvent(data);
        } else if ("cancel".equals(event)) {
            handleCancelEvent(data);
        } else if ("command".equals(event)) {
            handleCommandEvent(data);
        } else {
            logger.warn("Unsupported event from my.openHAB: {}", event);
        }
    }

    private void handleRequestEvent(JSONObject data) {
        try {
            // Get myOH unique request Id
            int requestId = data.getInt("id");
            logger.debug("Got request {}", requestId);
            // Get request path
            String requestPath = data.getString("path");
            // Get request method
            String requestMethod = data.getString("method");
            // Get request body
            String requestBody = data.getString("body");
            // Get JSONObject for request headers
            JSONObject requestHeadersJson = data.getJSONObject("headers");
            logger.debug(requestHeadersJson.toString());
            // Get JSONObject for request query parameters
            JSONObject requestQueryJson = data.getJSONObject("query");
            // Create URI builder with base request URI of openHAB and path from request
            String newPath = URIUtil.addPaths(localBaseUrl, requestPath);
            @SuppressWarnings("unchecked")
            Iterator<String> queryIterator = requestQueryJson.keys();
            // Add query parameters to URI builder, if any
            newPath += "?";
            while (queryIterator.hasNext()) {
                String queryName = queryIterator.next();
                newPath += queryName;
                newPath += "=";
                newPath += URLEncoder.encode(requestQueryJson.getString(queryName), "UTF-8");
                if (queryIterator.hasNext())
                    newPath += "&";
            }
            // Finally get the future request URI
            URI requestUri = new URI(newPath);
            // All preparations which are common for different methods are done
            // Now perform the request to openHAB
            // If method is GET
            logger.debug("Request method is " + requestMethod);
            Request request = jettyClient.newRequest(requestUri);
            setRequestHeaders(request, requestHeadersJson);
            request.header("X-Forwarded-Proto", "https");
            if (requestMethod.equals("GET")) {
                request.method(HttpMethod.GET);
            } else if (requestMethod.equals("POST")) {
                request.method(HttpMethod.POST);
                request.content(new BytesContentProvider(requestBody.getBytes()));
            } else if (requestMethod.equals("PUT")) {
                request.method(HttpMethod.PUT);
                request.content(new BytesContentProvider(requestBody.getBytes()));
            } else {
                // TODO: Reject unsupported methods
                logger.error("Unsupported request method " + requestMethod);
                return;
            }
            ResponseListener listener = new ResponseListener(requestId);
            request.onResponseHeaders(listener).onResponseContent(listener).onRequestFailure(listener).send(listener);
            // If successfully submitted request to http client, add it to the list of currently
            // running requests to be able to cancel it if needed
            runningRequests.put(requestId, request);
        } catch (JSONException e) {
            logger.error(e.getMessage());
        } catch (IOException e) {
            logger.error(e.getMessage());
        } catch (URISyntaxException e) {
            logger.error(e.getMessage());
        }
    }

    private void setRequestHeaders(Request request, JSONObject requestHeadersJson) {
        @SuppressWarnings("unchecked")
        Iterator<String> headersIterator = requestHeadersJson.keys();
        // Convert JSONObject of headers into Header ArrayList
        while (headersIterator.hasNext()) {
            String headerName = headersIterator.next();
            String headerValue;
            try {
                headerValue = requestHeadersJson.getString(headerName);
                logger.debug("Jetty set header " + headerName + " = " + headerValue);
                if (!headerName.equalsIgnoreCase("Content-Length")) {
                    request.header(headerName, headerValue);
                }
            } catch (JSONException e) {
                logger.error("Error processing request headers: {}", e.getMessage());
            }
        }
    }

    private void handleCancelEvent(JSONObject data) {
        try {
            int requestId = data.getInt("id");
            logger.debug("Received cancel for request {}", requestId);
            // Find and abort running request
            if (runningRequests.containsKey(requestId)) {
                Request request = runningRequests.get(requestId);
                request.abort(new InterruptedException());
                runningRequests.remove(requestId);
            }
        } catch (JSONException e) {
            logger.error(e.getMessage());
        }
    }

    private void handleCommandEvent(JSONObject data) {
        try {
            logger.debug("Received command " + data.getString("command") + " for item " + data.getString("item"));
            if (this.listener != null)
                this.listener.sendCommand(data.getString("item"), data.getString("command"));
        } catch (JSONException e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * This method sends notification to my.openHAB
     *
     * @param userId my.openHAB user id
     * @param message notification message text
     * @param icon name of the icon for this notification
     * @param severity severity name for this notification
     *
     */

    public void sendNotification(String userId, String message, String icon, String severity) {
        if (isConnected()) {
            JSONObject notificationMessage = new JSONObject();
            try {
                notificationMessage.put("userId", userId);
                notificationMessage.put("message", message);
                notificationMessage.put("icon", icon);
                notificationMessage.put("severity", severity);
                socket.emit("notification", notificationMessage);
            } catch (JSONException e) {
                logger.error(e.getMessage());
            }
        } else {
            logger.debug("No connection, notification is not sent");
        }
    }

    /**
     * This method sends log notification to my.openHAB
     *
     * @param message notification message text
     * @param icon name of the icon for this notification
     * @param severity severity name for this notification
     *
     */

    public void sendLogNotification(String message, String icon, String severity) {
        if (isConnected()) {
            JSONObject notificationMessage = new JSONObject();
            try {
                notificationMessage.put("message", message);
                notificationMessage.put("icon", icon);
                notificationMessage.put("severity", severity);
                socket.emit("lognotification", notificationMessage);
            } catch (JSONException e) {
                logger.error(e.getMessage());
            }
        } else {
            logger.debug("No connection, notification is not sent");
        }
    }

    /**
     * This method sends broadcast notification to my.openHAB
     *
     * @param message notification message text
     * @param icon name of the icon for this notification
     * @param severity severity name for this notification
     *
     */

    public void sendBroadcastNotification(String message, String icon, String severity) {
        if (isConnected()) {
            JSONObject notificationMessage = new JSONObject();
            try {
                notificationMessage.put("message", message);
                notificationMessage.put("icon", icon);
                notificationMessage.put("severity", severity);
                socket.emit("broadcastnotification", notificationMessage);
            } catch (JSONException e) {
                logger.error(e.getMessage());
            }
        } else {
            logger.debug("No connection, notification is not sent");
        }
    }

    /**
     * Send SMS to my.openHAB
     *
     * @param phone number to send notification to
     * @param message text to send
     *
     */

    public void sendSMS(String phone, String message) {
        if (isConnected()) {
            JSONObject smsMessage = new JSONObject();
            try {
                smsMessage.put("phone", phone);
                smsMessage.put("message", message);
                socket.emit("sms", smsMessage);
            } catch (JSONException e) {
                logger.error(e.getMessage());
            }
        } else {
            logger.debug("No connection, SMS is not sent");
        }
    }

    /**
     * Send item update to my.openHAB
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
                logger.error(e.getMessage());
            }
        } else {
            logger.debug("No connection, Item update is not sent");
        }
    }

    /**
     * Returns true if my.openHAB connection is active
     */

    public boolean isConnected() {
        return isConnected;
    }

    /**
     * Disconnect from my.openHAB
     */

    public void shutdown() {
        logger.info("Shutting down my.openHAB service connection");
        try {
            jettyClient.stop();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        socket.disconnect();
    }

    /**
     * Set base URL of my.openHAB cloud
     *
     * @param myOHBaseUrl base URL in http://host:port form
     *
     */

    public void setMyOHBaseUrl(String myOHBaseUrl) {
        myOpenHABBaseUrl = myOHBaseUrl;
    }

    /**
     * Set base local URL of openHAB
     *
     * @param ohBaseUrl base local URL of openHAB in http://host:port form
     *
     */

    public void setOHBaseUrl(String ohBaseUrl) {
        this.localBaseUrl = ohBaseUrl;
    }

    public String getOpenHABVersion() {
        return openHABVersion;
    }

    public void setOpenHABVersion(String openHABVersion) {
        this.openHABVersion = openHABVersion;
    }

    public void setListener(MyOpenHABClientListener listener) {
        this.listener = listener;
    }

    /*
     * An internal class which extends ContentExchange and forwards response
     * headers and data back to my.openHAB
     *
     */

    private class ResponseListener
            implements Response.CompleteListener, HeadersListener, ContentListener, FailureListener {

        private int mRequestId;
        private boolean mHeadersSent = false;

        public ResponseListener(int requestId) {
            mRequestId = requestId;
        }

        public JSONObject getJSONHeaders(HttpFields httpFields) {
            JSONObject headersJSON = new JSONObject();
            try {
                for (HttpField field : httpFields) {
                    headersJSON.put(field.getName(), field.getValue());
                }
            } catch (JSONException e) {
                logger.error("Error forming response headers: {}", e.getMessage());
            }
            return headersJSON;
        }

        @Override
        public void onComplete(Result result) {
            // Remove this request from list of running requests
            runningRequests.remove(mRequestId);
            if (result.isFailed()) {
                logger.warn("Jetty request {} failed: {}", mRequestId, result.getFailure().getMessage());
                logger.warn(result.getRequestFailure().getMessage());
                logger.warn(result.getResponseFailure().getMessage());
            }
            JSONObject responseJson = new JSONObject();
            try {
                responseJson.put("id", mRequestId);
                socket.emit("responseFinished", responseJson);
                logger.debug("Finished responding to request {}", mRequestId);
            } catch (JSONException e) {
                logger.error(e.getMessage());
            }

        }

        @Override
        public void onFailure(Request request, Throwable failure) {
            logger.error(failure.getMessage());
            JSONObject responseJson = new JSONObject();
            try {
                responseJson.put("id", mRequestId);
                responseJson.put("responseStatusText", "openHAB connection error: " + failure.getMessage());
                socket.emit("responseError", responseJson);
            } catch (JSONException e) {
                logger.error(e.getMessage());
            }
        }

        @Override
        public void onContent(Response response, ByteBuffer content) {
            logger.debug("Jetty received response content of size " + String.valueOf(content.remaining()));
            JSONObject responseJson = new JSONObject();
            try {
                responseJson.put("id", mRequestId);
                responseJson.put("body", BufferUtil.toArray(content));
                socket.emit("responseContentBinary", responseJson);
                logger.debug("Sent content to request {}", mRequestId);
            } catch (JSONException e) {
                logger.error(e.getMessage());
            }
        }

        @Override
        public void onHeaders(Response response) {
            if (!mHeadersSent) {
                logger.debug("Jetty finished receiving response header");
                JSONObject responseJson = new JSONObject();
                mHeadersSent = true;
                try {
                    responseJson.put("id", mRequestId);
                    responseJson.put("headers", getJSONHeaders(response.getHeaders()));
                    responseJson.put("responseStatusCode", response.getStatus());
                    responseJson.put("responseStatusText", "OK");
                    socket.emit("responseHeader", responseJson);
                    logger.debug("Sent headers to request {}", mRequestId);
                    logger.debug(responseJson.toString());
                } catch (JSONException e) {
                    logger.error(e.getMessage());
                }
            } else {
                // We should not send headers for the second time...
            }
        }
    }
}
