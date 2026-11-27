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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.transit.internal.config.TransitRouteConfiguration;
import org.openhab.binding.transit.internal.net.dto.RouteDetailsResult;
import org.openhab.binding.transit.internal.net.dto.RouteDetailsResult.Route;
import org.openhab.core.thing.Thing;

/**
 * The {@link TransitRouteDetailsHandler} shows the details and the service alerts of a route.
 *
 * @author Michael - Initial contribution
 */
@NonNullByDefault
public class TransitRouteDetailsHandler extends TransitPollingHandler<RouteDetailsResult> {

    private TransitRouteConfiguration config = new TransitRouteConfiguration();

    public TransitRouteDetailsHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected @Nullable String loadConfiguration() {
        config = getConfigAs(TransitRouteConfiguration.class);
        if (config.routeId.isBlank()) {
            return "@text/offline.conf-error-no-route-id";
        }
        return null;
    }

    @Override
    protected long getRefreshInterval() {
        return config.refreshInterval;
    }

    @Override
    protected RouteDetailsResult fetch(TransitBridgeHandler bridgeHandler) throws IOException, InterruptedException {
        return bridgeHandler.getRouteDetails(config.routeId);
    }

    @Override
    protected void publish(RouteDetailsResult result, long nowSeconds) {
        logger.debug("Successfully polled route details for route ID {}", config.routeId);
        Route route = result.route;
        updateGroupState(CHANNEL_GROUP_ROUTE, CHANNEL_ROUTE_LONG_NAME,
                toStringState(route != null ? route.routeLongName : null));
        updateGroupState(CHANNEL_GROUP_ROUTE, CHANNEL_ROUTE_SHORT_NAME,
                toStringState(route != null ? route.routeShortName : null));
        updateGroupState(CHANNEL_GROUP_ROUTE, CHANNEL_ROUTE_COLOR,
                toStringState(route != null ? route.routeColor : null));
        updateGroupState(CHANNEL_GROUP_ROUTE, CHANNEL_TRANSPORT_MODE,
                toStringState(route != null ? route.modeKey : null));
        publishAlerts(CHANNEL_GROUP_ROUTE, route != null ? route.alerts : null, nowSeconds);
    }
}
