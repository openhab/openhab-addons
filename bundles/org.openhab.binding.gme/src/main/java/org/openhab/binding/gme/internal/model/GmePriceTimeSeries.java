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
package org.openhab.binding.gme.internal.model;

import java.time.ZoneId;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.types.State;
import org.openhab.core.types.TimeSeries;

/**
 * Builds openHAB time series from GME price entries.
 *
 * @author Andrea Riela - Initial contribution
 */
@NonNullByDefault
public final class GmePriceTimeSeries {

    @FunctionalInterface
    public interface StateMapper {
        State apply(GmePriceEntry price);
    }

    private GmePriceTimeSeries() {
    }

    public static TimeSeries build(List<GmePriceEntry> prices, ZoneId zoneId, StateMapper stateMapper) {
        TimeSeries timeSeries = new TimeSeries(TimeSeries.Policy.REPLACE);

        for (GmePriceEntry price : prices) {
            timeSeries.add(GmePriceTimeline.getStartTime(price, zoneId).toInstant(), stateMapper.apply(price));
        }

        return timeSeries;
    }
}
