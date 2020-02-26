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
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.openhab.binding.hive.internal.client.Node;
import org.openhab.binding.hive.internal.client.feature.PhysicalDeviceFeature;

/**
 * A {@link ThingHandlerStrategy} for handling
 * {@link PhysicalDeviceFeature}.
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public final class PhysicalDeviceHandlerStrategy extends ThingHandlerStrategyBase {
    @Override
    public void handleUpdate(
            final Thing thing,
            final ThingHandlerCallback thingHandlerCallback,
            final Node hiveNode
    ) {
        useFeature(hiveNode, PhysicalDeviceFeature.class, physicalDeviceFeature -> {
            useAttribute(hiveNode, PhysicalDeviceFeature.class, "manufacturer", physicalDeviceFeature.getManufacturer(), manufacturerAttribute -> {
                thing.setProperty(Thing.PROPERTY_VENDOR, manufacturerAttribute.getDisplayValue());
            });

            useAttribute(hiveNode, PhysicalDeviceFeature.class, "model", physicalDeviceFeature.getModel(), modelAttribute -> {
                thing.setProperty(Thing.PROPERTY_MODEL_ID, modelAttribute.getDisplayValue());
            });

            useAttribute(hiveNode, PhysicalDeviceFeature.class, "softwareVersion", physicalDeviceFeature.getModel(), softwareVersionAttribute -> {
                thing.setProperty(Thing.PROPERTY_FIRMWARE_VERSION, softwareVersionAttribute.getDisplayValue());
            });

            useAttribute(hiveNode, PhysicalDeviceFeature.class, "hardwareIdentifier", physicalDeviceFeature.getModel(), hardwareIdentifierAttribute -> {
                thing.setProperty(Thing.PROPERTY_SERIAL_NUMBER, hardwareIdentifierAttribute.getDisplayValue());
            });
        });
    }
}
