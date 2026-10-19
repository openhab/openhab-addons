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
package org.openhab.binding.transit.internal.handler;

import static org.openhab.binding.transit.internal.TransitBindingConstants.*;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.transit.internal.config.TransitRouteConfiguration;
import org.openhab.binding.transit.internal.net.dto.RouteDetailsResult;
import org.openhab.binding.transit.internal.net.dto.RouteDetailsResult.Route;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonNullByDefault
public class TransitRouteDetailsHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(TransitRouteDetailsHandler.class);

    private TransitRouteConfiguration config = new TransitRouteConfiguration();
    private final AtomicBoolean pollInProgress = new AtomicBoolean();
    private @Nullable ScheduledFuture<?> refreshJob;

    public TransitRouteDetailsHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        config = getConfigAs(TransitRouteConfiguration.class);

        if (config.routeId.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-no-route-id");
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);

        Bridge bridge = getBridge();
        if (bridge != null && bridge.getStatus() == ThingStatus.ONLINE) {
            startPolling();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        synchronized (this) {
            if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
                logger.debug("Bridge is ONLINE, starting polling for {}", getThing().getUID());
                updateStatus(ThingStatus.UNKNOWN);
                startPolling();
            } else {
                logger.debug("Bridge is {}, stopping polling for {}", bridgeStatusInfo.getStatus(),
                        getThing().getUID());
                stopPolling();
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        }
    }

    private void startPolling() {
        synchronized (this) {
            ScheduledFuture<?> job = refreshJob;
            if (job == null || job.isCancelled()) {
                long refreshInterval = Math.max(30L, config.refreshInterval);
                refreshJob = scheduler.scheduleWithFixedDelay(this::pollTransitApi, 1, refreshInterval,
                        TimeUnit.SECONDS);
            }
        }
    }

    private void stopPolling() {
        synchronized (this) {
            ScheduledFuture<?> job = refreshJob;
            if (job != null && !job.isCancelled()) {
                job.cancel(true);
                refreshJob = null;
            }
        }
    }

    /**
     * Must be called while holding the handler lock. Returns {@code false} if polling was stopped or the bridge went
     * offline while a request was in flight, so that outdated results are not published.
     */
    private boolean isPollingActive() {
        Bridge bridge = getBridge();
        return refreshJob != null && bridge != null && bridge.getStatus() == ThingStatus.ONLINE;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            scheduler.execute(this::pollTransitApi);
        }
    }

    private void pollTransitApi() {
        // skip if the previous poll (scheduled or triggered by REFRESH) is still running
        if (!pollInProgress.compareAndSet(false, true)) {
            logger.debug("Previous poll still running, skipping this one for {}", getThing().getUID());
            return;
        }
        try {
            poll();
        } finally {
            pollInProgress.set(false);
        }
    }

    /**
     * Requests the data without holding the handler lock and publishes it while holding the handler lock, the same
     * lock that {@link #bridgeStatusChanged} and {@link #updateStatus} use, so no lock-order inversion is possible.
     */
    private void poll() {
        String routeId = config.routeId;

        Bridge bridge = getBridge();
        if (bridge == null || bridge.getStatus() != ThingStatus.ONLINE) {
            logger.debug("Bridge is not ONLINE. Skipping poll for {}", getThing().getUID());
            return;
        }

        try {
            TransitBridgeHandler bridgeHandler = getTransitBridgeHandler();
            if (bridgeHandler == null) {
                return;
            }
            RouteDetailsResult result = bridgeHandler.getRouteDetails(routeId);
            synchronized (this) {
                if (!isPollingActive()) {
                    logger.debug("Polling stopped while request was in flight, discarding result for {}",
                            getThing().getUID());
                    return;
                }
                logger.debug("Successfully polled route details for route ID {}", routeId);
                updateStatus(ThingStatus.ONLINE);

                Route route = result.getEffectiveRoute();
                if (route != null) {
                    String longName = route.routeLongName;
                    updateRouteState(CHANNEL_ROUTE_LONG_NAME,
                            longName != null ? new StringType(longName) : UnDefType.UNDEF);

                    String shortName = route.routeShortName;
                    updateRouteState(CHANNEL_ROUTE_SHORT_NAME,
                            shortName != null ? new StringType(shortName) : UnDefType.UNDEF);

                    String color = route.routeColor;
                    updateRouteState(CHANNEL_ROUTE_COLOR, color != null ? new StringType(color) : UnDefType.UNDEF);

                    // Update alerts count when route is known
                    var alerts = route.alerts;
                    updateRouteState(CHANNEL_ACTIVE_ALERTS_COUNT, new DecimalType(alerts != null ? alerts.size() : 0));
                } else {
                    updateRouteState(CHANNEL_ROUTE_LONG_NAME, UnDefType.UNDEF);
                    updateRouteState(CHANNEL_ROUTE_SHORT_NAME, UnDefType.UNDEF);
                    updateRouteState(CHANNEL_ROUTE_COLOR, UnDefType.UNDEF);
                    updateRouteState(CHANNEL_ACTIVE_ALERTS_COUNT, new DecimalType(0));
                }
            }
        } catch (InterruptedException e) {
            // Preserve interrupt status for proper task cancellation
            Thread.currentThread().interrupt();
            logger.debug("Route details polling task interrupted");
        } catch (IOException e) {
            synchronized (this) {
                if (!isPollingActive()) {
                    return;
                }
                String errorMessage = e.getMessage();
                logger.debug("Communication error while polling route {}: {}", routeId, errorMessage);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, errorMessage);
            }
        }
    }

    private void updateRouteState(String channel, State state) {
        updateState(new ChannelUID(getThing().getUID(), CHANNEL_GROUP_ROUTE, channel), state);
    }

    public @Nullable TransitBridgeHandler getTransitBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge != null && bridge.getHandler() instanceof TransitBridgeHandler bridgeHandler) {
            return bridgeHandler;
        }
        return null;
    }

    @Override
    public void dispose() {
        stopPolling();
        super.dispose();
    }
}
