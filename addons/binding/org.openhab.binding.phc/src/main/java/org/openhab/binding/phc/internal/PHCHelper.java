/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.phc.internal;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;

/**
 * The {@link PHCHelper} is responsible for finding the appropriate Thing(UID) to the Channel of the PHC module.
 *
 * @author Jonas Hohaus - Initial contribution
 */

public class PHCHelper {

    /**
     * Get the ThingUID by the given parameters.
     *
     * @param thingTypeUID
     * @param moduleAddr reverse (to the reverse address - DIP switches)
     * @return
     */
    public static ThingUID getThingUIDreverse(ThingTypeUID thingTypeUID, byte moduleAddr) {
        // convert to 5-bit binary string and reverse in second step
        String thingID = StringUtils.leftPad(StringUtils.trim(Integer.toBinaryString(moduleAddr & 0xFF)), 5, '0');
        thingID = new StringBuilder(thingID).reverse().toString();

        ThingUID thingUID = new ThingUID(thingTypeUID, thingID);

        return thingUID;
    }

    /**
     * Convert the byte b into an binary String
     *
     * @param b
     * @return
     */
    public static Object byteToBinaryString(byte b) {
        return StringUtils.leftPad(StringUtils.trim(Integer.toBinaryString(b & 0xFF)), 8, '0') + " ";
    }

    /**
     * Convert the byte b into an hex String
     *
     * @param b
     * @return
     */
    public static Object byteToHexString(byte b) {
        return Integer.toHexString(b & 0xFF) + " ";
    }
}
