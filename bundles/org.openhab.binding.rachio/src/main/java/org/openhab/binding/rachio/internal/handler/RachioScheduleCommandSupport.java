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
package org.openhab.binding.rachio.internal.handler;

import static org.openhab.binding.rachio.internal.RachioBindingConstants.*;

import java.util.OptionalDouble;
import java.util.function.DoubleConsumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.rachio.internal.api.RachioApiException;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.Command;
import org.slf4j.Logger;

/**
 * Shared ScheduleRuleService command handling for fixed and flex schedule Things.
 *
 * @author openHAB Contributors - Initial contribution
 */
@NonNullByDefault
final class RachioScheduleCommandSupport {
    private RachioScheduleCommandSupport() {
    }

    static void handleCommand(AbstractRachioThingHandler thingHandler, Logger logger, RachioBridgeHandler bridgeHandler,
            String scheduleRuleId, String channel, Command command, DoubleConsumer seasonalAdjustmentUpdated)
            throws RachioApiException {
        if (CHANNEL_SCHEDULE_START.equals(channel) && command == OnOffType.ON) {
            bridgeHandler.startScheduleRule(scheduleRuleId);
            thingHandler.updateChannel(CHANNEL_SCHEDULE_START, OnOffType.OFF);
        } else if (CHANNEL_SCHEDULE_SKIP.equals(channel) && command == OnOffType.ON) {
            bridgeHandler.skipScheduleRule(scheduleRuleId);
            thingHandler.updateChannel(CHANNEL_SCHEDULE_SKIP, OnOffType.OFF);
        } else if (CHANNEL_SCHEDULE_SEASONAL_ADJUSTMENT.equals(channel)) {
            OptionalDouble adjustment = RachioQuantityTypes.dimensionless(command);
            if (adjustment.isPresent()) {
                double value = adjustment.getAsDouble();
                bridgeHandler.setScheduleRuleSeasonalAdjustment(scheduleRuleId, value);
                seasonalAdjustmentUpdated.accept(value);
                thingHandler.updateChannel(CHANNEL_SCHEDULE_SEASONAL_ADJUSTMENT,
                        RachioQuantityTypes.fractionOrUndef(value));
            } else {
                logger.debug("{}: Seasonal adjustment command value is not dimensionless: {}", thingHandler.thingId,
                        command);
            }
        } else if (CHANNEL_SCHEDULE_SKIP_FORWARD_ZONE_RUN.equals(channel) && command == OnOffType.ON) {
            bridgeHandler.skipForwardZoneRun(scheduleRuleId);
            thingHandler.updateChannel(CHANNEL_SCHEDULE_SKIP_FORWARD_ZONE_RUN, OnOffType.OFF);
        }
    }
}
