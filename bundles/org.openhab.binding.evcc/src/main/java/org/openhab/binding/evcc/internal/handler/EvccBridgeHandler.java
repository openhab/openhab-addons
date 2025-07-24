/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.binding.evcc.internal.EvccConfiguration;
import org.openhab.binding.evcc.internal.discovery.EvccDiscoveryService;
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

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * The {@link EvccBridgeHandler} is responsible for creating the bridge and thing
 * handlers.
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class EvccBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(EvccBridgeHandler.class);
    private final Gson gson = new Gson();

    private final HttpClient httpClient;
    private final CopyOnWriteArrayList<EvccJsonAwareHandler> listeners = new CopyOnWriteArrayList<>();
    private @Nullable ScheduledFuture<?> pollJob;
    private volatile JsonObject lastState = new JsonObject();
    private String endpoint = "";

    public EvccBridgeHandler(Bridge bridge, HttpClientFactory httpClientFactory) {
        super(bridge);
        httpClient = httpClientFactory.getCommonHttpClient();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(EvccDiscoveryService.class);
    }

    @Override
    public void initialize() {
        EvccConfiguration config = getConfigAs(EvccConfiguration.class);
        endpoint = config.scheme + "://" + config.host + ":" + config.port + "/api/state";

        startPolling(config.pollInterval);

        fetchEvccState().ifPresent(state -> {
            this.lastState = state;
            updateStatus(ThingStatus.ONLINE);
        });
    }

    @Override
    public void dispose() {
        Optional.ofNullable(pollJob).ifPresent(job -> {
            job.cancel(true);
        });
        listeners.clear();
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public String getBaseURL() {
        return endpoint.substring(0, endpoint.lastIndexOf("/"));
    }

    private void startPolling(int refreshInterval) {
        if (refreshInterval <= 0) {
            refreshInterval = 30;
        }
        pollJob = scheduler.scheduleWithFixedDelay(() -> {
            fetchEvccState().ifPresent(state -> {
                this.lastState = state;
                notifyListeners(state);
            });
        }, refreshInterval, refreshInterval, TimeUnit.SECONDS);
    }

    public Optional<JsonObject> fetchEvccState() {
        try {
            ContentResponse response = httpClient.newRequest(endpoint).timeout(5, TimeUnit.SECONDS)
                    .header(HttpHeader.ACCEPT, "application/json").send();

            if (response.getStatus() == 200) {
                @Nullable
                JsonObject returnValue = gson.fromJson(response.getContentAsString(), JsonObject.class);
                if (null != returnValue) {
                    updateStatus(ThingStatus.ONLINE);
                    if (returnValue.has("result")) {
                        return Optional.of(returnValue.getAsJsonObject("result"));
                    } else {
                        return Optional.of(returnValue);
                    }
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        Integer.toString(response.getStatus()));
            }
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
        }

        return Optional.empty();
    }

    private void notifyListeners(JsonObject state) {
        for (EvccJsonAwareHandler listener : listeners) {
            try {
                listener.updateFromEvccState(state);
            } catch (Exception e) {
                if (listener instanceof BaseThingHandler handler) {
                    logger.warn("Listener {} couldn't parse evcc state", handler.getThing().getUID(), e);
                } else {
                    logger.debug("Listener {} is not instance of BaseThingHandlder", listener, e);
                }
            }
        }
    }

    public JsonObject getCachedEvccState() {
        return lastState;
    }

    public void register(EvccJsonAwareHandler handler) {
        listeners.addIfAbsent(handler);
        Optional.of(lastState).ifPresent(handler::updateFromEvccState);
    }

    public void unregister(EvccJsonAwareHandler handler) {
        listeners.remove(handler);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        return; // No commands to handle!
    }
}
