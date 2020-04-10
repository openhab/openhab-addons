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
package org.openhab.binding.dwdpollenflug.internal.handler;

import static org.openhab.binding.dwdpollenflug.internal.DWDPollenflugBindingConstants.*;

import java.net.URI;
import java.util.List;
import java.net.SocketTimeoutException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.dwdpollenflug.internal.DWDPollingException;
import org.openhab.binding.dwdpollenflug.internal.config.DWDPollenflugBridgeConfiguration;
import org.openhab.binding.dwdpollenflug.internal.dto.DWDPollenflug;
import org.openhab.binding.dwdpollenflug.internal.dto.DWDPollenflugJSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DWDPollenflugBridgeHandler} is the handler for bridge thing
 *
 * @author Johannes DerOetzi Ott - Initial contribution
 */
@NonNullByDefault
public class DWDPollenflugBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(DWDPollenflugBridgeHandler.class);

    private static final String DWD_URL = "https://opendata.dwd.de/climate_environment/health/alerts/s31fg.json";

    private DWDPollenflugBridgeConfiguration bridgeConfig = new DWDPollenflugBridgeConfiguration();

    private @Nullable ScheduledFuture<?> pollingJob;

    private @Nullable DWDPollenflug pollenflug;

    private final List<DWDPollenflugRegionListener> regionListeners = new CopyOnWriteArrayList<>();

    private final HttpClient client;

    private final Gson gson = new Gson();

    public DWDPollenflugBridgeHandler(Bridge bridge, HttpClient client) {
        super(bridge);
        this.client = client;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing DWD Pollenflug bridge handler");
        bridgeConfig = getConfigAs(DWDPollenflugBridgeConfiguration.class);

        if (bridgeConfig.isValid()) {
            onUpdate();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        }
    }

    private synchronized void onUpdate() {
        if (regionListeners.isEmpty()) {
            stopPolling();
            updateStatus(ThingStatus.ONLINE);
        } else {
            startPolling();
        }
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposed.");
        stopPolling();
    }

    public void startPolling() {
        final ScheduledFuture<?> localPollingJob = this.pollingJob;
        if (localPollingJob == null || localPollingJob.isCancelled()) {
            logger.debug("Start polling.");
            pollingJob = scheduler.scheduleWithFixedDelay(this::poll, INITIAL_DELAY,
                    bridgeConfig.getRefresh() * SECONDS_PER_MINUTE, TimeUnit.SECONDS);
        } else if (pollenflug != null) {
            notifyOnUpdate(pollenflug);
        }
    }

    public void stopPolling() {
        final ScheduledFuture<?> localPollingJob = this.pollingJob;
        if (localPollingJob != null && !localPollingJob.isCancelled()) {
            logger.debug("Stop polling.");
            localPollingJob.cancel(true);
            pollingJob = null;
        }
    }

    public void poll() {
        logger.debug("Polling");
        requestRefresh().handle((pollenflug, e) -> {
            if (pollenflug == null) {
                if (e == null) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                }
            } else {
                updateStatus(ThingStatus.ONLINE);
                notifyOnUpdate(pollenflug);
            }

            return null;
        });
    }

    private CompletableFuture<DWDPollenflug> requestRefresh() {
        CompletableFuture<DWDPollenflug> f = new CompletableFuture<>();
        Request request = client.newRequest(URI.create(DWD_URL));

        request.method(HttpMethod.GET).timeout(2000, TimeUnit.SECONDS).send(new BufferingResponseListener() {
            @NonNullByDefault({})
            @Override
            public void onComplete(Result result) {
                final HttpResponse response = (HttpResponse) result.getResponse();
                if (result.getFailure() != null) {
                    Throwable e = result.getFailure();
                    if (e instanceof SocketTimeoutException || e instanceof TimeoutException) {
                        f.completeExceptionally(new DWDPollingException("Request timeout", e));
                    } else {
                        f.completeExceptionally(new DWDPollingException("Request failed", e));
                    }
                } else if (response.getStatus() != 200) {
                    f.completeExceptionally(new DWDPollingException(getContentAsString()));
                } else {
                    try {
                        DWDPollenflugJSON pollenflugJSON = gson.fromJson(getContentAsString(), DWDPollenflugJSON.class);
                        f.complete(new DWDPollenflug(pollenflugJSON));
                    } catch (JsonSyntaxException ex2) {
                        f.completeExceptionally(new DWDPollingException("Parsing of response failed"));
                    }
                }
            }
        });

        return f;
    }

    public synchronized boolean registerRegionListener(DWDPollenflugRegionListener regionListener) {
        logger.debug("Register region listener");
        boolean result = regionListeners.add(regionListener);
        if (result) {
            startPolling();
        }
        return result;
    }

    public synchronized boolean unregisterRegionListener(DWDPollenflugRegionListener regionListener) {
        logger.debug("Unregister region listener");
        boolean result = regionListeners.remove(regionListener);
        if (result && regionListeners.isEmpty()) {
            stopPolling();
        }
        return result;
    }

    public synchronized void notifyOnUpdate(@Nullable DWDPollenflug newState) {
        pollenflug = newState;
        if (newState != null) {
            updateProperties(newState.getProperties());

            newState.getChannels().forEach((channelID, value) -> {
                logger.debug("Updating channel {} to {}", channelID, value);
                updateState(channelID, value);
            });

            for (DWDPollenflugRegionListener regionListener : regionListeners) {
                regionListener.notifyOnUpdate(newState);
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    public @Nullable DWDPollenflug getPollenflug() {
        return pollenflug;
    }
}
