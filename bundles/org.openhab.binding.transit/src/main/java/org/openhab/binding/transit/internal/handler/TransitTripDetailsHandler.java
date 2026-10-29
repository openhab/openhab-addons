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
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.transit.internal.config.TransitTripConfiguration;
import org.openhab.binding.transit.internal.net.dto.TripDetailsResult;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.UnDefType;

/**
 * The {@link TransitTripDetailsHandler} shows the upcoming stops of a trip.
 *
 * @author Michael - Initial contribution
 */
@NonNullByDefault
public class TransitTripDetailsHandler extends TransitPollingHandler<TripDetailsResult> {

    private TransitTripConfiguration config = new TransitTripConfiguration();

    public TransitTripDetailsHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected @Nullable String loadConfiguration() {
        config = getConfigAs(TransitTripConfiguration.class);
        if (config.tripSearchKey.isBlank()) {
            return "@text/offline.conf-error-no-trip-search-key";
        }
        return null;
    }

    @Override
    protected long getRefreshInterval() {
        return config.refreshInterval;
    }

    @Override
    protected TripDetailsResult fetch(TransitBridgeHandler bridgeHandler) throws IOException, InterruptedException {
        return bridgeHandler.getTripDetails(config.tripSearchKey);
    }

    @Override
    protected void publish(TripDetailsResult result, long now) {
        logger.debug("Successfully polled trip details for trip search key {}", config.tripSearchKey);

        var route = result.route;
        updateGroupState(CHANNEL_GROUP_TRIP, CHANNEL_ROUTE_SHORT_NAME,
                toStringState(route != null ? route.routeShortName : null));

        var targetStopId = config.targetStopId;
        updateGroupState(CHANNEL_GROUP_TRIP, CHANNEL_TIME_TO_TARGET, UnDefType.UNDEF);

        int stopIdx = 1;
        List<TripDetailsResult.ScheduleItem> scheduleItems = result.scheduleItems;
        if (scheduleItems != null) {
            for (var schedule : scheduleItems) {
                var stop = schedule.stop;
                if (stop == null) {
                    continue;
                }

                var gStopId = stop.globalStopId;
                var depTime = schedule.departureTime;

                if (targetStopId != null && !targetStopId.isBlank() && targetStopId.equals(gStopId)
                        && depTime != null) {
                    // Only show countdown for future departures
                    if (depTime >= now) {
                        long diff = (depTime - now) / 60;
                        updateGroupState(CHANNEL_GROUP_TRIP, CHANNEL_TIME_TO_TARGET,
                                new QuantityType<>(diff, Units.MINUTE));
                    }
                }

                if (stopIdx <= MAX_CHANNEL_GROUPS && depTime != null && depTime > now) {
                    String group = CHANNEL_GROUP_STOP_PREFIX + stopIdx;
                    updateGroupState(group, CHANNEL_STOP_NAME, toStringState(stop.stopName));
                    long diff = (depTime - now) / 60;
                    updateGroupState(group, CHANNEL_MINUTES_UNTIL_DEPARTURE, new QuantityType<>(diff, Units.MINUTE));
                    stopIdx++;
                }
            }
        }

        for (int i = stopIdx; i <= MAX_CHANNEL_GROUPS; i++) {
            String group = CHANNEL_GROUP_STOP_PREFIX + i;
            updateGroupState(group, CHANNEL_STOP_NAME, UnDefType.UNDEF);
            updateGroupState(group, CHANNEL_MINUTES_UNTIL_DEPARTURE, UnDefType.UNDEF);
        }
    }
}
