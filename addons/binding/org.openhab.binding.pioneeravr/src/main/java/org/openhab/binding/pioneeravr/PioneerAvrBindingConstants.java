/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pioneeravr;

import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link PioneerAvrBinding} class defines common constants, which are used across the whole binding.
 *
 * @author Antoine Besnard - Initial contribution
 */
public class PioneerAvrBindingConstants {

    public static final String BINDING_ID = "pioneeravr";

    public static final Set<String> SUPPORTED_DEVICE_MODELS = ImmutableSet.of("SC-57", "SC-LX85", "SC-55", "SC-1526",
            "SC-LX75", "VSX-53", "VSX-1326", "VSX-LX55", "VSX-2021", "VSA-LX55", "VSX-52", "VSX-1126", "VSX-1121",
            "VSX-51", "VSX-1021", "VSX-1026", "VSA-1021", "VSX-50", "VSX-926", "VSX-921", "VSA-921");

    // List of all Thing Type UIDs
    public static final ThingTypeUID IP_AVR_THING_TYPE = new ThingTypeUID(BINDING_ID, "ipAvr");
    public static final ThingTypeUID IP_AVR_UNSUPPORTED_THING_TYPE = new ThingTypeUID(BINDING_ID, "ipAvrUnsupported");
    public static final ThingTypeUID SERIAL_AVR_THING_TYPE = new ThingTypeUID(BINDING_ID, "serialAvr");

    // List of thing parameters names
    public static final String PROTOCOL_PARAMETER = "protocol";
    public static final String HOST_PARAMETER = "address";
    public static final String TCP_PORT_PARAMETER = "tcpPort";
    public static final String SERIAL_PORT_PARAMETER = "serialPort";

    public static final String IP_PROTOCOL_NAME = "IP";
    public static final String SERIAL_PROTOCOL_NAME = "serial";

    // List of all Channel names
    public static final String POWER_CHANNEL = "power";
    public static final String VOLUME_DIMMER_CHANNEL = "volumeDimmer";
    public static final String VOLUME_DB_CHANNEL = "volumeDb";
    public static final String MUTE_CHANNEL = "mute";
    public static final String SET_INPUT_SOURCE_CHANNEL = "setInputSource";
    public static final String DISPLAY_INFORMATION_CHANNEL = "displayInformation#displayInformation";

    public static final String GROUP_CHANNEL_PATTERN = "zone%s#%s";
    public static final Pattern GROUP_CHANNEL_ZONE_PATTERN = Pattern.compile("zone([0-3])#.*");

    // Used for Discovery service
    public static final String MANUFACTURER = "PIONEER";
    public static final String UPNP_DEVICE_TYPE = "MediaRenderer";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(IP_AVR_THING_TYPE,
            IP_AVR_UNSUPPORTED_THING_TYPE);

}
