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
package org.openhab.binding.solaredge.internal.handler;

import static org.openhab.binding.solaredge.internal.SolarEdgeBindingConstants.*;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.solaredge.internal.model.AggregatePeriod;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Correlates Monitoring API V2 measurements by polling cycle and derives site values.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
class PublicApiV2Data {
    private record TimedPower(double value, long cycleId) {
    }

    private static class AggregateBalance {
        private @Nullable TimedPower production;
        private @Nullable TimedPower imported;
        private @Nullable TimedPower exported;
        private @Nullable TimedPower charged;
        private @Nullable TimedPower discharged;
        private @Nullable TimedPower consumption;
    }

    private final ChannelProvider channels;
    private final Consumer<Map<Channel, State>> channelUpdater;
    private final IntSupplier criticalLevel;
    private long latestLiveCycle = -1;
    private final Map<AggregatePeriod, Long> latestAggregateCycles = new EnumMap<>(AggregatePeriod.class);
    private @Nullable TimedPower production;
    private @Nullable TimedPower imported;
    private @Nullable TimedPower exported;
    private @Nullable TimedPower charged;
    private @Nullable TimedPower discharged;
    private @Nullable TimedPower consumption;
    private final Map<AggregatePeriod, AggregateBalance> aggregateBalances = new EnumMap<>(AggregatePeriod.class);

    PublicApiV2Data(ChannelProvider channels, Consumer<Map<Channel, State>> channelUpdater, IntSupplier criticalLevel) {
        this.channels = channels;
        this.channelUpdater = channelUpdater;
        this.criticalLevel = criticalLevel;
    }

    synchronized void updateProduction(long cycleId, Map<Channel, State> values, @Nullable Double value) {
        if (!acceptLive(cycleId)) {
            return;
        }
        channelUpdater.accept(values);
        production = timed(value, cycleId);
        updateDeviceStatus(CHANNEL_ID_PV_STATUS, value);
        updateLiveValues(cycleId);
    }

    synchronized void updateGrid(long cycleId, Map<Channel, State> values, @Nullable Double importedPower,
            @Nullable Double exportedPower, @Nullable Double directConsumption) {
        if (!acceptLive(cycleId)) {
            return;
        }
        channelUpdater.accept(values);
        imported = timed(importedPower, cycleId);
        exported = timed(exportedPower, cycleId);
        consumption = timed(directConsumption, cycleId);
        updateDeviceStatus(CHANNEL_ID_GRID_STATUS, importedPower, exportedPower);
        updateLiveValues(cycleId);
    }

    synchronized void updateStorage(long cycleId, Map<Channel, State> values, @Nullable Double charge,
            @Nullable Double discharge, @Nullable Double level) {
        if (!acceptLive(cycleId)) {
            return;
        }
        channelUpdater.accept(values);
        charged = timed(charge, cycleId);
        discharged = timed(discharge, cycleId);
        Map<Channel, State> batteryValues = new HashMap<>();
        putState(batteryValues, CHANNEL_GROUP_LIVE, CHANNEL_ID_BATTERY_CRITICAL,
                batteryCriticalState(level, criticalLevel.getAsInt()));
        channelUpdater.accept(batteryValues);
        updateDeviceStatus(CHANNEL_ID_BATTERY_STATUS, charge, discharge);
        updateLiveValues(cycleId);
    }

    synchronized void updateAggregateProduction(long cycleId, AggregatePeriod period, Map<Channel, State> values,
            @Nullable Double value) {
        if (!acceptAggregate(cycleId, period)) {
            return;
        }
        channelUpdater.accept(values);
        AggregateBalance balance = aggregateBalance(period);
        balance.production = timed(value, cycleId);
        updateAggregateConsumption(cycleId, period, balance);
    }

    synchronized void updateAggregateGrid(long cycleId, AggregatePeriod period, Map<Channel, State> values,
            @Nullable Double importedEnergy, @Nullable Double exportedEnergy, @Nullable Double directConsumption) {
        if (!acceptAggregate(cycleId, period)) {
            return;
        }
        channelUpdater.accept(values);
        AggregateBalance balance = aggregateBalance(period);
        balance.imported = timed(importedEnergy, cycleId);
        balance.exported = timed(exportedEnergy, cycleId);
        balance.consumption = timed(directConsumption, cycleId);
        updateAggregateConsumption(cycleId, period, balance);
    }

    synchronized void updateAggregateStorage(long cycleId, AggregatePeriod period, Map<Channel, State> values,
            @Nullable Double charge, @Nullable Double discharge) {
        if (!acceptAggregate(cycleId, period)) {
            return;
        }
        channelUpdater.accept(values);
        AggregateBalance balance = aggregateBalance(period);
        balance.charged = timed(charge, cycleId);
        balance.discharged = timed(discharge, cycleId);
        updateAggregateConsumption(cycleId, period, balance);
    }

    private boolean acceptLive(long cycleId) {
        if (cycleId < latestLiveCycle) {
            return false;
        }
        latestLiveCycle = cycleId;
        return true;
    }

    private boolean acceptAggregate(long cycleId, AggregatePeriod period) {
        long latest = latestAggregateCycles.getOrDefault(period, -1L);
        if (cycleId < latest) {
            return false;
        }
        latestAggregateCycles.put(period, cycleId);
        return true;
    }

    private AggregateBalance aggregateBalance(AggregatePeriod period) {
        AggregateBalance balance = aggregateBalances.get(period);
        if (balance == null) {
            balance = new AggregateBalance();
            aggregateBalances.put(period, balance);
        }
        return balance;
    }

    private void updateAggregateConsumption(long cycleId, AggregatePeriod period, AggregateBalance balance) {
        TimedPower currentProduction = balance.production;
        TimedPower directConsumption = balance.consumption;
        Map<Channel, State> values = new HashMap<>();
        @Nullable
        Double derivedConsumption = null;
        if (directConsumption != null && directConsumption.cycleId == cycleId) {
            derivedConsumption = directConsumption.value;
        } else if (currentProduction != null && currentProduction.cycleId == cycleId && sameCycle(currentProduction,
                balance.imported, balance.exported, balance.charged, balance.discharged)) {
            derivedConsumption = calculateConsumption(valueOf(currentProduction), valueOf(balance.imported),
                    valueOf(balance.exported), valueOf(balance.charged), valueOf(balance.discharged));
        }
        if (derivedConsumption != null) {
            putState(values, aggregateGroup(period), CHANNEL_ID_CONSUMPTION,
                    new QuantityType<>(derivedConsumption, Units.WATT_HOUR));
        }
        if (currentProduction != null && currentProduction.cycleId == cycleId
                && sameCycle(currentProduction, balance.exported, balance.charged)) {
            double selfConsumption = calculateSelfConsumption(valueOf(currentProduction), valueOf(balance.exported),
                    valueOf(balance.charged));
            putState(values, aggregateGroup(period), CHANNEL_ID_SELF_CONSUMPTION_FOR_CONSUMPTION,
                    new QuantityType<>(selfConsumption, Units.WATT_HOUR));
            if (derivedConsumption != null) {
                putState(values, aggregateGroup(period), CHANNEL_ID_SELF_CONSUMPTION_COVERAGE,
                        new QuantityType<>(calculateCoverage(selfConsumption, derivedConsumption), Units.PERCENT));
            }
        }
        if (!values.isEmpty()) {
            channelUpdater.accept(values);
        }
    }

    private void updateLiveValues(long cycleId) {
        TimedPower directConsumption = consumption;
        if (directConsumption != null && directConsumption.cycleId == cycleId) {
            updateLiveConsumption(directConsumption.value);
            return;
        }
        TimedPower currentProduction = production;
        TimedPower currentImported = imported;
        TimedPower currentExported = exported;
        TimedPower currentCharged = charged;
        TimedPower currentDischarged = discharged;
        if (currentProduction != null && currentProduction.cycleId == cycleId
                && sameCycle(currentProduction, currentImported, currentExported, currentCharged, currentDischarged)) {
            updateLiveConsumption(calculateConsumption(valueOf(currentProduction), valueOf(currentImported),
                    valueOf(currentExported), valueOf(currentCharged), valueOf(currentDischarged)));
        }
    }

    private void updateLiveConsumption(double value) {
        Map<Channel, State> values = new HashMap<>();
        putState(values, CHANNEL_GROUP_LIVE, CHANNEL_ID_CONSUMPTION, new QuantityType<>(value, Units.WATT));
        putState(values, CHANNEL_GROUP_LIVE, CHANNEL_ID_LOAD_STATUS, new StringType(activeStatus(value)));
        channelUpdater.accept(values);
    }

    private void updateDeviceStatus(String channelId, @Nullable Double... powers) {
        boolean active = false;
        for (Double power : powers) {
            if (power == null) {
                return;
            }
            active |= power > 0;
        }
        Map<Channel, State> values = new HashMap<>();
        putState(values, CHANNEL_GROUP_LIVE, channelId, new StringType(active ? "Active" : "Idle"));
        channelUpdater.accept(values);
    }

    private void putState(Map<Channel, State> values, String group, String channelId, State state) {
        Channel channel = channels.getChannel(group, channelId);
        if (channel != null) {
            values.put(channel, state);
        }
    }

    private static @Nullable TimedPower timed(@Nullable Double value, long cycleId) {
        return value == null ? null : new TimedPower(value, cycleId);
    }

    private static boolean sameCycle(TimedPower reference, @Nullable TimedPower... values) {
        for (TimedPower value : values) {
            if (value == null || value.cycleId != reference.cycleId) {
                return false;
            }
        }
        return true;
    }

    private static double valueOf(@Nullable TimedPower power) {
        return Objects.requireNonNull(power).value;
    }

    static double calculateConsumption(double production, double imported, double exported, double charged,
            double discharged) {
        return Math.max(0, production + imported + discharged - exported - charged);
    }

    static double calculateSelfConsumption(double production, double exported, double charged) {
        return Math.max(0, production - exported - charged);
    }

    static double calculateCoverage(double selfConsumption, double consumption) {
        return consumption > 0 ? selfConsumption / consumption * 100 : 0;
    }

    static String activeStatus(double... powers) {
        for (double power : powers) {
            if (power > 0) {
                return "Active";
            }
        }
        return "Idle";
    }

    static State batteryCriticalState(@Nullable Double level, int threshold) {
        return level == null ? UnDefType.UNDEF : new StringType(Boolean.toString(level < threshold));
    }

    private static String aggregateGroup(AggregatePeriod period) {
        return switch (period) {
            case DAY -> CHANNEL_GROUP_AGGREGATE_DAY;
            case WEEK -> CHANNEL_GROUP_AGGREGATE_WEEK;
            case MONTH -> CHANNEL_GROUP_AGGREGATE_MONTH;
            case YEAR -> CHANNEL_GROUP_AGGREGATE_YEAR;
        };
    }
}
