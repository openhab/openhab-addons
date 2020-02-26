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
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.hive.internal.HiveBindingConstants;
import org.openhab.binding.hive.internal.client.DefaultFeatureAttribute;
import org.openhab.binding.hive.internal.client.Node;
import org.openhab.binding.hive.internal.client.feature.TransientModeHeatingActionsFeature;
import tec.uom.se.quantity.Quantities;
import tec.uom.se.unit.Units;

import javax.measure.Quantity;
import javax.measure.quantity.Temperature;
import java.time.Instant;

/**
 * A {@link ThingHandlerStrategy} for handling channels that interface with
 * the "set targetTemperature action" for transient override (boost) of
 * Hive Active Heating zones.
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public final class HeatingTransientModeHandlerStrategy extends ThingHandlerStrategyBase {
    public static final Quantity<Temperature> DEFAULT_BOOST_TEMPERATURE = Quantities.getQuantity(22, Units.CELSIUS);

    private static TransientModeHeatingActionsFeature getDefaultTransientModeActionsFeature() {
        final Instant now = Instant.now();

        return TransientModeHeatingActionsFeature.builder()
                .boostTargetTemperature(
                        DefaultFeatureAttribute.<Quantity<Temperature>>builder()
                                .displayValue(DEFAULT_BOOST_TEMPERATURE)
                                .reportedValue(DEFAULT_BOOST_TEMPERATURE)
                                .reportChangedTime(now)
                                .reportReceivedTime(now)
                                .build()
                )
                .build();
    }

    private static TransientModeHeatingActionsFeature getEffectiveTransientModeActionsFeature(final Node node) {
        @Nullable TransientModeHeatingActionsFeature transientModeActionsFeature = node.getFeature(TransientModeHeatingActionsFeature.class);
        if (transientModeActionsFeature == null) {
            transientModeActionsFeature = getDefaultTransientModeActionsFeature();
        }

        return transientModeActionsFeature;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @Nullable Node handleCommand(
            final ChannelUID channelUID,
            final Command command,
            final Node hiveNode
    ) {
        @Nullable TransientModeHeatingActionsFeature newTransientModeActionsFeature = null;

        if (channelUID.getId().equals(HiveBindingConstants.CHANNEL_TEMPERATURE_TARGET_BOOST)
                && command instanceof QuantityType
        ) {
            // N.B. Suppress unchecked because openHAB should hopefully only be passing us QuantityType<Temperature>
            final QuantityType<Temperature> newTargetHeatTemperature = (QuantityType<Temperature>) command;

            final TransientModeHeatingActionsFeature transientModeActionsFeature = getEffectiveTransientModeActionsFeature(hiveNode);

            newTransientModeActionsFeature = transientModeActionsFeature.withTargetBoostTargetTemperature(
                    Quantities.getQuantity(
                            newTargetHeatTemperature.toBigDecimal(),
                            newTargetHeatTemperature.getUnit()
                    )
            );
        }

        if (newTransientModeActionsFeature != null) {
            final Node.Builder nodeBuilder = Node.builder();
            nodeBuilder.from(hiveNode);

            nodeBuilder.putFeature(TransientModeHeatingActionsFeature.class, newTransientModeActionsFeature);

            return nodeBuilder.build();
        } else {
            return null;
        }
    }

    @Override
    public void handleUpdate(
            final Thing thing,
            final ThingHandlerCallback thingHandlerCallback,
            final Node hiveNode
    ) {
        final TransientModeHeatingActionsFeature transientModeActionsFeature = getEffectiveTransientModeActionsFeature(hiveNode);
        useAttribute(hiveNode, TransientModeHeatingActionsFeature.class, "boostTargetTemperature", transientModeActionsFeature.getBoostTargetTemperature(), boostTargetTemperatureAttribute -> {
            useChannel(thing, HiveBindingConstants.CHANNEL_TEMPERATURE_TARGET_BOOST, boostTargetHeatTemperatureChannel -> {
                thingHandlerCallback.stateUpdated(
                        boostTargetHeatTemperatureChannel,
                        new QuantityType<>(
                                boostTargetTemperatureAttribute.getDisplayValue().getValue(),
                                boostTargetTemperatureAttribute.getDisplayValue().getUnit()
                        )
                );
            });
        });
    }
}
