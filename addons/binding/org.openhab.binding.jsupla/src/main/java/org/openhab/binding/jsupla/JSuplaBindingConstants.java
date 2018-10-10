/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jsupla;

import com.google.common.collect.ImmutableSet;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

import java.util.Set;

/**
 * The {@link JSuplaBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Grzeslowski - Initial contribution
 */
@SuppressWarnings("WeakerAccess")
@NonNullByDefault
public class JSuplaBindingConstants {

    public static final String BINDING_ID = "jsupla";

    // List of all Thing Type UIDs
    public static final ThingTypeUID SUPLA_DEVICE_TYPE = new ThingTypeUID(BINDING_ID, "device");
    public static final ThingTypeUID JSUPLA_SERVER_TYPE = new ThingTypeUID(BINDING_ID, "server-bridge");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(SUPLA_DEVICE_TYPE, JSUPLA_SERVER_TYPE);

    // List of all Channel ids
    public static final String LIGHT_CHANNEL = "light-channel";
    public static final String SWITCH_CHANNEL = "switch-channel";

    // supla device
    public static final String SUPLA_DEVICE_GUID = "jsupla-device-guid";
    
    // jSuplaServer constants
    public static final int DEVICE_TIMEOUT_SEC = 10;
    public static final int DEFAULT_PORT = 2016;
    public static final String CONFIG_SERVER_ACCESS_ID = "serverAccessId";
    public static final String CONFIG_SERVER_ACCESS_ID_PASSWORD = "serverAccessIdPassword";
    public static final String CONFIG_PORT = "port";
    public static final String CONNECTED_DEVICES_CHANNEL_ID = "server-devices";

    public static class Channels {
        public static final String LIGHT_CHANNEL_ID = "light-channel";
        public static final String SWITCH_CHANNEL_ID = "switch-channel";
        public static final String DECIMAL_CHANNEL_ID = "decimal-channel";
        public static final String RGB_CHANNEL_ID = "rgb-channel";
        public static final String ROLLER_SHUTTER_CHANNEL_ID = "roller-shutter-channel";
        public static final String TEMPERATURE_CHANNEL_ID = "temperature-channel";
        public static final String TEMPERATURE_AND_HUMIDITY_CHANNEL_ID = "temperature-and-humidity-channel";
        public static final String UNKNOWN_CHANNEL_ID = "unknown-channel";
    }
}
