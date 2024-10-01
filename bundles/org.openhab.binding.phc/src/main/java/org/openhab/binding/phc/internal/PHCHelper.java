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
package org.openhab.binding.phc.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.util.StringUtils;

/**
 * The {@link PHCHelper} is responsible for finding the appropriate Thing(UID)
 * to the Channel of the PHC module.
 *
 * @author Jonas Hohaus - Initial contribution
 */
@NonNullByDefault
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
        String thingID = StringUtils.padLeft(Integer.toBinaryString(moduleAddr & 0xFF).trim(), 5, "0");
        thingID = new StringBuilder(thingID).reverse().toString();

        return new ThingUID(thingTypeUID, thingID);
    }

    /**
     * Convert the byte b into a binary String
     *
     * @param bytes
     * @return
     */
    public static Object bytesToBinaryString(byte[] bytes) {
        StringBuilder bin = new StringBuilder();
        for (byte b : bytes) {
            bin.append(StringUtils.padLeft(Integer.toBinaryString(b & 0xFF).trim(), 8, "0"));
            bin.append(' ');
        }

        return bin.toString();
    }
}
