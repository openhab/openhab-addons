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
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.openhab.binding.hive.internal.HiveBindingConstants;
import org.openhab.binding.hive.internal.client.HiveApiConstants;
import org.openhab.binding.hive.internal.client.Node;
import org.openhab.binding.hive.internal.client.feature.ZigbeeDeviceFeature;

/**
 * A {@link ThingHandlerStrategy} for handling
 * {@link ZigbeeDeviceFeature}.
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public final class ZigbeeDeviceHandlerStrategy extends ThingHandlerStrategyBase {
    @Override
    public void handleUpdate(
            final Thing thing,
            final ThingHandlerCallback thingHandlerCallback,
            final Node hiveNode
    ) {
        useFeature(hiveNode, ZigbeeDeviceFeature.class, zigbeeDeviceFeature -> {
            useAttribute(hiveNode, ZigbeeDeviceFeature.class, HiveApiConstants.ATTRIBUTE_NAME_ZIGBEE_DEVICE_V1_EUI64, zigbeeDeviceFeature.getEui64(), eui64Attribute -> {
                thing.setProperty(HiveBindingConstants.PROPERTY_EUI64, eui64Attribute.getDisplayValue().toString());

                // TODO: Extract MAC address from EUI64?
                //thing.setProperty(Thing.PROPERTY_MAC_ADDRESS, xxx);
            });

            useAttribute(hiveNode, ZigbeeDeviceFeature.class, HiveApiConstants.ATTRIBUTE_NAME_ZIGBEE_DEVICE_V1_AVERAGE_LQI, zigbeeDeviceFeature.getAverageLQI(), averageLQIAttribute -> {
                useChannel(thing, HiveBindingConstants.CHANNEL_RADIO_LQI_AVERAGE, averageLqiChannel -> {
                    thingHandlerCallback.stateUpdated(averageLqiChannel, new DecimalType(averageLQIAttribute.getDisplayValue()));
                });
            });

            useAttribute(hiveNode, ZigbeeDeviceFeature.class, HiveApiConstants.ATTRIBUTE_NAME_ZIGBEE_DEVICE_V1_LAST_KNOWN_LQI, zigbeeDeviceFeature.getLastKnownLQI(), lastKnownLQIAttribute -> {
                useChannel(thing, HiveBindingConstants.CHANNEL_RADIO_LQI_LAST_KNOWN, lastKnownLqiChannel -> {
                    thingHandlerCallback.stateUpdated(lastKnownLqiChannel, new DecimalType(lastKnownLQIAttribute.getDisplayValue()));
                });
            });

            useAttribute(hiveNode, ZigbeeDeviceFeature.class, HiveApiConstants.ATTRIBUTE_NAME_ZIGBEE_DEVICE_V1_AVERAGE_RSSI, zigbeeDeviceFeature.getAverageRSSI(), averageRSSIAttribute -> {
                useChannel(thing, HiveBindingConstants.CHANNEL_RADIO_RSSI_AVERAGE, averageRSSIChannel -> {
                    thingHandlerCallback.stateUpdated(averageRSSIChannel, new DecimalType(averageRSSIAttribute.getDisplayValue()));
                });
            });

            useAttribute(hiveNode, ZigbeeDeviceFeature.class, HiveApiConstants.ATTRIBUTE_NAME_ZIGBEE_DEVICE_V1_LAST_KNOWN_RSSI, zigbeeDeviceFeature.getLastKnownRSSI(), lastKnownRSSIAttribute -> {
                useChannel(thing, HiveBindingConstants.CHANNEL_RADIO_RSSI_LAST_KNOWN, lastKnownRSSIChannel -> {
                    thingHandlerCallback.stateUpdated(lastKnownRSSIChannel, new DecimalType(lastKnownRSSIAttribute.getDisplayValue()));
                });
            });
        });
    }
}
