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
package org.openhab.binding.dwdpollenflug.internal.handler;

import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.dwdpollenflug.internal.DWDPollingException;
import org.openhab.binding.dwdpollenflug.internal.config.DWDPollenflugBridgeConfiguration;
import org.openhab.binding.dwdpollenflug.internal.dto.DWDPollenflug;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link DWDPollenflugBridgeHandler} is the handler for bridge thing
 *
 * @author Johannes Ott - Initial contribution
 */
@NonNullByDefault
public class DWDPollenflugBridgeHandler extends BaseBridgeHandler {
    private static final String DWD_URL = "https://opendata.dwd.de/climate_environment/health/alerts/s31fg.json";

    private final Logger logger = LoggerFactory.getLogger(DWDPollenflugBridgeHandler.class);

    private DWDPollenflugBridgeConfiguration bridgeConfig = new DWDPollenflugBridgeConfiguration();
    private @Nullable ScheduledFuture<?> pollingJob;
    private @Nullable DWDPollenflug pollenflug;
    private final Set<DWDPollenflugRegionHandler> regionListeners = ConcurrentHashMap.newKeySet();
    private final HttpClient client;
    private final Gson gson = new Gson();

    public DWDPollenflugBridgeHandler(Bridge bridge, HttpClient client) {
        super(bridge);
        this.client = client;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            final DWDPollenflug localPollenflug = pollenflug;
            if (localPollenflug != null) {
                notifyOnUpdate(localPollenflug);
            }
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing DWD Pollenflug bridge handler");
        bridgeConfig = getConfigAs(DWDPollenflugBridgeConfiguration.class);

        if (bridgeConfig.isValid()) {
            updateStatus(ThingStatus.UNKNOWN);
            startPolling();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Refresh interval has to be at least 15 minutes.");
        }
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposed.");
        stopPolling();
    }

    private void startPolling() {
        final ScheduledFuture<?> localPollingJob = this.pollingJob;
        if (localPollingJob == null || localPollingJob.isCancelled()) {
            logger.debug("Start polling.");
            pollingJob = scheduler.scheduleWithFixedDelay(this::poll, 0, bridgeConfig.refresh, TimeUnit.MINUTES);
        }
    }

    private void stopPolling() {
        final ScheduledFuture<?> localPollingJob = this.pollingJob;
        if (localPollingJob != null && !localPollingJob.isCancelled()) {
            logger.debug("Stop polling.");
            localPollingJob.cancel(true);
            pollingJob = null;
        }
    }

    private void poll() {
        logger.debug("Polling");
        requestRefresh().handle((resultPollenflug, pollException) -> {
            if (resultPollenflug == null) {
                if (pollException == null) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            pollException.getMessage());
                }
            } else {
                updateStatus(ThingStatus.ONLINE);
                notifyOnUpdate(resultPollenflug);
            }

            return null;
        });
    }

    private CompletableFuture<@Nullable DWDPollenflug> requestRefresh() {
        CompletableFuture<@Nullable DWDPollenflug> f = new CompletableFuture<>();
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
                    f.completeExceptionally(new DWDPollingException(Objects.requireNonNull(getContentAsString())));
                } else {
                    try {
                        DWDPollenflug pollenflugJSON = gson.fromJson(getContentAsString(), DWDPollenflug.class);
                        f.complete(pollenflugJSON);
                    } catch (JsonSyntaxException ex2) {
                        f.completeExceptionally(new DWDPollingException("Parsing of response failed"));
                    }
                }
            }
        });

        return f;
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        if (childHandler instanceof DWDPollenflugRegionHandler regionListener) {
            logger.debug("Register region listener.");
            if (regionListeners.add(regionListener)) {
                final DWDPollenflug localPollenflug = pollenflug;
                if (localPollenflug != null) {
                    regionListener.notifyOnUpdate(localPollenflug);
                }
            } else {
                logger.warn("Tried to add listener {} but it was already present. This is probably an error.",
                        childHandler);
            }
        }
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        if (childHandler instanceof DWDPollenflugRegionHandler handler) {
            logger.debug("Unregister region listener.");
            if (!regionListeners.remove(handler)) {
                logger.warn("Tried to remove listener {} but it was not registered. This is probably an error.",
                        childHandler);
            }
        }
    }

    public void notifyOnUpdate(@Nullable DWDPollenflug newPollenflug) {
        if (newPollenflug != null) {
            pollenflug = newPollenflug;
            updateProperties(newPollenflug.getProperties());
            regionListeners.forEach(listener -> listener.notifyOnUpdate(newPollenflug));
            newPollenflug.getChannelsStateMap().forEach(this::updateState);
        }
    }

    public @Nullable DWDPollenflug getPollenflug() {
        return pollenflug;
    }
}
