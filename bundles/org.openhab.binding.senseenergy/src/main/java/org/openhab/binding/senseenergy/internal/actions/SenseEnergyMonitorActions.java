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

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

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
 * The { @link SenseEnergyMonitorActions } class defines actions for the Sense Energy Monitor
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
    // @formatter:off
    @RuleAction(label="Query Energy Trend", description="Queries energy trend over a period of time.")
    public  @ActionOutput(name="consumption", type="QuantityType<Energy>", description="the total energy (KWh) used over the scale period.")
            @ActionOutput(name="production", type="QuantityType<Energy>", description="the total energy (KWh) produced over the scale period.")
            @ActionOutput(name="fromGrid", type="QuantityType<Energy>", description="the total energy (KWh) from the grid over the scale period.")
            @ActionOutput(name="toGrid", type="QuantityType<Energy>", description="the total energy (KWh) to the grid over the scale period.")
            @ActionOutput(name="netProduction", type="QuantityType<Energy>", description="the difference in energy (KWh) between what was produced and consumed during the scale period.")
            @ActionOutput(name="solarPowered", type="QuantityType<Dimensionless>", description="the percent of solar energy production that was directly consumed (not sent to grid) during the scale period.")
        Map<String, Object> queryEnergyTrend(
            @ActionInput(name="scale", label="Scale", required=true, description="Scale to be returned (DAY, WEEK, MONTH, YEAR)") @Nullable String scale,
            @ActionInput(name="datetime", label="Date/Time", required=true, description="Restrict the query range to data samples since this datetime.") @Nullable Instant datetime) {
    // @formatter:on
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
        } catch (InterruptedException | TimeoutException | ExecutionException | SenseEnergyApiException e) {
            logger.warn("queryEnergyTrends function failed - {}", e.getMessage());
            return Collections.emptyMap();
        }

        if (trends == null) {
            return Collections.emptyMap();
        }

        Map<String, Object> valuesMap = new HashMap<String, Object>();

        valuesMap.put("consumption", new QuantityType<Energy>(trends.consumption.totalPower, Units.KILOWATT_HOUR));
        valuesMap.put("production", new QuantityType<Energy>(trends.production.totalPower, Units.KILOWATT_HOUR));
        valuesMap.put("toGrid", new QuantityType<Energy>(trends.toGridEnergy, Units.KILOWATT_HOUR));
        valuesMap.put("fromGrid", new QuantityType<Energy>(trends.fromGridEnergy, Units.KILOWATT_HOUR));
        valuesMap.put("netProduction", new QuantityType<Energy>(trends.netProduction, Units.KILOWATT_HOUR));
        valuesMap.put("solarPowered", new QuantityType<Dimensionless>(trends.solarPowered, Units.PERCENT));

        return valuesMap;
    }

    // Static method for Rules DSL backward compatibility
    public static @Nullable Map<String, Object> queryEnergyTrend(ThingActions actions, @Nullable String scale,
            @Nullable Instant datetime) {
        if (actions instanceof SenseEnergyMonitorActions localActions) {
            return localActions.queryEnergyTrend(scale, datetime);
        } else {
            throw new IllegalArgumentException("Instance is not a SenseEnergyMonitorActions class.");
        }
    }
}
