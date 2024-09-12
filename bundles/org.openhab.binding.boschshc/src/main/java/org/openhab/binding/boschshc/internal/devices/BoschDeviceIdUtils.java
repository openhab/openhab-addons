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
package org.openhab.binding.boschshc.internal.devices;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Utilities for handling parent/child relations in Bosch device IDs.
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
public final class BoschDeviceIdUtils {

    private static final String CHILD_ID_SEPARATOR = "#";

    private BoschDeviceIdUtils() {
        // Utility Class
    }

    /**
     * Returns whether the given device ID is a child device ID.
     * <p>
     * Example for a parent device ID:
     * 
     * <pre>
     * hdm:ZigBee:70ac08fffefead2d
     * </pre>
     * 
     * Example for a child device ID:
     * 
     * <pre>
     * hdm:ZigBee:70ac08fffefead2d#2
     * </pre>
     * 
     * @param deviceId the Bosch device ID to check
     * @return <code>true</code> if the device ID contains a hash character, <code>false</code> otherwise
     */
    public static boolean isChildDeviceId(String deviceId) {
        return deviceId.contains(CHILD_ID_SEPARATOR);
    }

    /**
     * If the given device ID is a child device ID, the parent device ID is derived by cutting off the part starting
     * from the hash character.
     * 
     * @param deviceId a device ID
     * @return the parent device ID, if derivable. Otherwise the given ID is returned.
     */
    public static String getParentDeviceId(String deviceId) {
        int hashIndex = deviceId.indexOf(CHILD_ID_SEPARATOR);
        if (hashIndex < 0) {
            return deviceId;
        }

        return deviceId.substring(0, hashIndex);
    }
}
