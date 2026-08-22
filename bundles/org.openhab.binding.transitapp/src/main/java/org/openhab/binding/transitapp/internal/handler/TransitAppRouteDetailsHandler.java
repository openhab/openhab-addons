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
package org.openhab.binding.transitapp.internal.handler;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.transitapp.internal.config.TransitAppRouteConfiguration;
import org.openhab.binding.transitapp.internal.net.dto.RouteDetailsResult;
import org.openhab.binding.transitapp.internal.net.dto.RouteDetailsResult.Route;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonNullByDefault
public class TransitAppRouteDetailsHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(TransitAppRouteDetailsHandler.class);

    private @Nullable ScheduledFuture<?> refreshJob;

    public TransitAppRouteDetailsHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        TransitAppRouteConfiguration config = getConfigAs(TransitAppRouteConfiguration.class);
        if (config.routeId == null || config.routeId.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Route ID is missing");
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
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            logger.debug("Bridge is ONLINE, starting polling for {}", getThing().getUID());
            updateStatus(ThingStatus.UNKNOWN);
            startPolling();
        } else {
            logger.debug("Bridge is {}, stopping polling for {}", bridgeStatusInfo.getStatus(), getThing().getUID());
            stopPolling();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    private void startPolling() {
        if (refreshJob == null || refreshJob.isCancelled()) {
            TransitAppRouteConfiguration config = getConfigAs(TransitAppRouteConfiguration.class);
            long refreshInterval = Math.max(30L, config.refreshInterval);
            refreshJob = scheduler.scheduleWithFixedDelay(this::pollTransitApi, 1, refreshInterval, TimeUnit.SECONDS);
        }
    }

    private void stopPolling() {
        ScheduledFuture<?> job = refreshJob;
        if (job != null && !job.isCancelled()) {
            job.cancel(true);
            refreshJob = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            scheduler.execute(this::pollTransitApi);
        }
    }

    private synchronized void pollTransitApi() {
        TransitAppRouteConfiguration config = getConfigAs(TransitAppRouteConfiguration.class);
        String routeId = config.routeId;
        if (routeId.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Route ID is missing");
            return;
        }

        Bridge bridge = getBridge();
        if (bridge == null || bridge.getStatus() != ThingStatus.ONLINE) {
            logger.debug("Bridge is not ONLINE. Skipping poll for {}", getThing().getUID());
            return;
        }

        try {
            TransitAppBridgeHandler bridgeHandler = getTransitBridgeHandler();
            if (bridgeHandler == null) {
                return;
            }
            RouteDetailsResult result = bridgeHandler.getRouteDetails(routeId);
            logger.debug("Successfully polled route details for route ID {}", routeId);
            updateStatus(ThingStatus.ONLINE);

            @Nullable
            Route route = result.getEffectiveRoute();
            if (route != null) {
                @Nullable
                String longName = route.routeLongName;
                if (longName != null) {
                    updateState("route#route-long-name", new StringType(longName));
                } else {
                    updateState("route#route-long-name", org.openhab.core.types.UnDefType.UNDEF);
                }
                @Nullable
                String shortName = route.routeShortName;
                if (shortName != null) {
                    updateState("route#route-short-name", new StringType(shortName));
                } else {
                    updateState("route#route-short-name", org.openhab.core.types.UnDefType.UNDEF);
                }
                @Nullable
                String color = route.routeColor;
                if (color != null) {
                    updateState("route#route-color", new StringType(color));
                } else {
                    updateState("route#route-color", org.openhab.core.types.UnDefType.UNDEF);
                }

                // Update alerts count when route is known
                int alertsCount = 0;
                if (route.alerts != null) {
                    alertsCount = route.alerts.size();
                }
                updateState("route#active-alerts-count", new DecimalType(alertsCount));
            } else {
                updateState("route#route-long-name", org.openhab.core.types.UnDefType.UNDEF);
                updateState("route#route-short-name", org.openhab.core.types.UnDefType.UNDEF);
                updateState("route#route-color", org.openhab.core.types.UnDefType.UNDEF);
                updateState("route#active-alerts-count", new DecimalType(0));
            }
        } catch (InterruptedException e) {
            // Preserve interrupt status for proper task cancellation
            Thread.currentThread().interrupt();
            logger.debug("Route details polling task interrupted");
        } catch (Exception e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : e.toString();
            logger.warn("Communication error while polling route {}: {}", routeId, errorMessage);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, errorMessage);
        }
    }

    public @Nullable TransitAppBridgeHandler getTransitBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge != null && bridge.getHandler() instanceof TransitAppBridgeHandler bridgeHandler) {
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
