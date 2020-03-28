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

import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.gson.Gson;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.dwdpollenflug.internal.config.DWDPollenflugConfiguration;
import org.openhab.binding.dwdpollenflug.internal.dto.DWDPollenflugindex;
import org.openhab.binding.dwdpollenflug.internal.dto.DWDRegion;
import org.openhab.binding.dwdpollenflug.internal.dto.DWDResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DWDPollenflugHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Johannes DerOetzi Ott - Initial contribution
 */
@NonNullByDefault
public class DWDPollenflugHandler extends BaseThingHandler {

    private static final int INITIAL_DELAY = 1;

    private static final int SECONDS_PER_MINUTE = 60;

    private static final String DWD_URL = "https://opendata.dwd.de/climate_environment/health/alerts/s31fg.json";

    private final Logger logger = LoggerFactory.getLogger(DWDPollenflugHandler.class);

    private boolean initializing;
    private @Nullable DWDPollenflugConfiguration config;

    private @Nullable ScheduledFuture<?> pollingJob;
    private boolean polling;

    private final HttpClient httpClient;
    private final Gson gson;

    public DWDPollenflugHandler(Thing thing, HttpClient httpClient, Gson gson) {
        super(thing);
        this.httpClient = httpClient;
        this.gson = gson;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            scheduler.submit(this::poll);
        }
    }

    private void poll() {
        if (polling) {
            logger.trace("Already polling. Ignoring refresh request.");
            return;
        }

        if (initializing) {
            logger.trace("Still initializing. Ignoring refresh request.");
            return;
        }

        logger.debug("Polling");

        polling = true;

        requestData().thenApply(this::parseDWDResponse).exceptionally(e -> {
            if (e instanceof SocketTimeoutException || e instanceof TimeoutException
                    || e instanceof CompletionException) {
                logger.debug("Failed to request data");
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }

            return null;
        }).thenAccept(pollenflugindex -> {
            if (pollenflugindex == null) {
                return;
            }

            DWDRegion region = pollenflugindex.getRegion(config.getRegionId());
            if (region == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Region not found!");
                return;
            }

            updateStatus(ThingStatus.ONLINE);

            updateData(region);
        });

        polling = false;
    }

    private void updateData(DWDRegion region) {
        updateProperties(region);
    }

    private void updateProperties(DWDRegion region) {
        logger.debug("Region ({}): {}", region.getRegionId(), region.getRegionName());

        Map<String, String> properties = new HashMap<String, String>();
        properties.put(PROPERTY_REGION_ID, Integer.toString(region.getRegionId()));
        properties.put(PROPERTY_REGION_NAME, region.getRegionName());

        if (region.isPartRegion()) {
            logger.debug("Partregion ({}): {}", region.getPartregionId(), region.getPartregionName());

            properties.put(PROPERTY_PARTREGION_ID, Integer.toString(region.getPartregionId()));
            properties.put(PROPERTY_PARTREGION_NAME, region.getPartregionName());
        }

        for (Entry<String, String> property : properties.entrySet()) {
            String existing = thing.getProperties().get(property.getKey());
            if (existing == null || !existing.equals(property.getValue())) {
                thing.setProperty(property.getKey(), property.getValue());
            }
        }
    }

    private @Nullable DWDPollenflugindex parseDWDResponse(DWDResponse r) {
        if (r.getResponseCode() == 200) {
            return gson.fromJson(r.getBody(), DWDPollenflugindex.class);
        } else {
            return null;
        }
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing!");
        initializing = true;
        updateStatus(ThingStatus.UNKNOWN);

        config = getConfigAs(DWDPollenflugConfiguration.class);

        if (config.isValid()) {
            stopPolling();
            startPolling();
        } else {
            logger.warn("Configuration error");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        }

        initializing = false;
        logger.debug("Finished initializing!");
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposed.");
        stopPolling();
    }

    private void startPolling() {
        if (pollingJob == null || pollingJob.isCancelled()) {
            logger.debug("start polling job at interval {}min", config.getRefresh());
            pollingJob = scheduler.scheduleWithFixedDelay(this::poll, INITIAL_DELAY,
                    config.getRefresh() * SECONDS_PER_MINUTE, TimeUnit.SECONDS);
        }
    }

    private void stopPolling() {
        if (!(pollingJob == null || pollingJob.isCancelled())) {
            logger.debug("stop polling job");
            pollingJob.cancel(true);
            pollingJob = null;
        }
    }

    private CompletableFuture<DWDResponse> requestData() {
        final CompletableFuture<DWDResponse> f = new CompletableFuture<>();
        Request request = httpClient.newRequest(URI.create(DWD_URL));

        request.method(HttpMethod.GET).timeout(2000, TimeUnit.MILLISECONDS).send(new BufferingResponseListener() {

            @NonNullByDefault({})
            @Override
            public void onComplete(Result result) {
                final HttpResponse response = (HttpResponse) result.getResponse();
                if (result.getFailure() != null) {
                    f.completeExceptionally(result.getFailure());
                    return;
                }
                f.complete(new DWDResponse(getContentAsString(), response.getStatus()));
            }
        });

        return f;
    }
}
