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
import org.openhab.binding.solaredge.internal.model.DeviceTelemetryResponsePublicApiV2.Measurement;
import org.openhab.binding.solaredge.internal.model.DeviceTelemetryResponsePublicApiV2.MeterTelemetry;
import org.openhab.binding.solaredge.internal.model.DeviceTelemetryResponsePublicApiV2.Series;
import org.openhab.binding.solaredge.internal.model.DeviceTelemetryResponsePublicApiV2.StorageTelemetry;
import org.openhab.core.thing.Channel;
import org.openhab.core.types.State;

/**
 * Transforms Monitoring API V2 site meter and storage telemetry.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class DeviceTelemetryResponseTransformerPublicApiV2 extends AbstractDataResponseTransformer {
    public record LivePowers(@Nullable Double imported, @Nullable Double exported, @Nullable Double consumption,
            @Nullable Double charged, @Nullable Double discharged, @Nullable Double level) {
    }

    public record AggregateEnergies(@Nullable Double imported, @Nullable Double exported, @Nullable Double consumption,
            @Nullable Double charged, @Nullable Double discharged) {
    }

    private final ChannelProvider channelProvider;

    public DeviceTelemetryResponseTransformerPublicApiV2(ChannelProvider channelProvider) {
        this.channelProvider = channelProvider;
    }

    public Map<Channel, State> transformLive(DeviceTelemetryResponsePublicApiV2 response) {
        Map<Channel, State> result = new HashMap<>();
        Map<String, MeterTelemetry> meters = response.meters;
        if (meters != null) {
            Double consumption = sumLatest(meters.values().stream().map(m -> m.consumptionPower).toList());
            if (consumption != null) {
                putPowerType(result, channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_CONSUMPTION),
                        consumption, unitOf(meters, false));
            }
            putPowerType(result, channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_IMPORT),
                    sumLatest(meters.values().stream().map(m -> m.importPower).toList()), unitOf(meters, true));
            putPowerType(result, channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_EXPORT),
                    sumLatest(meters.values().stream().map(m -> m.exportPower).toList()), unitOfExport(meters));
        }
        Map<String, StorageTelemetry> storage = response.storage;
        if (storage != null) {
            Double charge = sumLatest(storage.values().stream().map(s -> s.chargePower).toList());
            Double discharge = sumLatest(storage.values().stream().map(s -> s.dischargePower).toList());
            String unit = unitOf(storage.values().stream().map(s -> s.chargePower).toList(), "W");
            putPowerType(result, channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_BATTERY_CHARGE), charge,
                    unit);
            putPowerType(result, channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_BATTERY_DISCHARGE),
                    discharge, unit);
            putPowerType(result, channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_BATTERY_CHARGE_DISCHARGE),
                    charge == null || discharge == null ? null : charge - discharge, unit);
            Double level = averageLatest(storage.values().stream().map(s -> s.stateOfEnergy).toList());
            // The API currently returns fractions although its documentation describes values from 0 to 100.
            if (level != null && level >= 0 && level <= 1) {
                level *= 100;
            }
            putPercentType(result, channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_BATTERY_LEVEL), level);
        }
        return result;
    }

    public LivePowers extractLivePowers(DeviceTelemetryResponsePublicApiV2 response) {
        Map<String, MeterTelemetry> meters = response.meters;
        Map<String, StorageTelemetry> storage = response.storage;
        Double level = storage == null ? null
                : averageLatest(storage.values().stream().map(s -> s.stateOfEnergy).toList());
        if (level != null && level >= 0 && level <= 1) {
            level *= 100;
        }
        return new LivePowers(
                meters == null ? null : sumLatest(meters.values().stream().map(m -> m.importPower).toList()),
                meters == null ? null : sumLatest(meters.values().stream().map(m -> m.exportPower).toList()),
                meters == null ? null : sumLatest(meters.values().stream().map(m -> m.consumptionPower).toList()),
                storage == null ? null : sumLatest(storage.values().stream().map(s -> s.chargePower).toList()),
                storage == null ? null : sumLatest(storage.values().stream().map(s -> s.dischargePower).toList()),
                level);
    }

    public AggregateEnergies extractAggregateEnergies(DeviceTelemetryResponsePublicApiV2 response) {
        return extractAggregateEnergies(response, null);
    }

    public AggregateEnergies extractAggregateEnergies(DeviceTelemetryResponsePublicApiV2 response,
            @Nullable OffsetDateTime from) {
        Map<String, MeterTelemetry> meters = response.meters;
        Map<String, StorageTelemetry> storage = response.storage;
        return new AggregateEnergies(
                meters == null ? null : sumAll(meters.values().stream().map(m -> m.importEnergy).toList(), from),
                meters == null ? null : sumAll(meters.values().stream().map(m -> m.exportEnergy).toList(), from),
                meters == null ? null : sumAll(meters.values().stream().map(m -> m.consumptionEnergy).toList(), from),
                storage == null ? null : sumPositive(storage.values().stream().map(s -> s.chargeEnergy).toList(), from),
                storage == null ? null
                        : sumPositive(storage.values().stream().map(s -> s.dischargeEnergy).toList(), from));
    }

    public Map<Channel, State> transformAggregate(DeviceTelemetryResponsePublicApiV2 response, AggregatePeriod period) {
        return transformAggregate(response, period, null);
    }

    public Map<Channel, State> transformAggregate(DeviceTelemetryResponsePublicApiV2 response, AggregatePeriod period,
            @Nullable OffsetDateTime from) {
        Map<Channel, State> result = new HashMap<>();
        String group = convertPeriodToGroup(period);
        Map<String, MeterTelemetry> meters = response.meters;
        if (meters != null) {
            Double consumption = sumAll(meters.values().stream().map(m -> m.consumptionEnergy).toList(), from);
            if (consumption != null) {
                putEnergyType(result, channelProvider.getChannel(group, CHANNEL_ID_CONSUMPTION), consumption,
                        energyUnit(meters, 0));
            }
            putEnergyType(result, channelProvider.getChannel(group, CHANNEL_ID_IMPORT),
                    sumAll(meters.values().stream().map(m -> m.importEnergy).toList(), from), energyUnit(meters, 1));
            putEnergyType(result, channelProvider.getChannel(group, CHANNEL_ID_EXPORT),
                    sumAll(meters.values().stream().map(m -> m.exportEnergy).toList(), from), energyUnit(meters, 2));
        }
        Map<String, StorageTelemetry> storage = response.storage;
        if (storage != null) {
            var series = storage.values().stream().map(s -> s.dischargeEnergy).toList();
            putEnergyType(result, channelProvider.getChannel(group, CHANNEL_ID_BATTERY_SELF_CONSUMPTION),
                    sumPositive(series, from), unitOf(series, "WH"));
        }
        return result;
    }

    private static @Nullable Double sumLatest(Iterable<@Nullable Series> series) {
        double sum = 0;
        boolean found = false;
        for (@Nullable
        Series item : series) {
            Double value = latest(item);
            if (value != null) {
                sum += value;
                found = true;
            }
        }
        return found ? sum : null;
    }

    private static @Nullable Double averageLatest(Iterable<@Nullable Series> series) {
        double sum = 0;
        int count = 0;
        for (@Nullable
        Series item : series) {
            Double value = latest(item);
            if (value != null) {
                sum += value;
                count++;
            }
        }
        return count == 0 ? null : sum / count;
    }

    private static @Nullable Double latest(@Nullable Series series) {
        Double result = null;
        List<Measurement> values = series == null ? null : series.values;
        if (values != null) {
            for (Measurement measurement : values) {
                Double value = measurement.value;
                if (value != null) {
                    result = value;
                }
            }
        }
        return result;
    }

    private static @Nullable Double sumAll(Iterable<@Nullable Series> series, @Nullable OffsetDateTime from) {
        double sum = 0;
        boolean found = false;
        for (@Nullable
        Series item : series) {
            List<Measurement> values = item == null ? null : item.values;
            if (values != null) {
                for (Measurement measurement : values) {
                    Double value = measurement.value;
                    if (value != null && isAtOrAfter(measurement.timestamp, from)) {
                        sum += value.doubleValue();
                        found = true;
                    }
                }
            }
        }
        return found ? sum : null;
    }

    private static @Nullable Double sumPositive(Iterable<@Nullable Series> series, @Nullable OffsetDateTime from) {
        double sum = 0;
        boolean found = false;
        for (@Nullable
        Series item : series) {
            List<Measurement> values = item == null ? null : item.values;
            if (values != null) {
                for (Measurement measurement : values) {
                    Double value = measurement.value;
                    if (value != null && value >= 0 && isAtOrAfter(measurement.timestamp, from)) {
                        sum += value.doubleValue();
                        found = true;
                    }
                }
            }
        }
        return found ? sum : null;
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

    private static String unitOf(Map<String, MeterTelemetry> meters, boolean imported) {
        return unitOf(meters.values().stream().map(m -> imported ? m.importPower : m.consumptionPower).toList(), "W");
    }

    private static String unitOfExport(Map<String, MeterTelemetry> meters) {
        return unitOf(meters.values().stream().map(m -> m.exportPower).toList(), "W");
    }

    private static String energyUnit(Map<String, MeterTelemetry> meters, int type) {
        return unitOf(meters.values().stream()
                .map(m -> type == 0 ? m.consumptionEnergy : type == 1 ? m.importEnergy : m.exportEnergy).toList(),
                "WH");
    }

    private static String unitOf(Iterable<@Nullable Series> series, String fallback) {
        for (@Nullable
        Series item : series) {
            if (item != null) {
                String unit = item.unit;
                if (unit != null) {
                    return unit;
                }
            }
        }
        return fallback;
    }
}
