/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.senseenergy.internal.actions;

import static org.openhab.binding.senseenergy.internal.SenseEnergyBindingConstants.*;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Energy;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.senseenergy.internal.api.SenseEnergyApi;
import org.openhab.binding.senseenergy.internal.api.SenseEnergyApiException;
import org.openhab.binding.senseenergy.internal.api.dto.SenseEnergyApiGetTrends;
import org.openhab.binding.senseenergy.internal.handler.SenseEnergyMonitorHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The { @link SenseEnergyMonitorActions } class implements the action(s) methods for the binding.
 *
 * @author Jeff James - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = SenseEnergyMonitorActions.class)
@ThingActionsScope(name = "senseenergy")
@NonNullByDefault
public class SenseEnergyMonitorActions implements ThingActions {
    private final Logger logger = LoggerFactory.getLogger(SenseEnergyMonitorActions.class);

    private @Nullable SenseEnergyMonitorHandler deviceHandler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof SenseEnergyMonitorHandler deviceHandler) {
            this.deviceHandler = deviceHandler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return deviceHandler;
    }

    /*
     * Query water usage
     */
    @RuleAction(label = "Query Energy Trend", description = "@text/actions.description.query-energy-trend")
    public @ActionOutput(name = ACTION_OUTPUT_CONSUMPTION, type = "QuantityType<Energy>", description = "@text/actions.output.description.consumption") //
    @ActionOutput(name = ACTION_OUTPUT_PRODUCTION, type = "QuantityType<Energy>", description = "@text/actions.output.description.production") //
    @ActionOutput(name = ACTION_OUTPUT_FROM_GRID, type = "QuantityType<Energy>", description = "@text/actions.output.description.from-grid") //
    @ActionOutput(name = ACTION_OUTPUT_TO_GRID, type = "QuantityType<Energy>", description = "@text/actions.output.description.to-grid") //
    @ActionOutput(name = ACTION_OUTPUT_NET_PRODUCTION, type = "QuantityType<Energy>", description = "@text/actions.output.description.net-production") //
    @ActionOutput(name = ACTION_OUTPUT_SOLAR_POWERED, type = "QuantityType<Dimensionless>", description = "@text/actions.output.description.solar-powered") //
    Map<String, QuantityType<?>> queryEnergyTrend( //
            @ActionInput(name = ACTION_INPUT_SCALE, label = "Scale", required = true, description = "@text/actions.input.description.scale") @Nullable String scale, //
            @ActionInput(name = ACTION_INPUT_DATETIME, label = "Date/Time", required = true, description = "@text/actions.input.description.datetime") @Nullable Instant datetime) {
        logger.info("queryEnergyTrend called");

        SenseEnergyMonitorHandler localDeviceHandler = deviceHandler;
        if (localDeviceHandler == null) {
            logger.warn("querying device usage, but device is undefined.");
            return Collections.emptyMap();
        }

        Instant localDateTime = (datetime == null) ? Instant.now() : datetime;

        if (scale == null) {
            logger.warn("queryEnergyTrends called with null inputs");
            return Collections.emptyMap();
        }

        if (!SenseEnergyApi.TrendScale.contains(scale)) {
            logger.warn("Invalid scale type in call to queryEnergyTrend");
            return Collections.emptyMap();
        }
        SenseEnergyApi.TrendScale trendScale = SenseEnergyApi.TrendScale.valueOf(scale);

        SenseEnergyApiGetTrends trends;
        try {
            trends = localDeviceHandler.getApi().getTrendData(localDeviceHandler.getId(), trendScale, localDateTime);
        } catch (SenseEnergyApiException e) {
            logger.warn("queryEnergyTrends function failed - {}", e.getMessage());
            return Collections.emptyMap();
        }

        if (trends == null) {
            return Collections.emptyMap();
        }

        Map<String, QuantityType<?>> valuesMap = new HashMap<>();

        valuesMap.put(ACTION_OUTPUT_CONSUMPTION,
                new QuantityType<Energy>(trends.consumption.totalPower, Units.KILOWATT_HOUR));
        valuesMap.put(ACTION_OUTPUT_PRODUCTION,
                new QuantityType<Energy>(trends.production.totalPower, Units.KILOWATT_HOUR));
        valuesMap.put(ACTION_OUTPUT_TO_GRID, new QuantityType<Energy>(trends.toGridEnergy, Units.KILOWATT_HOUR));
        valuesMap.put(ACTION_OUTPUT_FROM_GRID, new QuantityType<Energy>(trends.fromGridEnergy, Units.KILOWATT_HOUR));
        valuesMap.put(ACTION_OUTPUT_NET_PRODUCTION,
                new QuantityType<Energy>(trends.netProduction, Units.KILOWATT_HOUR));
        valuesMap.put(ACTION_OUTPUT_SOLAR_POWERED, new QuantityType<Dimensionless>(trends.solarPowered, Units.PERCENT));

        return valuesMap;
    }

    // Static method for Rules DSL backward compatibility
    public static @Nullable Map<String, QuantityType<?>> queryEnergyTrend(ThingActions actions, @Nullable String scale,
            @Nullable Instant datetime) {
        if (actions instanceof SenseEnergyMonitorActions localActions) {
            return localActions.queryEnergyTrend(scale, datetime);
        } else {
            throw new IllegalArgumentException("Instance is not a SenseEnergyMonitorActions class.");
        }
    }
}
