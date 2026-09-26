/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.hasslink.internal.api;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.api.WebSocketPolicy;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.hasslink.internal.api.dto.EntityState;
import org.openhab.binding.hasslink.internal.api.dto.EntityStateCompressedEvent;
import org.openhab.binding.hasslink.internal.api.dto.HomeAssistantConfig;
import org.openhab.binding.hasslink.internal.api.dto.ResultMessage;
import org.openhab.binding.hasslink.internal.api.dto.ResultMessage.ResultError;
import org.openhab.binding.hasslink.internal.api.dto.StateChangedEventData;
import org.openhab.binding.hasslink.internal.api.dto.commands.AuthCommand;
import org.openhab.binding.hasslink.internal.api.dto.commands.BaseCommand;
import org.openhab.binding.hasslink.internal.api.dto.commands.CallServiceCommand;
import org.openhab.binding.hasslink.internal.api.dto.commands.GetConfigCommand;
import org.openhab.binding.hasslink.internal.api.dto.commands.GetEntitiesCommand;
import org.openhab.binding.hasslink.internal.api.dto.commands.RegistryListCommand;
import org.openhab.binding.hasslink.internal.api.dto.commands.SubscribeEntitiesCommand;
import org.openhab.binding.hasslink.internal.api.dto.commands.SubscribeEventsCommand;
import org.openhab.binding.hasslink.internal.api.dto.commands.UnsubscribeEventsCommand;
import org.openhab.binding.hasslink.internal.api.exception.CommunicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * The {@link HomeAssistantWebSocketClient} manages the persistent WebSocket connection to Home Assistant.
 * It performs authentication, initial entity/registry fetching, live state/compressed updates,
 * event subscriptions for registry changes, and automatic reconnection.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class HomeAssistantWebSocketClient implements WebSocketListener {

    private static final int MAX_TEXT_MESSAGE_SIZE = 2 * 1024 * 1024;
    private static final int INITIAL_RECONNECT_DELAY_SECONDS = 5;
    private static final int MAX_RECONNECT_DELAY_SECONDS = 300;
    private static final String SHUTDOWN_REASON = "Binding disposed";

    private static final String TYPE_AUTH_REQUIRED = "auth_required";
    private static final String TYPE_AUTH_OK = "auth_ok";
    private static final String TYPE_AUTH_INVALID = "auth_invalid";
    private static final String TYPE_RESULT = "result";
    private static final String TYPE_EVENT = "event";

    private static final String EVENT_TYPE_STATE_CHANGED = "state_changed";
    private static final String EVENT_DEVICE_REGISTRY_UPDATED = "device_registry_updated";
    private static final String EVENT_ENTITY_REGISTRY_UPDATED = "entity_registry_updated";
    private static final String EVENT_AREA_REGISTRY_UPDATED = "area_registry_updated";

    private final Logger logger = LoggerFactory.getLogger(HomeAssistantWebSocketClient.class);
    private final WebSocketClient wsClient;
    private final String accessToken;
    private final HomeAssistantConnectionListener listener;
    private final ScheduledExecutorService scheduler;
    private final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    private final Object lifecycleLock = new Object();
    private final Object sendLock = new Object();
    private final AtomicInteger idCounter = new AtomicInteger(1);

    private volatile String uri = "";
    private volatile boolean shuttingDown = false;
    private volatile int reconnectDelaySeconds = INITIAL_RECONNECT_DELAY_SECONDS;
    private volatile int pendingGetConfigId = -1;
    private volatile int pendingGetEntitiesId = -1;
    private volatile int pendingDeviceRegistryId = -1;
    private volatile int pendingEntityRegistryId = -1;
    private volatile int pendingSubscribeId = -1;

    private @Nullable Session session;
    private @Nullable Future<?> sessionFuture;
    private @Nullable ScheduledFuture<?> reconnectFuture;

    private final Set<String> subscribedEntityIds = ConcurrentHashMap.newKeySet();
    private volatile int activeSubscriptionId = -1;

    public HomeAssistantWebSocketClient(WebSocketClient wsClient, String accessToken,
            HomeAssistantConnectionListener listener, ScheduledExecutorService scheduler) {
        this.wsClient = wsClient;
        this.accessToken = accessToken;
        this.listener = listener;
        this.scheduler = scheduler;
    }

    /**
     * Initiates the WebSocket connection to the given Home Assistant WebSocket URI. If a connection
     * or connection attempt is already active, this method returns silently.
     *
     * @param uri the {@code /api/websocket} URI to connect to
     * @throws CommunicationException if the connection attempt could not be initiated
     */
    public void start(String uri) throws CommunicationException {
        synchronized (lifecycleLock) {
            this.uri = uri;
            shuttingDown = false;
            startLocked();
        }
    }

    /**
     * Checks whether the WebSocket session is currently active and open.
     */
    public boolean isConnected() {
        Session localSession = session;
        return localSession != null && localSession.isOpen();
    }

    private void startLocked() throws CommunicationException {
        if (shuttingDown || hasActiveConnectionOrAttemptLocked()) {
            return;
        }
        try {
            sessionFuture = wsClient.connect(this, new URI(uri));
        } catch (IOException | URISyntaxException e) {
            throw new CommunicationException("Failed to connect to Home Assistant WebSocket (" + uri + ")", e);
        }
    }

    private boolean hasActiveConnectionOrAttemptLocked() {
        if (session != null) {
            return true;
        }
        Future<?> future = sessionFuture;
        return future != null && !future.isDone();
    }

    /**
     * Stops the WebSocket connection and cancels any scheduled reconnect attempt.
     */
    public void stop() {
        synchronized (lifecycleLock) {
            shuttingDown = true;
            ScheduledFuture<?> reconnect = reconnectFuture;
            if (reconnect != null) {
                reconnect.cancel(false);
                reconnectFuture = null;
            }
            Session localSession = session;
            if (localSession != null) {
                localSession.close(StatusCode.NORMAL, SHUTDOWN_REASON);
            }
            session = null;
            sessionFuture = null;
        }
    }

    /**
     * Sends a Home Assistant service call with payload data and returns a Future.
     *
     * @param domain Target domain (e.g. "light", "switch")
     * @param service Target service (e.g. "turn_on")
     * @param serviceData Payload attributes including optional entity_id
     */
    public CompletableFuture<@Nullable Void> callService(String domain, String service, @Nullable String entityId,
            @Nullable Map<String, Object> serviceData) {
        CompletableFuture<@Nullable Void> future = new CompletableFuture<>();
        Session localSession = session;
        if (localSession == null) {
            future.completeExceptionally(new CommunicationException("Not connected to Home Assistant"));
            return future;
        }

        try {
            int id = nextMessageId();
            if (entityId != null) {
                sendCommand(new CallServiceCommand(id, domain, service, entityId, serviceData));
            } else {
                sendCommand(new CallServiceCommand(id, domain, service, serviceData));
            }
            future.complete(null);
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
        return future;
    }

    /**
     * @return the next unique outgoing message id
     */
    public int nextMessageId() {
        return idCounter.getAndIncrement();
    }

    public void sendRegistryListRequest() {
        int deviceRegistryId = nextMessageId();
        pendingDeviceRegistryId = deviceRegistryId;
        sendCommand(new RegistryListCommand(deviceRegistryId, "device_registry"));

        int entityRegistryId = nextMessageId();
        pendingEntityRegistryId = entityRegistryId;
        sendCommand(new RegistryListCommand(entityRegistryId, "entity_registry"));
    }

    private void sendCommand(BaseCommand command) {
        Session localSession = session;
        if (localSession == null) {
            logger.warn("Cannot send '{}' command, not connected to Home Assistant", command.type);
            return;
        }
        String json = gson.toJson(command);
        try {
            RemoteEndpoint remote = localSession.getRemote();
            synchronized (sendLock) {
                logger.trace("SEND | {}", json);
                remote.sendString(json);
            }
        } catch (IOException e) {
            logger.warn("Failed to send '{}' command: {}", command.type, e.getMessage());
        }
    }

    /**
     * Updates the active subscription on Home Assistant to match the supplied set of entity IDs.
     */
    public void updateEntitySubscriptions(Set<String> entityIds) {
        synchronized (lifecycleLock) {
            if (!isConnected()) {
                return;
            }

            if (entityIds.isEmpty()) {
                if (activeSubscriptionId != -1) {
                    sendCommand(new UnsubscribeEventsCommand(nextMessageId(), activeSubscriptionId));
                    activeSubscriptionId = -1;
                }
                subscribedEntityIds.clear();
                return;
            }

            if (subscribedEntityIds.equals(entityIds) && activeSubscriptionId != -1) {
                return;
            }

            if (activeSubscriptionId != -1) {
                sendCommand(new UnsubscribeEventsCommand(nextMessageId(), activeSubscriptionId));
                activeSubscriptionId = -1;
            }

            int subscribeId = nextMessageId();
            pendingSubscribeId = subscribeId;
            activeSubscriptionId = subscribeId;
            subscribedEntityIds.clear();
            subscribedEntityIds.addAll(entityIds);

            sendCommand(new SubscribeEntitiesCommand(subscribeId, entityIds));
        }
    }

    @Override
    public void onWebSocketConnect(@NonNullByDefault({}) Session session) {
        logger.debug("Connected to Home Assistant WebSocket");
        synchronized (lifecycleLock) {
            if (shuttingDown) {
                session.close(StatusCode.SHUTDOWN, SHUTDOWN_REASON);
                return;
            }
            this.session = session;
            WebSocketPolicy policy = session.getPolicy();
            if (policy != null) {
                policy.setMaxTextMessageSize(MAX_TEXT_MESSAGE_SIZE);
            }
        }
    }

    @Override
    public void onWebSocketClose(int statusCode, @NonNullByDefault({}) String reason) {
        logger.debug("Home Assistant WebSocket closed: {} ({})", reason, statusCode);
        boolean expectedShutdown;
        synchronized (lifecycleLock) {
            expectedShutdown = shuttingDown;
            session = null;
            sessionFuture = null;
        }
        listener.onConnectionClosed(reason);
        if (statusCode == StatusCode.NORMAL && expectedShutdown) {
            return;
        }
        scheduleReconnect();
    }

    @Override
    public void onWebSocketError(@NonNullByDefault({}) Throwable cause) {
        String message = cause.getMessage();
        logger.debug("Home Assistant WebSocket error: {}", message, cause);
        synchronized (lifecycleLock) {
            Session localSession = session;
            if (localSession != null) {
                localSession.close(StatusCode.SERVER_ERROR, "Communication error");
            }
            session = null;
            sessionFuture = null;
        }
        listener.onConnectionError(message != null ? message : cause.getClass().getSimpleName());
        scheduleReconnect();
    }

    @Override
    public void onWebSocketBinary(@NonNullByDefault({}) byte[] payload, int offset, int len) {
        logger.debug("Ignoring unsupported binary WebSocket frame (offset={}, len={})", offset, len);
    }

    @Override
    public void onWebSocketText(@NonNullByDefault({}) String message) {
        logger.trace("RECV | {}", message);
        JsonObject frame;
        try {
            frame = JsonParser.parseString(message).getAsJsonObject();
        } catch (JsonParseException | IllegalStateException e) {
            logger.warn("Failed to parse incoming WebSocket message: {}", e.getMessage());
            return;
        }
        JsonElement typeElement = frame.get("type");
        String type = typeElement != null && typeElement.isJsonPrimitive() ? typeElement.getAsString() : null;
        if (type == null) {
            logger.debug("Received WebSocket message with no type, ignoring");
            return;
        }
        switch (type) {
            case TYPE_AUTH_REQUIRED -> sendCommand(new AuthCommand(accessToken));
            case TYPE_AUTH_OK -> handleAuthOk(frame);
            case TYPE_AUTH_INVALID -> handleAuthInvalid(frame);
            case TYPE_RESULT -> {
                ResultMessage result = gson.fromJson(frame, ResultMessage.class);
                if (result != null) {
                    handleResult(result);
                }
            }
            case TYPE_EVENT -> handleEvent(frame);
            default -> logger.trace("Unhandled message type: {}", type);
        }
    }

    private void handleEvent(JsonObject frame) {
        JsonElement eventElement = frame.get("event");
        if (eventElement == null || !eventElement.isJsonObject()) {
            return;
        }
        JsonObject eventObj = eventElement.getAsJsonObject();

        // Check for subscribe_entities compressed diff payload ("c", "a", or "r" keys)
        if (eventObj.has("c") || eventObj.has("a") || eventObj.has("r")) {
            EntityStateCompressedEvent compressedEvent = gson.fromJson(eventObj, EntityStateCompressedEvent.class);
            if (compressedEvent != null) {
                listener.onCompressedEvent(compressedEvent);
            }
            return;
        }

        if (eventObj.has("event_type")) {
            String eventType = eventObj.get("event_type").getAsString();

            switch (eventType) {
                case EVENT_TYPE_STATE_CHANGED -> {
                    // Fallback check for subscribe_events state_changed payload
                    // (if subscribe_entities is not supported)
                    try {
                        StateChangedEventData data = gson.fromJson(eventObj.get("data"), StateChangedEventData.class);
                        EntityState newState = data != null ? data.newState : null;
                        if (newState != null) {
                            listener.onEntityStateChanged(newState);
                        }
                    } catch (RuntimeException e) { // Catch RuntimeException from EntityState constructor
                        logger.debug("Failed to parse state_changed event data: {}", e.getMessage());
                    }
                }
                case EVENT_DEVICE_REGISTRY_UPDATED, EVENT_ENTITY_REGISTRY_UPDATED, EVENT_AREA_REGISTRY_UPDATED -> {
                    logger.debug("Received registry update event ({}), refreshing registry lists", eventType);
                    sendRegistryListRequest();
                }
                default -> logger.trace("Unhandled event type: {}", eventType);
            }
        }
    }

    private void handleAuthOk(JsonObject frame) {
        reconnectDelaySeconds = INITIAL_RECONNECT_DELAY_SECONDS;
        JsonElement versionElement = frame.get("ha_version");
        String haVersion = versionElement != null && versionElement.isJsonPrimitive() ? versionElement.getAsString()
                : "";

        activeSubscriptionId = -1;
        subscribedEntityIds.clear();

        listener.onAuthenticated(haVersion);

        int getConfigId = nextMessageId();
        pendingGetConfigId = getConfigId;
        sendCommand(new GetConfigCommand(getConfigId));

        int getEntitiesId = nextMessageId();
        pendingGetEntitiesId = getEntitiesId;
        sendCommand(new GetEntitiesCommand(getEntitiesId));

        sendRegistryListRequest();
        subscribeRegistryEvents();
    }

    private void subscribeRegistryEvents() {
        sendCommand(new SubscribeEventsCommand(nextMessageId(), EVENT_DEVICE_REGISTRY_UPDATED));
        sendCommand(new SubscribeEventsCommand(nextMessageId(), EVENT_ENTITY_REGISTRY_UPDATED));
        sendCommand(new SubscribeEventsCommand(nextMessageId(), EVENT_AREA_REGISTRY_UPDATED));
    }

    private void handleAuthInvalid(JsonObject frame) {
        JsonElement messageElement = frame.get("message");
        String message = messageElement != null && messageElement.isJsonPrimitive() ? messageElement.getAsString()
                : "Authentication rejected by Home Assistant";
        listener.onAuthenticationFailed(message);
    }

    private void handleResult(ResultMessage result) {
        Integer id = result.id;
        if (id == null) {
            return;
        }
        if (id == pendingGetConfigId) {
            handleGetConfigResult(result);
            return;
        }
        if (id == pendingGetEntitiesId) {
            handleGetEntitiesResult(result);
            return;
        }
        if (id == pendingDeviceRegistryId) {
            listener.onDeviceRegistrySnapshot(toJsonObjectList(result));
            return;
        }
        if (id == pendingEntityRegistryId) {
            listener.onEntityRegistrySnapshot(toJsonObjectList(result));
            return;
        }
        ResultError error = result.error;
        if (id == pendingSubscribeId) {
            if (!result.success) {
                logger.warn("Failed to subscribe to Home Assistant entity updates: {}",
                        error != null ? error.message : "unknown error");
                int fallbackId = nextMessageId();
                pendingSubscribeId = fallbackId;
                sendCommand(new SubscribeEventsCommand(fallbackId, EVENT_TYPE_STATE_CHANGED));
            }
            return;
        }
        if (logger.isDebugEnabled() && !result.success) {
            logger.debug("Received error result for message id {}: {}", id,
                    error != null ? error.message : "unknown error");
        }
    }

    private void handleGetConfigResult(ResultMessage result) {
        JsonElement resultData = result.result;
        if (!result.success || resultData == null || !resultData.isJsonObject()) {
            return;
        }

        JsonObject rawConfig = resultData.getAsJsonObject();
        HomeAssistantConfig config = gson.fromJson(rawConfig, HomeAssistantConfig.class);
        if (config != null) {
            listener.onConfigSnapshot(config, rawConfig);
        }
    }

    private void handleGetEntitiesResult(ResultMessage result) {
        JsonElement resultData = result.result;
        if (!result.success || resultData == null || !resultData.isJsonArray()) {
            ResultError error = result.error;
            logger.warn("Failed to retrieve Home Assistant entities: {}",
                    error != null ? error.message : "unknown error");
            return;
        }
        List<EntityState> entities = new ArrayList<>();
        for (JsonElement element : resultData.getAsJsonArray()) {
            try {
                EntityState entity = gson.fromJson(element, EntityState.class);
                if (entity != null) {
                    entities.add(entity);
                }
            } catch (RuntimeException e) {
                logger.debug("Failed to parse entity state: {}", e.getMessage());
            }
        }
        listener.onEntitiesSnapshot(entities);
    }

    private List<JsonObject> toJsonObjectList(ResultMessage result) {
        JsonElement resultData = result.result;
        if (!result.success || resultData == null || !resultData.isJsonArray()) {
            ResultError error = result.error;
            logger.warn("Failed to retrieve Home Assistant registry: {}",
                    error != null ? error.message : "unknown error");
            return List.of();
        }
        List<JsonObject> entries = new ArrayList<>();
        for (JsonElement element : resultData.getAsJsonArray()) {
            if (element.isJsonObject()) {
                entries.add(element.getAsJsonObject());
            }
        }
        return entries;
    }

    private void scheduleReconnect() {
        synchronized (lifecycleLock) {
            if (shuttingDown || reconnectFuture != null) {
                return;
            }
            int delay = reconnectDelaySeconds;
            reconnectDelaySeconds = Math.min(reconnectDelaySeconds * 2, MAX_RECONNECT_DELAY_SECONDS);
            logger.debug("Scheduling reconnect to Home Assistant WebSocket in {} seconds", delay);
            reconnectFuture = scheduler.schedule(this::reconnect, delay, TimeUnit.SECONDS);
        }
    }

    private void reconnect() {
        synchronized (lifecycleLock) {
            reconnectFuture = null;
            try {
                startLocked();
            } catch (CommunicationException e) {
                logger.debug("Reconnect attempt to Home Assistant WebSocket failed: {}", e.getMessage());
                scheduleReconnect();
            }
        }
    }
}
