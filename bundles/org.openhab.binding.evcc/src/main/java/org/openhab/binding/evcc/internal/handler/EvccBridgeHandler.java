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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.evcc.internal.EvccBridgeConfiguration;
import org.openhab.binding.evcc.internal.api.EvccRequestQueue;
import org.openhab.binding.evcc.internal.api.EvccWebSocketClient;
import org.openhab.binding.evcc.internal.discovery.EvccThingDiscoveryService;
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
import com.google.gson.JsonObject;

/**
 * The {@link EvccBridgeHandler} is responsible for creating the bridge and dispatch ws messages to the thing
 * handlers.
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class EvccBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(EvccBridgeHandler.class);

    private final ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool("evcc-bridge-handler");

    private final Map<String, EvccThingLifecycleAware> listeners = new ConcurrentHashMap<>();
    private final Map<String, EvccThingLifecycleAware> pendingHandlers = new ConcurrentHashMap<>();
    private final Map<String, String> propertyByRoot = new ConcurrentHashMap<>();

    final EvccRequestQueue requestQueue;

    private final TranslationProvider i18nProvider;
    private final LocaleProvider localeProvider;

    private final CachedJsonState cachedState = new CachedJsonState();
    private volatile boolean initialStateReceived = false;

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

    @Override
    public void dispose() {
        Optional.ofNullable(wsClient).ifPresent(EvccWebSocketClient::stop);
        listeners.clear();
        pendingHandlers.clear();
        requestQueue.stop();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // no commands
    }

    public String getBaseURL() {
        return endpoint;
    }

    public TranslationProvider getI18nProvider() {
        return i18nProvider;
    }

    public LocaleProvider getLocaleProvider() {
        return localeProvider;
    }

    // --------------------------------------------------------------------
    // WebSocket Callbacks
    // --------------------------------------------------------------------

    private void onConnected() {
        updateStatus(ThingStatus.ONLINE);
        logger.info("EVCC WebSocket connected");
    }

    private void onDisconnected() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "WebSocket disconnected");
        logger.info("EVCC WebSocket disconnected");
    }

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

    private void onPartialUpdate(String key, JsonElement value) {
        logger.trace("Received partial update: {}", key);
        cachedState.updatePartial(key, value);
        processPendingHandlers();
        dispatchUpdate(key, value);
    }

    // --------------------------------------------------------------------
    // Listener Registration
    // --------------------------------------------------------------------

    public void register(EvccThingLifecycleAware handler) {
        String handlerKey = handler.getType() + "$" + handler.getIdentifier();
        if (initialStateReceived) {
            logger.debug("Initial state received, activating handler immediately: {}", handlerKey);
            activateHandler(handlerKey, handler);
        } else {
            logger.debug("Initial state not yet received, queuing handler for later activation: {}", handlerKey);
            pendingHandlers.put(handlerKey, handler);
        }
    }

    private void activateHandler(String handlerKey, EvccThingLifecycleAware handler) {
        listeners.put(handlerKey, handler);
        for (String root : handler.getRootTypes()) {
            propertyByRoot.put(root, handler.getType());
        }
        try {
            logger.debug("Initializing handler from latest state: {}", handlerKey);
            handler.initializeThingFromLatestState(cachedState.getCopy());
            logger.debug("Successfully activated handler: {}", handlerKey);
        } catch (Exception e) {
            logListenerError(handler, e);
        }
    }

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

    public void unregister(EvccThingLifecycleAware handler) {
        listeners.remove(handler.getType() + "$" + handler.getIdentifier());
    }

    // --------------------------------------------------------------------
    // Dispatching
    // --------------------------------------------------------------------

    private void dispatchUpdate(String key, JsonElement value) {
        String[] p = key.split("\\.", 3);
        String root = p.length > 1 ? p[0] : "site";
        String id = p.length > 1 ? p[1] : "";
        String sub = p.length == 3 ? p[2] : p[0];

        for (EvccThingLifecycleAware l : new ArrayList<>(listeners.values())) {
            if (matchesType(l.getType(), root) && matchesIdentifier(l.getIdentifier(), id)) {
                l.handleUpdate(sub, value);
            }
        }
    }

    private boolean matchesType(String type, String root) {
        return type.equals(propertyByRoot.getOrDefault(root, ""));
    }

    private boolean matchesIdentifier(Object listenerId, String identifier) {
        if (listenerId instanceof Integer idInt) {
            return identifier.matches("\\d+") && idInt == Integer.parseInt(identifier);
        }
        if (listenerId instanceof String idStr) {
            return idStr.equals(identifier);
        }
        return false;
    }

    private void logListenerError(EvccThingLifecycleAware listener, Exception e) {
        if (listener instanceof BaseThingHandler handler) {
            logger.warn("Listener {} failed to process EVCC update", handler.getThing().getUID(), e);
        }
    }

    public JsonObject getCachedEvccState() {
        return cachedState.getCopy();
    }
}
