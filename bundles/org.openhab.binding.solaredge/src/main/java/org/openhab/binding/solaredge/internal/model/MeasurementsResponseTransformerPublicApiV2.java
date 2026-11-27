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
package org.openhab.binding.solaredge.internal.model;

import static org.openhab.binding.solaredge.internal.SolarEdgeBindingConstants.*;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.solaredge.internal.handler.ChannelProvider;
import org.openhab.binding.solaredge.internal.model.MeasurementsResponsePublicApiV2.Measurement;
import org.openhab.core.thing.Channel;
import org.openhab.core.types.State;

/**
 * Transforms SolarEdge Monitoring API V2 measurement envelopes into openHAB states.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class MeasurementsResponseTransformerPublicApiV2 extends AbstractDataResponseTransformer {

    private final ChannelProvider channelProvider;

    public MeasurementsResponseTransformerPublicApiV2(ChannelProvider channelProvider) {
        this.channelProvider = channelProvider;
    }

    public Map<Channel, State> transformPower(MeasurementsResponsePublicApiV2 response) {
        Map<Channel, State> result = new HashMap<>();
        Double latestValue = null;
        List<Measurement> values = response.values;
        if (values != null) {
            for (Measurement measurement : values) {
                if (measurement.value != null) {
                    latestValue = measurement.value;
                }
            }
        }
        putPowerType(result, channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_PRODUCTION), latestValue,
                response.unit);
        return result;
    }

    public @Nullable Double latestValue(MeasurementsResponsePublicApiV2 response) {
        Double latestValue = null;
        List<Measurement> values = response.values;
        if (values != null) {
            for (Measurement measurement : values) {
                if (measurement.value != null) {
                    latestValue = measurement.value;
                }
            }
        }
        return latestValue;
    }

    public Map<Channel, State> transformEnergy(MeasurementsResponsePublicApiV2 response, AggregatePeriod period) {
        return transformEnergy(response, period, null);
    }

    public Map<Channel, State> transformEnergy(MeasurementsResponsePublicApiV2 response, AggregatePeriod period,
            @Nullable OffsetDateTime from) {
        Map<Channel, State> result = new HashMap<>();
        Double total = totalValue(response, from);
        putEnergyType(result, channelProvider.getChannel(convertPeriodToGroup(period), CHANNEL_ID_PRODUCTION), total,
                response.unit);
        return result;
    }

    public @Nullable Double totalValue(MeasurementsResponsePublicApiV2 response) {
        return totalValue(response, null);
    }

    public @Nullable Double totalValue(MeasurementsResponsePublicApiV2 response, @Nullable OffsetDateTime from) {
        Double total = null;
        List<Measurement> values = response.values;
        if (values != null) {
            double sum = 0;
            boolean hasValue = false;
            for (Measurement measurement : values) {
                Double value = measurement.value;
                if (value != null && isAtOrAfter(measurement.timestamp, from)) {
                    sum += value;
                    hasValue = true;
                }
            }
            total = hasValue ? sum : null;
        }
        return total;
    }

    private static boolean isAtOrAfter(@Nullable String timestamp, @Nullable OffsetDateTime from) {
        if (from == null) {
            return true;
        }
        if (timestamp == null) {
            return false;
        }
        try {
            return !OffsetDateTime.parse(timestamp).isBefore(from);
        } catch (java.time.format.DateTimeParseException e) {
            return false;
        }
    }
}
