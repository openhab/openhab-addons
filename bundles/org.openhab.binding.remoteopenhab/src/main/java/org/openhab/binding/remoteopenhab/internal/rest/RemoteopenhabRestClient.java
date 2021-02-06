/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.remoteopenhab.internal.rest;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.sse.InboundSseEvent;
import javax.ws.rs.sse.SseEventSource;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.InputStreamResponseListener;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.remoteopenhab.internal.data.RemoteopenhabChannelTriggerEvent;
import org.openhab.binding.remoteopenhab.internal.data.RemoteopenhabEvent;
import org.openhab.binding.remoteopenhab.internal.data.RemoteopenhabEventPayload;
import org.openhab.binding.remoteopenhab.internal.data.RemoteopenhabItem;
import org.openhab.binding.remoteopenhab.internal.data.RemoteopenhabRestApi;
import org.openhab.binding.remoteopenhab.internal.data.RemoteopenhabStatusInfo;
import org.openhab.binding.remoteopenhab.internal.data.RemoteopenhabThing;
import org.openhab.binding.remoteopenhab.internal.exceptions.RemoteopenhabException;
import org.openhab.binding.remoteopenhab.internal.listener.RemoteopenhabItemsDataListener;
import org.openhab.binding.remoteopenhab.internal.listener.RemoteopenhabStreamingDataListener;
import org.openhab.binding.remoteopenhab.internal.listener.RemoteopenhabThingsDataListener;
import org.openhab.core.types.Command;
import org.osgi.service.jaxrs.client.SseEventSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * A client to use the openHAB REST API and to receive/parse events received from the openHAB REST API Server-Sent
 * Events (SSE).
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class RemoteopenhabRestClient {

    private static final int REQUEST_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(30);

    private final Logger logger = LoggerFactory.getLogger(RemoteopenhabRestClient.class);

    private final ClientBuilder clientBuilder;
    private final SseEventSourceFactory eventSourceFactory;
    private final Gson jsonParser;

    private final Object startStopLock = new Object();
    private final List<RemoteopenhabStreamingDataListener> listeners = new CopyOnWriteArrayList<>();
    private final List<RemoteopenhabItemsDataListener> itemsListeners = new CopyOnWriteArrayList<>();
    private final List<RemoteopenhabThingsDataListener> thingsListeners = new CopyOnWriteArrayList<>();

    private HttpClient httpClient;
    private @Nullable String restUrl;
    private @Nullable String restApiVersion;
    private Map<String, @Nullable String> apiEndPointsUrls = new HashMap<>();
    private @Nullable String topicNamespace;
    private String accessToken;
    private boolean trustedCertificate;
    private boolean connected;
    private boolean completed;

    private @Nullable SseEventSource eventSource;
    private long lastEventTimestamp;

    public RemoteopenhabRestClient(final HttpClient httpClient, final ClientBuilder clientBuilder,
            final SseEventSourceFactory eventSourceFactory, final Gson jsonParser) {
        this.httpClient = httpClient;
        this.clientBuilder = clientBuilder;
        this.eventSourceFactory = eventSourceFactory;
        this.jsonParser = jsonParser;
        this.accessToken = "";
    }

    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public String getRestUrl() throws RemoteopenhabException {
        String url = restUrl;
        if (url == null) {
            throw new RemoteopenhabException("REST client not correctly setup");
        }
        return url;
    }

    public void setRestUrl(String restUrl) {
        this.restUrl = restUrl;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setTrustedCertificate(boolean trustedCertificate) {
        this.trustedCertificate = trustedCertificate;
    }

    public void tryApi() throws RemoteopenhabException {
        try {
            String jsonResponse = executeGetUrl(getRestUrl(), "application/json", false);
            if (jsonResponse.isEmpty()) {
                throw new RemoteopenhabException("JSON response is empty");
            }
            RemoteopenhabRestApi restApi = jsonParser.fromJson(jsonResponse, RemoteopenhabRestApi.class);
            restApiVersion = restApi.version;
            logger.debug("REST API version = {}", restApiVersion);
            apiEndPointsUrls.clear();
            for (int i = 0; i < restApi.links.length; i++) {
                apiEndPointsUrls.put(restApi.links[i].type, restApi.links[i].url);
            }
            logger.debug("REST API items = {}", apiEndPointsUrls.get("items"));
            logger.debug("REST API things = {}", apiEndPointsUrls.get("things"));
            logger.debug("REST API events = {}", apiEndPointsUrls.get("events"));
            topicNamespace = restApi.runtimeInfo != null ? "openhab" : "smarthome";
            logger.debug("topic namespace = {}", topicNamespace);
        } catch (RemoteopenhabException | JsonSyntaxException e) {
            throw new RemoteopenhabException("Failed to execute the root REST API: " + e.getMessage(), e);
        }
    }

    public List<RemoteopenhabItem> getRemoteItems(@Nullable String fields) throws RemoteopenhabException {
        try {
            String url = String.format("%s?recursive=false", getRestApiUrl("items"));
            if (fields != null) {
                url += "&fields=" + fields;
            }
            boolean asyncReading = fields == null || Arrays.asList(fields.split(",")).contains("state");
            String jsonResponse = executeGetUrl(url, "application/json", asyncReading);
            if (jsonResponse.isEmpty()) {
                throw new RemoteopenhabException("JSON response is empty");
            }
            return Arrays.asList(jsonParser.fromJson(jsonResponse, RemoteopenhabItem[].class));
        } catch (RemoteopenhabException | JsonSyntaxException e) {
            throw new RemoteopenhabException(
                    "Failed to get the list of remote items using the items REST API: " + e.getMessage(), e);
        }
    }

    public String getRemoteItemState(String itemName) throws RemoteopenhabException {
        try {
            String url = String.format("%s/%s/state", getRestApiUrl("items"), itemName);
            return executeGetUrl(url, "text/plain", true);
        } catch (RemoteopenhabException e) {
            throw new RemoteopenhabException("Failed to get the state of remote item " + itemName
                    + " using the items REST API: " + e.getMessage(), e);
        }
    }

    public void sendCommandToRemoteItem(String itemName, Command command) throws RemoteopenhabException {
        try {
            String url = String.format("%s/%s", getRestApiUrl("items"), itemName);
            executeUrl(HttpMethod.POST, url, "application/json", command.toFullString(), "text/plain", false, true);
        } catch (RemoteopenhabException e) {
            throw new RemoteopenhabException("Failed to send command to the remote item " + itemName
                    + " using the items REST API: " + e.getMessage(), e);
        }
    }

    public List<RemoteopenhabThing> getRemoteThings() throws RemoteopenhabException {
        try {
            String jsonResponse = executeGetUrl(getRestApiUrl("things"), "application/json", false);
            if (jsonResponse.isEmpty()) {
                throw new RemoteopenhabException("JSON response is empty");
            }
            return Arrays.asList(jsonParser.fromJson(jsonResponse, RemoteopenhabThing[].class));
        } catch (RemoteopenhabException | JsonSyntaxException e) {
            throw new RemoteopenhabException(
                    "Failed to get the list of remote things using the things REST API: " + e.getMessage(), e);
        }
    }

    public RemoteopenhabThing getRemoteThing(String uid) throws RemoteopenhabException {
        try {
            String url = String.format("%s/%s", getRestApiUrl("things"), uid);
            String jsonResponse = executeGetUrl(url, "application/json", false);
            if (jsonResponse.isEmpty()) {
                throw new RemoteopenhabException("JSON response is empty");
            }
            return Objects.requireNonNull(jsonParser.fromJson(jsonResponse, RemoteopenhabThing.class));
        } catch (RemoteopenhabException | JsonSyntaxException e) {
            throw new RemoteopenhabException(
                    "Failed to get the remote thing " + uid + " using the things REST API: " + e.getMessage(), e);
        }
    }

    public @Nullable String getRestApiVersion() {
        return restApiVersion;
    }

    private String getRestApiUrl(String endPoint) throws RemoteopenhabException {
        String url = apiEndPointsUrls.get(endPoint);
        return url != null ? url : getRestUrl() + "/" + endPoint;
    }

    public String getTopicNamespace() {
        String namespace = topicNamespace;
        return namespace != null ? namespace : "openhab";
    }

    public void start() {
        synchronized (startStopLock) {
            logger.debug("Opening EventSource");
            reopenEventSource();
            logger.debug("EventSource started");
        }
    }

    public void stop(boolean waitingForCompletion) {
        synchronized (startStopLock) {
            logger.debug("Closing EventSource");
            closeEventSource(waitingForCompletion);
            logger.debug("EventSource stopped");
            lastEventTimestamp = 0;
        }
    }

    private SseEventSource createEventSource(String restSseUrl) {
        Client client;
        // Avoid a timeout exception after 1 minute by setting the read timeout to 0 (infinite)
        if (trustedCertificate) {
            client = clientBuilder.sslContext(httpClient.getSslContextFactory().getSslContext())
                    .hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(@Nullable String hostname, @Nullable SSLSession session) {
                            return true;
                        }
                    }).readTimeout(0, TimeUnit.SECONDS).register(new RemoteopenhabStreamingRequestFilter(accessToken))
                    .build();
        } else {
            client = clientBuilder.readTimeout(0, TimeUnit.SECONDS)
                    .register(new RemoteopenhabStreamingRequestFilter(accessToken)).build();
        }
        SseEventSource eventSource = eventSourceFactory.newSource(client.target(restSseUrl));
        eventSource.register(this::onEvent, this::onError, this::onComplete);
        return eventSource;
    }

    private void reopenEventSource() {
        logger.debug("Reopening EventSource");

        String url;
        try {
            url = String.format("%s?topics=%s/items/*/*,%s/things/*/*,%s/channels/*/triggered", getRestApiUrl("events"),
                    getTopicNamespace(), getTopicNamespace(), getTopicNamespace());
        } catch (RemoteopenhabException e) {
            logger.debug("{}", e.getMessage());
            return;
        }

        closeEventSource(true);

        logger.debug("Opening new EventSource {}", url);
        SseEventSource localEventSource = createEventSource(url);
        localEventSource.open();

        eventSource = localEventSource;
    }

    private void closeEventSource(boolean waitingForCompletion) {
        SseEventSource localEventSource = eventSource;
        if (localEventSource != null) {
            if (!localEventSource.isOpen() || completed) {
                logger.debug("Existing EventSource is already closed");
            } else if (localEventSource.close(waitingForCompletion ? 10 : 0, TimeUnit.SECONDS)) {
                logger.debug("Succesfully closed existing EventSource");
            } else {
                logger.debug("Failed to close existing EventSource");
            }
            eventSource = null;
        }
        connected = false;
    }

    public boolean addStreamingDataListener(RemoteopenhabStreamingDataListener listener) {
        return listeners.add(listener);
    }

    public boolean removeStreamingDataListener(RemoteopenhabStreamingDataListener listener) {
        return listeners.remove(listener);
    }

    public boolean addItemsDataListener(RemoteopenhabItemsDataListener listener) {
        return itemsListeners.add(listener);
    }

    public boolean removeItemsDataListener(RemoteopenhabItemsDataListener listener) {
        return itemsListeners.remove(listener);
    }

    public boolean addThingsDataListener(RemoteopenhabThingsDataListener listener) {
        return thingsListeners.add(listener);
    }

    public boolean removeThingsDataListener(RemoteopenhabThingsDataListener listener) {
        return thingsListeners.remove(listener);
    }

    public long getLastEventTimestamp() {
        return lastEventTimestamp;
    }

    private void onEvent(InboundSseEvent inboundEvent) {
        String name = inboundEvent.getName();
        String data = inboundEvent.readData();
        logger.trace("Received event name {} date {}", name, data);

        lastEventTimestamp = System.currentTimeMillis();
        if (!connected) {
            logger.debug("Connected to streaming events");
            connected = true;
            listeners.forEach(listener -> listener.onConnected());
        }

        if (!"message".equals(name)) {
            logger.debug("Received unhandled event with name '{}' and data '{}'", name, data);
            return;
        }

        try {
            RemoteopenhabEvent event = jsonParser.fromJson(data, RemoteopenhabEvent.class);
            String itemName;
            String thingUID;
            RemoteopenhabEventPayload payload;
            RemoteopenhabItem item;
            RemoteopenhabThing thing;
            switch (event.type) {
                case "ItemStateEvent":
                    itemName = extractItemNameFromTopic(event.topic, event.type, "state");
                    payload = jsonParser.fromJson(event.payload, RemoteopenhabEventPayload.class);
                    itemsListeners.forEach(
                            listener -> listener.onItemStateEvent(itemName, payload.type, payload.value, false));
                    break;
                case "ItemStateChangedEvent":
                    itemName = extractItemNameFromTopic(event.topic, event.type, "statechanged");
                    payload = jsonParser.fromJson(event.payload, RemoteopenhabEventPayload.class);
                    itemsListeners.forEach(
                            listener -> listener.onItemStateEvent(itemName, payload.type, payload.value, true));
                    break;
                case "GroupItemStateChangedEvent":
                    itemName = extractItemNameFromTopic(event.topic, event.type, "statechanged");
                    payload = jsonParser.fromJson(event.payload, RemoteopenhabEventPayload.class);
                    itemsListeners.forEach(
                            listener -> listener.onItemStateEvent(itemName, payload.type, payload.value, false));
                    break;
                case "ItemAddedEvent":
                    itemName = extractItemNameFromTopic(event.topic, event.type, "added");
                    item = Objects.requireNonNull(jsonParser.fromJson(event.payload, RemoteopenhabItem.class));
                    itemsListeners.forEach(listener -> listener.onItemAdded(item));
                    break;
                case "ItemRemovedEvent":
                    itemName = extractItemNameFromTopic(event.topic, event.type, "removed");
                    item = Objects.requireNonNull(jsonParser.fromJson(event.payload, RemoteopenhabItem.class));
                    itemsListeners.forEach(listener -> listener.onItemRemoved(item));
                    break;
                case "ItemUpdatedEvent":
                    itemName = extractItemNameFromTopic(event.topic, event.type, "updated");
                    RemoteopenhabItem[] updItem = jsonParser.fromJson(event.payload, RemoteopenhabItem[].class);
                    if (updItem.length == 2) {
                        itemsListeners.forEach(listener -> listener.onItemUpdated(updItem[0], updItem[1]));
                    } else {
                        logger.debug("Invalid payload for event type {} for topic {}", event.type, event.topic);
                    }
                    break;
                case "ThingStatusInfoChangedEvent":
                    thingUID = extractThingUIDFromTopic(event.topic, event.type, "statuschanged");
                    RemoteopenhabStatusInfo[] updStatus = jsonParser.fromJson(event.payload,
                            RemoteopenhabStatusInfo[].class);
                    if (updStatus.length == 2) {
                        thingsListeners.forEach(listener -> listener.onThingStatusUpdated(thingUID, updStatus[0]));
                    } else {
                        logger.debug("Invalid payload for event type {} for topic {}", event.type, event.topic);
                    }
                    break;
                case "ThingAddedEvent":
                    thingUID = extractThingUIDFromTopic(event.topic, event.type, "added");
                    thing = Objects.requireNonNull(jsonParser.fromJson(event.payload, RemoteopenhabThing.class));
                    thingsListeners.forEach(listener -> listener.onThingAdded(thing));
                    break;
                case "ThingRemovedEvent":
                    thingUID = extractThingUIDFromTopic(event.topic, event.type, "removed");
                    thing = Objects.requireNonNull(jsonParser.fromJson(event.payload, RemoteopenhabThing.class));
                    thingsListeners.forEach(listener -> listener.onThingRemoved(thing));
                    break;
                case "ChannelTriggeredEvent":
                    RemoteopenhabChannelTriggerEvent triggerEvent = jsonParser.fromJson(event.payload,
                            RemoteopenhabChannelTriggerEvent.class);
                    thingsListeners
                            .forEach(listener -> listener.onChannelTriggered(triggerEvent.channel, triggerEvent.event));
                    break;
                case "ItemStatePredictedEvent":
                case "ItemCommandEvent":
                case "ThingStatusInfoEvent":
                case "ThingUpdatedEvent":
                    logger.trace("Ignored event type {} for topic {}", event.type, event.topic);
                    break;
                default:
                    logger.debug("Unexpected event type {} for topic {}", event.type, event.topic);
                    break;
            }
        } catch (RemoteopenhabException | JsonSyntaxException e) {
            logger.debug("An exception occurred while processing the inbound '{}' event containg data: {}", name, data,
                    e);
        }
    }

    private void onComplete() {
        logger.debug("Disconnected from streaming events");
        completed = true;
        listeners.forEach(listener -> listener.onDisconnected());
    }

    private void onError(Throwable error) {
        logger.debug("Error occurred while receiving events", error);
        listeners.forEach(listener -> listener.onError("Error occurred while receiving events"));
    }

    private String extractItemNameFromTopic(String topic, String eventType, String finalPart)
            throws RemoteopenhabException {
        String[] parts = topic.split("/");
        int expectedNbParts = "GroupItemStateChangedEvent".equals(eventType) ? 5 : 4;
        if (parts.length != expectedNbParts || !getTopicNamespace().equals(parts[0]) || !"items".equals(parts[1])
                || !finalPart.equals(parts[parts.length - 1])) {
            throw new RemoteopenhabException("Invalid event topic " + topic + " for event type " + eventType);
        }
        return parts[2];
    }

    private String extractThingUIDFromTopic(String topic, String eventType, String finalPart)
            throws RemoteopenhabException {
        String[] parts = topic.split("/");
        int expectedNbParts = 4;
        if (parts.length != expectedNbParts || !getTopicNamespace().equals(parts[0]) || !"things".equals(parts[1])
                || !finalPart.equals(parts[parts.length - 1])) {
            throw new RemoteopenhabException("Invalid event topic " + topic + " for event type " + eventType);
        }
        return parts[2];
    }

    public String executeGetUrl(String url, String acceptHeader, boolean asyncReading) throws RemoteopenhabException {
        return executeUrl(HttpMethod.GET, url, acceptHeader, null, null, asyncReading, true);
    }

    public String executeUrl(HttpMethod httpMethod, String url, String acceptHeader, @Nullable String content,
            @Nullable String contentType, boolean asyncReading, boolean retryIfEOF) throws RemoteopenhabException {
        final Request request = httpClient.newRequest(url).method(httpMethod).timeout(REQUEST_TIMEOUT,
                TimeUnit.MILLISECONDS);

        request.header(HttpHeaders.ACCEPT, acceptHeader);
        if (!accessToken.isEmpty()) {
            request.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        }

        if (content != null && (HttpMethod.POST.equals(httpMethod) || HttpMethod.PUT.equals(httpMethod))
                && contentType != null) {
            request.content(new StringContentProvider(content), contentType);
        }

        try {
            if (asyncReading) {
                InputStreamResponseListener listener = new InputStreamResponseListener();
                request.send(listener);
                Response response = listener.get(5, TimeUnit.SECONDS);
                int statusCode = response.getStatus();
                if (statusCode != HttpStatus.OK_200) {
                    response.abort(new Exception(response.getReason()));
                    String statusLine = statusCode + " " + response.getReason();
                    throw new RemoteopenhabException("HTTP call failed: " + statusLine);
                }
                ByteArrayOutputStream responseContent = new ByteArrayOutputStream();
                try (InputStream input = listener.getInputStream()) {
                    input.transferTo(responseContent);
                }
                return new String(responseContent.toByteArray(), StandardCharsets.UTF_8.name());
            } else {
                ContentResponse response = request.send();
                int statusCode = response.getStatus();
                if (statusCode >= HttpStatus.BAD_REQUEST_400) {
                    String statusLine = statusCode + " " + response.getReason();
                    throw new RemoteopenhabException("HTTP call failed: " + statusLine);
                }
                String encoding = response.getEncoding() != null ? response.getEncoding().replaceAll("\"", "").trim()
                        : StandardCharsets.UTF_8.name();
                return new String(response.getContent(), encoding);
            }
        } catch (RemoteopenhabException e) {
            throw e;
        } catch (ExecutionException e) {
            // After a long network outage, the first HTTP request will fail with an EOFException exception.
            // We retry the request a second time in this case.
            Throwable cause = e.getCause();
            if (retryIfEOF && cause instanceof EOFException) {
                logger.debug("EOFException - retry the request");
                return executeUrl(httpMethod, url, acceptHeader, content, contentType, asyncReading, false);
            } else {
                throw new RemoteopenhabException(e);
            }
        } catch (Exception e) {
            throw new RemoteopenhabException(e);
        }
    }
}
