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
package org.openhab.binding.evcc.internal.handler;

import static org.openhab.binding.evcc.internal.EvccBindingConstants.API_PATH_STATE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.evcc.internal.EvccBridgeConfiguration;
import org.openhab.binding.evcc.internal.api.EvccRequestQueue;
import org.openhab.binding.evcc.internal.api.EvccWebSocketClient;
import org.openhab.binding.evcc.internal.discovery.EvccThingDiscoveryService;
import org.openhab.binding.evcc.internal.handler.routing.MessageRouter;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

/**
 * The {@link EvccBridgeHandler} is responsible for creating the bridge and dispatch ws messages to the thing
 * handlers.
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class EvccBridgeHandler extends BaseBridgeHandler {
    private static final long PLAN_STATE_REFRESH_INTERVAL_MINUTES = 5;

    private final Logger logger = LoggerFactory.getLogger(EvccBridgeHandler.class);

    private final ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool("evcc-bridge-handler");

    private final Map<String, EvccThingLifecycleAware> listeners = new ConcurrentHashMap<>();
    private final Map<String, EvccThingLifecycleAware> pendingHandlers = new ConcurrentHashMap<>();
    final EvccRequestQueue requestQueue;

    private final TranslationProvider i18nProvider;
    private final LocaleProvider localeProvider;

    private final CachedJsonState cachedState = new CachedJsonState();
    private volatile boolean initialStateReceived = false;

    private final MessageRouter messageRouter = new MessageRouter();
    private @Nullable ScheduledFuture<?> planRefreshTask;

    private @Nullable EvccWebSocketClient wsClient;

    private String endpoint = "";

    public EvccBridgeHandler(Bridge bridge, HttpClientFactory httpClientFactory, TranslationProvider i18nProvider,
            LocaleProvider localeProvider) {
        super(bridge);
        this.i18nProvider = i18nProvider;
        this.localeProvider = localeProvider;

        requestQueue = new EvccRequestQueue(httpClientFactory.getCommonHttpClient());
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(EvccThingDiscoveryService.class);
    }

    @Override
    public void initialize() {
        EvccBridgeConfiguration cfg = getConfigAs(EvccBridgeConfiguration.class);

        endpoint = cfg.scheme + "://" + cfg.host + ":" + cfg.port + "/api";
        String wsUrl = endpoint.replace("http", "ws").replace("/api", "/ws");

        EvccWebSocketClient client = new EvccWebSocketClient(wsUrl, scheduler, this::onFullState, this::onPartialUpdate,
                this::onConnected, this::onDisconnected);

        client.start();
        wsClient = client;
        requestQueue.start();
    }

    /**
     * Dispose of the bridge handler.
     *
     * Stops the websocket client, clears all registered handlers and pending handlers,
     * and stops the request queue. Called when the bridge is being removed.
     */
    @Override
    public void dispose() {
        Optional.ofNullable(wsClient).ifPresent(EvccWebSocketClient::stop);
        stopPlanRefreshTask();
        listeners.clear();
        pendingHandlers.clear();
        requestQueue.stop();
    }

    /**
     * Handle incoming commands from the openHAB framework.
     *
     * The bridge does not support any commands; all commands are ignored.
     *
     * @param channelUID The channel that received the command
     * @param command The command to handle
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // no commands
    }

    /**
     * Get the base URL of the EVCC API.
     *
     * @return The endpoint URL (e.g., http://localhost:7070)
     */
    public String getBaseURL() {
        return endpoint;
    }

    /**
     * Get the translation provider for i18n support.
     *
     * @return The TranslationProvider for the binding
     */
    public TranslationProvider getI18nProvider() {
        return i18nProvider;
    }

    /**
     * Get the locale provider for i18n support.
     *
     * @return The LocaleProvider for the binding
     */
    public LocaleProvider getLocaleProvider() {
        return localeProvider;
    }

    /**
     * Handle websocket connection establishment.
     *
     * Updates bridge status to ONLINE and logs the connection event.
     */
    private void onConnected() {
        updateStatus(ThingStatus.ONLINE);
        logger.info("EVCC WebSocket connected");
    }

    /**
     * Handle websocket disconnection.
     *
     * Updates bridge status to OFFLINE with communication error detail and logs the event.
     */
    private void onDisconnected() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "WebSocket disconnected");
        logger.info("EVCC WebSocket disconnected");
    }

    /**
     * Handle full state updates from the websocket.
     *
     * Updates the cached state, processes any pending handlers waiting for initial state,
     * and notifies all registered handlers to reinitialize from the new full state.
     *
     * @param state The complete current state from the EVCC API
     */
    private void onFullState(JsonObject state) {
        logger.debug("Received full state from WebSocket");
        cachedState.updateFull(state);
        if (!initialStateReceived) {
            initialStateReceived = true;
            logger.info("Initial state received, activating all pending handlers");
        }
        processPendingHandlers();
        JsonObject stateCopy = cachedState.getCopy();
        for (EvccThingLifecycleAware listener : new ArrayList<>(listeners.values())) {
            try {
                listener.initializeThingFromLatestState(stateCopy);
            } catch (Exception e) {
                logListenerError(listener, e);
            }
        }
    }

    /**
     * Handle partial state updates from the websocket.
     *
     * Updates the cached state and dispatches the update to relevant handlers.
     * Also processes any pending handlers if initial state has now been received.
     *
     * @param key The update key (e.g., "battery", "grid", "pv.0")
     * @param value The updated value (partial JSON structure)
     */
    private void onPartialUpdate(String key, JsonElement value) {
        logger.trace("Received partial update: {}", key);
        cachedState.updatePartial(key, value);
        processPendingHandlers();
        dispatchUpdate(key, value);
    }

    /**
     * Register a handler to receive updates from this bridge.
     *
     * If initial state has already been received, the handler is immediately activated.
     * Otherwise, it is queued as a pending handler to be activated once initial state arrives.
     *
     * @param handler The handler to register (typically called from handler.initialize())
     */
    public void register(EvccThingLifecycleAware handler) {
        String handlerKey = handler.getType() + "$" + handler.getIdentifier();
        if (initialStateReceived) {
            logger.debug("Initial state received, activating handler immediately: {}", handlerKey);
            activateHandler(handlerKey, handler);
        } else {
            logger.debug("Initial state not yet received, queuing handler for later activation: {}", handlerKey);
            pendingHandlers.put(handlerKey, handler);
        }
        updatePlanRefreshTask();
    }

    /**
     * Activate a registered handler by initializing it from the latest cached state.
     *
     * Calls the handler's initializeThingFromLatestState() method to bootstrap its state.
     * Logs errors if activation fails but does not re-throw.
     *
     * @param handlerKey The handler's unique key (type + identifier)
     * @param handler The handler to activate
     */
    private void activateHandler(String handlerKey, EvccThingLifecycleAware handler) {
        listeners.put(handlerKey, handler);
        try {
            logger.debug("Initializing handler from latest state: {}", handlerKey);
            handler.initializeThingFromLatestState(cachedState.getCopy());
            logger.debug("Successfully activated handler: {}", handlerKey);
        } catch (Exception e) {
            logListenerError(handler, e);
        }
    }

    /**
     * Process all pending handlers waiting for initial state.
     *
     * Attempts to activate each pending handler. Successfully activated handlers are removed from the queue.
     * Failed activations are logged and will be retried on the next update.
     */
    private void processPendingHandlers() {
        if (pendingHandlers.isEmpty()) {
            return;
        }
        logger.debug("Processing {} pending handler(s)", pendingHandlers.size());
        List<String> keysToRemove = new ArrayList<>();
        for (Map.Entry<String, EvccThingLifecycleAware> entry : pendingHandlers.entrySet()) {
            try {
                activateHandler(entry.getKey(), entry.getValue());
                keysToRemove.add(entry.getKey());
            } catch (Exception e) {
                logger.debug("Failed to activate pending handler {}, will retry on next update", entry.getKey(), e);
            }
        }
        keysToRemove.forEach(key -> {
            pendingHandlers.remove(key);
            logger.debug("Removed handler from pending queue: {}", key);
        });
        if (!pendingHandlers.isEmpty()) {
            logger.debug("Still {} handler(s) pending after processing", pendingHandlers.size());
        }
    }

    /**
     * Unregister a handler from receiving further updates.
     *
     * Called when a handler is being disposed. The handler will no longer receive
     * partial updates or full state refreshes from the bridge.
     *
     * @param handler The handler to unregister
     */
    public void unregister(EvccThingLifecycleAware handler) {
        listeners.remove(handler.getType() + "$" + handler.getIdentifier());
        pendingHandlers.remove(handler.getType() + "$" + handler.getIdentifier());
        updatePlanRefreshTask();
    }

    /**
     * Get the message router used for websocket partial-update registration.
     *
     * Handlers call this during initialization to register the websocket keys
     * they consume and the extraction logic that normalizes incoming payloads.
     *
     * @return The MessageRouter instance for this bridge (never null)
     */
    public MessageRouter getMessageRouter() {
        return messageRouter;
    }

    /**
     * Dispatch an update to relevant handlers based on the message key.
     *
     * If a route matches the key, it is dispatched through the router.
     * Otherwise, if the key is a top-level key (no dots) and a site handler exists,
     * route it to the site handler as a fallback. This provides a safety net for
     * new evcc API fields that haven't been explicitly routed yet.
     *
     * @param key The update key (e.g., "battery", "pv.0", "grid")
     * @param value The update value (JSON structure)
     */
    private void dispatchUpdate(String key, JsonElement value) {
        if (messageRouter.route(key, value)) {
            return;
        }
        // Fallback: route top-level keys to site handler if no explicit route matched
        if (!key.contains(".")) {
            EvccThingLifecycleAware siteHandler = listeners.get("site$");
            if (siteHandler != null) {
                logger.debug("Routing unmatched top-level key '{}' to site handler", key);
                try {
                    siteHandler.handleUpdate(key, value);
                } catch (Exception e) {
                    logger.warn("Site handler failed to process fallback update for key '{}'", key, e);
                }
                return;
            }
        }
        logger.debug("No route registered for websocket update key '{}' and no site handler available for fallback",
                key);
    }

    /**
     * Log an error from a listener handler.
     *
     * Extracts the Thing UID from the handler and logs a warning message.
     * Silently ignores errors if the listener is not a BaseThingHandler.
     *
     * @param listener The handler that experienced an error
     * @param e The exception that occurred
     */
    private void logListenerError(EvccThingLifecycleAware listener, Exception e) {
        if (listener instanceof BaseThingHandler handler) {
            logger.warn("Listener {} failed to process EVCC update", handler.getThing().getUID(), e);
        }
    }

    /**
     * Get a copy of the current cached EVCC API state.
     *
     * This is used by handlers to initialize their state during activation.
     * A copy is returned to prevent external modification of the cached state.
     *
     * @return A JSON object containing the complete current EVCC state
     */
    public JsonObject getCachedEvccState() {
        return cachedState.getCopy();
    }

    private void updatePlanRefreshTask() {
        if (hasPlanHandler()) {
            ScheduledFuture<?> currentPlanRefreshTask = planRefreshTask;
            if (currentPlanRefreshTask == null || currentPlanRefreshTask.isCancelled()) {
                planRefreshTask = scheduler.scheduleWithFixedDelay(this::refreshPlanHandlersFromState,
                        PLAN_STATE_REFRESH_INTERVAL_MINUTES, PLAN_STATE_REFRESH_INTERVAL_MINUTES, TimeUnit.MINUTES);
            }
        } else {
            stopPlanRefreshTask();
        }
    }

    private boolean hasPlanHandler() {
        return listeners.values().stream().anyMatch(EvccPlanHandler.class::isInstance)
                || pendingHandlers.values().stream().anyMatch(EvccPlanHandler.class::isInstance);
    }

    private void stopPlanRefreshTask() {
        Optional.ofNullable(planRefreshTask).ifPresent(task -> task.cancel(true));
        planRefreshTask = null;
    }

    private void refreshPlanHandlersFromState() {
        JsonObject stateCopy = fetchStateSnapshot();
        for (EvccThingLifecycleAware listener : new ArrayList<>(listeners.values())) {
            if (!(listener instanceof EvccPlanHandler planHandler)) {
                continue;
            }
            try {
                planHandler.refreshFromState(stateCopy);
            } catch (Exception e) {
                logListenerError(planHandler, e);
            }
        }
    }

    private JsonObject fetchStateSnapshot() {
        String stateEndpoint = String.join("/", endpoint, API_PATH_STATE);
        try {
            final JsonObject[] responseHolder = new JsonObject[1];
            final Exception[] errorHolder = new Exception[1];
            final java.util.concurrent.CountDownLatch completion = new java.util.concurrent.CountDownLatch(1);
            requestQueue.enqueueRequest(stateEndpoint, org.eclipse.jetty.http.HttpMethod.GET, JsonNull.INSTANCE,
                    response -> {
                        try {
                            JsonElement parsed = com.google.gson.JsonParser.parseString(response.getContentAsString());
                            if (parsed.isJsonObject()) {
                                responseHolder[0] = parsed.getAsJsonObject();
                            }
                        } catch (Exception e) {
                            errorHolder[0] = e;
                        } finally {
                            completion.countDown();
                        }
                    }, error -> {
                        errorHolder[0] = error;
                        completion.countDown();
                    });
            if (!completion.await(6, TimeUnit.SECONDS) || errorHolder[0] != null || responseHolder[0] == null) {
                logger.debug("Falling back to cached state for plan refresh");
                return cachedState.getCopy();
            }
            return responseHolder[0];
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.debug("Interrupted while fetching evcc state snapshot", e);
            return cachedState.getCopy();
        }
    }
}
