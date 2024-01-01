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
package org.openhab.binding.remoteopenhab.internal.rest;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
import org.openhab.binding.remoteopenhab.internal.data.RemoteopenhabChannelDescriptionChangedEvent;
import org.openhab.binding.remoteopenhab.internal.data.RemoteopenhabChannelTriggerEvent;
import org.openhab.binding.remoteopenhab.internal.data.RemoteopenhabCommandDescription;
import org.openhab.binding.remoteopenhab.internal.data.RemoteopenhabCommandOptions;
import org.openhab.binding.remoteopenhab.internal.data.RemoteopenhabEvent;
import org.openhab.binding.remoteopenhab.internal.data.RemoteopenhabEventPayload;
import org.openhab.binding.remoteopenhab.internal.data.RemoteopenhabItem;
import org.openhab.binding.remoteopenhab.internal.data.RemoteopenhabRestApi;
import org.openhab.binding.remoteopenhab.internal.data.RemoteopenhabStateDescription;
import org.openhab.binding.remoteopenhab.internal.data.RemoteopenhabStateOptions;
import org.openhab.binding.remoteopenhab.internal.data.RemoteopenhabStatusInfo;
import org.openhab.binding.remoteopenhab.internal.data.RemoteopenhabThing;
import org.openhab.binding.remoteopenhab.internal.exceptions.RemoteopenhabException;
import org.openhab.binding.remoteopenhab.internal.listener.RemoteopenhabItemsDataListener;
import org.openhab.binding.remoteopenhab.internal.listener.RemoteopenhabStreamingDataListener;
import org.openhab.binding.remoteopenhab.internal.listener.RemoteopenhabThingsDataListener;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.types.Command;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
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
    private final TranslationProvider i18nProvider;
    private final Bundle bundle;

    private final Object startStopLock = new Object();
    private final List<RemoteopenhabStreamingDataListener> listeners = new CopyOnWriteArrayList<>();
    private final List<RemoteopenhabItemsDataListener> itemsListeners = new CopyOnWriteArrayList<>();
    private final List<RemoteopenhabThingsDataListener> thingsListeners = new CopyOnWriteArrayList<>();

    private HttpClient httpClient;
    private @Nullable String restUrl;
    private @Nullable String restApiVersion;
    private Map<String, @Nullable String> apiEndPointsUrls = new HashMap<>();
    private @Nullable String topicNamespace;
    private boolean authenticateAnyway;
    private String accessToken;
    private String credentialToken;
    private boolean trustedCertificate;
    private boolean connected;
    private boolean completed;

    private @Nullable SseEventSource eventSource;
    private long lastEventTimestamp;

    public RemoteopenhabRestClient(final HttpClient httpClient, final ClientBuilder clientBuilder,
            final SseEventSourceFactory eventSourceFactory, final Gson jsonParser,
            final TranslationProvider i18nProvider) {
        this.httpClient = httpClient;
        this.clientBuilder = clientBuilder;
        this.eventSourceFactory = eventSourceFactory;
        this.jsonParser = jsonParser;
        this.i18nProvider = i18nProvider;
        this.bundle = FrameworkUtil.getBundle(this.getClass());
        this.accessToken = "";
        this.credentialToken = "";
    }

    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public String getRestUrl() throws RemoteopenhabException {
        String url = restUrl;
        if (url == null) {
            throw new RemoteopenhabException("@text/exception.rest-client-not-setup");
        }
        return url;
    }

    public void setRestUrl(String restUrl) {
        this.restUrl = restUrl;
    }

    public void setAuthenticationData(boolean authenticateAnyway, String accessToken, String username,
            String password) {
        this.authenticateAnyway = authenticateAnyway;
        this.accessToken = accessToken;
        if (username.isBlank() || password.isBlank()) {
            this.credentialToken = "";
        } else {
            String token = username + ":" + password;
            this.credentialToken = Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
        }
    }

    public void setTrustedCertificate(boolean trustedCertificate) {
        this.trustedCertificate = trustedCertificate;
    }

    public void tryApi() throws RemoteopenhabException {
        try {
            String jsonResponse = executeGetUrl(getRestUrl(), "application/json", false, false);
            if (jsonResponse.isEmpty()) {
                throw new RemoteopenhabException("@text/exception.json-response-empty");
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
            throw new RemoteopenhabException("@text/exception.root-rest-api-failed", e);
        }
    }

    public List<RemoteopenhabItem> getRemoteItems(@Nullable String fields) throws RemoteopenhabException {
        try {
            String url = String.format("%s?recursive=false", getRestApiUrl("items"));
            if (fields != null) {
                url += "&fields=" + fields;
            }
            boolean asyncReading = fields == null || Arrays.asList(fields.split(",")).contains("state");
            String jsonResponse = executeGetUrl(url, "application/json", false, asyncReading);
            if (jsonResponse.isEmpty()) {
                throw new RemoteopenhabException("@text/exception.json-response-empty");
            }
            return Arrays.asList(jsonParser.fromJson(jsonResponse, RemoteopenhabItem[].class));
        } catch (RemoteopenhabException | JsonSyntaxException e) {
            throw new RemoteopenhabException("@text/exception.get-list-items-api-failed", e);
        }
    }

    public String getRemoteItemState(String itemName) throws RemoteopenhabException {
        try {
            String url = String.format("%s/%s/state", getRestApiUrl("items"), itemName);
            return executeGetUrl(url, "text/plain", false, true);
        } catch (RemoteopenhabException e) {
            throw new RemoteopenhabException("@text/get-item-state-api-failed", e, itemName);
        }
    }

    public void sendCommandToRemoteItem(String itemName, Command command) throws RemoteopenhabException {
        try {
            String url = String.format("%s/%s", getRestApiUrl("items"), itemName);
            executeUrl(HttpMethod.POST, url, "application/json", command.toFullString(), "text/plain", false, false,
                    true);
        } catch (RemoteopenhabException e) {
            throw new RemoteopenhabException("@text/exception.send-item-command-api-failed", e, itemName);
        }
    }

    public List<RemoteopenhabThing> getRemoteThings() throws RemoteopenhabException {
        try {
            String jsonResponse = executeGetUrl(getRestApiUrl("things"), "application/json", true, false);
            if (jsonResponse.isEmpty()) {
                throw new RemoteopenhabException("@text/exception.json-response-empty");
            }
            return Arrays.asList(jsonParser.fromJson(jsonResponse, RemoteopenhabThing[].class));
        } catch (RemoteopenhabException | JsonSyntaxException e) {
            throw new RemoteopenhabException("@text/exception.get-list-things-api-failed", e);
        }
    }

    public RemoteopenhabThing getRemoteThing(String uid) throws RemoteopenhabException {
        try {
            String url = String.format("%s/%s", getRestApiUrl("things"), uid);
            String jsonResponse = executeGetUrl(url, "application/json", true, false);
            if (jsonResponse.isEmpty()) {
                throw new RemoteopenhabException("@text/exception.json-response-empty");
            }
            return Objects.requireNonNull(jsonParser.fromJson(jsonResponse, RemoteopenhabThing.class));
        } catch (RemoteopenhabException | JsonSyntaxException e) {
            throw new RemoteopenhabException("@text/exception.get-thing-api-failed", e, uid);
        }
    }

    public @Nullable String getRestApiVersion() {
        return restApiVersion;
    }

    private String getRestApiUrl(String endPoint) throws RemoteopenhabException {
        String url = apiEndPointsUrls.get(endPoint);
        if (url == null) {
            url = getRestUrl();
            if (!url.endsWith("/")) {
                url += "/";
            }
            url += endPoint;
        }
        return url;
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
        String credentialToken = restSseUrl.startsWith("https:") || authenticateAnyway ? this.credentialToken : "";

        RemoteopenhabStreamingRequestFilter filter;
        boolean filterRegistered = clientBuilder.getConfiguration()
                .isRegistered(RemoteopenhabStreamingRequestFilter.class);
        if (filterRegistered) {
            filter = clientBuilder.getConfiguration().getInstances().stream()
                    .filter(instance -> instance instanceof RemoteopenhabStreamingRequestFilter)
                    .map(instance -> (RemoteopenhabStreamingRequestFilter) instance).findAny().orElseThrow();
        } else {
            filter = new RemoteopenhabStreamingRequestFilter();
        }
        filter.setCredentialToken(restSseUrl, credentialToken);

        Client client;
        // Avoid a timeout exception after 1 minute by setting the read timeout to 0 (infinite)
        if (trustedCertificate) {
            HostnameVerifier alwaysValidHostname = new HostnameVerifier() {
                @Override
                public boolean verify(@Nullable String hostname, @Nullable SSLSession session) {
                    return true;
                }
            };
            if (filterRegistered) {
                client = clientBuilder.sslContext(httpClient.getSslContextFactory().getSslContext())
                        .hostnameVerifier(alwaysValidHostname).readTimeout(0, TimeUnit.SECONDS).build();
            } else {
                client = clientBuilder.sslContext(httpClient.getSslContextFactory().getSslContext())
                        .hostnameVerifier(alwaysValidHostname).readTimeout(0, TimeUnit.SECONDS).register(filter)
                        .build();
            }
        } else {
            if (filterRegistered) {
                client = clientBuilder.readTimeout(0, TimeUnit.SECONDS).build();
            } else {
                client = clientBuilder.readTimeout(0, TimeUnit.SECONDS).register(filter).build();
            }
        }

        SseEventSource eventSource = eventSourceFactory.newSource(client.target(restSseUrl));
        eventSource.register(this::onEvent, this::onError, this::onComplete);
        return eventSource;
    }

    private void reopenEventSource() {
        logger.debug("Reopening EventSource");

        String url;
        try {
            url = String.format(
                    "%s?topics=%s/items/*/*,%s/things/*/*,%s/channels/*/triggered,openhab/channels/*/descriptionchanged",
                    getRestApiUrl("events"), getTopicNamespace(), getTopicNamespace(), getTopicNamespace());
        } catch (RemoteopenhabException e) {
            logger.debug("reopenEventSource failed: {}", e.getMessage(bundle, i18nProvider));
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
        logger.trace("Received event name {} data {}", name, data);

        lastEventTimestamp = System.currentTimeMillis();
        if (!connected) {
            logger.debug("Connected to streaming events");
            connected = true;
            listeners.forEach(listener -> listener.onConnected());
        }
        if (!"message".equals(name)) {
            // Ignore silently all events which are not "message" events. This includes the "alive" events.
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
                case "ChannelDescriptionChangedEvent":
                    RemoteopenhabStateDescription stateDescription = new RemoteopenhabStateDescription();
                    RemoteopenhabCommandDescription commandDescription = new RemoteopenhabCommandDescription();
                    RemoteopenhabChannelDescriptionChangedEvent descriptionChanged = Objects.requireNonNull(
                            jsonParser.fromJson(event.payload, RemoteopenhabChannelDescriptionChangedEvent.class));
                    switch (descriptionChanged.field) {
                        case "STATE_OPTIONS":
                            RemoteopenhabStateOptions stateOptions = Objects.requireNonNull(
                                    jsonParser.fromJson(descriptionChanged.value, RemoteopenhabStateOptions.class));
                            stateDescription.options = stateOptions.options;
                            break;
                        case "COMMAND_OPTIONS":
                            RemoteopenhabCommandOptions commandOptions = Objects.requireNonNull(
                                    jsonParser.fromJson(descriptionChanged.value, RemoteopenhabCommandOptions.class));
                            commandDescription.commandOptions = commandOptions.options;
                            break;
                        default:
                            break;
                    }
                    if (stateDescription.options != null || commandDescription.commandOptions != null) {
                        descriptionChanged.linkedItemNames.forEach(linkedItemName -> {
                            RemoteopenhabItem item1 = new RemoteopenhabItem();
                            item1.name = linkedItemName;
                            item1.stateDescription = stateDescription;
                            item1.commandDescription = commandDescription;
                            itemsListeners.forEach(listener -> listener.onItemOptionsUpdatedd(item1));
                        });
                    }
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
            throw new RemoteopenhabException("@text/exception.invalid-event-topic", topic, eventType);
        }
        return parts[2];
    }

    private String extractThingUIDFromTopic(String topic, String eventType, String finalPart)
            throws RemoteopenhabException {
        String[] parts = topic.split("/");
        int expectedNbParts = 4;
        if (parts.length != expectedNbParts || !getTopicNamespace().equals(parts[0]) || !"things".equals(parts[1])
                || !finalPart.equals(parts[parts.length - 1])) {
            throw new RemoteopenhabException("@text/exception.invalid-event-topic", topic, eventType);
        }
        return parts[2];
    }

    public String executeGetUrl(String url, String acceptHeader, boolean provideAccessToken, boolean asyncReading)
            throws RemoteopenhabException {
        return executeUrl(HttpMethod.GET, url, acceptHeader, null, null, provideAccessToken, asyncReading, true);
    }

    public String executeUrl(HttpMethod httpMethod, String url, String acceptHeader, @Nullable String content,
            @Nullable String contentType, boolean provideAccessToken, boolean asyncReading, boolean retryIfEOF)
            throws RemoteopenhabException {
        final Request request = httpClient.newRequest(url).method(httpMethod)
                .timeout(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS).followRedirects(false)
                .header(HttpHeaders.ACCEPT, acceptHeader);

        if (url.startsWith("https:") || authenticateAnyway) {
            boolean useAlternativeHeader = false;
            if (!credentialToken.isEmpty()) {
                request.header(HttpHeaders.AUTHORIZATION, "Basic " + credentialToken);
                useAlternativeHeader = true;
            }
            if (provideAccessToken && !accessToken.isEmpty()) {
                if (useAlternativeHeader) {
                    request.header("X-OPENHAB-TOKEN", accessToken);
                } else {
                    request.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
                }
            }
        }

        if (content != null && (HttpMethod.POST.equals(httpMethod) || HttpMethod.PUT.equals(httpMethod))
                && contentType != null) {
            request.content(new StringContentProvider(content), contentType);
        }

        logger.debug("Request {} {}", request.getMethod(), request.getURI());

        try {
            if (asyncReading) {
                InputStreamResponseListener listener = new InputStreamResponseListener();
                request.send(listener);
                Response response = listener.get(5, TimeUnit.SECONDS);
                int statusCode = response.getStatus();
                if (statusCode != HttpStatus.OK_200) {
                    response.abort(new Exception(response.getReason()));
                    String statusLine = statusCode + " " + response.getReason();
                    throw new RemoteopenhabException("@text/exception.http-call-failed", statusLine);
                }
                ByteArrayOutputStream responseContent = new ByteArrayOutputStream();
                try (InputStream input = listener.getInputStream()) {
                    input.transferTo(responseContent);
                }
                return new String(responseContent.toByteArray(), StandardCharsets.UTF_8.name());
            } else {
                ContentResponse response = request.send();
                int statusCode = response.getStatus();
                if (statusCode == HttpStatus.MOVED_PERMANENTLY_301 || statusCode == HttpStatus.FOUND_302) {
                    String locationHeader = response.getHeaders().get(HttpHeaders.LOCATION);
                    if (locationHeader != null && !locationHeader.isBlank()) {
                        logger.debug("The remopte server redirected the request to this URL: {}", locationHeader);
                        return executeUrl(httpMethod, locationHeader, acceptHeader, content, contentType,
                                provideAccessToken, asyncReading, retryIfEOF);
                    } else {
                        String statusLine = statusCode + " " + response.getReason();
                        throw new RemoteopenhabException("@text/exception.http-call-failed", statusLine);
                    }
                } else if (statusCode >= HttpStatus.BAD_REQUEST_400) {
                    String statusLine = statusCode + " " + response.getReason();
                    throw new RemoteopenhabException("@text/exception.http-call-failed", statusLine);
                }
                String encoding = response.getEncoding() != null ? response.getEncoding().replace("\"", "").trim()
                        : StandardCharsets.UTF_8.name();
                return new String(response.getContent(), encoding);
            }
        } catch (ExecutionException e) {
            // After a long network outage, the first HTTP request will fail with an EOFException exception.
            // We retry the request a second time in this case.
            Throwable cause = e.getCause();
            if (retryIfEOF && cause instanceof EOFException) {
                logger.debug("EOFException - retry the request");
                return executeUrl(httpMethod, url, acceptHeader, content, contentType, provideAccessToken, asyncReading,
                        false);
            } else {
                throw new RemoteopenhabException(e);
            }
        } catch (IOException | TimeoutException e) {
            throw new RemoteopenhabException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RemoteopenhabException(e);
        }
    }
}
