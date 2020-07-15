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
package org.openhab.binding.gardena.internal.util;

import static org.openhab.binding.gardena.internal.GardenaBindingConstants.BINDING_ID;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.gardena.internal.handler.GardenaDeviceConfig;
import org.openhab.binding.gardena.internal.model.Device;

/**
 * Utility class for converting between a Thing and a Gardena device.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class UidUtils {

    /**
     * Generates the ThingUID for the given device in the given account.
     */
    public static ThingUID generateThingUID(Device device, Bridge account) {
        ThingTypeUID thingTypeUID = new ThingTypeUID(BINDING_ID, device.getCategory());
        return new ThingUID(thingTypeUID, account.getUID(), device.getId());
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
            if (deviceId.equals(device.getId())) {
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
