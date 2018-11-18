/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.canrelay.internal;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link CanRelayBindingConstants} class defines common constants and utility methods, which are
 * used across the whole binding.
 *
 * @author Lubos Housa - Initial contribution
 */
@NonNullByDefault
public class CanRelayBindingConstants {

    private static final String BINDING_ID = "canrelay";

    /**
     * nodeID of a given light on a given CanRelay, used as representation-property to identify the respective
     * thing/light in the handler
     */
    public static final String CONFIG_NODEID = "nodeID";

    /**
     * initial state of a given light on a given CanRelay, used to pass the initial value of the light switch from can
     * relay to the light handler on init
     */
    public static final String CONFIG_INITIALSTATE = "initialState";

    /**
     * Name of the owner used in registering the serial port
     */
    public static final String CANRELAY_PORT_NAME = "org.openhab.binding.canrelay";

    /**
     * Name of the configuration property serial port of the HW bridge
     */
    public static final String HW_BIDGE_CONFIG_SERIALPORT = "serialPort";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_HW_BRIDGE = new ThingTypeUID(BINDING_ID, "hwBridge");
    public static final ThingTypeUID THING_TYPE_LIGHT = new ThingTypeUID(BINDING_ID, "light");
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<ThingTypeUID>(
            Arrays.asList(THING_TYPE_HW_BRIDGE, THING_TYPE_LIGHT));

    // List of all Channel IDs
    public static final String CHANNEL_LIGHT_SWITCH = "lightSwitch";

    /**
     * Return the String representation of nodeID
     *
     * @param nodeID nodeID to translate
     * @return String hex 2-digit representation of the in-passed nodeID (with 0x prefix, e.g. "0x02" for 2)
     */
    public static String nodeAsString(int nodeID) {
        return String.format("0x%02x", nodeID);
    }
}
