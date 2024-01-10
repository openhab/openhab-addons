/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.gardena.internal.util;

import static org.openhab.binding.gardena.internal.GardenaBindingConstants.BINDING_ID;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.gardena.internal.handler.GardenaDeviceConfig;
import org.openhab.binding.gardena.internal.model.dto.Device;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;

/**
 * Utility class for converting between a Thing and a Gardena device.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@NonNullByDefault
public class UidUtils {

    /**
     * Generates the ThingUID for the given device in the given account.
     */
    public static ThingUID generateThingUID(Device device, Bridge account) {
        ThingTypeUID thingTypeUID = new ThingTypeUID(BINDING_ID, device.deviceType);
        return new ThingUID(thingTypeUID, account.getUID(), device.id);
    }

    /**
     * Returns all ThingUIDs for a given device.
     */
    public static List<ThingUID> getThingUIDs(Device device, Bridge account) {
        List<ThingUID> thingUIDs = new ArrayList<>();
        for (Thing thing : account.getThings()) {
            String deviceId = thing.getConfiguration().as(GardenaDeviceConfig.class).deviceId;
            if (deviceId == null) {
                deviceId = thing.getUID().getId();
            }
            if (deviceId.equals(device.id)) {
                thingUIDs.add(thing.getUID());
            }
        }
        return thingUIDs;
    }

    /**
     * Returns the device id of the Gardena device from the given thing.
     */
    public static String getGardenaDeviceId(Thing thing) {
        String deviceId = thing.getConfiguration().as(GardenaDeviceConfig.class).deviceId;
        if (deviceId != null) {
            return deviceId;
        }

        return thing.getUID().getId();
    }
}
