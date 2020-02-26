/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.hive.internal.handler.strategy;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.openhab.binding.hive.internal.HiveBindingConstants;
import org.openhab.binding.hive.internal.client.FeatureAttribute;
import org.openhab.binding.hive.internal.client.HiveApiConstants;
import org.openhab.binding.hive.internal.client.Node;
import org.openhab.binding.hive.internal.client.OverrideMode;
import org.openhab.binding.hive.internal.client.feature.HeatingThermostatFeature;
import org.openhab.binding.hive.internal.client.feature.TransientModeFeature;
import org.openhab.binding.hive.internal.client.feature.WaterHeaterFeature;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * A {@link ThingHandlerStrategy} that handles the "transient-remaining"
 * channel.
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public final class BoostTimeRemainingHandlerStrategy extends ThingHandlerStrategyBase  {
    @Override
    public void handleUpdate(
            final Thing thing,
            final ThingHandlerCallback thingHandlerCallback,
            final Node hiveNode
    ) {
        useFeature(hiveNode, TransientModeFeature.class, transientModeFeature -> {
            useAttribute(hiveNode, TransientModeFeature.class, HiveApiConstants.ATTRIBUTE_NAME_TRANSIENT_MODE_V1_END_DATETIME, transientModeFeature.getEndDatetime(), endDatetimeAttribute -> {
                long minutesRemaining = Instant.now().until(endDatetimeAttribute.getDisplayValue(), ChronoUnit.MINUTES);
                minutesRemaining = Math.max(0, minutesRemaining);

                // If we know the transient mode has been cancelled set remaining
                // time to 0
                final @Nullable HeatingThermostatFeature heatingThermostatFeature = hiveNode.getFeature(HeatingThermostatFeature.class);
                final @Nullable WaterHeaterFeature waterHeaterFeature = hiveNode.getFeature(WaterHeaterFeature.class);
                final @Nullable FeatureAttribute<OverrideMode> overrideModeAttribute;
                if (heatingThermostatFeature != null) {
                    overrideModeAttribute = heatingThermostatFeature.getTemporaryOperatingModeOverride();
                } else if (waterHeaterFeature != null) {
                    overrideModeAttribute = waterHeaterFeature.getTemporaryOperatingModeOverride();
                } else {
                    overrideModeAttribute = null;
                    this.logger.trace("Could not find either of \"HeatingThermostatFeature\" or \"WaterHeaterFeature\"");
                }

                if (overrideModeAttribute != null && overrideModeAttribute.getDisplayValue() == OverrideMode.NONE) {
                    minutesRemaining = 0;
                }

                final long finalMinutesRemaining = minutesRemaining;
                useChannel(thing, HiveBindingConstants.CHANNEL_TRANSIENT_REMAINING, transientRemainingChannel -> {
                    thingHandlerCallback.stateUpdated(transientRemainingChannel, new DecimalType(finalMinutesRemaining));
                });
            });
        });
    }
}
