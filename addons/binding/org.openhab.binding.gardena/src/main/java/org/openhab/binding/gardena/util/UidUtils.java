/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gardena.util;

import static org.openhab.binding.gardena.GardenaBindingConstants.BINDING_ID;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
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
     * Returns the device id of the Gardena device from the given thing.
     */
    public static String getGardenaDeviceId(Thing thing) {
        return thing.getUID().getId();
    }

}
