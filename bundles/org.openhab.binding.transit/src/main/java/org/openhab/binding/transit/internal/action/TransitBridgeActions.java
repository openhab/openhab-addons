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
package org.openhab.binding.transit.internal.action;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.transit.internal.handler.TransitBridgeHandler;
import org.openhab.binding.transit.internal.net.TransitPlace;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonNullByDefault
@ThingActionsScope(name = "transit")
@Component(service = TransitBridgeActions.class, scope = ServiceScope.PROTOTYPE)
public class TransitBridgeActions implements ThingActions {

    private final Logger logger = LoggerFactory.getLogger(TransitBridgeActions.class);
    private @Nullable ThingHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof TransitBridgeHandler) {
            this.handler = handler;
        } else {
            this.handler = null;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return this.handler;
    }

    @RuleAction(label = "@text/action.getNearbyStops.label", description = "@text/action.getNearbyStops.description")
    public @Nullable String getNearbyStops(@ActionInput(name = "lat") double lat,
            @ActionInput(name = "lon") double lon) {
        ThingHandler currentHandler = this.handler;
        if (currentHandler instanceof TransitBridgeHandler bridgeHandler) {
            try {
                return bridgeHandler.fetchNearbyStops(lat, lon);
            } catch (IOException e) {
                logger.debug("Failed to fetch nearby stops: {}", e.getMessage());
                return null;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        logger.debug("Action getNearbyStops called, but handler is not ready or not a Bridge handler");
        return null;
    }

    @RuleAction(label = "@text/action.searchStops.label", description = "@text/action.searchStops.description")
    public @Nullable String searchStops(@ActionInput(name = "lat") double lat, @ActionInput(name = "lon") double lon,
            @ActionInput(name = "query") String query) {
        TransitBridgeHandler bridgeHandler = getBridgeHandler("searchStops");
        if (bridgeHandler == null) {
            return null;
        }
        try {
            return bridgeHandler.fetchSearchStops(lat, lon, query);
        } catch (IOException e) {
            logger.debug("Failed to search stops: {}", e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return null;
    }

    @RuleAction(label = "@text/action.getTripPlan.label", description = "@text/action.getTripPlan.description")
    public @Nullable String getTripPlan(@ActionInput(name = "fromLat") double fromLat,
            @ActionInput(name = "fromLon") double fromLon, @ActionInput(name = "toLat") double toLat,
            @ActionInput(name = "toLon") double toLon) {
        TransitBridgeHandler bridgeHandler = getBridgeHandler("getTripPlan");
        if (bridgeHandler == null) {
            return null;
        }
        try {
            return bridgeHandler.fetchPlan(TransitPlace.ofLocation(fromLat, fromLon),
                    TransitPlace.ofLocation(toLat, toLon));
        } catch (IOException e) {
            logger.debug("Failed to get trip plan: {}", e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return null;
    }

    @RuleAction(label = "@text/action.estimatePlanDuration.label", description = "@text/action.estimatePlanDuration.description")
    public @Nullable Long estimatePlanDuration(@ActionInput(name = "fromLat") double fromLat,
            @ActionInput(name = "fromLon") double fromLon, @ActionInput(name = "toLat") double toLat,
            @ActionInput(name = "toLon") double toLon) {
        TransitBridgeHandler bridgeHandler = getBridgeHandler("estimatePlanDuration");
        if (bridgeHandler == null) {
            return null;
        }
        try {
            Double duration = bridgeHandler.getEstimatePlanDuration(TransitPlace.ofLocation(fromLat, fromLon),
                    TransitPlace.ofLocation(toLat, toLon)).getDuration();
            return duration != null ? Math.round(duration) : null;
        } catch (IOException e) {
            logger.debug("Failed to estimate plan duration: {}", e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return null;
    }

    private @Nullable TransitBridgeHandler getBridgeHandler(String action) {
        if (this.handler instanceof TransitBridgeHandler bridgeHandler) {
            return bridgeHandler;
        }
        logger.debug("Action {} called, but handler is not ready or not a Bridge handler", action);
        return null;
    }
}
