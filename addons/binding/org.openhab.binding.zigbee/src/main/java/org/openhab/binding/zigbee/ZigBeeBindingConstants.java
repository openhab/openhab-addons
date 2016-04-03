/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zigbee;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link ZigBeeBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Chris Jackson - Initial contribution
 */
public class ZigBeeBindingConstants {

    // Binding Name
    public static final String BINDING_ID = "zigbee";

    // Coordinator (Bridges)
    public final static ThingTypeUID COORDINATOR_TYPE_CC2530 = new ThingTypeUID(BINDING_ID, "coordinator_cc2530");

    // List of Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_GENERIC_DEVICE = new ThingTypeUID(BINDING_ID, "device");

    // List of Channel ids
    public final static String CHANNEL_CFG_BINDING = "binding";

    public final static String CHANNEL_TEMPERATURE = "temperature";
    public final static String CHANNEL_SWITCH = "switch";
    public static final String CHANNEL_COLORTEMPERATURE = "color_temperature";
    public static final String CHANNEL_COLOR = "color";
    public static final String CHANNEL_BRIGHTNESS = "brightness";

    public static final String CHANNEL_SWITCH_DIMMER = "switch_dimmer";
    public static final String CHANNEL_SWITCH_ONOFF = "switch_onoff";

    public static final String CHANNEL_COLOR_COLOR = "color_color";
    public static final String CHANNEL_COLOR_TEMPERATURE = "color_temperature";

    public static final String CHANNEL_PROPERTY_ADDRESS = "zigbee_address";
    public static final String CHANNEL_PROPERTY_CLUSTER = "zigbee_cluster";

    // List of all parameters
    public final static String PARAMETER_PANID = "panid";
    public final static String PARAMETER_CHANNEL = "channel";
    public final static String PARAMETER_PORT = "port";

    public final static String PARAMETER_MACADDRESS = "macAddress";

    public final static Set<ThingTypeUID> SUPPORTED_BRIDGE_TYPES_UIDS = ImmutableSet.of(COORDINATOR_TYPE_CC2530);
}
