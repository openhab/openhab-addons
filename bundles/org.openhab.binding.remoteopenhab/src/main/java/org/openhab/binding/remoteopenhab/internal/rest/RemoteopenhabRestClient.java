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
package org.openhab.binding.remoteopenhab.internal.rest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.sse.InboundSseEvent;
import javax.ws.rs.sse.SseEventSource;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.remoteopenhab.internal.data.Event;
import org.openhab.binding.remoteopenhab.internal.data.EventPayload;
import org.openhab.binding.remoteopenhab.internal.data.Item;
import org.openhab.binding.remoteopenhab.internal.data.RestApi;
import org.openhab.binding.remoteopenhab.internal.exceptions.RemoteopenhabException;
import org.openhab.binding.remoteopenhab.internal.listener.RemoteopenhabStreamingDataListener;
import org.openhab.core.io.net.http.HttpUtil;
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
    private String accessToken;
    private final String restUrl;

    private final Object startStopLock = new Object();
    private final List<RemoteopenhabStreamingDataListener> listeners = new CopyOnWriteArrayList<>();

    private @Nullable String restApiVersion;
    private @Nullable String restApiItems;
    private @Nullable String restApiEvents;
    private @Nullable String topicNamespace;
    private boolean connected;

    private @Nullable SseEventSource eventSource;
    private long lastEventTimestamp;

    public RemoteopenhabRestClient(final ClientBuilder clientBuilder, final SseEventSourceFactory eventSourceFactory,
            final Gson jsonParser, final String accessToken, final String restUrl) {
        this.clientBuilder = clientBuilder;
        this.eventSourceFactory = eventSourceFactory;
        this.jsonParser = jsonParser;
        this.accessToken = accessToken;
        this.restUrl = restUrl;
    }

    public void tryApi() throws RemoteopenhabException {
        try {
            Properties httpHeaders = new Properties();
            if (!accessToken.isEmpty()) {
                httpHeaders.put(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
            }
            httpHeaders.put(HttpHeaders.ACCEPT, "application/json");
            String jsonResponse = HttpUtil.executeUrl("GET", restUrl, httpHeaders, null, null, REQUEST_TIMEOUT);
            if (jsonResponse.isEmpty()) {
                throw new RemoteopenhabException("Failed to execute the root REST API");
            }
            RestApi restApi = jsonParser.fromJson(jsonResponse, RestApi.class);
            restApiVersion = restApi.version;
            logger.debug("REST API version = {}", restApiVersion);
            restApiItems = null;
            for (int i = 0; i < restApi.links.length; i++) {
                if ("items".equals(restApi.links[i].type)) {
                    restApiItems = restApi.links[i].url;
                } else if ("events".equals(restApi.links[i].type)) {
                    restApiEvents = restApi.links[i].url;
                }
            }
            logger.debug("REST API items = {}", restApiItems);
            logger.debug("REST API events = {}", restApiEvents);
            topicNamespace = restApi.runtimeInfo != null ? "openhab" : "smarthome";
            logger.debug("topic namespace = {}", topicNamespace);
        } catch (RemoteopenhabException e) {
            throw new RemoteopenhabException(e.getMessage());
        } catch (JsonSyntaxException e) {
            throw new RemoteopenhabException("Failed to parse the result of the root REST API", e);
        } catch (IOException e) {
            throw new RemoteopenhabException("Failed to execute the root REST API", e);
        }
    }

    public List<Item> getRemoteItems() throws RemoteopenhabException {
        try {
            Properties httpHeaders = new Properties();
            if (!accessToken.isEmpty()) {
                httpHeaders.put(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
            }
            httpHeaders.put(HttpHeaders.ACCEPT, "application/json");
            String url = String.format("%s?recursive=fasle", getRestApiItems());
            String jsonResponse = HttpUtil.executeUrl("GET", url, httpHeaders, null, null, REQUEST_TIMEOUT);
            return Arrays.asList(jsonParser.fromJson(jsonResponse, Item[].class));
        } catch (IOException | JsonSyntaxException e) {
            throw new RemoteopenhabException("Failed to get the list of remote items using the items REST API", e);
        }
    }

    public String getRemoteItemState(String itemName) throws RemoteopenhabException {
        try {
            Properties httpHeaders = new Properties();
            if (!accessToken.isEmpty()) {
                httpHeaders.put(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
            }
            httpHeaders.put(HttpHeaders.ACCEPT, "text/plain");
            String url = String.format("%s/%s/state", getRestApiItems(), itemName);
            return HttpUtil.executeUrl("GET", url, httpHeaders, null, null, REQUEST_TIMEOUT);
        } catch (IOException e) {
            throw new RemoteopenhabException(
                    "Failed to get the state of remote item " + itemName + " using the items REST API", e);
        }
    }

    public void sendCommandToRemoteItem(String itemName, Command command) throws RemoteopenhabException {
        try {
            Properties httpHeaders = new Properties();
            if (!accessToken.isEmpty()) {
                httpHeaders.put(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
            }
            httpHeaders.put(HttpHeaders.ACCEPT, "application/json");
            String url = String.format("%s/%s", getRestApiItems(), itemName);
            InputStream stream = new ByteArrayInputStream(command.toFullString().getBytes(StandardCharsets.UTF_8));
            HttpUtil.executeUrl("POST", url, httpHeaders, stream, "text/plain", REQUEST_TIMEOUT);
            stream.close();
        } catch (IOException e) {
            throw new RemoteopenhabException(
                    "Failed to send command to the remote item " + itemName + " using the items REST API", e);
        }
    }

    public @Nullable String getRestApiVersion() {
        return restApiVersion;
    }

    public String getRestApiItems() {
        String url = restApiItems;
        return url != null ? url : restUrl + "/items";
    }

    public String getRestApiEvents() {
        String url = restApiEvents;
        return url != null ? url : restUrl + "/events";
    }

    public String getTopicNamespace() {
        String namespace = topicNamespace;
        return namespace != null ? namespace : "openhab";
    }

    public void start() {
        synchronized (startStopLock) {
            logger.debug("Opening EventSource {}", getItemsRestSseUrl());
            reopenEventSource();
            logger.debug("EventSource started");
        }
    }

    public void stop() {
        synchronized (startStopLock) {
            logger.debug("Closing EventSource {}", getItemsRestSseUrl());
            closeEventSource(0, TimeUnit.SECONDS);
            logger.debug("EventSource stopped");
        }
    }

    private String getItemsRestSseUrl() {
        return String.format("%s?topics=%s/items/*/*", getRestApiEvents(), getTopicNamespace());
    }

    private SseEventSource createEventSource(String restSseUrl) {
        Client client = clientBuilder.register(new RemoteopenhabStreamingRequestFilter(accessToken)).build();
        SseEventSource eventSource = eventSourceFactory.newSource(client.target(restSseUrl));
        eventSource.register(this::onEvent, this::onError);
        return eventSource;
    }

    private void reopenEventSource() {
        logger.debug("Reopening EventSource");
        closeEventSource(10, TimeUnit.SECONDS);

        logger.debug("Opening new EventSource {}", getItemsRestSseUrl());
        SseEventSource localEventSource = createEventSource(getItemsRestSseUrl());
        localEventSource.open();

        eventSource = localEventSource;
    }

    private void closeEventSource(long timeout, TimeUnit timeoutUnit) {
        SseEventSource localEventSource = eventSource;
        if (localEventSource != null) {
            if (!localEventSource.isOpen()) {
                logger.debug("Existing EventSource is already closed");
            } else if (localEventSource.close(timeout, timeoutUnit)) {
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
            Event event = jsonParser.fromJson(data, Event.class);
            String itemName;
            EventPayload payload;
            Item item;
            switch (event.type) {
                case "ItemStateEvent":
                    itemName = extractItemNameFromTopic(event.topic, event.type, "state");
                    payload = jsonParser.fromJson(event.payload, EventPayload.class);
                    listeners.forEach(listener -> listener.onItemStateEvent(itemName, payload.type, payload.value));
                    break;
                case "GroupItemStateChangedEvent":
                    itemName = extractItemNameFromTopic(event.topic, event.type, "statechanged");
                    payload = jsonParser.fromJson(event.payload, EventPayload.class);
                    listeners.forEach(listener -> listener.onItemStateEvent(itemName, payload.type, payload.value));
                    break;
                case "ItemAddedEvent":
                    itemName = extractItemNameFromTopic(event.topic, event.type, "added");
                    item = jsonParser.fromJson(event.payload, Item.class);
                    listeners.forEach(listener -> listener.onItemAdded(item));
                    break;
                case "ItemRemovedEvent":
                    itemName = extractItemNameFromTopic(event.topic, event.type, "removed");
                    item = jsonParser.fromJson(event.payload, Item.class);
                    listeners.forEach(listener -> listener.onItemRemoved(item));
                    break;
                case "ItemUpdatedEvent":
                    itemName = extractItemNameFromTopic(event.topic, event.type, "updated");
                    Item[] updItem = jsonParser.fromJson(event.payload, Item[].class);
                    if (updItem.length == 2) {
                        listeners.forEach(listener -> listener.onItemUpdated(updItem[0], updItem[1]));
                    } else {
                        logger.debug("Invalid payload for event type {} for topic {}", event.type, event.topic);
                    }
                    break;
                case "ItemStatePredictedEvent":
                case "ItemStateChangedEvent":
                case "ItemCommandEvent":
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
}
