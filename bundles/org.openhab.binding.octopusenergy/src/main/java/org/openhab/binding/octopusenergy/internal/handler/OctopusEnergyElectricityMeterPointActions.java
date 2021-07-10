/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.octopusenergy.internal.handler;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.octopusenergy.internal.dto.Price;
import org.openhab.binding.octopusenergy.internal.dto.PriceOptimiserResult;
import org.openhab.binding.octopusenergy.internal.exception.NotEnoughDataException;
import org.openhab.binding.octopusenergy.internal.exception.RecordNotFoundException;
import org.openhab.binding.octopusenergy.internal.util.PriceOptimiser;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OctopusEnergyElectricityMeterPointActions} class implements actions for Electricity Meter Points.
 *
 * @author Rene Scherer - Initial contribution
 */
@ThingActionsScope(name = "octopusenergy")
@NonNullByDefault
public class OctopusEnergyElectricityMeterPointActions implements ThingActions {

    private static final PriceOptimiser PO = PriceOptimiser.getInstance();

    private final Logger logger = LoggerFactory.getLogger(OctopusEnergyElectricityMeterPointActions.class);

    private @Nullable OctopusEnergyElectricityMeterPointHandler handler;

    @Override
    public void setThingHandler(ThingHandler handler) {
        this.handler = (OctopusEnergyElectricityMeterPointHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "optimise", description = "Calculates the optimal time and cost for an activity.")
    public @Nullable @ActionOutput(name = "lastUpdatedTime", type = "java.time.ZonedDateTime") @ActionOutput(name = "optimisedStartTime", type = "java.time.ZonedDateTime") @ActionOutput(name = "optimisedAverageUnitCost", type = "java.math.BigDecimal") @ActionOutput(name = "requestedDuration", type = "java.time.Duration") @ActionOutput(name = "requestedEndTime", type = "java.time.ZonedDateTime") @ActionOutput(name = "requestedStartTime", type = "java.time.ZonedDateTime") Map<String, Object> optimise(
            @ActionInput(name = "duration", label = "duration", description = "The duration of the activity.") Duration duration) {
        try {
            return toResultMap(PO.optimiseWithAbsoluteStartTime(duration, ZonedDateTime.now(), getPriceList()));
        } catch (NotEnoughDataException e) {
            logger.warn("Not enough data available for optimizer - {}", e.getMessage());
        } catch (RecordNotFoundException e) {
            logger.warn("No Meter Point found for optimizer - {}", e.getMessage());
        }
        return new HashMap<String, Object>();
    }

    public static @Nullable Map<String, Object> optimise(@Nullable ThingActions actions, Duration duration) {
        if (actions instanceof OctopusEnergyElectricityMeterPointActions) {
            return ((OctopusEnergyElectricityMeterPointActions) actions).optimise(duration);
        } else {
            throw new IllegalArgumentException("Instance is not an OctopusEnergyElectricityMeterPointActions class.");
        }
    }

    @RuleAction(label = "optimiseWithRecurringEndTime", description = "Calculates the optimal time and cost for an activity.")
    public @Nullable @ActionOutput(name = "lastUpdatedTime", type = "java.time.ZonedDateTime") @ActionOutput(name = "optimisedStartTime", type = "java.time.ZonedDateTime") @ActionOutput(name = "optimisedAverageUnitCost", type = "java.math.BigDecimal") @ActionOutput(name = "requestedDuration", type = "java.time.Duration") @ActionOutput(name = "requestedEndTime", type = "java.time.ZonedDateTime") @ActionOutput(name = "requestedStartTime", type = "java.time.ZonedDateTime") Map<String, Object> optimiseWithRecurringEndTime(
            @ActionInput(name = "duration", label = "duration", description = "The duration of the activity.") Duration duration,
            @ActionInput(name = "latestEndHour", label = "latestEndHour", description = "The latest hour the activity must finish.") int latestEndHour,
            @ActionInput(name = "latestEndMinute", label = "latestEndMinute", description = "The latest minute the activity must finish.") int latestEndMinute) {
        try {
            return toResultMap(PO.optimiseWithRecurringEndTime(duration, ZonedDateTime.now(), latestEndHour,
                    latestEndMinute, getPriceList()));
        } catch (NotEnoughDataException e) {
            logger.warn("Not enough data available for optimizer - {}", e.getMessage());
        } catch (RecordNotFoundException e) {
            logger.warn("No Meter Point found for optimizer - {}", e.getMessage());
        }
        return new HashMap<String, Object>();
    }

    public static @Nullable Map<String, Object> optimiseWithRecurringEndTime(@Nullable ThingActions actions,
            Duration duration, int latestEndHour, int latestEndMinute) {
        if (actions instanceof OctopusEnergyElectricityMeterPointActions) {
            return ((OctopusEnergyElectricityMeterPointActions) actions).optimiseWithRecurringEndTime(duration,
                    latestEndHour, latestEndMinute);
        } else {
            throw new IllegalArgumentException("Instance is not an OctopusEnergyElectricityMeterPointActions class.");
        }
    }

    @RuleAction(label = "optimiseWithRecurringStartTime", description = "Calculates the optimal time and cost for an activity.")
    public @Nullable @ActionOutput(name = "lastUpdatedTime", type = "java.time.ZonedDateTime") @ActionOutput(name = "optimisedStartTime", type = "java.time.ZonedDateTime") @ActionOutput(name = "optimisedAverageUnitCost", type = "java.math.BigDecimal") @ActionOutput(name = "requestedDuration", type = "java.time.Duration") @ActionOutput(name = "requestedEndTime", type = "java.time.ZonedDateTime") @ActionOutput(name = "requestedStartTime", type = "java.time.ZonedDateTime") Map<String, Object> optimiseWithRecurringStartTime(
            @ActionInput(name = "duration", label = "duration", description = "The duration of the activity.") Duration duration,
            @ActionInput(name = "earliestStartHour", label = "earliestStartHour", description = "The earliest hour the activity can start.") int earliestStartHour,
            @ActionInput(name = "earliestStartMinute", label = "earliestStartMinute", description = "The earliest minute the activity can start.") int earliestStartMinute) {
        try {
            return toResultMap(PO.optimiseWithRecurringStartTime(duration, earliestStartHour, earliestStartMinute,
                    ZonedDateTime.now(), getPriceList()));
        } catch (NotEnoughDataException e) {
            logger.warn("Not enough data available for optimizer - {}", e.getMessage());
        } catch (RecordNotFoundException e) {
            logger.warn("No Meter Point found for optimizer - {}", e.getMessage());
        }
        return new HashMap<String, Object>();
    }

    public static @Nullable Map<String, Object> optimiseWithRecurringStartTime(@Nullable ThingActions actions,
            Duration duration, int earliestStartHour, int earliestStartMinute) {
        if (actions instanceof OctopusEnergyElectricityMeterPointActions) {
            return ((OctopusEnergyElectricityMeterPointActions) actions).optimiseWithRecurringStartTime(duration,
                    earliestStartHour, earliestStartMinute);
        } else {
            throw new IllegalArgumentException("Instance is not an OctopusEnergyElectricityMeterPointActions class.");
        }
    }

    @RuleAction(label = "optimiseWithAbsoluteStartTime", description = "Calculates the optimal time and cost for anactivity.")
    public @Nullable @ActionOutput(name = "lastUpdatedTime", type = "java.time.ZonedDateTime") @ActionOutput(name = "optimisedStartTime", type = "java.time.ZonedDateTime") @ActionOutput(name = "optimisedAverageUnitCost", type = "java.math.BigDecimal") @ActionOutput(name = "requestedDuration", type = "java.time.Duration") @ActionOutput(name = "requestedEndTime", type = "java.time.ZonedDateTime") @ActionOutput(name = "requestedStartTime", type = "java.time.ZonedDateTime") Map<String, Object> optimiseWithAbsoluteStartTime(
            @ActionInput(name = "duration", label = "duration", description = "The duration of the activity.") Duration duration,
            @ActionInput(name = "earliestStartTime", label = "earliestStartTime", description = "The earliest time the activity can start. If null, as soon as possible.") ZonedDateTime earliestStartTime) {
        try {
            return toResultMap(PO.optimiseWithAbsoluteStartTime(duration, earliestStartTime, getPriceList()));
        } catch (NotEnoughDataException e) {
            logger.warn("Not enough data available for optimizer - {}", e.getMessage());
        } catch (RecordNotFoundException e) {
            logger.warn("No Meter Point found for optimizer - {}", e.getMessage());
        }
        return new HashMap<String, Object>();
    }

    public static @Nullable Map<String, Object> optimiseWithAbsoluteStartTime(@Nullable ThingActions actions,
            Duration duration, ZonedDateTime earliestStartTime) {
        if (actions instanceof OctopusEnergyElectricityMeterPointActions) {
            return ((OctopusEnergyElectricityMeterPointActions) actions).optimiseWithAbsoluteStartTime(duration,
                    earliestStartTime);
        } else {
            throw new IllegalArgumentException("Instance is not an OctopusEnergyElectricityMeterPointActions class.");
        }
    }

    private List<Price> getPriceList() throws RecordNotFoundException {
        OctopusEnergyElectricityMeterPointHandler handler = this.handler;
        if (handler == null) {
            throw new RecordNotFoundException("No Handler found");
        }
        return handler.getMeterPoint().priceList;
    }

    private Map<String, Object> toResultMap(PriceOptimiserResult result) {
        Map<String, Object> map = new HashMap<>();
        map.put("lastUpdatedTime", result.lastUpdatedTime);
        map.put("optimisedStartTime", result.optimisedStartTime);
        map.put("optimisedAverageUnitCost", result.optimisedAverageUnitCost);
        map.put("requestedDuration", result.requestedDuration);
        map.put("requestedEndTime", result.requestedEndTime);
        map.put("requestedStartTime", result.requestedStartTime);
        return map;
    }
}
