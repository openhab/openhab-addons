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
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
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
        ScheduledFuture<?> job = refreshJob;
        if (job != null) {
            job.cancel(true);
        }
        TransitAppRouteConfiguration config = getConfigAs(TransitAppRouteConfiguration.class);
        long refreshInterval = Math.max(30L, config.refreshInterval);
        refreshJob = scheduler.scheduleWithFixedDelay(this::pollTransitApi, 1, refreshInterval, TimeUnit.SECONDS);
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            pollTransitApi();
        }
    }

    private void pollTransitApi() {
        TransitAppRouteConfiguration config = getConfigAs(TransitAppRouteConfiguration.class);
        String routeId = config.routeId;
        if (routeId.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Route ID is missing");
            return;
        }

        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Bridge not found");
            return;
        }

        try {
            TransitAppBridgeHandler bridgeHandler = getTransitBridgeHandler();
            if (bridgeHandler == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Bridge handler not initialized");
                return;
            }
            RouteDetailsResult result = bridgeHandler.getRouteDetails(routeId);
            logger.debug("Successfully polled route details for route ID {}", routeId);
            updateStatus(ThingStatus.ONLINE);

            RouteDetailsResult.Route route = result.getEffectiveRoute();
            if (route != null) {
                String longName = route.routeLongName;
                if (longName != null) {
                    updateState("route#route-long-name", new StringType(longName));
                } else {
                    updateState("route#route-long-name", org.openhab.core.types.UnDefType.UNDEF);
                }
                String shortName = route.routeShortName;
                if (shortName != null) {
                    updateState("route#route-short-name", new StringType(shortName));
                } else {
                    updateState("route#route-short-name", org.openhab.core.types.UnDefType.UNDEF);
                }
                String color = route.routeColor;
                if (color != null) {
                    updateState("route#route-color", new StringType(color));
                } else {
                    updateState("route#route-color", org.openhab.core.types.UnDefType.UNDEF);
                }
            } else {
                updateState("route#route-long-name", org.openhab.core.types.UnDefType.UNDEF);
                updateState("route#route-short-name", org.openhab.core.types.UnDefType.UNDEF);
                updateState("route#route-color", org.openhab.core.types.UnDefType.UNDEF);
            }

            int alertsCount = result.alerts != null ? result.alerts.size() : 0;
            updateState("route#active-alerts-count", new DecimalType(alertsCount));
        } catch (Exception e) {
            logger.warn("Communication error while polling route {}: {}", routeId, e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
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
        ScheduledFuture<?> job = refreshJob;
        if (job != null) {
            job.cancel(true);
        }
        super.dispose();
    }
}
