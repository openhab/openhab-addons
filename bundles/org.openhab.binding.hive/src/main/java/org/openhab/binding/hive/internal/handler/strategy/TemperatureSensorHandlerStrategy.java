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
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.openhab.binding.hive.internal.HiveBindingConstants;
import org.openhab.binding.hive.internal.client.HiveApiConstants;
import org.openhab.binding.hive.internal.client.Node;
import org.openhab.binding.hive.internal.client.feature.TemperatureSensorFeature;

/**
 * A {@link ThingHandlerStrategy} for handling
 * {@link TemperatureSensorFeature}.
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public final class TemperatureSensorHandlerStrategy extends ThingHandlerStrategyBase {
    @Override
    public void handleUpdate(
            final Thing thing,
            final ThingHandlerCallback thingHandlerCallback,
            final Node hiveNode
    ) {
        useFeature(hiveNode, TemperatureSensorFeature.class, temperatureSensorFeature -> {
            useAttribute(hiveNode, TemperatureSensorFeature.class, HiveApiConstants.ATTRIBUTE_NAME_TEMPERATURE_SENSOR_V1_TEMPERATURE, temperatureSensorFeature.getTemperature(), temperatureAttribute -> {
                useChannel(thing, HiveBindingConstants.CHANNEL_TEMPERATURE_CURRENT, temperatureChannel -> {
                    thingHandlerCallback.stateUpdated(
                            temperatureChannel,
                            new QuantityType<>(
                                    temperatureAttribute.getDisplayValue().getValue(),
                                    temperatureAttribute.getDisplayValue().getUnit()
                            )
                    );
                });
            });
        });
    }
}
