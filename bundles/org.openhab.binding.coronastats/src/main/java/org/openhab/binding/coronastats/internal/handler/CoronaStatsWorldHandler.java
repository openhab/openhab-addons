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
package org.openhab.binding.coronastats.internal.handler;

import java.net.SocketTimeoutException;
import java.net.URI;
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
import org.openhab.binding.coronastats.internal.CoronaStatsPollingException;
import org.openhab.binding.coronastats.internal.config.CoronaStatsWorldConfiguration;
import org.openhab.binding.coronastats.internal.dto.CoronaStats;
import org.openhab.binding.coronastats.internal.dto.CoronaStatsWorld;
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
 * The {@link CoronaStatsWorldHandler} is the handler for bridge thing
 *
 * @author Johannes Ott - Initial contribution
 */
@NonNullByDefault
public class CoronaStatsWorldHandler extends BaseBridgeHandler {
    private static final String CORONASTATS_URL = "https://corona-stats.online/?format=json";

    private final Logger logger = LoggerFactory.getLogger(CoronaStatsWorldHandler.class);

    private CoronaStatsWorldConfiguration worldConfig = new CoronaStatsWorldConfiguration();
    private @Nullable ScheduledFuture<?> pollingJob;
    private @Nullable CoronaStats coronaStats;
    private final Set<CoronaStatsCountryHandler> countryListeners = ConcurrentHashMap.newKeySet();
    private final HttpClient client;
    private final Gson gson = new Gson();

    public CoronaStatsWorldHandler(Bridge bridge, HttpClient client) {
        super(bridge);
        this.client = client;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            final CoronaStats localCoronaStats = coronaStats;
            if (localCoronaStats != null) {
                notifyOnUpdate(localCoronaStats);
            }
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Corona Stats bridge handler");
        worldConfig = getConfigAs(CoronaStatsWorldConfiguration.class);

        if (worldConfig.isValid()) {
            startPolling();
            updateStatus(ThingStatus.UNKNOWN);
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
            pollingJob = scheduler.scheduleWithFixedDelay(this::poll, 0, worldConfig.refresh, TimeUnit.MINUTES);
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
        requestRefresh().handle((resultCoronaStats, pollException) -> {
            if (resultCoronaStats == null) {
                if (pollException == null) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            pollException.getMessage());
                }
            } else {
                updateStatus(ThingStatus.ONLINE);
                notifyOnUpdate(resultCoronaStats);
            }

            return null;
        });
    }

    private CompletableFuture<@Nullable CoronaStats> requestRefresh() {
        CompletableFuture<@Nullable CoronaStats> f = new CompletableFuture<>();
        Request request = client.newRequest(URI.create(CORONASTATS_URL));

        request.method(HttpMethod.GET).timeout(2000, TimeUnit.SECONDS).send(new BufferingResponseListener() {
            @NonNullByDefault({})
            @Override
            public void onComplete(Result result) {
                final HttpResponse response = (HttpResponse) result.getResponse();
                if (result.getFailure() != null) {
                    Throwable e = result.getFailure();
                    if (e instanceof SocketTimeoutException || e instanceof TimeoutException) {
                        f.completeExceptionally(new CoronaStatsPollingException("Request timeout", e));
                    } else {
                        f.completeExceptionally(new CoronaStatsPollingException("Request failed", e));
                    }
                } else if (response.getStatus() != 200) {
                    f.completeExceptionally(new CoronaStatsPollingException(getContentAsString()));
                } else {
                    try {
                        CoronaStats coronaStatsJSON = gson.fromJson(getContentAsString(), CoronaStats.class);
                        f.complete(coronaStatsJSON);
                    } catch (JsonSyntaxException parseException) {
                        logger.error("Parsing failed: {}", parseException.getMessage());
                        f.completeExceptionally(new CoronaStatsPollingException("Parsing of response failed"));
                    }
                }
            }
        });

        return f;
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        if (childHandler instanceof CoronaStatsCountryHandler listener) {
            logger.debug("Register thing listener.");
            if (countryListeners.add(listener)) {
                final CoronaStats localCoronaStats = coronaStats;
                if (localCoronaStats != null) {
                    listener.notifyOnUpdate(localCoronaStats);
                }
            } else {
                logger.warn("Tried to add listener {} but it was already present. This is probably an error.",
                        childHandler);
            }
        }
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        if (childHandler instanceof CoronaStatsCountryHandler countryHandler) {
            logger.debug("Unregister thing listener.");
            if (!countryListeners.remove(countryHandler)) {
                logger.warn("Tried to remove listener {} but it was not registered. This is probably an error.",
                        childHandler);
            }
        }
    }

    public void notifyOnUpdate(@Nullable CoronaStats newCoronaStats) {
        if (newCoronaStats != null) {
            coronaStats = newCoronaStats;

            CoronaStatsWorld world = newCoronaStats.getWorld();
            if (world == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "World stats not found");
                return;
            }

            world.getChannelsStateMap().forEach(this::updateState);
            countryListeners.forEach(listener -> listener.notifyOnUpdate(newCoronaStats));
        }
    }

    public @Nullable CoronaStats getCoronaStats() {
        return coronaStats;
    }
}
