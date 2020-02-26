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
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.hive.internal.HiveBindingConstants;
import org.openhab.binding.hive.internal.client.HiveApiConstants;
import org.openhab.binding.hive.internal.client.Node;
import org.openhab.binding.hive.internal.client.feature.AutoBoostFeature;
import tec.uom.se.quantity.Quantities;

import javax.measure.quantity.Temperature;
import java.time.Duration;

/**
 * A {@link ThingHandlerStrategy} for handling
 * {@link AutoBoostFeature}.
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public final class AutoBoostHandlerStrategy extends ThingHandlerStrategyBase {
    @SuppressWarnings("unchecked")
    @Override
    public @Nullable Node handleCommand(
            final ChannelUID channelUID,
            final Command command,
            final Node hiveNode
    ) {
        return useFeature(hiveNode, AutoBoostFeature.class, autoBoostFeature -> {
            @Nullable AutoBoostFeature updatedAutoBoostFeature = null;
            if (channelUID.getId().equals(HiveBindingConstants.CHANNEL_AUTO_BOOST_DURATION)
                    && command instanceof DecimalType
            ) {
                final DecimalType newAutoBoostDuration = (DecimalType) command;

                updatedAutoBoostFeature = autoBoostFeature.withTargetAutoBoostDuration(
                        Duration.ofMinutes(newAutoBoostDuration.longValue())
                );
            } else if (channelUID.getId().equals(HiveBindingConstants.CHANNEL_AUTO_BOOST_TEMPERATURE_TARGET)
                    && command instanceof QuantityType
            ) {
                // N.B. Suppress unchecked because openHAB should hopefully only be passing us QuantityType<Temperature>
                final QuantityType<Temperature> newTargetHeatTemperature = (QuantityType<Temperature>) command;

                updatedAutoBoostFeature = autoBoostFeature.withTargetAutoBoostTargetHeatTemperature(
                        Quantities.getQuantity(
                                newTargetHeatTemperature.toBigDecimal(),
                                newTargetHeatTemperature.getUnit()
                        )
                );
            }

            if (updatedAutoBoostFeature != null) {
                return hiveNode.withFeature(AutoBoostFeature.class, updatedAutoBoostFeature);
            } else {
                return null;
            }
        });
    }

    @Override
    public void handleUpdate(
            final Thing thing,
            final ThingHandlerCallback thingHandlerCallback,
            final Node hiveNode
    ) {
        useFeature(hiveNode, AutoBoostFeature.class, autoBoostFeature -> {
            useAttribute(hiveNode, AutoBoostFeature.class, HiveApiConstants.ATTRIBUTE_NAME_AUTO_BOOST_V1_AUTO_BOOST_DURATION, autoBoostFeature.getAutoBoostDuration(), autoBoostDurationAttribute -> {
                useChannel(thing, HiveBindingConstants.CHANNEL_AUTO_BOOST_DURATION, autoBoostDurationChannel -> {
                    thingHandlerCallback.stateUpdated(autoBoostDurationChannel, new DecimalType(autoBoostDurationAttribute.getDisplayValue().toMinutes()));
                });
            });

            useAttribute(hiveNode, AutoBoostFeature.class, HiveApiConstants.ATTRIBUTE_NAME_AUTO_BOOST_V1_AUTO_BOOST_TARGET_HEAT_TEMPERATURE, autoBoostFeature.getAutoBoostTargetHeatTemperature(), autoBoostTargetHeatTemperatureAttribute -> {
                useChannel(thing, HiveBindingConstants.CHANNEL_AUTO_BOOST_TEMPERATURE_TARGET, autoBoostTargetHeatTemperatureChannel -> {
                    thingHandlerCallback.stateUpdated(
                            autoBoostTargetHeatTemperatureChannel,
                            new QuantityType<>(
                                    autoBoostTargetHeatTemperatureAttribute.getDisplayValue().getValue(),
                                    autoBoostTargetHeatTemperatureAttribute.getDisplayValue().getUnit()
                            )
                    );
                });
            });
        });
    }
}
