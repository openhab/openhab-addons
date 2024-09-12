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
package org.openhab.binding.dsmr.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * This class defines common constants, which are used across the whole binding.
 *
 * @author M. Volaart - Initial contribution
 * @author Hilbrand Bouwkamp - Removed time constants
 */
@NonNullByDefault
public final class DSMRBindingConstants {
    /**
     * Binding id.
     */
    public static final String BINDING_ID = "dsmr";

    /**
     * Key to use to identify serial port.
     */
    public static final String DSMR_PORT_NAME = "org.openhab.binding.dsmr";

    /**
     * Bridge device things
     */
    public static final ThingTypeUID THING_TYPE_DSMR_BRIDGE = new ThingTypeUID(BINDING_ID, "dsmrBridge");
    public static final ThingTypeUID THING_TYPE_SMARTY_BRIDGE = new ThingTypeUID(BINDING_ID, "smartyBridge");

    /**
     * Configuration parameter for the serial port.
     */
    public static final String CONFIGURATION_SERIAL_PORT = "serialPort";

    public static final String CONFIGURATION_DECRYPTION_KEY = "decryptionKey";
    public static final String CONFIGURATION_DECRYPTION_KEY_EMPTY = "";
    public static final String CONFIGURATION_ADDITIONAL_KEY = "additionalKey";
    public static final String CONFIGURATION_ADDITIONAL_KEY_DEFAULT = "3000112233445566778899AABBCCDDEEFF";

    private DSMRBindingConstants() {
        // Constants class
    }
}
