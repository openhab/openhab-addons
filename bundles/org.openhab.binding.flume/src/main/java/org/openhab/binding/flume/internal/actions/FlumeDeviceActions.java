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
package org.openhab.binding.flume.internal.actions;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.measure.quantity.Volume;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.flume.internal.api.FlumeApi;
import org.openhab.binding.flume.internal.api.FlumeApiException;
import org.openhab.binding.flume.internal.api.dto.FlumeApiQueryWaterUsage;
import org.openhab.binding.flume.internal.handler.FlumeDeviceHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FlumeDeviceActions} class defines actions for the Flume Device
 *
 * @author Jeff James - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = FlumeDeviceActions.class)
@ThingActionsScope(name = "flume")
@NonNullByDefault
public class FlumeDeviceActions implements ThingActions {
    private final Logger logger = LoggerFactory.getLogger(FlumeDeviceActions.class);
    private static final String QUERYID = "action_query";

    private @Nullable FlumeDeviceHandler deviceHandler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof FlumeDeviceHandler deviceHandler) {
            this.deviceHandler = deviceHandler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return deviceHandler;
    }

    /**
     * Query water usage
     */
    @RuleAction(label = "query water usage", description = "Queries water usage over a period of time.")
    public @Nullable @ActionOutput(label = "Water Usage", type = "QuantityType<Volume>") QuantityType<Volume> queryWaterUsage(
            @ActionInput(name = "sinceDateTime", label = "Since Date/Time", required = true, description = "Restrict the query range to data samples since this datetime.") @Nullable LocalDateTime sinceDateTime,
            @ActionInput(name = "untilDateTime", label = "Until Date/Time", required = true, description = "Restrict the query range to data samples until this datetime.") @Nullable LocalDateTime untilDateTime,
            @ActionInput(name = "bucket", label = "Bucket size", required = true, description = "The bucket grouping of the data we are querying (MIN, HR, DAY, MON, YR).") @Nullable String bucket,
            @ActionInput(name = "operation", label = "Operation", required = true, description = "The aggregate/accumulate operation to perform (SUM, AVG, MIN, MAX, CNT).") @Nullable String operation) {
        logger.info("queryWaterUsage called");

        FlumeDeviceHandler localDeviceHandler = deviceHandler;
        if (localDeviceHandler == null) {
            logger.debug("querying device usage, but device is undefined.");
            return null;
        }

        boolean imperialUnits = localDeviceHandler.isImperial();

        if (operation == null || bucket == null || sinceDateTime == null || untilDateTime == null) {
            logger.warn("queryWaterUsage called with null inputs");
            return null;
        }
        if (!untilDateTime.isAfter(sinceDateTime)) {
            logger.warn("sinceDateTime must be earlier than untilDateTime");
            return null;
        }
        if (!FlumeApi.OperationType.contains(operation)) {
            logger.warn("Invalid aggregation operation in call to queryWaterUsage");
            return null;
        }
        if (!FlumeApi.BucketType.contains(bucket)) {
            logger.warn("Invalid bucket type in call to queryWaterUsage");
            return null;
        }

        FlumeApiQueryWaterUsage query = new FlumeApiQueryWaterUsage(QUERYID, //
                sinceDateTime, //
                untilDateTime, //
                FlumeApi.BucketType.valueOf(bucket), //
                100, //
                FlumeApi.OperationType.valueOf(operation), //
                imperialUnits ? FlumeApi.UnitType.GALLONS : FlumeApi.UnitType.LITERS, FlumeApi.SortDirectionType.ASC);

        Float usage;
        try {
            usage = localDeviceHandler.getApi().queryUsage(localDeviceHandler.getId(), query);
        } catch (FlumeApiException | IOException | InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("queryWaterUsage function failed - {}", e.getMessage());
            return null;
        }

        if (usage == null) {
            return null;
        }

        return new QuantityType<Volume>(usage, imperialUnits ? ImperialUnits.GALLON_LIQUID_US : Units.LITRE);
    }

    // Static method for Rules DSL backward compatibility
    public static @Nullable QuantityType<Volume> queryWaterUsage(ThingActions actions,
            @Nullable LocalDateTime sinceDateTime, @Nullable LocalDateTime untilDateTime, @Nullable String bucket,
            @Nullable String operation) {
        if (actions instanceof FlumeDeviceActions localActions) {
            return localActions.queryWaterUsage(sinceDateTime, untilDateTime, bucket, operation);
        } else {
            throw new IllegalArgumentException("Instance is not a FlumeDeviceActions class.");
        }
    }
}
