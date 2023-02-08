/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.onebusaway.internal.handler;

import static org.openhab.binding.onebusaway.internal.OneBusAwayBindingConstants.THING_TYPE_STOP;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.onebusaway.internal.config.StopConfiguration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link StopHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Shawn Wilsher - Initial contribution
 */
public class StopHandler extends BaseBridgeHandler {

    public static final ThingTypeUID SUPPORTED_THING_TYPE = THING_TYPE_STOP;

    private final Logger logger = LoggerFactory.getLogger(StopHandler.class);

    private StopConfiguration config;
    private Gson gson;
    private HttpClient httpClient;
    private ScheduledFuture<?> pollingJob;
    private AtomicBoolean fetchInProgress = new AtomicBoolean(false);
    private long routeDataLastUpdateMs = 0;
    private final Map<String, List<ObaStopArrivalResponse.ArrivalAndDeparture>> routeData = new HashMap<>();
    private List<RouteDataListener> routeDataListeners = new CopyOnWriteArrayList<>();

    public StopHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        this.httpClient = httpClient;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (RefreshType.REFRESH == command) {
            logger.debug("Refreshing {}...", channelUID);
            forceUpdate();
        } else {
            logger.debug("The OneBusAway Stop is a read-only and can not handle commands.");
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing OneBusAway stop bridge...");

        config = loadAndCheckConfiguration();
        if (config == null) {
            logger.debug("Initialization of OneBusAway bridge failed!");
            return;
        }

        // Do the rest of the work asynchronously because it can take a while.
        scheduler.submit(() -> {
            gson = new Gson();

            pollingJob = scheduler.scheduleWithFixedDelay(this::fetchAndUpdateStopData, 0, config.getInterval(),
                    TimeUnit.SECONDS);
        });
    }

    @Override
    public void dispose() {
        if (pollingJob != null && !pollingJob.isCancelled()) {
            pollingJob.cancel(true);
            pollingJob = null;
        }
    }

    /**
     * Registers the listener to receive updates about arrival and departure times for its route.
     *
     * @param listener
     * @return true if successful.
     */
    protected boolean registerRouteDataListener(RouteDataListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("It makes no sense to register a null listener!");
        }
        routeDataListeners.add(listener);
        String routeId = listener.getRouteId();
        List<ObaStopArrivalResponse.ArrivalAndDeparture> copiedRouteData;
        synchronized (routeData) {
            copiedRouteData = new ArrayList<>(routeData.getOrDefault(routeId, List.of()));
        }
        Collections.sort(copiedRouteData);
        listener.onNewRouteData(routeDataLastUpdateMs, copiedRouteData);

        return true;
    }

    /**
     * Unregisters the listener so it no longer receives updates about arrival and departure times for its route.
     *
     * @param listener
     * @return true if successful.
     */
    protected boolean unregisterRouteDataListener(RouteDataListener listener) {
        return routeDataListeners.remove(listener);
    }

    /**
     * Forced an update to be scheduled immediately.
     */
    protected void forceUpdate() {
        scheduler.execute(this::fetchAndUpdateStopData);
    }

    private ApiHandler getApiHandler() {
        return (ApiHandler) getBridge().getHandler();
    }

    private StopConfiguration loadAndCheckConfiguration() {
        StopConfiguration config = getConfigAs(StopConfiguration.class);
        if (config.getInterval() == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "interval is not set");
            return null;
        }
        if (config.getStopId() == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "stopId is not set");
            return null;
        }
        return config;
    }

    private boolean fetchAndUpdateStopData() {
        try {
            ApiHandler apiHandler = getApiHandler();
            if (apiHandler == null) {
                // We must be offline.
                return false;
            }
            boolean alreadyFetching = !fetchInProgress.compareAndSet(false, true);
            if (alreadyFetching) {
                return false;
            }
            logger.debug("Fetching data for stop ID {}", config.getStopId());
            String url = String.format("http://%s/api/where/arrivals-and-departures-for-stop/%s.json?key=%s",
                    apiHandler.getApiServer(), config.getStopId(), apiHandler.getApiKey());
            URI uri;
            try {
                uri = new URI(url);
            } catch (URISyntaxException e) {
                logger.error("Unable to parse {} as a URI.", url);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                        "stopId or apiKey is set to a bogus value");
                return false;
            }
            ContentResponse response;
            try {
                response = httpClient.newRequest(uri).send();
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
                return false;
            }
            if (response.getStatus() != HttpStatus.OK_200) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        String.format("While fetching stop data: %d: %s", response.getStatus(), response.getReason()));
                return false;
            }
            ObaStopArrivalResponse data = gson.fromJson(response.getContentAsString(), ObaStopArrivalResponse.class);
            routeDataLastUpdateMs = data.currentTime;
            updateStatus(ThingStatus.ONLINE);

            Map<String, List<ObaStopArrivalResponse.ArrivalAndDeparture>> copiedRouteData = new HashMap<>();
            synchronized (routeData) {
                routeData.clear();
                for (ObaStopArrivalResponse.ArrivalAndDeparture d : data.data.entry.arrivalsAndDepartures) {
                    routeData.put(d.routeId, Arrays.asList(d));
                }
                for (String key : routeData.keySet()) {
                    List<ObaStopArrivalResponse.ArrivalAndDeparture> copy = new ArrayList<>(
                            routeData.getOrDefault(key, List.of()));
                    Collections.sort(copy);
                    copiedRouteData.put(key, copy);
                }
            }
            for (RouteDataListener listener : routeDataListeners) {
                listener.onNewRouteData(routeDataLastUpdateMs, copiedRouteData.get(listener.getRouteId()));
            }
            return true;
        } catch (Exception e) {
            logger.debug("Exception refreshing route data", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            return false;
        } finally {
            fetchInProgress.set(false);
        }
    }
}
